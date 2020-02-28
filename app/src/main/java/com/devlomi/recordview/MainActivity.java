package com.devlomi.recordview;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.devlomi.record_view.OnRecordActionListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecordView recordView = findViewById(R.id.record_view);
        RecordButton recordButton = findViewById(R.id.record_button);
        Button btnChangeOnclick = findViewById(R.id.btn_change_onclick);

        //IMPORTANT
        recordButton.setRecordView(recordView);

        //if you want to click the button (in case if you want to make the record button a Send Button for example..)
        //recordButton.setRecordActionListeningEnabled(false);

        btnChangeOnclick.setOnClickListener(v -> {
            if (recordButton.isRecordActionListeningEnabled()) {
                recordButton.setRecordActionListeningEnabled(false);
                Toast.makeText(MainActivity.this, "RecordActionListeningDisabled", Toast.LENGTH_SHORT).show();
            } else {
                recordButton.setRecordActionListeningEnabled(true);
                Toast.makeText(MainActivity.this, "RecordActionListeningEnabled", Toast.LENGTH_SHORT).show();
            }
        });

        //ListenForRecord must be false ,otherwise onClick will not be called
        recordButton.setOnRecordButtonClickListener(v -> {
            Toast.makeText(MainActivity.this, "onRecordButtonClick", Toast.LENGTH_SHORT).show();
            Log.d("RecordButton", "onRecordButtonClick");
        });


        //Cancel Bounds is when the Slide To Cancel text gets before the timer . default is 8
        recordView.setCancelBoundsInDP(8);


        recordView.setSmallMicColor(Color.parseColor("#c2185b"));

        //prevent recording under one Second
        recordView.setMinRecordDurationInSeconds(1);

        //prevent recording under one Second and have maximum bound of five seconds
        //you can pass 0 as maximum boundary to have endless limit (the default)
        recordView.setRecordDurationBoundsInSeconds(1, 5);



        recordView.setSlideToCancelText("Slide To Cancel");


        recordView.setCustomSounds(R.raw.record_start, R.raw.record_finished, 0);


        recordView.setOnRecordListener(new OnRecordActionListener() {
            @Override
            public void onStart() {
                Toast.makeText(MainActivity.this, "OnStart", Toast.LENGTH_SHORT).show();
                Log.d("RecordView", "onStart");
            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "onCancel", Toast.LENGTH_SHORT).show();
                Log.d("RecordView", "onCancel");

            }

            @Override
            public void onMaxDurationReached() {
                Toast.makeText(MainActivity.this, "onMaxDurationReached", Toast.LENGTH_SHORT).show();
                Log.d("RecordView", "onMaxDurationReached");
            }

            @Override
            public void onFinish(long recordTime) {
                String time = getHumanTimeText(recordTime);
                Toast.makeText(MainActivity.this, "onFinish - Recorded Time : " + time, Toast.LENGTH_SHORT).show();
                Log.d("RecordView", "onFinish");
                Log.d("RecordTime", time);
            }

            @Override
            public void onLessThanMinimumDuration() {
                Toast.makeText(MainActivity.this, "onLessThanMinimumDuration", Toast.LENGTH_SHORT).show();
                Log.d("RecordView", "onLessThanMinimumDuration");
            }
        });


        recordView.setOnBasketAnimationEndListener(() -> {
            Toast.makeText(MainActivity.this, "onBasketAnimationEnd", Toast.LENGTH_SHORT).show();
            Log.d("RecordView", "onBasketAnimationEnd");
        });


    }


    @SuppressLint("DefaultLocale")
    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }


}
