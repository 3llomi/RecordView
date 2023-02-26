package com.devlomi.record_view;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import java.io.IOException;

import io.supercharge.shimmerlayout.ShimmerLayout;

/**
 * Created by Devlomi on 24/08/2017.
 */

public class RecordView extends RelativeLayout implements RecordLockViewListener {

    public static final int DEFAULT_CANCEL_BOUNDS = 8; //8dp
    private ImageView smallBlinkingMic, basketImg;
    private Chronometer counterTime;
    private TextView slideToCancel, cancelTextView;
    private ShimmerLayout slideToCancelLayout;
    private ImageView arrow;
    private float initialRecordButtonX, initialRecordButtonY, recordButtonYInWindow, basketInitialY, difX = 0;
    private float cancelBounds = DEFAULT_CANCEL_BOUNDS;
    private long startTime, elapsedTime = 0;
    private Context context;
    private OnRecordListener recordListener;
    private RecordPermissionHandler recordPermissionHandler;
    private boolean isSwiped, isLessThanSecondAllowed = false;
    private boolean isSoundEnabled = true;
    private int RECORD_START = R.raw.record_start;
    private int RECORD_FINISHED = R.raw.record_finished;
    private int RECORD_ERROR = R.raw.record_error;
    private MediaPlayer player;
    private AnimationHelper animationHelper;
    private boolean isRecordButtonGrowingAnimationEnabled = true;
    private boolean shimmerEffectEnabled = true;
    private long timeLimit = -1;
    private Runnable runnable;
    private Handler handler;
    private RecordButton recordButton;

    private boolean canRecord = true;

    private RecordLockView recordLockView;
    private boolean isLockEnabled = false;
    float recordLockYInWindow = 0f;
    float recordLockXInWindow = 0f;
    private boolean fractionReached = false;
    private float currentYFraction = 0f;
    private boolean isLockInSameParent = false;


    public RecordView(Context context) {
        super(context);
        this.context = context;
        init(context, null, 0, 0);
    }


