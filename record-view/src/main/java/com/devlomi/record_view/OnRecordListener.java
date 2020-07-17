package com.devlomi.record_view;

import java.io.File;

/**
 * Created by Devlomi on 24/08/2017.
 */

public interface OnRecordListener {
    void onStart();
    void onCancel();
    void onFinish(long recordTime, File recordingPath);
    void onLessThanSecond();
    boolean isPermissionAvailable();
}
