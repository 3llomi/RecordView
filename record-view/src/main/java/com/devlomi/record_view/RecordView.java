package com.devlomi.record_view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wnafee.vector.compat.AnimatedVectorDrawable;

import java.io.IOException;

/**
 * Created by Devlomi on 24/08/2017.
 */

public class RecordView extends RelativeLayout {
    private ImageView recordBtn, smallBlinkingMic, basketImg;
    private Chronometer counterTime;
    private TextView slideToCancel;


    private float initialX, slideInitialX, basketInitialY, difX, recordScaleX, recordScaleY = 0;
    private float cancelBounds = 130;
    private long startTime, elapsedTime = 0;
    private Context context;
    private AlphaAnimation alphaAnimation1, alphaAnimation2;
    private TransitionDrawable colorAnimation;
    private OnRecordListener recordListener;
    private AnimatedVectorDrawable animatedVectorDrawable;
    private boolean isSwiped, isLessThanSecondAllowed = false;
    private boolean isSoundEnabled = true;
    private int RECORD_START = R.raw.record_start;
    private int RECORD_FINISHED = R.raw.record_finished;
    private int RECORD_ERROR = R.raw.record_error;


    private MediaPlayer player;


    public RecordView(Context context) {
        super(context);
        this.context = context;
        init(context, null, -1, -1);


    }


