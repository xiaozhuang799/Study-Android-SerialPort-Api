package com.xz.googleandroidserialport2.util;

import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;


public abstract class SerialPortHelper
{
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private boolean _isOpen = false;
    //串口配置
    private String sPort;
    private int iBaudRate;
    private int parity = 0; //默认无校验
    private int dataBits = 8;//默认数据位8位
    private int stopBits = 1;//默认停止位1位

    public SerialPortHelper(String sPort, int iBaudRate) {
        this.sPort = sPort;
        this.iBaudRate = iBaudRate;
    }

    public SerialPortHelper(String sPort, int iBaudRate, int parity, int dataBits, int stopBits) {
        this.sPort = sPort;
        this.iBaudRate = iBaudRate;
        this.parity = parity;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
    }


    public void open() throws SecurityException, IOException {
        this.mSerialPort = new SerialPort(new File(sPort), iBaudRate, parity, dataBits, stopBits);
        this.mOutputStream = this.mSerialPort.getOutputStream();
        this.mInputStream = this.mSerialPort.getInputStream();
        this.mReadThread = new ReadThread();
        this.mReadThread.start();
        this._isOpen = true;

    }

    public void close() {
        if (this.mReadThread != null) {
            this.mReadThread.interrupt();
        }
        if (this.mSerialPort != null) {
            this.mSerialPort.close();
            this.mSerialPort = null;
        }
        this._isOpen = false;
    }

    private void send(byte[] bOutArray) {
        try {
            this.mOutputStream.write(bOutArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendHex(String sHex) {
        byte[] bOutArray = ByteUtil.HexToByteArr(sHex);
        send(bOutArray);
    }

    public void sendTxt(String sTxt) {
        byte[] bOutArray = sTxt.getBytes();
        send(bOutArray);
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    if (SerialPortHelper.this.mInputStream == null) {
                        return;
                    }
                    int available = SerialPortHelper.this.mInputStream.available();
                    if (available > 0) {
                        byte[] buffer = new byte[available];
                        int size = SerialPortHelper.this.mInputStream.read(buffer);
                        if (size > 0) {
                            SerialPortHelper.this.onDataReceived(buffer, size);
                        }
                    } else {
                        SystemClock.sleep(50);
                    }


                } catch (Throwable e) {
                    Log.e("error", e.getMessage());
                    return;
                }
            }
        }
    }

    public boolean isOpen() {
        return this._isOpen;
    }

    protected abstract void onDataReceived(byte[] buffer, int size);

}
