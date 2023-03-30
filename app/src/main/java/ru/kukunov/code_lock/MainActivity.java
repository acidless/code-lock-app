package ru.kukunov.code_lock;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BluetoothSocket currentSocket = null;
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                Log.v("Palov", isGranted ? "Granted" : "fuck");
            });

    private EditText.OnEditorActionListener SendNewKey = new EditText.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                OutputStream outputStream = null;
                try {
                    String data = textView.getText().toString();
                    if(data.length() == 4){
                        outputStream = currentSocket.getOutputStream();
                        outputStream.write(String.format("s%s", data).getBytes());
                        outputStream.flush();
                    }

                    ShowDialog("Код успешно задан!");
                } catch (Exception e) {
                }

                return true;
            }
            return false;
        }
    };

    private void ShowDialog(String data){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(data);
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.v("Palov", "AAAAAAA");
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().trim().equals("lock")) {
                String deviceAddress = device.getAddress();
                try {
                    Method createMethod = device.getClass().getMethod("createInsecureRfcommSocket", new Class[] { int.class });
                    currentSocket = (BluetoothSocket)createMethod.invoke(device, 1);
                    currentSocket.connect();
                } catch (Exception e){
                }

                break;
            }
        }

        ((EditText)findViewById(R.id.codeInput)).setOnEditorActionListener(SendNewKey);
        findViewById(R.id.openLock).setOnClickListener(new CommandButtonHandler(currentSocket, "o"));
        findViewById(R.id.closeLock).setOnClickListener(new CommandButtonHandler(currentSocket, "c"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(currentSocket != null && currentSocket.isConnected()){
            try {
                currentSocket.close();
            } catch (IOException e) {
            }
        }
    }
}