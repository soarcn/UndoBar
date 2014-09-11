/*
 * Copyright 2014 LiaoKai
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cocosw.undobar.R.drawable;
import com.cocosw.undobar.R.id;
import com.cocosw.undobar.R.string;

import java.lang.reflect.Method;

public class UndoBarController extends LinearLayout {

    private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
    private static final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";
    private static final String SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar";

    public static final UndoBarStyle UNDOSTYLE = new UndoBarStyle(
            drawable.ic_undobar_undo, string.undo);
    private UndoBarStyle style = UndoBarController.UNDOSTYLE;
    public static final UndoBarStyle RETRYSTYLE = new UndoBarStyle(drawable.ic_retry,
            string.retry, -1);
    public static final UndoBarStyle MESSAGESTYLE = new UndoBarStyle(-1, -1, 5000);


    public static final UndoBarStyle MATERIAL_UNDOSTYLE = new UndoBarStyle(-1, string.undo).setAnim(snackIn(),snackOut());
    public static final UndoBarStyle MATERIAL_RETRYSTYLE = new UndoBarStyle(-1, string.retry, -1).setAnim(snackIn(),snackOut());

    private static Animation inAnimation = inFromBottomAnimation(null);
    private static Animation outAnimation = outToBottomAnimation(null);
    private final TextView mMessageView;
    private final TextView mButton;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            if (mUndoListener instanceof AdvancedUndoListener) {
                ((AdvancedUndoListener) mUndoListener).onHide(mUndoToken);
            }
            if (mImmediate) {
                hideUndoBar(true);
            } else {
                hideUndoBar(false);
            }
        }
    };
    private UndoListener mUndoListener;
    private boolean mImmediate;
    // State objects
    private Parcelable mUndoToken;
    private CharSequence mUndoMessage;
    //Only for KitKat translucent mode
    private int translucent = -1;
    private boolean mInPortrait;
    private String sNavBarOverride;
    private boolean mNavBarAvailable;
    private float mSmallestWidthDp;

    private static Animation snackIn() {
        AnimationSet mInAnimationSet = new AnimationSet(false);

        TranslateAnimation mSlideInAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 1.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f);

        AlphaAnimation mFadeInAnimation = new AlphaAnimation(0.0f, 1.0f);

        mInAnimationSet.addAnimation(mSlideInAnimation);
        mInAnimationSet.addAnimation(mFadeInAnimation);
        mInAnimationSet.setDuration(500);
        return mInAnimationSet;
    }

    private static Animation snackOut() {
        AnimationSet mOutAnimationSet = new AnimationSet(false);

        TranslateAnimation mSlideOutAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 1.0f);

        AlphaAnimation mFadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);

        mOutAnimationSet.addAnimation(mSlideOutAnimation);
        mOutAnimationSet.addAnimation(mFadeOutAnimation);
        mOutAnimationSet.setDuration(500);
        return mOutAnimationSet;
    }

    public UndoBarController(final Context context, final AttributeSet attrs) {
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
                        if (mImmediate) {
                            hideUndoBar(true);
                        } else {
                            hideUndoBar(false);
                        }
                    }
                }
        );

        hideUndoBar(true);

        // https://github.com/jgilfelt/SystemBarTint/blob/master/library/src/com/readystatesoftware/systembartint/SystemBarTintManager.java
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            mInPortrait = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
                sNavBarOverride = null;
            }

            // check theme attrs
            int[] as = {android.R.attr.windowTranslucentStatus,
                    android.R.attr.windowTranslucentNavigation};
            TypedArray a = context.obtainStyledAttributes(as);
            try {
                mNavBarAvailable = a.getBoolean(1, false);
            } finally {
                a.recycle();
            }

            // check window flags
            assert (getContext()) != null;
            WindowManager.LayoutParams winParams = ((Activity) getContext()).getWindow().getAttributes();
            int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            if ((winParams.flags & bits) != 0) {
                mNavBarAvailable = true;
            }
            mSmallestWidthDp = getSmallestWidthDp(wm);
        }
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

    @Deprecated
    /**
     * Deprecated, use {@link com.cocosw.undobar.UndoBarController.UndoBar} instead
     *
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
        return show(activity, message, listener, undoToken, immediate, style, -1);

    }

    @Deprecated
    public static UndoBarController show(final Activity activity,
                                         final CharSequence message, UndoListener listener,
                                         Parcelable undoToken, boolean immediate, UndoBarStyle style,
                                         int translucent) {
        UndoBarController undo = ensureView(activity);
        if (style == null)
            throw new IllegalArgumentException("style must not be empty.");
        undo.style = style;
        undo.setUndoListener(listener);
        undo.showUndoBar(immediate, message, undoToken);
        undo.translucent = translucent;
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

    @Deprecated
    public static UndoBarController show(final Activity activity,
                                         final int message, final UndoListener listener,
                                         final Parcelable undoToken, final boolean immediate) {
        return UndoBarController.show(activity, activity.getText(message),
                listener, undoToken, immediate, UndoBarController.UNDOSTYLE);
    }

    @Deprecated
    public static UndoBarController show(final Activity activity,
                                         final CharSequence message, final UndoListener listener,
                                         final Parcelable undoToken) {
        return UndoBarController.show(activity, message, listener, undoToken,
                false, UndoBarController.UNDOSTYLE);
    }

    @Deprecated
    public static UndoBarController show(final Activity activity,
                                         final CharSequence message, final UndoListener listener,
                                         final UndoBarStyle style) {
        return UndoBarController.show(activity, message, listener, null, false,
                style);
    }

    @Deprecated
    public static UndoBarController show(final Activity activity,
                                         final CharSequence message, final UndoListener listener) {
        return UndoBarController.show(activity, message, listener, null, false,
                UndoBarController.UNDOSTYLE);
    }

    @Deprecated
    public static UndoBarController show(final Activity activity,
                                         final CharSequence message) {
        return UndoBarController.show(activity, message, null, null, false,
                UndoBarController.MESSAGESTYLE);
    }

    @Deprecated
    /**
     * @Deprecated, use {@link com.cocosw.undobar.UndoBarController.UndoBar} clear() instead
     *
     * Hide all undo bar immediately
     *
     * @param activity The activity where the undobar in
     */
    public static void clear(final Activity activity) {
        final UndoBarController v = UndoBarController.getView(activity);
        if (v != null) {
            v.setVisibility(View.GONE);
            v.mHideHandler.removeCallbacks(v.mHideRunnable);
            if (v.mUndoListener instanceof AdvancedUndoListener) {
                ((AdvancedUndoListener) v.mUndoListener).onClear();
            }
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

    @SuppressLint("NewApi")
    private float getSmallestWidthDp(WindowManager wm) {
        DisplayMetrics metrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            wm.getDefaultDisplay().getRealMetrics(metrics);
        } else {
            //this is not correct, but we don't really care pre-kitkat
            wm.getDefaultDisplay().getMetrics(metrics);
        }
        float widthDp = metrics.widthPixels / metrics.density;
        float heightDp = metrics.heightPixels / metrics.density;
        return Math.min(widthDp, heightDp);
    }

    @TargetApi(14)
    private int getNavigationBarHeight(Context context) {
        Resources res = context.getResources();
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (hasNavBar(context)) {
                String key;
                if (mInPortrait) {
                    key = NAV_BAR_HEIGHT_RES_NAME;
                } else {
                    if (!isNavigationAtBottom())
                        return 0;
                    key = NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME;
                }
                return getInternalDimensionSize(res, key);
            }
        }
        return result;
    }

    @TargetApi(14)
    private boolean hasNavBar(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier(SHOW_NAV_BAR_RES_NAME, "bool", "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            // check override flag (see static block)
            if ("1".equals(sNavBarOverride)) {
                hasNav = false;
            } else if ("0".equals(sNavBarOverride)) {
                hasNav = true;
            }
            return hasNav;
        } else { // fallback
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }

    private int getInternalDimensionSize(Resources res, String key) {
        int result = 0;
        int resourceId = res.getIdentifier(key, "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Should a navigation bar appear at the bottom of the screen in the current
     * device configuration? A navigation bar may appear on the right side of
     * the screen in certain configurations.
     *
     * @return True if navigation should appear at the bottom of the screen, False otherwise.
     */
    private boolean isNavigationAtBottom() {
        return (mSmallestWidthDp >= 600 || mInPortrait);
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
        outState.putBoolean("immediate", mImmediate);
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
            mImmediate = bundle.getBoolean("immediate");
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
        mImmediate = immediate;
        mUndoToken = undoToken;
        mUndoMessage = message;
        mMessageView.setText(mUndoMessage, TextView.BufferType.SPANNABLE);

        if (style.titleRes > 0) {
            mButton.setVisibility(View.VISIBLE);
            findViewById(id.undobar_divider).setVisibility(View.VISIBLE);
            mButton.setText(style.titleRes);
            if (style.iconRes > 0) {
                Drawable drawable = getResources().getDrawable(style.iconRes);
                int iColor = mButton.getTextColors().getDefaultColor();

                int red = (iColor & 0xFF0000) / 0xFFFF;
                int green = (iColor & 0xFF00) / 0xFF;
                int blue = iColor & 0xFF;

                float[] matrix = { 0, 0, 0, 0, red
                        , 0, 0, 0, 0, green
                        , 0, 0, 0, 0, blue
                        , 0, 0, 0, 1, 0 };

                ColorFilter colorFilter = new ColorMatrixColorFilter(matrix);

                drawable.setColorFilter(colorFilter);

                mButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && translucent != 0) {
            if (translucent == 1 || mNavBarAvailable) {
                setPadding(0, 0, 0, getNavigationBarHeight(getContext()));
            }
        }
    }



    private void changeBitmapColor(Bitmap sourceBitmap, int color) {
        Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0,
                sourceBitmap.getWidth() - 1, sourceBitmap.getHeight() - 1);
        Paint p = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 1);
        p.setColorFilter(filter);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, p);
    }

    public interface UndoListener {

        /**
         * The callback function will be called when user press button in Undobar
         *
         * @param token
         */
        void onUndo(Parcelable token);
    }

    public interface AdvancedUndoListener extends UndoListener {
        /**
         * The callback function will be called when the Undobar fade out after duration without button clicked.
         *
         * @param token
         */
        void onHide(Parcelable token);

        void onClear();
    }


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
        private int translucent = -1;

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
         * Translucent mode will be used, meaning undobar will be shown in a upper place than usual
         * This is only for Kitkat+
         * Undobar will determin if translucent mode is used or not if you do not set this.
         *
         * @param enable
         * @return
         */
        public UndoBar translucent(boolean enable) {
            translucent = enable ? 1 : 0;
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
            return UndoBarController.show(activity, message, listener, undoToken, !anim, style, translucent);
        }

        /**
         * Show undobar with animation
         *
         * @return
         */
        public UndoBarController show() {
            return show(true);
        }

        public void clear() {
            UndoBarController.clear(activity);
        }
    }
}
