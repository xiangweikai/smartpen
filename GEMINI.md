
## Project Overview

This project is an Android application for a "smartpen" device. The application communicates with the smartpen via a custom USB protocol to control various features.

## Key Technologies

*   **Android:** The application is built as a native Android app.
*   **Java:** The primary programming language is Java.
*   **Gradle:** The project uses Gradle for building and dependency management.

## Project Structure

*   `app/`: The main application module.
    *   `build.gradle`: Application-specific build configuration.
    *   `src/main/`: Main application source code.
        *   `AndroidManifest.xml`: Android application manifest.
        *   `java/`: Java source code.
        *   `res/`: Application resources.
*   `build.gradle`: Top-level build file.
*   `doc.txt`: Contains the custom protocol definition for communication with the smartpen.
*   `AndroidDesign.md`: Placeholder for Android design documentation.
*   `UI.txt`: Placeholder for UI-related notes.

## Smartpen Protocol

The `doc.txt` file defines a custom protocol for communicating with the smartpen. The protocol defines various modes and functions, each with a specific data format.

**Modes:**

*   Electronic Laser
*   Magnifying Glass
*   Spotlight
*   Normal

**Functions:**

*   Annotation (pen and eraser)
*   Voice
*   Page Up/Down
*   Mouse (on/off, left/right click, movement)
*   Shortcuts (fullscreen, black screen, open hyperlink, switch window, whiteboard, task view)

## Build and Run

To build the project, use the following command:

```bash
./gradlew build
```

To run the application, open the project in Android Studio and run it on an emulator or a connected device.
