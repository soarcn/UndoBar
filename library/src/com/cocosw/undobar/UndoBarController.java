/*
 * Copyright 2012 Roman Nurik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cocosw.undobar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cocosw.undobar.R.drawable;
import com.cocosw.undobar.R.id;
import com.cocosw.undobar.R.string;

public class UndoBarController extends LinearLayout {

    public static UndoBarStyle UNDOSTYLE = new UndoBarStyle(
            drawable.ic_undobar_undo, string.undo);
    private UndoBarStyle style = UndoBarController.UNDOSTYLE;
    public static UndoBarStyle RETRYSTYLE = new UndoBarStyle(drawable.ic_retry,
            string.retry, -1);
    public static UndoBarStyle MESSAGESTYLE = new UndoBarStyle(-1, -1, 5000);
    private static Animation inAnimation = inFromBottomAnimation(null);
    private static Animation outAnimation = outToBottomAnimation(null);
    private final TextView mMessageView;
    private final TextView mButton;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideUndoBar(false);
        }
    };
    private UndoListener mUndoListener;
    // State objects
    private Parcelable mUndoToken;

    private CharSequence mUndoMessage;

    private UndoBarController(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.undobar, this, true);
        mMessageView = (TextView) findViewById(R.id.undobar_message);
        mButton = (TextView) findViewById(id.undobar_button);
        mButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        if (mUndoListener != null) {
                            mUndoListener.onUndo(mUndoToken);
                        }
                        hideUndoBar(false);
                    }
                }
        );

        hideUndoBar(true);
    }

    private static Animation outToBottomAnimation(
            final android.view.animation.Animation.AnimationListener animationlistener) {
        final TranslateAnimation translateanimation = new TranslateAnimation(2,
                0F, 2, 0F, 2, 0F, 2, 1F);
        translateanimation.setDuration(500L);
        translateanimation.setInterpolator(new AnticipateOvershootInterpolator(
                1.0f));
        translateanimation.setAnimationListener(animationlistener);
        return translateanimation;
    }

    private static Animation inFromBottomAnimation(
            final android.view.animation.Animation.AnimationListener animationlistener) {
        final TranslateAnimation translateanimation = new TranslateAnimation(2,
                0F, 2, 0F, 2, 1F, 2, 0F);
        translateanimation.setDuration(500L);
        translateanimation.setInterpolator(new OvershootInterpolator(1.0f));
        translateanimation.setAnimationListener(animationlistener);
        return translateanimation;
    }

    /**
     * Quick method to insert a UndoBar into an Activity
     *
     * @param activity  Activity to hold this view
     * @param message   The message will be shown in left side in undobar
     * @param listener  Callback listener triggered after click undobar
     * @param undoToken Token info,will pass to callback to help you to undo
     * @param immediate Show undobar immediately or show it with animation
     * @param style     {@link UndoBarStyle}
     * @return UndoBarController
     */
    public static UndoBarController show(final Activity activity,
                                         final CharSequence message, final UndoListener listener,
                                         final Parcelable undoToken, final boolean immediate,
                                         final UndoBarStyle style) {
        UndoBarController undo = ensureView(activity);
        if (style == null)
            throw new IllegalArgumentException("style must not be empty.");
        undo.style = style;
        undo.setUndoListener(listener);
        undo.showUndoBar(immediate, message, undoToken);
        return undo;
    }

    private static UndoBarController ensureView(Activity activity) {
        UndoBarController undo = UndoBarController.getView(activity);
        if (undo == null) {
            undo = new UndoBarController(activity, null);
            ((ViewGroup) activity.findViewById(android.R.id.content))
                    .addView(undo);
        }
        return undo;
    }

    private static UndoBarController getView(final Activity activity) {
        final View view = activity.findViewById(id._undobar);
        UndoBarController undo = null;
        if (view != null) {
            undo = (UndoBarController) view.getParent();
        }
        return undo;
    }

    public static UndoBarController show(final Activity activity,
                                         final int message, final UndoListener listener,
                                         final Parcelable undoToken, final boolean immediate) {
        return UndoBarController.show(activity, activity.getText(message),
                listener, undoToken, immediate, UndoBarController.UNDOSTYLE);
    }

    public static UndoBarController show(final Activity activity,
                                         final CharSequence message, final UndoListener listener,
                                         final Parcelable undoToken) {
        return UndoBarController.show(activity, message, listener, undoToken,
                false, UndoBarController.UNDOSTYLE);
    }

    public static UndoBarController show(final Activity activity,
                                         final CharSequence message, final UndoListener listener,
                                         final UndoBarStyle style) {
        return UndoBarController.show(activity, message, listener, null, false,
                style);
    }

    public static UndoBarController show(final Activity activity,
                                         final CharSequence message, final UndoListener listener) {
        return UndoBarController.show(activity, message, listener, null, false,
                UndoBarController.UNDOSTYLE);
    }

    public static UndoBarController show(final Activity activity,
                                         final CharSequence message) {
        return UndoBarController.show(activity, message, null, null, false,
                UndoBarController.MESSAGESTYLE);
    }

    /**
     * Hide all undo bar immediately
     *
     * @param activity The activity where the undobar in
     */
    public static void clear(final Activity activity) {
        final UndoBarController v = UndoBarController.getView(activity);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
    }

    /**
     * Change the default In/Out animation
     *
     * @param inAnimation
     * @param outAnimation
     */
    public static void setAnimation(Animation inAnimation, Animation outAnimation) {
        if (inAnimation != null)
            UndoBarController.inAnimation = inAnimation;
        if (outAnimation != null)
            UndoBarController.outAnimation = outAnimation;
    }

    /**
     * Get callback listener
     *
     * @return
     */
    public UndoListener getUndoListener() {
        return mUndoListener;
    }

    private void setUndoListener(final UndoListener mUndoListener) {
        this.mUndoListener = mUndoListener;
    }

    private void hideUndoBar(final boolean immediate) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mUndoToken = null;
        if (immediate) {
            setVisibility(View.GONE);
        } else {
            clearAnimation();
            if (style.outAnimation != null)
                startAnimation(style.outAnimation);
            else
                startAnimation(outAnimation);
            setVisibility(View.GONE);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Bundle outState = new Bundle();
        outState.putCharSequence("undo_message", mUndoMessage);
        outState.putParcelable("undo_token", mUndoToken);
        outState.putParcelable("undo_style", style);
        outState.putInt("visible", getVisibility());
        return outState;
    }


    @Override
    public void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mUndoMessage = bundle.getCharSequence("undo_message");
            mUndoToken = bundle.getParcelable("undo_token");
            style = bundle.getParcelable("undo_style");
            if (bundle.getInt("visible") == View.VISIBLE)
                showUndoBar(true, mUndoMessage, mUndoToken);
            return;
        }
        super.onRestoreInstanceState(state);
    }

    @SuppressWarnings("ConstantConditions")
    private void showUndoBar(final boolean immediate,
                             final CharSequence message, final Parcelable undoToken) {
        mUndoToken = undoToken;
        mUndoMessage = message;
        mMessageView.setText(mUndoMessage);

        if (style.titleRes > 0) {
            mButton.setVisibility(View.VISIBLE);
            findViewById(id.undobar_divider).setVisibility(View.VISIBLE);
            mButton.setText(style.titleRes);
            if (style.iconRes > 0) {
                mButton.setCompoundDrawablesWithIntrinsicBounds(getResources()
                        .getDrawable(style.iconRes), null, null, null);
            }
        } else {
            mButton.setVisibility(View.GONE);
            findViewById(id.undobar_divider).setVisibility(View.GONE);
        }
        if (style.bgRes > 0)
            findViewById(id._undobar).setBackgroundResource(style.bgRes);

        mHideHandler.removeCallbacks(mHideRunnable);
        if (style.duration > 0) {
            mHideHandler.postDelayed(mHideRunnable, style.duration);
        }
        if (!immediate) {
            clearAnimation();
            if (style.inAnimation != null)
                startAnimation(style.inAnimation);
            else
                startAnimation(inAnimation);
        }
        setVisibility(View.VISIBLE);
    }

    public interface UndoListener {
        void onUndo(Parcelable token);
    }