    public RecordView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context, attrs, -1, -1);


    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context, attrs, defStyleAttr, -1);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RecordView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        View view = View.inflate(context, R.layout.record_view, null);
        addView(view);
        recordBtn = view.findViewById(R.id.mic_button);
        slideToCancel = view.findViewById(R.id.slide_to_cancel);
        smallBlinkingMic = view.findViewById(R.id.glowing_mic);
        counterTime = view.findViewById(R.id.counter_tv);
        basketImg = view.findViewById(R.id.basket_img);

        hideViews();
        recordScaleX = recordBtn.getScaleX();
        recordScaleY = recordBtn.getScaleY();


        if (attrs != null && defStyleAttr == -1 && defStyleRes == -1) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordView,
                    defStyleAttr, defStyleRes);


            Drawable recordBtnTransitionBackground = typedArray.getDrawable(R.styleable.RecordView_record_btn_transition_background);

            if (recordBtnTransitionBackground != null)
                recordBtn.setBackground(recordBtnTransitionBackground);
            else
                recordBtn.setBackgroundResource(R.drawable.transition_drawable);

        }


        colorAnimation = (TransitionDrawable) recordBtn.getBackground();


        Drawable leftDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_keyboard_arrow_left);


        slideToCancel.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null);

        animatedVectorDrawable = AnimatedVectorDrawable.getDrawable(context, R.drawable.basket_animated);

        recordBtn.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {


                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (recordListener != null)
                            recordListener.onStart();


                        initialX = recordBtn.getX();
                        slideInitialX = slideToCancel.getX();
                        difX = slideInitialX - recordBtn.getX();
                        basketInitialY = basketImg.getY() + 90;


                        playSound(RECORD_START);

                        showViews();
                        animateRecordColor();
                        animateSmallMicAlpha();
                        counterTime.setBase(SystemClock.elapsedRealtime());
                        startTime = System.currentTimeMillis();
                        counterTime.start();
                        isSwiped = false;

                        break;

                    case MotionEvent.ACTION_MOVE:


                        //this if is to prevent move views after call moveImageToBack
                        if (!isSwiped) {


                            //Swipe To Cancel
                            if (motionEvent.getRawX() + difX <= counterTime.getX() + cancelBounds) {
                                hideViews();
                                moveImageToBack();
                                counterTime.stop();
                                animateBasket();
                                if (recordListener != null)
                                    recordListener.onCancel();

                                isSwiped = true;


                            } else {

                                //if statement is to Prevent Swiping out of bounds
                                if (motionEvent.getRawX() < initialX) {
                                    recordBtn.animate()
                                            .x(motionEvent.getRawX())
                                            .setDuration(0)
                                            .start();


                                    slideToCancel.animate()
                                            .x(motionEvent.getRawX() + difX)
                                            .setDuration(0)
                                            .start();

                                }


                            }

                        }

                        break;


                    case MotionEvent.ACTION_UP:


                        elapsedTime = System.currentTimeMillis() - startTime;

                        if (!isLessThanSecondAllowed && !isLessThanOneSecond(elapsedTime) && !isSwiped) {
                            if (recordListener != null)
                                recordListener.onLessThanSecond();

                            playSound(RECORD_ERROR);


                        } else {
                            if (recordListener != null && !isSwiped)
                                recordListener.onFinish(elapsedTime);

                            if (!isSwiped)
                                playSound(RECORD_FINISHED);

                        }


                        hideViews();


                        if (!isSwiped)
                            clearAlphaAnimation();


                        moveImageToBack();
                        counterTime.stop();
                        slideToCancel.setX(slideInitialX);


                        break;


                }


                return false;
            }
        });

    }


    private void animateBasket() {
        basketImg.setVisibility(VISIBLE);
        final TranslateAnimation translateAnimation1 = new TranslateAnimation(0, 0, basketInitialY, basketInitialY - 90);
        translateAnimation1.setDuration(250);
        basketImg.startAnimation(translateAnimation1);


        final TranslateAnimation translateAnimation2 = new TranslateAnimation(0, 0, basketInitialY - 130, basketInitialY);
        translateAnimation2.setDuration(350);

        basketImg.setImageDrawable(animatedVectorDrawable);


        translateAnimation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animatedVectorDrawable.start();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        basketImg.startAnimation(translateAnimation2);
                        clearAlphaAnimation();
                        basketImg.setVisibility(INVISIBLE);
                    }
                }, 350);


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        translateAnimation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                basketImg.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    private void hideViews() {
        slideToCancel.setVisibility(INVISIBLE);
        smallBlinkingMic.setVisibility(GONE);
        counterTime.setVisibility(GONE);
    }

    private void showViews() {
        slideToCancel.setVisibility(VISIBLE);
        smallBlinkingMic.setVisibility(VISIBLE);
        counterTime.setVisibility(VISIBLE);
    }

    private void animateRecordColor() {
        colorAnimation.reverseTransition(300);
        colorAnimation.startTransition(300);
    }

    private void moveImageToBack() {
        final ValueAnimator positionAnimator =
                ValueAnimator.ofFloat(recordBtn.getX(), initialX);

        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float x = (Float) animation.getAnimatedValue();
                recordBtn.setX(x);
            }
        });

        positionAnimator.setDuration(100);
        positionAnimator.start();

        colorAnimation.resetTransition();
        //move slideToCancel to initial Value
        slideToCancel.animate().x(slideInitialX).setDuration(0).start();

    }

    private void animateSmallMicAlpha() {


        alphaAnimation1 = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation1.setDuration(500);


        alphaAnimation1.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation arg0) {
                smallBlinkingMic.startAnimation(alphaAnimation2);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }

        });

        alphaAnimation2 = new AlphaAnimation(1.0f, 0.0f);

        alphaAnimation2.setDuration(500);


        alphaAnimation2.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation arg0) {
                // start animation1 when animation2 ends (repeat)
                smallBlinkingMic.startAnimation(alphaAnimation1);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }

        });

        smallBlinkingMic.startAnimation(alphaAnimation1);


    }

    private void clearAlphaAnimation() {
        alphaAnimation1.cancel();
        alphaAnimation1.reset();
        alphaAnimation1.setAnimationListener(null);
        alphaAnimation2.cancel();
        alphaAnimation2.reset();
        alphaAnimation2.setAnimationListener(null);
        smallBlinkingMic.clearAnimation();
        smallBlinkingMic.setVisibility(View.GONE);
    }


    private boolean isLessThanOneSecond(long time) {
        return time >= 1000;
    }


    private void playSound(int soundRes) {

        if (isSoundEnabled) {
            if (soundRes == 0)
                return;

            try {
                player = new MediaPlayer();
                AssetFileDescriptor afd = context.getResources().openRawResourceFd(soundRes);
                if (afd == null) return;
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                player.prepare();
                player.start();
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
//                         TODO Auto-generated method stub
                        mp.release();
                    }

                });
                player.setLooping(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void setOnRecordListener(OnRecordListener recrodListener) {
        this.recordListener = recrodListener;
    }

    public void setSoundEnabled(boolean isEnabled) {
        isSoundEnabled = isEnabled;
    }

    public void setLessThanSecondAllowed(boolean isAllowed) {
        isLessThanSecondAllowed = isAllowed;
    }

    public void setRecordButtonIcon(int drawable) {
        recordBtn.setImageResource(drawable);
    }

    public void setRecordButtonColor(int color) {
        recordBtn.setColorFilter(color);
    }


    public void setRecordButtonTransitionBackground(int drawable) {
        recordBtn.setBackgroundResource(drawable);
    }

    public void setSlideToCancelText(String text) {
        slideToCancel.setText(text);
    }

    public void setSlideToCancelTextColor(int color) {
        slideToCancel.setTextColor(color);
    }

    public void setSmallMicColor(int color) {
        smallBlinkingMic.setColorFilter(color);
    }

    public void setSmallMicIcon(int icon) {
        smallBlinkingMic.setImageResource(icon);
    }


    public void setCustomSounds(int startSound, int finishedSound, int errorSound) {
        //0 means do not play sound
        RECORD_START = startSound;
        RECORD_FINISHED = finishedSound;
        RECORD_ERROR = errorSound;
    }

    public float getCancelBounds() {
        return cancelBounds;
    }

    public void setCancelBounds(float cancelBounds) {
        this.cancelBounds = cancelBounds;
    }
}


