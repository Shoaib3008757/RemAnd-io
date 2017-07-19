package com.ranglerz.remand_io;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.teamviewer.sdk.screensharing.api.TVConfigurationID;
import com.teamviewer.sdk.screensharing.api.TVCreationError;
import com.teamviewer.sdk.screensharing.api.TVSession;
import com.teamviewer.sdk.screensharing.api.TVSessionCode;
import com.teamviewer.sdk.screensharing.api.TVSessionConfiguration;
import com.teamviewer.sdk.screensharing.api.TVSessionCreationCallback;
import com.teamviewer.sdk.screensharing.api.TVSessionFactory;

public class MainActivity extends AppCompatActivity {

    RelativeLayout btMakeCall, btScreenShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btMakeCall = (RelativeLayout) findViewById(R.id.rr_call);
        btScreenShare = (RelativeLayout) findViewById(R.id.bt_shhareScreen);


        starCallingActvity();
        shareScreenClass();

    }

    //start calling actvity
    public void starCallingActvity(){

        btMakeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog inputDialog = new Dialog(MainActivity.this);
                inputDialog.setContentView(R.layout.input_dialog_for_call);
                Button diloagBtOk = (Button)inputDialog.findViewById(R.id.bt_ok);
                Button diloagBtCancel = (Button)inputDialog.findViewById(R.id.bt_canel);
                final EditText dialogCallerName = (EditText)inputDialog.findViewById(R.id.et_username);
                final EditText dialogReciverName = (EditText)inputDialog.findViewById(R.id.et_recivername);

                diloagBtCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        inputDialog.dismiss();

                    }
                });

                diloagBtOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String callerName = dialogCallerName.getText().toString();
                        String reciverName = dialogReciverName.getText().toString();
                        if (callerName.length()==0 || reciverName.length()==0){
                            Toast.makeText(MainActivity.this, "Please Enter Caller and Reciver Name", Toast.LENGTH_SHORT).show();

                        }else {


                            Intent startCallActivity = new Intent(MainActivity.this, CallActivity.class);

                            startCallActivity.putExtra("callerId", callerName);
                            startCallActivity.putExtra("recipientId", reciverName);
                            inputDialog.dismiss();
                            startActivity(startCallActivity);
                        }
                    }
                });

                inputDialog.setCancelable(false);
                inputDialog.show();


            }
        });
    }

    public void shareScreenClass(){

        btScreenShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent screenShareClass = new Intent(MainActivity.this, SharingScreen.class);
                startActivity(screenShareClass);
            }
        });
    }
}
