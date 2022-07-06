package com.devlomi.record_view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by Devlomi on 13/12/2017.
 */

public class ScaleAnim {
    private View view;
    private float scaleUpTo = 2.0f;

    public ScaleAnim(View view) {
        this.view = view;
    }

    public void setScaleUpTo(float scaleUpTo) {
        this.scaleUpTo = scaleUpTo;
    }

    void start() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", scaleUpTo);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", scaleUpTo);

        set.setDuration(150);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(scaleY, scaleX);
        set.start();
    }

    void stop() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f);

        set.setDuration(150);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.playTogether(scaleY, scaleX);
        set.start();
    }
}
