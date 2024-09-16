package io.github.theaester.pcremotecontrol.comms;

public interface RecvCallback {
    public void onMessage(String message);
    public void onError(Exception e);
}
