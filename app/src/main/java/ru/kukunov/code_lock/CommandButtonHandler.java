package ru.kukunov.code_lock;

import android.bluetooth.BluetoothSocket;
import android.view.View;

import java.io.OutputStream;

public class CommandButtonHandler implements View.OnClickListener {
    private String code = "";
    private BluetoothSocket currentSocket = null;


    CommandButtonHandler(BluetoothSocket currentSocket, String code){
        this.code = code;
        this.currentSocket = currentSocket;
    }

    @Override
    public void onClick(View view) {
        OutputStream outputStream = null;
        try {
            outputStream = currentSocket.getOutputStream();
            outputStream.write(code.getBytes());
            outputStream.flush();
        } catch (Exception e) {
        }
    }
}
