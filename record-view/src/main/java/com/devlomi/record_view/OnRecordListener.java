package com.devlomi.record_view;

/**
 * Created by Devlomi on 24/08/2017.
 */

public interface OnRecordListener {
    void onStart();
    void onCancel();
    void onFinish(long recordTime,boolean limitReached);
    void onLessThanSecond();
}
