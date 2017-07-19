package com.ranglerz.remand_io;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;


public class SplashActivity extends AppCompatActivity {

    private static int SplashScreenTimeOut = 3000;//3 seconds8
    private int timer = 2;
    Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mHandler = new Handler();


        useHandler();
    }

    //Thread for starting mainActivity
    private Runnable mRunnableStartMainActivity = new Runnable() {
        @Override
        public void run() {
            Log.d("Handler", " Calls");
            timer--;
            mHandler = new Handler();
            mHandler.postDelayed(this, 1000);

            if (timer == 2) {
                //loading.setText("Loading...");
            }
            if (timer == 1) {
                //loading.setText("Loading.");
            }
            if (timer == 0) {
                //loading.setText("Loading..");

                /*Intent i = new Intent(SplashScreen.this, SignInSignUpActivity.class);*/


                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                //    Intent i = new Intent(Splash_Screen.this, TipsForDonorsActvity.class);

                startActivity(i);
                finish();
            }
        }
    };


    //handler for the starign activity
    Handler newHandler;
    public void useHandler(){

        newHandler = new Handler();
        newHandler.postDelayed(mRunnableStartMainActivity, 1000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnableStartMainActivity);
    }
}
