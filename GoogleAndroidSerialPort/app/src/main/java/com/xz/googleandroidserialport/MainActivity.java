package com.xz.googleandroidserialport;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.xz.googleandroidserialport.util.ByteUtil;
import com.xz.googleandroidserialport.util.SerialPortHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener
{

    @BindView(R.id.rg_type)
    RadioGroup mRgType;
    @BindView(R.id.et_read_content)
    EditText mEtReadContent;
    @BindView(R.id.et_send_content)
    EditText mEtSendContent;


    private SerialPortHelper mSerialPortHelper;
    private boolean isHexType = false;
    private String text = "";

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            byte[] readBuffer = (byte[]) msg.obj;
            SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");
            String time = sDateFormat.format(new Date());
            String rxText;
            rxText = new String(readBuffer);
            if (isHexType) {
                //转成十六进制数据
                rxText = ByteUtil.ByteArrToHex(readBuffer);
            }
            text += "Rx-> " + time + ": " + rxText + "\r" + "\n";
            mEtReadContent.setText(text);
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mRgType.setOnCheckedChangeListener(this);

        initSerialConfig();
    }

    private void initSerialConfig() {
        mSerialPortHelper = new SerialPortHelper(Const.SPORT_NAME, Const.BAUD_RATE) {
            @Override
            protected void onDataReceived(byte[] buffer, int size) {
                Message message = mHandler.obtainMessage();
                message.obj = buffer;
                mHandler.sendMessage(message);
            }
        };
    }

    @OnClick({R.id.bt_open, R.id.bt_close, R.id.bt_send, R.id.bt_clear_content})
    public void onButtonClicked(View view){
        switch (view.getId()) {
            case R.id.bt_open:
                if (mSerialPortHelper.isOpen()) {
                    Toast.makeText(this, Const.SPORT_NAME + "串口已经打开", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    mSerialPortHelper.open();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this, Const.SPORT_NAME + "串口打开成功", Toast.LENGTH_SHORT).show();
                break;

            case R.id.bt_close:
                if (mSerialPortHelper.isOpen()) {
                    mSerialPortHelper.close();
                    Toast.makeText(this, Const.SPORT_NAME + "串口已经关闭", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.bt_clear_content:
                text = "";
                mEtReadContent.setText(text);
                break;

            case R.id.bt_send:
                if (!mSerialPortHelper.isOpen()) {
                    Toast.makeText(this, Const.SPORT_NAME + "串口没打开 发送失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                String sendContent = mEtSendContent.getText().toString();
                if (isHexType) {
                    mSerialPortHelper.sendHex(sendContent);
                } else {
                    mSerialPortHelper.sendTxt(sendContent);
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_txt:
                isHexType = false;
                mEtSendContent.setText(Const.TXT_TYPE_SEND);
                break;

            case R.id.rb_hex:
                isHexType = true;
                mEtSendContent.setText(Const.HEX_TYPE_SEND);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSerialPortHelper.close();
        mSerialPortHelper = null;
    }
}
