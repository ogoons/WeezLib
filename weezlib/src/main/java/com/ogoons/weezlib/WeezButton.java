package com.ogoons.weezlib;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by ogoons on 2016-09-13.
 */
public class WeezButton extends ImageView {
    private final int DEF_DOWN_PADDING = 6;

    private int             mPadding;
    private FeedbackStyle   mFeedbackStyle;
    private int             mPressColor;

    public WeezButton(Context context) {
        super(context);
        initialize();
    }

    public WeezButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public WeezButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        mPadding    = DEF_DOWN_PADDING;
        mPressColor = Color.argb(17, 0, 0, 0);
        mFeedbackStyle = FeedbackStyle.OVER_COLOR;

        setClickable(true);
        setOnTouchListener(new WeezButtonTouchListener());
    }

    public void setFeedbackStyle(FeedbackStyle feedbackStyle) {
        mFeedbackStyle = feedbackStyle;
    }

    public void setDownPadding(int dp) {
        mPadding = WeezUtil.Size.dpToPx(getContext(), dp);
    }

    public void setPressColor(int alpha, int r, int g, int b) {
        mPressColor = Color.argb(alpha, r, g, b);
    }

    private void actionDown(ImageView view) {
        switch (mFeedbackStyle) {
            case PADDING:
                view.setPadding(mPadding, mPadding, mPadding, mPadding);
                break;
            case OVER_COLOR:
                view.setColorFilter(mPressColor, PorterDuff.Mode.SRC_OVER);
                break;
            default:
                break;
        }
    }

    private void actionUp(ImageView view) {
        switch (mFeedbackStyle) {
            case PADDING:
                view.setPadding(0, 0, 0, 0);
                break;
            case OVER_COLOR:
                view.setColorFilter(0x0000000, PorterDuff.Mode.SRC_OVER);
                break;
            default:
                break;
        }
    }

    private class WeezButtonTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView view = (ImageView) v;
            int action = event.getAction();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    actionDown(view);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    actionUp(view);
                    break;
                default:
                    break;
            }

            return false;
        }
    }

    public enum FeedbackStyle {
        PADDING,
        OVER_COLOR,
    }
}