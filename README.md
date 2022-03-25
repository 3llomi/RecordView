[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RecordView-orange.svg?style=flat)](https://android-arsenal.com/details/1/6259)
 [ ![Download](https://api.bintray.com/packages/devlomi/maven/RecordView/images/download.svg) ](https://bintray.com/devlomi/maven/RecordView/_latestVersion)

# RecordView

<img src="Logotype primary.png" width="70%" height="70%" />

A Simple Audio Recorder View with hold to Record Button and Swipe to Cancel


## Demo
<p align="center">
  <img src="etc/demo.GIF" height="500" alt="demo image" />
</p>




## Install
Add this to your project build.gradle
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Add this to your module build.gradle

```gradle
dependencies {
    //for AppCompat use:
    //appcompat v26+ is higly recommended to support older APIs
    implementation 'com.devlomi.record-view:record-view:2.0.1'
  

    //for AndroidX use:
    implementation 'com.github.3llomi:RecordView:3.1.1'


}
```


## Usage

### XML

```xml

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/parent_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.devlomi.record_view.RecordView
        android:id="@+id/record_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_toLeftOf="@id/record_button"
        app:slide_to_cancel_arrow="@drawable/recv_ic_arrow"
        app:slide_to_cancel_arrow_color="#000000"
        app:slide_to_cancel_bounds="8dp"
        app:slide_to_cancel_margin_right="10dp"
        app:slide_to_cancel_text="Slide To Cancel"
        app:counter_time_color="#ff0000"
        />

    <com.devlomi.record_view.RecordButton
        android:id="@+id/record_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_alignParentRight="true"
        android:background="@drawable/recv_bg_mic"
        android:scaleType="centerInside"
        app:mic_icon="@drawable/recv_ic_mic_white" />


</RelativeLayout>


```


### Java

```java

RecordView recordView = (RecordView) findViewById(R.id.record_view);
RecordButton recordButton = (RecordButton) findViewById(R.id.record_button);

//IMPORTANT
recordButton.setRecordView(recordView);

```

### Handling States

```java
recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart");
            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel");

            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                //Stop Recording..
                //limitReached to determine if the Record was finished when time limit reached.
                String time = getHumanTimeText(recordTime);
                Log.d("RecordView", "onFinish");

                Log.d("RecordTime", time);
            }

            @Override
            public void onLessThanSecond() {
              //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond");
            }
        });

```

### Handle Clicks for Record Button
```java

recordButton.setListenForRecord(false);

 //ListenForRecord must be false ,otherwise onClick will not be called
recordButton.setOnRecordClickListener(new OnRecordClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT).show();
                Log.d("RecordButton","RECORD BUTTON CLICKED");
            }
        });
```

### Listen for Basket Animation End

```java

recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                Log.d("RecordView", "Basket Animation Finished");
            }
        });

```

### Enable Record Lock Feature
```xml
<com.devlomi.record_view.RecordLockView
        android:id="@+id/record_lock"
        android:layout_width="50dp"
        android:layout_height="50dp"
        android:layout_above="@id/record_view_relative_layout"
        android:layout_alignParentRight="true"
        android:layout_marginRight="4dp"
        android:layout_marginBottom="100dp" />
```

```java
recordView.setLockEnabled(true);
recordView.setRecordLockImageView(findViewById(R.id.record_lock));
```



Change Swipe To Cancel Bounds (when the 'Slide To Cancel' Text View get before Counter).
default is 8dp

```java
recordView.setCancelBounds(8);//dp
```

Handling Permissions(Optional)
```java
recordView.setRecordPermissionHandler(new RecordPermissionHandler() {
            @Override
            public boolean isPermissionGranted() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return true;
                }

                boolean recordPermissionAvailable = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED;
                if (recordPermissionAvailable) {
                    return true;
                }


                ActivityCompat.
                        requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                0);

                return false;

            }
        });
```


### Some Customization

```java
recordView.setSmallMicColor(Color.parseColor("#c2185b"));

recordView.setSlideToCancelText("TEXT");

//disable Sounds
recordView.setSoundEnabled(false);

//prevent recording under one Second (it's false by default)
recordView.setLessThanSecondAllowed(false);

//set Custom sounds onRecord 
//you can pass 0 if you don't want to play sound in certain state
recordView.setCustomSounds(R.raw.record_start,R.raw.record_finished,0);

//change slide To Cancel Text Color
recordView.setSlideToCancelTextColor(Color.parseColor("#ff0000"));
//change slide To Cancel Arrow Color
recordView.setSlideToCancelArrowColor(Color.parseColor("#ff0000"));
//change Counter Time (Chronometer) color
recordView.setCounterTimeColor(Color.parseColor("#ff0000"));

//enable or disable ShimmerEffect
recordView.setShimmerEffectEnabled(true);

//auto cancelling recording after timeLimit (In millis)  
recordView.setTimeLimit(30000);//30 sec

//set Trash Icon Color (when slide to cancel is triggered)
recordView.setTrashIconColor(Color.parseColor("#fff000"));

// enable or disable the Growing animation for record Button.
recordView.setRecordButtonGrowingAnimationEnabled(true);

//Lock Customization
recordLockView.setDefaultCircleColor(Color.parseColor("#0A81AB"));
recordLockView.setCircleLockedColor(Color.parseColor("#314E52"));
recordLockView.setLockColor(Color.WHITE);

recordButton.setSendIconResource(R.drawable.recv_ic_send)


```

### Thanks/Credits
- [NetoDevel](https://github.com/NetoDevel) for some inspiration and some code in his lib [audio-recorder-button](https://github.com/safetysystemtechnology/audio-recorder-button) 
- [alexjlockwood](https://github.com/alexjlockwood) for making this Awesome tool  [ShapeShifter](https://shapeshifter.design/) which helped me to animate vectors easily
- team-supercharge for making [ShimmerLayout](https://github.com/team-supercharge/ShimmerLayout)

## Looking for IOS Version?
try out [iRecordView](https://github.com/3llomi/iRecordView)

## Need a Chat app 💬 ?
Check out [FireApp Chat](https://1.envato.market/oebBM9)

<a href="https://1.envato.market/oebBM9">
<img src="http://devlomi.com/wp-content/uploads/2022/02/1.-Header-1.png" height="500" alt="FireApp" />
</a>

```
   Copyright 2018 AbdulAlim Rajjoub

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
