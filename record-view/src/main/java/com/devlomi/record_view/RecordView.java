package com.devlomi.record_view;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
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

public class RecordView extends RelativeLayout {

    public static final int DEFAULT_CANCEL_BOUNDS = 8; //8dp
    public static final int DEFAULT_MIN_RECORD_DURATION = 1;
    public static final int ENDLESS_MAX_RECORD_DURATION = 0;
    private ImageView smallBlinkingMic, basketImg;
    private Chronometer counterTime;
    private TextView slideToCancel;
    private ShimmerLayout slideToCancelLayout;
    private ImageView arrow;
    private float initialX, basketInitialY, difX = 0;
    private float cancelBounds = DEFAULT_CANCEL_BOUNDS;
    private long startTime, elapsedTime = 0;
    private Context context;
    private OnRecordActionListener recordActionListener;
    private boolean isSwiped;
    private boolean isSoundEnabled = true;
    private int RECORD_START = R.raw.record_start;
    private int RECORD_FINISHED = R.raw.record_finished;
    private int RECORD_ERROR = R.raw.record_error;
    private AnimationHelper animationHelper;
    private boolean isRTL;
    private int minRecordDurationInSeconds = DEFAULT_MIN_RECORD_DURATION, maxRecordDurationInSeconds = ENDLESS_MAX_RECORD_DURATION;


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

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        isRTL = getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;

        inflate(context, R.layout.record_view_layout, this);

        setClipChildren(false);

        arrow = findViewById(R.id.arrow);
        slideToCancel = findViewById(R.id.slide_to_cancel);
        smallBlinkingMic = findViewById(R.id.glowing_mic);
        counterTime = findViewById(R.id.counter_tv);
        basketImg = findViewById(R.id.basket_img);
        slideToCancelLayout = findViewById(R.id.shimmer_layout);

        counterTime.setOnChronometerTickListener(chronometer -> {
            elapsedTime = SystemClock.elapsedRealtime() - chronometer.getBase();
            if (maxRecordDurationInSeconds > 0 && elapsedTime >= maxRecordDurationInSeconds * 1000) {
                chronometer.stop();
                if (recordActionListener != null)
                    recordActionListener.onMaxDurationReached();
            }
        });

        hideViews(true);


