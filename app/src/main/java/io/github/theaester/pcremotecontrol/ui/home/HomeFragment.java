package io.github.theaester.pcremotecontrol.ui.home;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import io.github.theaester.pcremotecontrol.MainActivity;
import io.github.theaester.pcremotecontrol.R;
import io.github.theaester.pcremotecontrol.components.InterceptingEditText;
import io.github.theaester.pcremotecontrol.components.TrackPad;
import io.github.theaester.pcremotecontrol.databinding.FragmentHomeBinding;
import io.github.theaester.pcremotecontrol.helper.SimpleTouchListener;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    int beginCount = 0;
    int endCount = 0;
    String mode = "None";
    float cx=0, cy=0;
    private Handler handler;
    boolean isViewDown = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstance){
        TrackPad trackPad = TrackPad.CreateWithView(getActivity(), binding.trackpadView, binding.trackpadRight, binding.trackpadLeft);
        TrackPad.TrackPadCallback callback = ((MainActivity) requireActivity()).getCommsCallback();
        trackPad.setCallback(callback);

        InterceptingEditText.OnKeyListener keysCallback = ((MainActivity) requireActivity()).getKeysCallback();
        binding.keyboardInterceptor.setOnKeyListener(keysCallback);
        binding.keyboardInterceptor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                moveEntireView(b ? -200 : isViewDown ? -330 : 40);
            }
        });

        binding.keyShift.addOnCheckedChangeListener(new MaterialButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(MaterialButton button, boolean isChecked) {
                if (isChecked) {
                    keysCallback.onHold("shift");
                } else {
                    keysCallback.onRelease("shift");
                }
            }
        });
        binding.keyCtrl.addOnCheckedChangeListener(new MaterialButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(MaterialButton button, boolean isChecked) {
                if (isChecked) {
                    keysCallback.onHold("ctrl");
                } else {
                    keysCallback.onRelease("ctrl");
                }
            }
        });
        binding.keyCaps.addOnCheckedChangeListener(new MaterialButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(MaterialButton button, boolean isChecked) {
                keysCallback.onSpecial("capslock");
            }
        });
        binding.keyAlt.addOnCheckedChangeListener(new MaterialButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(MaterialButton button, boolean isChecked) {
                if (isChecked) {
                    keysCallback.onHold("alt");
                } else {
                    keysCallback.onRelease("alt");
                }
            }
        });
        handler = new Handler(requireActivity().getMainLooper());
        binding.keySuper.setOnTouchListener(new View.OnTouchListener() {
             long downTime;
             final long THRESHOLD = 100;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        downTime = System.currentTimeMillis();
                        handler.postDelayed(() -> {
                            keysCallback.onHold("win");
                        }, THRESHOLD);
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        long now = System.currentTimeMillis();
                        if(now - downTime < 100){
                            handler.removeCallbacksAndMessages(null);
                            keysCallback.onSpecial("win");
                        }else {
                            keysCallback.onRelease("win");
                        }
                        break;
                }
                return true;
            }
        });
        binding.keyTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keysCallback.onSpecial("tab");
            }
        });
        binding.keyEsc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keysCallback.onSpecial("esc");
            }
        });
        binding.keyFn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keysCallback.onSpecial("fn");
            }
        });
        binding.keyUp.setOnTouchListener(new SimpleTouchListener(keysCallback, "up"));
        binding.keyDown.setOnTouchListener(new SimpleTouchListener(keysCallback, "down"));
        binding.keyRight.setOnTouchListener(new SimpleTouchListener(keysCallback, "right"));
        binding.keyLeft.setOnTouchListener(new SimpleTouchListener(keysCallback, "left"));

        Map<String, View> extrasMap = new HashMap<>();
        extrasMap.put("ins", binding.keyIns);
        extrasMap.put("home", binding.keyHome);
        extrasMap.put("del", binding.keyDel);
        extrasMap.put("end", binding.keyEnd);
        extrasMap.put("pgup", binding.keyPgup);
        extrasMap.put("pgdn", binding.keyPgdn);
        extrasMap.put("f1", binding.keyF1);
        extrasMap.put("f2", binding.keyF2);
        extrasMap.put("f3", binding.keyF3);
        extrasMap.put("f4", binding.keyF4);
        extrasMap.put("f5", binding.keyF5);
        extrasMap.put("f6", binding.keyF6);
        extrasMap.put("f7", binding.keyF7);
        extrasMap.put("f8", binding.keyF8);
        extrasMap.put("f9", binding.keyF9);
        extrasMap.put("f10", binding.keyF10);
        extrasMap.put("f11", binding.keyF11);
        extrasMap.put("f12", binding.keyF12);
        extrasMap.put("volumeup", binding.volUp);
        extrasMap.put("volumedown", binding.volDn);
        for(Map.Entry<String, View> entry: extrasMap.entrySet()){
            View v = entry.getValue();
            v.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    keysCallback.onSpecial(entry.getKey());
                }
            });
        }
        binding.volMute.setOnClickListener(new View.OnClickListener() {
            boolean isMute = false;
            @Override
            public void onClick(View view) {
                keysCallback.onSpecial("volumemute");
                isMute = !isMute;
                binding.volMute.setImageDrawable(ContextCompat.getDrawable(getContext(), isMute ? R.drawable.baseline_volume_mute_24 : R.drawable.baseline_volume_up_24));
            }
        });
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isViewDown = !isViewDown;
                if(isViewDown){
                    moveEntireView(-330);
                    binding.fab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.baseline_arrow_upward_24));
                }else {
                    moveEntireView(40);
                    binding.fab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.baseline_arrow_downward_24));
                }
            }
        });
    }

    private void moveEntireView(int targetMarginTop) {
        final ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.trackpadView.getLayoutParams();
        final int startMarginTop = layoutParams.topMargin;

        // Convert dp to pixels
        float scale = getResources().getDisplayMetrics().density;
        int targetMarginTopInPixels = (int) (targetMarginTop * scale + 0.5f);

        ValueAnimator animator = ValueAnimator.ofInt(startMarginTop, targetMarginTopInPixels);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                layoutParams.topMargin = (int) animation.getAnimatedValue();
                binding.trackpadView.setLayoutParams(layoutParams);
            }
        });
        animator.setDuration(300); // Animation duration in milliseconds
        animator.start();
    }

    private void syncText() {
        binding.textStatus.setText(String.format("Being Count=%d\nEnd Count=%d\nMode=%s\ncoords=%f,%f", beginCount, endCount, mode, cx, cy));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}