    public RecordView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context, attrs, 0, 0);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context, attrs, defStyleAttr, 0);
    }


    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        View view = View.inflate(context, R.layout.record_view_layout, null);
        addView(view);


        ViewGroup viewGroup = (ViewGroup) view.getParent();
        viewGroup.setClipChildren(false);

        arrow = view.findViewById(R.id.arrow);
        slideToCancel = view.findViewById(R.id.slide_to_cancel);
        smallBlinkingMic = view.findViewById(R.id.glowing_mic);
        counterTime = view.findViewById(R.id.counter_tv);
        basketImg = view.findViewById(R.id.basket_img);
        slideToCancelLayout = view.findViewById(R.id.shimmer_layout);
        cancelTextView = view.findViewById(R.id.recv_tv_cancel);


        hideViews(true);


        if (attrs != null && defStyleAttr == 0 && defStyleRes == 0) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordView,
                    defStyleAttr, defStyleRes);


            int slideArrowResource = typedArray.getResourceId(R.styleable.RecordView_slide_to_cancel_arrow, -1);
            String slideToCancelText = typedArray.getString(R.styleable.RecordView_slide_to_cancel_text);
            int slideMarginRight = (int) typedArray.getDimension(R.styleable.RecordView_slide_to_cancel_margin_right, 30);
            int counterTimeColor = typedArray.getColor(R.styleable.RecordView_counter_time_color, -1);
            int arrowColor = typedArray.getColor(R.styleable.RecordView_slide_to_cancel_arrow_color, -1);

            String cancelText = typedArray.getString(R.styleable.RecordView_cancel_text);
            int cancelMarginRight = (int) typedArray.getDimension(R.styleable.RecordView_cancel_text_margin_right, 30);
            int cancelTextColor = typedArray.getColor(R.styleable.RecordView_cancel_text_color, -1);


            int cancelBounds = typedArray.getDimensionPixelSize(R.styleable.RecordView_slide_to_cancel_bounds, -1);

            if (cancelBounds != -1)
                setCancelBounds(cancelBounds, false);//don't convert it to pixels since it's already in pixels


            if (slideArrowResource != -1) {
                Drawable slideArrow = AppCompatResources.getDrawable(getContext(), slideArrowResource);
                arrow.setImageDrawable(slideArrow);
            }

            if (slideToCancelText != null)
                slideToCancel.setText(slideToCancelText);

            if (counterTimeColor != -1)
                setCounterTimeColor(counterTimeColor);


            if (arrowColor != -1)
                setSlideToCancelArrowColor(arrowColor);

            if (cancelText != null) {
                cancelTextView.setText(cancelText);
            }

            if (cancelTextColor != -1) {
                cancelTextView.setTextColor(cancelTextColor);
            }

            setMarginRight(slideMarginRight, true);
            setCancelMarginRight(cancelMarginRight, true);

            typedArray.recycle();
        }


        animationHelper = new AnimationHelper(context, basketImg, smallBlinkingMic, isRecordButtonGrowingAnimationEnabled);

        cancelTextView.setOnClickListener(v -> {
            animationHelper.animateBasket(basketInitialY);
            cancelAndDeleteRecord();
        });

    }

    private void cancelAndDeleteRecord() {
        if (isTimeLimitValid()) {
            removeTimeLimitCallbacks();
        }


        isSwiped = true;

        animationHelper.setStartRecorded(false);

        if (recordListener != null) {
            recordListener.onCancel();
        }

        resetRecord(recordButton);
    }

    private boolean isTimeLimitValid() {
        return timeLimit > 0;
    }

    private void initTimeLimitHandler() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                if (recordListener != null && !isSwiped)
                    recordListener.onFinish(elapsedTime, true);

                removeTimeLimitCallbacks();

                animationHelper.setStartRecorded(false);


                if (!isSwiped)
                    playSound(RECORD_FINISHED);


                if (recordButton != null) {
                    resetRecord(recordButton);
                }
                isSwiped = true;

            }

        };
    }


    private void hideViews(boolean hideSmallMic) {
        slideToCancelLayout.setVisibility(GONE);
        counterTime.setVisibility(GONE);
        cancelTextView.setVisibility(GONE);
        if (isLockEnabled && recordLockView != null) {
            recordLockView.setVisibility(GONE);
        }
        if (hideSmallMic)
            smallBlinkingMic.setVisibility(GONE);
    }

    private void showViews() {
        slideToCancelLayout.setVisibility(VISIBLE);
        smallBlinkingMic.setVisibility(VISIBLE);
        counterTime.setVisibility(VISIBLE);
        if (isLockEnabled && recordLockView != null) {
            recordLockView.setVisibility(VISIBLE);
        }

    }


    private boolean isLessThanOneSecond(long time) {
        return time <= 1000;
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
                        mp.release();
                    }

                });
                player.setLooping(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


    protected void onActionDown(RecordButton recordBtn, MotionEvent motionEvent) {

        if (!isRecordPermissionGranted()) {
            return;
        }


        if (recordListener != null)
            recordListener.onStart();

        if (isTimeLimitValid()) {
            removeTimeLimitCallbacks();
            handler.postDelayed(runnable, timeLimit);
        }

        animationHelper.setStartRecorded(true);
        animationHelper.resetBasketAnimation();
        animationHelper.resetSmallMic();


        if (isRecordButtonGrowingAnimationEnabled) {
            recordBtn.startScale();
        }

        if (shimmerEffectEnabled) {
            slideToCancelLayout.startShimmerAnimation();
        }

        initialRecordButtonX = recordBtn.getX();


        int[] recordButtonLocation = new int[2];
        recordBtn.getLocationInWindow(recordButtonLocation);

        initialRecordButtonY = recordButton.getY();

        if (isLockEnabled && recordLockView != null) {
            isLockInSameParent = isLockAndRecordButtonHaveSameParent();
            int[] recordLockLocation = new int[2];
            recordLockView.getLocationInWindow(recordLockLocation);
            recordLockXInWindow = recordLockLocation[0];
            recordLockYInWindow = isLockInSameParent ? recordLockView.getY() : recordLockLocation[1];
            recordButtonYInWindow = isLockInSameParent ? recordButton.getY() : recordButtonLocation[1];
        }


        basketInitialY = basketImg.getY() + 90;

        playSound(RECORD_START);

        showViews();

        animationHelper.animateSmallMicAlpha();
        counterTime.setBase(SystemClock.elapsedRealtime());
        startTime = System.currentTimeMillis();
        counterTime.start();
        isSwiped = false;
        currentYFraction = 0f;

    }


    protected void onActionMove(final RecordButton recordBtn, MotionEvent motionEvent) {

        if (!canRecord || fractionReached) {
            return;
        }

        long time = System.currentTimeMillis() - startTime;

        if (!isSwiped) {

            //Swipe To Cancel
            if (slideToCancelLayout.getX() != 0 && slideToCancelLayout.getX() <= counterTime.getRight() + cancelBounds) {

                //if the time was less than one second then do not start basket animation
                if (isLessThanOneSecond(time)) {
                    hideViews(true);
                    animationHelper.clearAlphaAnimation(false);


                    animationHelper.onAnimationEnd();

                } else {
                    hideViews(false);
                    animationHelper.animateBasket(basketInitialY);
                }

                animationHelper.moveRecordButtonAndSlideToCancelBack(recordBtn, slideToCancelLayout, initialRecordButtonX, initialRecordButtonY, difX, isLockEnabled);

                counterTime.stop();
                if (shimmerEffectEnabled) {
                    slideToCancelLayout.stopShimmerAnimation();
                }

                isSwiped = true;


                animationHelper.setStartRecorded(false);

                if (recordListener != null)
                    recordListener.onCancel();

                if (isTimeLimitValid()) {
                    removeTimeLimitCallbacks();
                }


            } else {


                if (canMoveX(motionEvent)) {
                    recordBtn.animate()
                            .x(motionEvent.getRawX())
                            .setDuration(0)
                            .start();


                    if (difX == 0)
                        difX = (initialRecordButtonX - slideToCancelLayout.getX());


                    slideToCancelLayout.animate()
                            .x(motionEvent.getRawX() - difX)
                            .setDuration(0)
                            .start();


                }

                  /*
                  if RecordLock was NOT inside the same parent as RecordButton
                   animate.y() OR view.setY() will setY value INSIDE its parent
                   we need a way to convert the inner value to outer value
                   since motionEvent.getRawY() returns Y's location onScreen
                   we had to get screen height and get the difference between motionEvent and screen height
                 */
                float newY = isLockInSameParent ? motionEvent.getRawY() : motionEvent.getRawY() - recordButtonYInWindow;
                if (canMoveY(motionEvent, newY)) {

                    recordBtn.animate()
                            .y(newY)
                            .setDuration(0)
                            .start();

                    float currentY = motionEvent.getRawY();
                    float minY = recordLockYInWindow;
                    float maxY = recordButtonYInWindow;

                    float fraction = (currentY - minY) / (maxY - minY);
                    fraction = 1 - fraction;
                    currentYFraction = fraction;

                    recordLockView.animateLock(fraction);

                    if (isRecordButtonGrowingAnimationEnabled) {
                        //convert fraction to scale
                        //so instead of starting from 0 to 1, it will start from 1 to 0
                        float scale = 1 - fraction + 1;
                        recordBtn.animate().scaleX(scale).scaleY(scale).setDuration(0).start();
                    }
                }
            }

        }
    }


    private boolean canMoveX(MotionEvent motionEvent) {
        //Prevent Swiping out of bounds
        if (motionEvent.getRawX() < initialRecordButtonX) {
            if (isLockEnabled) {
                //prevent swiping X if record button goes up
                return currentYFraction <= 0.3;
            }
            return true;
        }

        return false;
    }

    private boolean canMoveY(MotionEvent motionEvent, float dif) {

        if (isLockEnabled) {
            /*
             1. prevent swiping below record button
             2. prevent swiping up if record button is NOT near record Lock's X
             */
            if(isLockInSameParent){
                return motionEvent.getRawY() < initialRecordButtonY && motionEvent.getRawX() >= recordLockXInWindow;
            }else {
                return dif <= initialRecordButtonY && motionEvent.getRawX() >= recordLockXInWindow;
            }
        }

        return false;

    }

    protected void onActionUp(RecordButton recordBtn) {

        if (!canRecord || fractionReached) {
            return;
        }

        finishAndSaveRecord();

    }

    private void finishAndSaveRecord() {
        elapsedTime = System.currentTimeMillis() - startTime;

        if (!isLessThanSecondAllowed && isLessThanOneSecond(elapsedTime) && !isSwiped) {
            if (recordListener != null)
                recordListener.onLessThanSecond();

            removeTimeLimitCallbacks();
            animationHelper.setStartRecorded(false);

            playSound(RECORD_ERROR);


        } else {
            if (recordListener != null && !isSwiped)
                recordListener.onFinish(elapsedTime, false);

            removeTimeLimitCallbacks();

            animationHelper.setStartRecorded(false);


            if (!isSwiped)
                playSound(RECORD_FINISHED);

        }

        resetRecord(recordButton);
    }

    private void switchToLockedMode() {
        cancelTextView.setVisibility(VISIBLE);
        slideToCancelLayout.setVisibility(GONE);

        recordButton.animate()
                .x(initialRecordButtonX)
                .y(initialRecordButtonY)
                .setDuration(100)
                .start();

        if (isRecordButtonGrowingAnimationEnabled) {
            recordButton.stopScale();
        }

        recordButton.setListenForRecord(false);
        recordButton.setInLockMode(true);
        recordButton.changeIconToSend();

    }

    private boolean isLockAndRecordButtonHaveSameParent() {
        if (recordLockView == null){
            return false;
        }

        ViewParent lockParent = recordLockView.getParent();
        ViewParent recordButtonParent = recordButton.getParent();
        if (lockParent == null || recordButtonParent == null) {
            return false;
        }
        return lockParent == recordButtonParent;
    }

    private void resetRecord(RecordButton recordBtn) {
        //if user has swiped then do not hide SmallMic since it will be hidden after swipe Animation
        hideViews(!isSwiped);
        fractionReached = false;

        if (!isSwiped)
            animationHelper.clearAlphaAnimation(true);

        animationHelper.moveRecordButtonAndSlideToCancelBack(recordBtn, slideToCancelLayout, initialRecordButtonX, initialRecordButtonY, difX, isLockEnabled);
        counterTime.stop();
        if (shimmerEffectEnabled) {
            slideToCancelLayout.stopShimmerAnimation();
        }

        if (isLockEnabled) {
            recordLockView.reset();
            recordBtn.changeIconToRecord();
        }

        cancelTextView.setVisibility(GONE);
        recordBtn.setListenForRecord(true);
        recordBtn.setInLockMode(false);
    }

    private void removeTimeLimitCallbacks() {
        if (isTimeLimitValid()) {
            handler.removeCallbacks(runnable);
        }
    }


    private boolean isRecordPermissionGranted() {

        if (recordPermissionHandler == null) {
            canRecord = true;
        } else {
            canRecord = recordPermissionHandler.isPermissionGranted();
        }

        return canRecord;
    }

    private void setMarginRight(int marginRight, boolean convertToDp) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) slideToCancelLayout.getLayoutParams();
        if (convertToDp) {
            layoutParams.rightMargin = (int) DpUtil.toPixel(marginRight, context);
        } else
            layoutParams.rightMargin = marginRight;

        slideToCancelLayout.setLayoutParams(layoutParams);
    }

    private void setCancelMarginRight(int marginRight, boolean convertToDp) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) slideToCancelLayout.getLayoutParams();
        if (convertToDp) {
            layoutParams.rightMargin = (int) DpUtil.toPixel(marginRight, context);
        } else
            layoutParams.rightMargin = marginRight;

        cancelTextView.setLayoutParams(layoutParams);
    }


    public void setOnRecordListener(OnRecordListener recrodListener) {
        this.recordListener = recrodListener;
    }

    public void setRecordPermissionHandler(RecordPermissionHandler recordPermissionHandler) {
        this.recordPermissionHandler = recordPermissionHandler;
    }

    public void setOnBasketAnimationEndListener(OnBasketAnimationEnd onBasketAnimationEndListener) {
        animationHelper.setOnBasketAnimationEndListener(onBasketAnimationEndListener);
    }

    public void setSoundEnabled(boolean isEnabled) {
        isSoundEnabled = isEnabled;
    }

    public void setLessThanSecondAllowed(boolean isAllowed) {
        isLessThanSecondAllowed = isAllowed;
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

    public void setSlideMarginRight(int marginRight) {
        setMarginRight(marginRight, true);
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
        setCancelBounds(cancelBounds, true);
    }

    //set Chronometer color
    public void setCounterTimeColor(int color) {
        counterTime.setTextColor(color);
    }

    public void setSlideToCancelArrowColor(int color) {
        arrow.setColorFilter(color);
    }


    private void setCancelBounds(float cancelBounds, boolean convertDpToPixel) {
        float bounds = convertDpToPixel ? DpUtil.toPixel(cancelBounds, context) : cancelBounds;
        this.cancelBounds = bounds;
    }

    public boolean isRecordButtonGrowingAnimationEnabled() {
        return isRecordButtonGrowingAnimationEnabled;
    }

    public void setRecordButtonGrowingAnimationEnabled(boolean recordButtonGrowingAnimationEnabled) {
        isRecordButtonGrowingAnimationEnabled = recordButtonGrowingAnimationEnabled;
        animationHelper.setRecordButtonGrowingAnimationEnabled(recordButtonGrowingAnimationEnabled);
    }

    public boolean isShimmerEffectEnabled() {
        return shimmerEffectEnabled;
    }

    public void setShimmerEffectEnabled(boolean shimmerEffectEnabled) {
        this.shimmerEffectEnabled = shimmerEffectEnabled;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;

        if (handler != null && runnable != null) {
            removeTimeLimitCallbacks();
        }
        initTimeLimitHandler();
    }

    public void setTrashIconColor(int color) {
        animationHelper.setTrashIconColor(color);
    }

    public void setRecordLockImageView(RecordLockView recordLockView) {
        this.recordLockView = recordLockView;
        this.recordLockView.setRecordLockViewListener(this);
        this.recordLockView.setVisibility(INVISIBLE);
    }

    public void setLockEnabled(boolean lockEnabled) {
        isLockEnabled = lockEnabled;
    }

    protected void setRecordButton(RecordButton recordButton) {
        this.recordButton = recordButton;
        this.recordButton.setSendClickListener(v -> {
            finishAndSaveRecord();
        });
    }

    /*
    Use this if you want to Finish And save the Record if user closes the app for example in 'onPause()'
     */
    public void finishRecord() {
        finishAndSaveRecord();
    }

    /*
    Use this if you want to Cancel And delete the Record if user closes the app for example in 'onPause()'
     */
    public void cancelRecord() {
        hideViews(true);
        animationHelper.clearAlphaAnimation(false);
        cancelAndDeleteRecord();
    }

    @Override
    public void onFractionReached() {
        fractionReached = true;
        switchToLockedMode();
        if (recordListener != null) {
            recordListener.onLock();
        }
    }
}


