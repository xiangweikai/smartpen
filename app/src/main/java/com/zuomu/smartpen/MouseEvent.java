package com.zuomu.smartpen;

public class MouseEvent {
    public int type;
    public int code;
    public int value;

    public MouseEvent(int type, int code, int value) {
        this.type = type;
        this.code = code;
        this.value = value;
    }

    @Override
    public String toString() {
        return "MouseEvent{type=" + type + ", code=" + code + ", value=" + value + "}";
    }
}
