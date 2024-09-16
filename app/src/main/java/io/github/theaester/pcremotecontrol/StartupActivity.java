package io.github.theaester.pcremotecontrol;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.MutableLiveData;

import java.net.ConnectException;
import java.util.List;
import java.util.Objects;

import javax.crypto.BadPaddingException;

import io.github.theaester.pcremotecontrol.comms.ConnectionCallback;
import io.github.theaester.pcremotecontrol.comms.ConnectionManager;
import io.github.theaester.pcremotecontrol.databinding.ActivityMainBinding;
import io.github.theaester.pcremotecontrol.databinding.ActivityStartupBinding;
import io.github.theaester.pcremotecontrol.helper.NetworkUtils;

public class StartupActivity extends AppCompatActivity {

    private ActivityStartupBinding binding;
    private ConnectionManager connMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_startup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding = ActivityStartupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ConnectionManager.init(getApplicationContext());
        connMan = ConnectionManager.getInstance();

        MutableLiveData<List<String>> addrData = new MutableLiveData<>();
        addrData.observe(this, (list) -> {
            if (list == null || list.isEmpty()) return;
            StringBuilder builder = new StringBuilder();
            builder.append("IP helper\nBelow is a list of all ip addresses associated with your phone");
            for (String str : list){
                builder.append("\n");
                builder.append(str);
            }
            binding.startupIpHelper.setText(builder.toString());
        });
        NetworkUtils.mutateIpAddresses(this, addrData);

        binding.startupAddr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!isValidIpAddr(binding.startupAddr.getText().toString())){
                    binding.startupAddrL.setError("Invalid Ip address");
                }else{
                    binding.startupAddrL.setError(null);
                }
            }
        });
        binding.startupPort.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!isValidPort(binding.startupPort.getText().toString())){
                    binding.startupPortL.setError("Invalid Port number");
                }else{
                    binding.startupPortL.setError(null);
                }
            }
        });

        binding.startupConnect.setOnClickListener((v) -> {
            String ip = Objects.requireNonNull(binding.startupAddr.getText()).toString();
            String port = Objects.requireNonNull(binding.startupPort.getText()).toString();
            String pass = Objects.requireNonNull(binding.startupPass.getText()).toString();
            if (!isValidIpAddr(ip) || !isValidPort(port) || pass.length() != 8) {
                Toast.makeText(StartupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
            binding.startupConnect.setEnabled(false);
            connMan.connect(ip + ":" + port, pass, new ConnectionCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        binding.startupConnect.setEnabled(true);
                        Toast.makeText(StartupActivity.this, "Success" , Toast.LENGTH_SHORT).show();
                    });
                    Intent intent = new Intent(StartupActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                @Override
                public void onFailure(String message) {
                    runOnUiThread(() -> {
                        binding.startupConnect.setEnabled(true);
                        Toast.makeText(StartupActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(Exception e) {
                    if(e instanceof ConnectException){
                        runOnUiThread(() -> {
                            binding.startupConnect.setEnabled(true);
                            Toast.makeText(StartupActivity.this, "Connection refused.", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                    if(e instanceof BadPaddingException){
                        runOnUiThread(() -> {
                            binding.startupConnect.setEnabled(true);
                            Toast.makeText(StartupActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        binding.startupConnect.setEnabled(true);
                        Toast.makeText(StartupActivity.this, "Unexpected error", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    private boolean isValidPort(String string) {
        try {
            int num = Integer.parseInt(string);
            return num > 0 && num < 1 << 16;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private boolean isValidIpAddr(String string) {
        return string.matches("([0123456789]{1,3}\\.){3}[0123456789]{1,3}");
    }
}