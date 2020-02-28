package com.devlomi.record_view;

/**
 * Created by Devlomi on 24/08/2017.
 */

public interface OnRecordActionListener {
    void onStart();

    void onCancel();

    void onMaxDurationReached();

    void onFinish(long recordTime);

    void onLessThanMinimumDuration();
}
