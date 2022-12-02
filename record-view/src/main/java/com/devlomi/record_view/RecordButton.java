package com.devlomi.record_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by Devlomi on 13/12/2017.
 */

public class RecordButton extends AppCompatImageView implements View.OnTouchListener, View.OnClickListener {

    private ScaleAnim scaleAnim;
    private RecordView recordView;
    private boolean listenForRecord = true;
    private OnRecordClickListener onRecordClickListener;
    private OnRecordClickListener sendClickListener;
    private boolean isInLockMode = false;
    private Drawable micIcon, sendIcon;

    public void setRecordView(RecordView recordView) {
        this.recordView = recordView;
        recordView.setRecordButton(this);
    }

    public RecordButton(Context context) {
        super(context);
        init(context, null);
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        float scaleUpTo = 1f;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordButton);

            int imageResource = typedArray.getResourceId(R.styleable.RecordButton_mic_icon, -1);
            int sendResource = typedArray.getResourceId(R.styleable.RecordButton_send_icon, -1);
            scaleUpTo = typedArray.getFloat(R.styleable.RecordButton_scale_up_to, -1f);

            if (imageResource != -1) {
                setTheImageResource(imageResource);
            }

            if (sendResource != -1) {
                sendIcon = AppCompatResources.getDrawable(getContext(), sendResource);
            }

            typedArray.recycle();
        }

        scaleAnim = new ScaleAnim(this);
        if (scaleUpTo > 1) {
            scaleAnim.setScaleUpTo(scaleUpTo);
        }

        this.setOnTouchListener(this);
        this.setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setClip(this);
    }

    public void setScaleUpTo(Float scaleTo) {
        scaleAnim.setScaleUpTo(scaleTo);
    }

    public void setClip(View v) {
        if (v.getParent() == null) {
            return;
        }

        if (v instanceof ViewGroup) {
            ((ViewGroup) v).setClipChildren(false);
            ((ViewGroup) v).setClipToPadding(false);
        }

        if (v.getParent() instanceof View) {
            setClip((View) v.getParent());
        }
    }


    private void setTheImageResource(int imageResource) {
        Drawable image = AppCompatResources.getDrawable(getContext(), imageResource);
        setImageDrawable(image);
        micIcon = image;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isListenForRecord()) {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    recordView.onActionDown((RecordButton) v, event);
                    break;


                case MotionEvent.ACTION_MOVE:
                    recordView.onActionMove((RecordButton) v, event);
                    break;

                case MotionEvent.ACTION_UP:
                    recordView.onActionUp((RecordButton) v);
                    break;

            }

        }

        return isListenForRecord();
    }


    protected void startScale() {
        scaleAnim.start();
    }

    protected void stopScale() {
        scaleAnim.stop();
    }

    public void setListenForRecord(boolean listenForRecord) {
        this.listenForRecord = listenForRecord;
    }

    public boolean isListenForRecord() {
        return listenForRecord;
    }

    public void setOnRecordClickListener(OnRecordClickListener onRecordClickListener) {
        this.onRecordClickListener = onRecordClickListener;
    }

    protected void setSendClickListener(OnRecordClickListener sendClickListener) {
        this.sendClickListener = sendClickListener;
    }

    protected void setInLockMode(boolean inLockMode) {
        isInLockMode = inLockMode;
    }

    public void setSendIconResource(int resource) {
        sendIcon = AppCompatResources.getDrawable(getContext(), resource);
    }

    @Override
    public void onClick(View v) {
        if (isInLockMode && sendClickListener != null) {
            sendClickListener.onClick(v);
        } else if (onRecordClickListener != null) {
            onRecordClickListener.onClick(v);
        }
    }

    protected void changeIconToSend() {
        if (sendIcon != null) {
            setImageDrawable(sendIcon);
        }
    }

    protected void changeIconToRecord() {
        if (micIcon != null) {
            setImageDrawable(micIcon);
        }
    }
}
