package io.github.theaester.pcremotecontrol.helper;

import java.util.Locale;

public class MessageUtils {
    public static String sequenceBeginMessage() {
        return "{\"type\":\"begin\"}";
    }

    public static String sequenceEndMessage() {
        return "{\"type\":\"end\"}";
    }

    public static String newMotionMessage(float x, float y, long timestamp) {
        return String.format(Locale.getDefault(), "{\"type\":\"motion\",\"x\":%f,\"y\":%f,\"timestamp\":%d}", x, y, timestamp);
    }

    public static String newScrollMessage(String subType, float value, long timestamp) {
        return String.format(Locale.getDefault(), "{\"type\":\"%s\",\"value\":%f,\"timestamp\":%d}", subType, value, timestamp);
    }

    public static String mouseDownMessage(boolean rightClick) {
        return String.format(Locale.getDefault(), "{\"type\":\"mouse-down\",\"button\":\"%s\"}", rightClick ? "right": "left");
    }

    public static String mouseUpMessage(boolean rightClick) {
        return String.format(Locale.getDefault(), "{\"type\":\"mouse-up\",\"button\":\"%s\"}", rightClick ? "right": "left");
    }

    public static String pressMessage(String s) {
        return String.format(Locale.getDefault(), "{\"type\":\"press\",\"key\":\"%s\"}", s);
    }

    public static String holdMessage(String id) {
        return String.format(Locale.getDefault(), "{\"type\":\"hold\",\"key\":\"%s\"}", id);
    }
    public static String releaseMessage(String id) {
        return String.format(Locale.getDefault(), "{\"type\":\"release\",\"key\":\"%s\"}", id);
    }
}
