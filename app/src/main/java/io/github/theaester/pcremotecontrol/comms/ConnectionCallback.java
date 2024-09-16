package io.github.theaester.pcremotecontrol.comms;

public interface ConnectionCallback {
    public void onSuccess();
    public void onFailure(String message);
    public void onError(Exception e);
}
