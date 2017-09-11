package com.devlomi.recordview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecordView recordView = (RecordView) findViewById(R.id.record_view);

        //Cancel Bounds is when the Slide To Cancel text gets before the timer . default is 130
        recordView.setCancelBounds(130);


        recordView.setSmallMicColor(Color.parseColor("#c2185b"));

        recordView.setRecordButtonColor(Color.parseColor("#ffffff"));

        recordView.setRecordButtonTransitionBackground(R.drawable.transition_drawable);


        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                Log.d("RecordView", "onStart");
            }

            @Override
            public void onCancel() {
                Log.d("RecordView", "onCancel");

            }

            @Override
            public void onFinish(long recordTime) {
                String time = getHumanTimeText(recordTime);
                Log.d("RecordView", "onFinish");

                Log.d("RecordTime", time);
            }

            @Override
            public void onLessThanSecond() {
                Log.d("RecordView", "onLessThanSecond");
            }
        });


    }

    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }



}