//    /**
//     * This interface mush be called in activity onCreate if you want to keep UndoBar state without putting UndoBar in your view layout
//     *
//     *
//     * @param act
//     */
//    public static void onCreateActivity(Activity act) {
//        ensureView(act);
//    }

    /**
     * UndoBar Builder
     */
    public static class UndoBar {

        private final Activity activity;
        private UndoBarStyle style;
        private CharSequence message;
        private long duration;
        private Parcelable undoToken;
        private UndoListener listener;

        public UndoBar(Activity activity) {
            this.activity = activity;
        }

        public UndoBar style(UndoBarStyle style) {
            this.style = style;
            return this;
        }

        /**
         * Set the message to be displayed on the left of the undobar.
         *
         * @param message message
         * @return
         */
        public UndoBar message(CharSequence message) {
            this.message = message;
            return this;
        }

        /**
         * Set the message to be displayed on the left of the undobar.
         *
         * @param messageRes
         * @return
         */
        public UndoBar message(int messageRes) {
            this.message = activity.getText(messageRes);
            return this;
        }

        /**
         * Sets the duration the undo bar will be shown.<br>
         * Default is defined in style
         *
         * @param duraton
         * @return
         */
        public UndoBar duration(long duraton) {
            this.duration = duraton;
            return this;
        }

        /**
         * Sets the listener which will be trigger when button been clicked.
         *
         * @param mUndoListener
         * @return
         */
        public UndoBar listener(UndoListener mUndoListener) {
            this.listener = mUndoListener;
            return this;
        }


        /**
         * Sets a token for undobar which will be returned in listener
         *
         * @param undoToken
         * @return
         */
        public UndoBar token(Parcelable undoToken) {
            this.undoToken = undoToken;
            return this;
        }


        /**
         * Show undobar with animation or not
         *
         * @param anim show animation or not
         * @return
         */
        public UndoBarController show(boolean anim) {
            if (listener == null && style == null) {
                style = MESSAGESTYLE;
            }
            if (style == null)
                style = UNDOSTYLE;
            if (message == null)
                message = "";
            if (duration > 0) {
                style.duration = duration;
            }
            return UndoBarController.show(activity, message, listener, undoToken, !anim, style);
        }

        /**
         * Show undobar with animation
         *
         * @return
         */
        public UndoBarController show() {
            return show(true);
        }
    }
}
