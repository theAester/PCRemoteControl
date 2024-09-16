package io.github.theaester.pcremotecontrol.helper;

import android.view.MotionEvent;
import android.view.View;

import io.github.theaester.pcremotecontrol.components.InterceptingEditText;

public class SimpleTouchListener implements View.OnTouchListener{
    InterceptingEditText.OnKeyListener callback;
    String id;
    public SimpleTouchListener(InterceptingEditText.OnKeyListener callback, String id){
        this.callback = callback;
        this.id = id;
    }
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                callback.onHold(id);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                callback.onRelease(id);
                break;
        }
        return true;
    }
}