        if (attrs != null && defStyleAttr == -1 && defStyleRes == -1) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordView,
                    defStyleAttr, defStyleRes);


            int slideArrowResource = typedArray.getResourceId(R.styleable.RecordView_slide_to_cancel_arrow, -1);
            String slideToCancelText = typedArray.getString(R.styleable.RecordView_slide_to_cancel_text);
            int slideMarginEnd = (int) typedArray.getDimension(R.styleable.RecordView_slide_to_cancel_margin_end, 30);
            int counterTimeColor = typedArray.getColor(R.styleable.RecordView_counter_time_color, -1);
            int arrowColor = typedArray.getColor(R.styleable.RecordView_slide_to_cancel_arrow_color, -1);


            int cancelBounds = typedArray.getDimensionPixelSize(R.styleable.RecordView_slide_to_cancel_bounds, -1);

            if (cancelBounds != -1)
                setCancelBounds(cancelBounds, false);


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


            setMarginEnd(slideMarginEnd, true);

            typedArray.recycle();
        }


        animationHelper = new AnimationHelper(context, basketImg, smallBlinkingMic);

    }


    private void hideViews(boolean hideSmallMic) {
        slideToCancelLayout.setVisibility(GONE);
        counterTime.setVisibility(GONE);
        if (hideSmallMic)
            smallBlinkingMic.setVisibility(GONE);
    }

    private void showViews() {
        slideToCancelLayout.setVisibility(VISIBLE);
        smallBlinkingMic.setVisibility(VISIBLE);
        counterTime.setVisibility(VISIBLE);
    }

    private boolean isLessThanMinimumDuration(long time) {
        return time <= minRecordDurationInSeconds * 1000;
    }

    private void playSound(int soundRes) {

        if (isSoundEnabled) {
            if (soundRes == 0)
                return;

            try {
                MediaPlayer player = new MediaPlayer();
                AssetFileDescriptor afd = context.getResources().openRawResourceFd(soundRes);
                if (afd == null) return;
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                player.prepare();
                player.start();
                player.setOnCompletionListener(MediaPlayer::release);
                player.setLooping(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


    protected void onActionDown(RecordButton recordBtn) {
        if (recordActionListener != null)
            recordActionListener.onStart();


        animationHelper.setStartRecorded(true);
        animationHelper.resetBasketAnimation();
        animationHelper.resetSmallMic();


        recordBtn.startScale();
        slideToCancelLayout.startShimmerAnimation();

        initialX = isRTL
                ? recordBtn.getX() + recordBtn.getWidth()
                : recordBtn.getX();

        basketInitialY = basketImg.getY() + 90;

        playSound(RECORD_START);

        showViews();

        animationHelper.animateSmallMicAlpha();
        counterTime.setBase(SystemClock.elapsedRealtime());
        startTime = System.currentTimeMillis();
        counterTime.start();
        isSwiped = false;

    }

    protected void onActionMove(RecordButton recordBtn, MotionEvent motionEvent) {
        long time = System.currentTimeMillis() - startTime;

        if (!isSwiped) {

            //Swipe To Cancel
            boolean shouldStartCancelAnimation = isRTL
                    ? slideToCancelLayout.getX() != 0
                    && slideToCancelLayout.getX() + slideToCancelLayout.getWidth() >= counterTime.getLeft() - cancelBounds
                    : slideToCancelLayout.getX() != 0
                    && slideToCancelLayout.getX() <= counterTime.getRight() + cancelBounds;
            if (shouldStartCancelAnimation) {
                //if the time was less than one second then do not start basket animation
                if (isLessThanMinimumDuration(time)) {
                    hideViews(true);
                    animationHelper.clearAlphaAnimation(false);


                    animationHelper.onAnimationEnd();

                } else {
                    hideViews(false);
                    animationHelper.animateBasket(basketInitialY);
                }

                animationHelper.moveRecordButtonAndSlideToCancelBack(recordBtn, slideToCancelLayout, initialX, difX, isRTL);

                counterTime.stop();
                slideToCancelLayout.stopShimmerAnimation();
                isSwiped = true;


                animationHelper.setStartRecorded(false);

                if (recordActionListener != null)
                    recordActionListener.onCancel();


            } else {


                //if statement is to Prevent Swiping out of bounds
                boolean isSwiped = isRTL
                        ? motionEvent.getRawX() > initialX
                        : motionEvent.getRawX() < initialX;
                if (isSwiped) {
                    recordBtn.animate()
                            .x(isRTL
                                    ? motionEvent.getRawX() - recordBtn.getWidth()
                                    : motionEvent.getRawX())
                            .setDuration(0)
                            .start();


                    if (difX == 0)
                        difX = isRTL
                                ? (slideToCancelLayout.getX() + slideToCancelLayout.getWidth() - initialX)
                                : (initialX - slideToCancelLayout.getX());


                    slideToCancelLayout.animate()
                            .x(isRTL
                                    ? motionEvent.getRawX() + difX - slideToCancelLayout.getWidth()
                                    : motionEvent.getRawX() - difX)
                            .setDuration(0)
                            .start();


                }


            }

        }
    }

    protected void onActionUp(RecordButton recordBtn) {

        if (isLessThanMinimumDuration(elapsedTime) && !isSwiped) {
            if (recordActionListener != null)
                recordActionListener.onLessThanMinimumDuration();

            animationHelper.setStartRecorded(false);

            playSound(RECORD_ERROR);


        } else {
            if (recordActionListener != null && !isSwiped)
                recordActionListener.onFinish(elapsedTime);

            animationHelper.setStartRecorded(false);


            if (!isSwiped)
                playSound(RECORD_FINISHED);

        }


        //if user has swiped then do not hide SmallMic since it will be hidden after swipe Animation
        hideViews(!isSwiped);


        if (!isSwiped)
            animationHelper.clearAlphaAnimation(true);

        animationHelper.moveRecordButtonAndSlideToCancelBack(recordBtn, slideToCancelLayout, initialX, difX, isRTL);
        counterTime.stop();
        slideToCancelLayout.stopShimmerAnimation();


    }


    private void setMarginEnd(int marginEnd, boolean convertToDp) {
        LayoutParams layoutParams = (LayoutParams) slideToCancelLayout.getLayoutParams();
        if (convertToDp) {
            layoutParams.setMarginEnd((int) DpUtil.toPixel(marginEnd, context));
        } else
            layoutParams.setMarginEnd(marginEnd);

        slideToCancelLayout.setLayoutParams(layoutParams);
    }

    private void setCancelBounds(float cancelBounds, boolean convertDpToPixel) {
        this.cancelBounds = convertDpToPixel ? DpUtil.toPixel(cancelBounds, context) : cancelBounds;
    }


    // public setters and getters

    public void setOnRecordActionListener(OnRecordActionListener recordActionListener) {
        this.recordActionListener = recordActionListener;
    }

    public void setOnBasketAnimationEndListener(OnBasketAnimationEndListener onBasketAnimationEndListener) {
        animationHelper.setOnBasketAnimationEndListener(onBasketAnimationEndListener);
    }

    public void setSoundEnabled(boolean isEnabled) {
        isSoundEnabled = isEnabled;
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

    public void setSlideMarginEndInDp(int marginEnd) {
        setMarginEnd(marginEnd, true);
    }

    public void setCancelBoundsInDP(float cancelBounds) {
        setCancelBounds(cancelBounds, true);
    }

    public void setCustomSounds(int startSound, int finishedSound, int errorSound) {
        //0 means do not play sound
        RECORD_START = startSound;
        RECORD_FINISHED = finishedSound;
        RECORD_ERROR = errorSound;
    }

    public void setCounterTimeColor(int color) {
        counterTime.setTextColor(color);
    }

    public void setSlideToCancelArrowColor(int color) {
        arrow.setColorFilter(color);
    }

    public void setMinRecordDurationInSeconds(int minRecordDurationInSeconds) throws RecordDurationBoundariesException {
        if (minRecordDurationInSeconds <= 0
                || (maxRecordDurationInSeconds != ENDLESS_MAX_RECORD_DURATION && maxRecordDurationInSeconds <= minRecordDurationInSeconds))
            throw new RecordDurationBoundariesException();
        this.minRecordDurationInSeconds = minRecordDurationInSeconds;
    }

    public void setRecordDurationBoundsInSeconds(int minRecordDurationInSeconds, int maxRecordDurationInSeconds) throws RecordDurationBoundariesException {
        if (minRecordDurationInSeconds <= 0
                || (maxRecordDurationInSeconds != ENDLESS_MAX_RECORD_DURATION && maxRecordDurationInSeconds <= minRecordDurationInSeconds))
            throw new RecordDurationBoundariesException();
        this.minRecordDurationInSeconds = minRecordDurationInSeconds;
        this.maxRecordDurationInSeconds = maxRecordDurationInSeconds;
    }

    public float getCancelBounds() {
        return cancelBounds;
    }

    public int getMinRecordDurationInSeconds() {
        return minRecordDurationInSeconds;
    }

    public int getMaxRecordDurationInSeconds() {
        return maxRecordDurationInSeconds;
    }
}


