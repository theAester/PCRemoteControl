package io.github.theaester.pcremotecontrol.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;

import android.os.Handler;

public class TrackPad {
    private final Context context;
    private final View view;
    private TrackPadCallback callback;
    private boolean sequenceActive;
    private short twoState;
    private short stateCount = 5;
    private float oldX;
    private float oldY;
    private String subType;
    private short topState;
    private int pointerCache;
    private short topStateCount;
    private float coeffX;
    private float coeffY;
    private static final long PRESS_HOLD_DELAY = 300;
    private boolean leftHold = false;
    private int reset;

    public static TrackPad CreateWithView(Context context, View touchView, View rightClickView, View leftClickView){
        return new TrackPad(context, touchView, rightClickView, leftClickView);
    }

    public void setCoeff(float x, float y){
        if(x > 0) coeffX = x;
        if(y > 0) coeffY = y;
    }

    private TrackPad(Context context, View touchView, View rightClickView, View leftClickView){
        this.context = context;
        this.view = touchView;
        this.sequenceActive = false;
        this.coeffX = 35_000;
        this.coeffY = 15_000;
        touchView.setOnTouchListener(new View.OnTouchListener() {
            int pointerCount;
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int action = event.getActionMasked();
                if(topState != 2) {
                    pointerCount = event.getPointerCount();
                }
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (!sequenceActive) {
                            sequenceActive = true;
                            callback.onSequenceBegin();
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (topState == 0){
                            topState = 1;
                            topStateCount = 2;
                        }else if (topState == 1){
                            if (topStateCount != 0) topStateCount--;
                            else topState = 2;
                            reset = 1;
                        } else if (topState == 2){
                            if (sequenceActive) {
                                if (pointerCount == 1) {
                                    handleOneFingerMove(event);
                                } else if (pointerCount >= 2) {
                                    handleTwoFingerMove(event);
                                }
                            }
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (sequenceActive) {
                            sequenceActive = false;
                            callback.onSequenceEnd();
                            twoState = 0;
                            topState = 0;
                        }
                        break;
                }

                return true;
            }
        });
        leftClickView.setOnTouchListener(new View.OnTouchListener() {
            Handler holdHandler = new android.os.Handler(context.getMainLooper());
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int action = event.getAction();
                switch (action){

                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        callback.onMouseDown(false);
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        callback.onMouseUp(false);
                        break;

                }
                return true;
            }
        });
        rightClickView.setOnTouchListener(new View.OnTouchListener() {
            Handler holdHandler = new android.os.Handler(context.getMainLooper());
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int action = event.getAction();
                switch (action){

                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        callback.onMouseDown(true);
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        callback.onMouseUp(true);
                        break;

                }
                return true;
            }
        });
    }
    public void setCallback(TrackPadCallback callback){
        this.callback = callback;
    }

    private void handleOneFingerMove(@NonNull MotionEvent event) {
        float x = event.getX() / view.getWidth();
        float y = event.getY() / view.getHeight();
        long timestamp = System.currentTimeMillis();
        if(reset == 1){
            oldX = x;
            oldY = y;
            reset = 0;
        }
        float valX = ssqr(x - oldX) * coeffX;
        float valY = ssqr((y - oldY)) * coeffY;
        if(Math.abs(valX) > 0.5f || Math.abs(valY) > 0.5f) {
            callback.onMotion(valX, valY, timestamp);
            oldX = x;
            oldY = y;
        }
    }

    private float ssqr(float v) {
        float e = 1.0f;
        if(v < 0){
            e = -1.0f;
            //v = -v;
        }
        return e * v*v;
    }

    private void handleTwoFingerMove(@NonNull MotionEvent event) {
        // Calculate the median point of two fingers
        float x1 = event.getX(0);
        float y1 = event.getY(0);
        float medianX;
        float medianY;

        try {
            float x2 = event.getX(1);
            float y2 = event.getY(1);
            medianX = (x1 + x2) / 2 / view.getWidth();
            medianY = (y1 + y2) / 2 / view.getHeight();
        }catch (Exception e){
            medianX = x1 / view.getWidth();
            medianY = y1 / view.getHeight();
        }

        long timestamp = System.currentTimeMillis();

        // Check the direction of the scroll (vertical or horizontal)
        // This is a simplified example; you may need a more robust implementation
        if(twoState == 0){
            oldX=medianX;
            oldY=medianY;
            stateCount = 1;
            twoState = 1;
        }else if (twoState == 1){
            if (stateCount != 0) stateCount--;
            else{
                subType = Math.abs(oldX - medianX) > Math.abs(oldY - medianY) ? "hscroll" : "vscroll";
                twoState = 2;
            }
        }else if (twoState == 2){
            float valX = -ssqr(medianX - oldX) * coeffX;
            float valY = -ssqr(medianY - oldY) * coeffY;
            if(Math.abs(valX) > 1 || Math.abs(valY) > 1) {
                callback.onScroll(subType, valX, valY, timestamp);
                oldX = medianX;
                oldY = medianY;
            }
        }
    }
    public interface TrackPadCallback {
        void onSequenceBegin();
        void onMotion(float x, float y, long timestamp);
        void onScroll(String subType, float x, float y, long timestamp);
        void onSequenceEnd();
        void onMouseDown(boolean rightClick);
        void onMouseUp(boolean rightClick);
    }
}
