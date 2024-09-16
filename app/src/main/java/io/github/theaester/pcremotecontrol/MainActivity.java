package io.github.theaester.pcremotecontrol;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.ViewTreeObserver;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import io.github.theaester.pcremotecontrol.comms.ConnectionCallback;
import io.github.theaester.pcremotecontrol.comms.ConnectionManager;
import io.github.theaester.pcremotecontrol.components.InterceptingEditText;
import io.github.theaester.pcremotecontrol.components.TrackPad;
import io.github.theaester.pcremotecontrol.databinding.ActivityMainBinding;
import io.github.theaester.pcremotecontrol.helper.MessageUtils;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private ConnectionManager connMan;
    private TrackPad.TrackPadCallback commsCallback;
    private InterceptingEditText.OnKeyListener keysCallback;
    private String address;
    private String key;
    private View activityRootView;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("addr", address);
        savedInstanceState.putString("key", key);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            connMan = ConnectionManager.getInstance();
            address = connMan.getConnectedAddress();
            key = connMan.getConnectionKey();
        }else {
            Log.d("MainActivity", "savedInstance");
            address = savedInstanceState.getString("addr");
            key = savedInstanceState.getString("key");
            ConnectionManager.init(getApplicationContext());
            connMan = ConnectionManager.getInstance();
            connMan.retryConnection(address, key);
        }

        keysCallback = new InterceptingEditText.OnKeyListener(){
            @Override
            public void onKey(char key){
                Log.d("MainActivity", "okKey " + key);
                String message = MessageUtils.pressMessage(""+key);
                connMan.send(message);
            }
            @Override
            public void onSpecial(String id){
                String message = MessageUtils.pressMessage(id);
                connMan.send(message);
            }

            @Override
            public void onHold(String id) {
                String message = MessageUtils.holdMessage(id);
                connMan.send(message);
            }

            @Override
            public void onRelease(String id) {
                String message = MessageUtils.releaseMessage(id);
                connMan.send(message);
            }
        };

        commsCallback = new TrackPad.TrackPadCallback() {
            @Override
            public void onSequenceBegin() {
                String message = MessageUtils.sequenceBeginMessage();
                connMan.send(message);
            }

            @Override
            public void onMotion(float x, float y, long timestamp) {
                String message = MessageUtils.newMotionMessage(x, y, timestamp);
                Log.d("MainActivity", "on motion: " + message);
                connMan.send(message);
            }

            @Override
            public void onScroll(String subType, float x, float y, long timestamp) {
                float value;
                if (subType.equals("vscroll")) {
                    value = y;
                } else if (subType.equals("hscroll")){
                    value = x;
                } else {
                    throw new RuntimeException("oops");
                }
                String message = MessageUtils.newScrollMessage(subType, value, timestamp);
                connMan.send(message);
            }

            @Override
            public void onSequenceEnd() {
                String message = MessageUtils.sequenceEndMessage();
                connMan.send(message);
            }

            @Override
            public void onMouseDown(boolean rightClick){
                String message = MessageUtils.mouseDownMessage(rightClick);
                connMan.send(message);
            }
            @Override
            public void onMouseUp(boolean rightClick){
                String message = MessageUtils.mouseUpMessage(rightClick);
                connMan.send(message);
            }
        };

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        activityRootView = binding.getRoot();
        setContentView(activityRootView);

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        connMan.disconnect(new ConnectionCallback() {
            @Override
            public void onSuccess() {
                Log.d("MainActivity", "disconnected");
            }

            @Override
            public void onFailure(String message) {
                Log.d("MainActivity", "disconnect failed " + message);
            }

            @Override
            public void onError(Exception e) {
                Log.d("MainActivity", "disconnect error");
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onResume(){
       super.onResume();
       Log.d("MainActivity", "onResume");
       connMan.retryConnection(address, key);
    }

    public TrackPad.TrackPadCallback getCommsCallback(){
        return commsCallback;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public InterceptingEditText.OnKeyListener getKeysCallback() {
        return keysCallback;
    }
}