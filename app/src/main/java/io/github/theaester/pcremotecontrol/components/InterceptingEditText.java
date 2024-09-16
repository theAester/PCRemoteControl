package io.github.theaester.pcremotecontrol.components;

import android.content.Context;
import android.net.ipsec.ike.TunnelModeChildSessionParams;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import androidx.appcompat.widget.AppCompatEditText;

import java.security.Key;

public class InterceptingEditText extends AppCompatEditText {

    private OnKeyListener onKeyListener;
    private KeyListener oldKeyListener;
    private KeyListener newKeyListener;

    public InterceptingEditText(Context context) {
        super(context);
        init();
    }

    public InterceptingEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InterceptingEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    InterceptingEditText.this.clearFocus();
                    return false;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    private void init() {
        // No need to disable standard keyboard input here
        oldKeyListener = getKeyListener();
        // for hardware keys
        newKeyListener = new KeyListener() {
            @Override
            public int getInputType() {
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL;
            }

            @Override
            public boolean onKeyDown(View view, Editable editable, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL){
                    if (onKeyListener != null) onKeyListener.onSpecial("backspace");
                    return true;
                } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    if (onKeyListener != null) onKeyListener.onSpecial("enter");
                    return true;
                }
                return false;
            }

            @Override
            public boolean onKeyUp(View view, Editable editable, int i, KeyEvent keyEvent) {
                return false;
            }

            @Override
            public boolean onKeyOther(View view, Editable editable, KeyEvent keyEvent) {
                return false;
            }

            @Override
            public void clearMetaKeyState(View view, Editable editable, int i) {
                oldKeyListener.clearMetaKeyState(view, editable, i);
            }
        };
        setKeyListener(newKeyListener);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int len = charSequence.length();
                if(len == 0) return;
                if(len != 1) {
                    Log.e("INTERCEPT", "incompatible charSequence encountered: " + charSequence);
                    return;
                }
                if(onKeyListener != null) onKeyListener.onKey(charSequence.charAt(0));
            }

            @Override
            public void afterTextChanged(Editable editable) {
                editable.clear();
            }
        });
    }

    public void setOnKeyListener(OnKeyListener listener){
        this.onKeyListener = listener;
    }

    public interface OnKeyListener {
        public void onKey(char c);
        public void onSpecial(String id);
        public void onHold(String id);
        public void onRelease(String id);
    }
}
