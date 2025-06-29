#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <linux/input.h>
#include <android/log.h>
#include <errno.h>
#include <pthread.h>
#include <signal.h>

#define LOG_TAG "NativeMouseEvent"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static JavaVM *g_JavaVM;
static jobject g_callbackObject = NULL;
static jmethodID g_onMouseEventMethodID = NULL;
static volatile sig_atomic_t g_isReading = 0;
static pthread_t g_readingThread;

// JNI_OnLoad is called when the .so library is loaded
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    g_JavaVM = vm;
    return JNI_VERSION_1_6;
}

void *mouse_event_reader_thread(void *arg) {
    JNIEnv *env;
    // Attach the current thread to the JVM
    int getEnvStat = (*g_JavaVM)->GetEnv(g_JavaVM, (void **)&env, JNI_VERSION_1_6);
    if (getEnvStat == JNI_EDETACHED) {
        LOGD("Attaching current thread to JVM");
        if ((*g_JavaVM)->AttachCurrentThread(g_JavaVM, &env, NULL) != 0) {
            LOGE("Failed to attach current thread");
            return NULL;
        }
    } else if (getEnvStat == JNI_EVERSION) {
        LOGE("JNI version not supported");
        return NULL;
    }

    int fd;
    struct input_event ev;
    const char *device_path = "/dev/input/event2"; // Example device path

    LOGD("Thread: Attempting to open device: %s", device_path);

    fd = open(device_path, O_RDONLY | O_NONBLOCK); // Use O_NONBLOCK for non-blocking read
    if (fd < 0) {
        LOGE("Thread: Failed to open device %s: %s", device_path, strerror(errno));
        // Callback error to Java if possible
        if (g_callbackObject && g_onMouseEventMethodID) {
            // You might want a separate error callback method
        }
        if (getEnvStat == JNI_EDETACHED) {
            (*g_JavaVM)->DetachCurrentThread(g_JavaVM);
        }
        return NULL;
    }

    LOGD("Thread: Device opened successfully. Reading events...");

    while (g_isReading) {
        ssize_t bytes = read(fd, &ev, sizeof(struct input_event));
        if (bytes == sizeof(struct input_event)) {
            LOGD("Thread: Event read: type %d, code %d, value %d", ev.type, ev.code, ev.value);

            if (g_callbackObject && g_onMouseEventMethodID) {
                jclass mouseEventClass = (*env)->FindClass(env, "com/zuomu/smartpen/MouseEvent");
                if (mouseEventClass == NULL) {
                    LOGE("Thread: Failed to find MouseEvent class");
                    break;
                }

                jmethodID constructor = (*env)->GetMethodID(env, mouseEventClass, "<init>", "(III)V");
                if (constructor == NULL) {
                    LOGE("Thread: Failed to find MouseEvent constructor");
                    break;
                }

                jobject mouseEventObject = (*env)->NewObject(env, mouseEventClass, constructor, ev.type, ev.code, ev.value);
                if (mouseEventObject == NULL) {
                    LOGE("Thread: Failed to create MouseEvent object");
                    break;
                }

                (*env)->CallVoidMethod(env, g_callbackObject, g_onMouseEventMethodID, mouseEventObject);
                (*env)->DeleteLocalRef(env, mouseEventObject);
                (*env)->DeleteLocalRef(env, mouseEventClass);
            }
        } else if (bytes == -1) {
            if (errno == EAGAIN || errno == EWOULDBLOCK) {
                // No data available, sleep for a bit and try again
                usleep(10000); // 10ms
            } else {
                LOGE("Thread: Failed to read event: %s", strerror(errno));
                break;
            }
        } else {
            LOGE("Thread: Incomplete event read: %zd bytes", bytes);
            break;
        }
    }

    close(fd);
    LOGD("Thread: Device closed.");

    if (g_callbackObject) {
        (*env)->DeleteGlobalRef(env, g_callbackObject);
        g_callbackObject = NULL;
    }

    if (getEnvStat == JNI_EDETACHED) {
        (*g_JavaVM)->DetachCurrentThread(g_JavaVM);
    }

    return NULL;
}

JNIEXPORT void JNICALL
Java_com_zuomu_smartpen_NativeUtils_startReadingMouseEvents(JNIEnv *env, jclass clazz, jobject callback) {
    if (g_isReading) {
        LOGD("Already reading mouse events.");
        return;
    }

    g_callbackObject = (*env)->NewGlobalRef(env, callback);
    if (g_callbackObject == NULL) {
        LOGE("Failed to create global reference for callback object");
        return;
    }

    jclass callbackClass = (*env)->GetObjectClass(env, g_callbackObject);
    if (callbackClass == NULL) {
        LOGE("Failed to get callback class");
        (*env)->DeleteGlobalRef(env, g_callbackObject);
        g_callbackObject = NULL;
        return;
    }

    g_onMouseEventMethodID = (*env)->GetMethodID(env, callbackClass, "onMouseEvent", "(Lcom/zuomu/smartpen/MouseEvent;)V");
    if (g_onMouseEventMethodID == NULL) {
        LOGE("Failed to find onMouseEvent method ID");
        (*env)->DeleteGlobalRef(env, g_callbackObject);
        g_callbackObject = NULL;
        return;
    }
    (*env)->DeleteLocalRef(env, callbackClass);

    g_isReading = 1;
    if (pthread_create(&g_readingThread, NULL, mouse_event_reader_thread, NULL) != 0) {
        LOGE("Failed to create reading thread");
        g_isReading = 0;
        (*env)->DeleteGlobalRef(env, g_callbackObject);
        g_callbackObject = NULL;
    }
}

JNIEXPORT void JNICALL
Java_com_zuomu_smartpen_NativeUtils_stopReadingMouseEvents(JNIEnv *env, jclass clazz) {
    if (!g_isReading) {
        LOGD("Not currently reading mouse events.");
        return;
    }

    g_isReading = 0;
    if (pthread_join(g_readingThread, NULL) != 0) {
        LOGE("Failed to join reading thread");
    }
    LOGD("Reading thread stopped.");
}