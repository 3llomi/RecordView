package com.devlomi.recordview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordClickListener;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    private static final int EXTERNAL_STORAGE_MIC_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecordView recordView = (RecordView) findViewById(R.id.record_view);
        final RecordButton recordButton = (RecordButton) findViewById(R.id.record_button);
        Button btnChangeOnclick = (Button) findViewById(R.id.btn_change_onclick);

        //IMPORTANT
        recordButton.setRecordView(recordView);

        // if you want to click the button (in case if you want to make the record button a Send Button for example..)
//        recordButton.setListenForRecord(false);

        btnChangeOnclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (recordButton.isListenForRecord()) {
                    recordButton.setListenForRecord(false);
                    Toast.makeText(MainActivity.this, "onClickEnabled", Toast.LENGTH_SHORT).show();
                } else {
                    recordButton.setListenForRecord(true);
                    Toast.makeText(MainActivity.this, "onClickDisabled", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //ListenForRecord must be false ,otherwise onClick will not be called
        recordButton.setOnRecordClickListener(new OnRecordClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT).show();
                Log.d("RecordButton", "RECORD BUTTON CLICKED");
            }
        });


        //Cancel Bounds is when the Slide To Cancel text gets before the timer . default is 8
        recordView.setCancelBounds(8);


        recordView.setSmallMicColor(Color.parseColor("#c2185b"));

        //prevent recording under one Second
        recordView.setLessThanSecondAllowed(false);


        recordView.setSlideToCancelText("Slide To Cancel");


        recordView.setCustomSounds(R.raw.record_start, R.raw.record_finished, 0);


        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                Log.d("RecordView", "onStart");
                Toast.makeText(MainActivity.this, "OnStartRecord", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "onCancel", Toast.LENGTH_SHORT).show();

                Log.d("RecordView", "onCancel");

            }

            @Override
            public void onFinish(long recordTime, File recordingFile) {

                String time = getHumanTimeText(recordTime);
                Toast.makeText(MainActivity.this, "onFinishRecord - Recorded Time is: " + time, Toast.LENGTH_SHORT).show();
                Log.d("RecordView", "onFinish");

                Log.d("RecordTime", time);
                MediaPlayer mp = new MediaPlayer();

                try {
                    mp.setDataSource(recordingFile.getPath());
                    mp.prepare();
                    mp.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onLessThanSecond() {
                Toast.makeText(MainActivity.this, "OnLessThanSecond", Toast.LENGTH_SHORT).show();
                Log.d("RecordView", "onLessThanSecond");
            }

            @Override
            public boolean isPermissionAvailable() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED&&
                            ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        Log.v("TAG", "Permission is granted");
                        return true;
                    } else {
                        Log.v("TAG", "Permission is revoked");
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO}, EXTERNAL_STORAGE_MIC_PERMISSION);
                        return false;
                    }
                } else {
                    return true;
                }
            }
        });


        recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                Log.d("RecordView", "Basket Animation Finished");
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
