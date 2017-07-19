package com.ranglerz.remand_io;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import org.w3c.dom.Text;

import java.util.List;


public class CallActivity extends AppCompatActivity {

    private Call call;
    private TextView callState;
    private SinchClient sinchClient;
    private RelativeLayout button;
    private String callerId;
    private String recipientId;

    private TextView textView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);


        Intent intent = getIntent();
  /*      callerId = "CBA";
        recipientId = "ABC";*/


        callerId = intent.getStringExtra("callerId");
        recipientId = intent.getStringExtra("recipientId");

        Log.e("TAG", "callerID: " + callerId);
        Log.e("TAG", "recipientID: " + recipientId);



        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(callerId)
                .applicationKey("f5480496-eb89-4433-8e3c-62a2ee49f36c")
                .applicationSecret("G2GMyN186E6saAFpbeAckw==")
                .environmentHost("sandbox.sinch.com")
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();

        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());

        button = (RelativeLayout) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.tv_text);
        callState = (TextView) findViewById(R.id.callState);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (call == null) {
                    call = sinchClient.getCallClient().callUser(recipientId);
                    call.addCallListener(new SinchCallListener());
                    textView.setText("Hang Up");
                } else {
                    call.hangup();
                }
            }
        });
    }

    private class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call endedCall) {
            call = null;
            textView.setText("Call");
            callState.setText("");
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }

        @Override
        public void onCallEstablished(Call establishedCall) {
            callState.setText("connected");
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallProgressing(Call progressingCall) {
            callState.setText("ringing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {}
    }

    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            call = incomingCall;
            call.answer();
            call.addCallListener(new SinchCallListener());
            textView.setText("Hang Up");
        }
    }
}