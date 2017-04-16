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
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cocosw.swipehelper.Container;
import com.cocosw.undobar.R.drawable;
import com.cocosw.undobar.R.id;
import com.cocosw.undobar.R.string;

import java.lang.reflect.Method;
import java.util.LinkedList;

public class UndoBarController extends Container {

    private static final String SAVED_STATE = "_state_undobar";
    private static final String STATE_CURRENT_MESSAGE = "_state_undobar_current";
    private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
    private static final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";
    private static final String SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar";

    public static final UndoBarStyle UNDOSTYLE = new UndoBarStyle(
            drawable.ic_undobar_undo, string.undo);
    public static final UndoBarStyle RETRYSTYLE = new UndoBarStyle(drawable.ic_retry,
            string.retry, -1);
    public static final UndoBarStyle MESSAGESTYLE = new UndoBarStyle(-1, -1, 5000);


    private LinkedList<Message> mMessages = new LinkedList<>();
    private Message currentMessage;
    private boolean mShowing;


    private static Animation inAnimation;
    private static Animation outAnimation;
    private final TextView mMessageView;
    private final TextView mButton;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            if (listener instanceof AdvancedUndoListener) {
                ((AdvancedUndoListener) listener).onHide(currentMessage.undoToken);
            }
            if (currentMessage.immediate) {
                hideUndoBar(true);
            } else {
                hideUndoBar(false);
            }
        }
    };
    //Only for KitKat translucent mode
    private boolean mInPortrait;
    private String sNavBarOverride;
    private boolean mNavBarAvailable;
    private float mSmallestWidthDp;
    private UndoListener listener;

    private void addMessage(Message message) {
        mMessages.add(message);
    }

    public UndoBarController(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{R.attr.undoBarStyle});
        int style = ta.getResourceId(0, R.style.UndoBarDefaultStyle);
        context.getTheme().applyStyle(style, true);
        ta.recycle();

        ta = context.getTheme().obtainStyledAttributes(new int[]{R.attr.ub_inAnimation, R.attr.ub_outAnimation});
        inAnimation = AnimationUtils.loadAnimation(context, ta.getResourceId(0, R.anim.undobar_classic_in_anim));
        outAnimation = AnimationUtils.loadAnimation(context, ta.getResourceId(1, R.anim.undobar_classic_out_anim));

        LayoutInflater.from(context).inflate(R.layout.undobar, this, true);
        ta.recycle();

        mMessageView = (TextView) findViewById(R.id.undobar_message);
        mButton = (TextView) findViewById(id.undobar_button);
        mButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        // #44
                        if (!mShowing)
                            return;
                        if (listener != null) {
                            listener.onUndo(currentMessage.undoToken);
                        }
                        if (currentMessage.immediate) {
                            hideUndoBar(true);
                        } else {
                            hideUndoBar(false);
                        }
                    }
                }
        );

        setVisibility(View.GONE);

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


    @SuppressWarnings("deprecation")
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
        if (style == null)
            throw new IllegalArgumentException("style must not be empty.");
        return new UndoBar(activity).message(message).listener(listener).token(undoToken).style(style).translucent((translucent == 1)).show(!immediate);

    }

    private static UndoBarController getBar(final Activity activity, UndoBar undobar) {
        UndoBarController undo = ensureView(activity);
        undo.listener = undobar.listener;
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

    @SuppressWarnings("deprecation")
    @Deprecated
    public static UndoBarController show(final Activity activity,
                                         final int message, final UndoListener listener,
                                         final Parcelable undoToken, final boolean immediate) {
        return new UndoBar(activity).message(message).listener(listener).token(undoToken).show(false);
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    public static UndoBarController show(final Activity activity,
                                         final CharSequence message, final UndoListener listener,
                                         final Parcelable undoToken) {
        return new UndoBar(activity).message(message).listener(listener).token(undoToken).show();
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    public static UndoBarController show(final Activity activity,
                                         final CharSequence message, final UndoListener listener,
                                         final UndoBarStyle style) {
        return new UndoBar(activity).message(message).listener(listener).style(style).show();
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    public static UndoBarController show(final Activity activity,
                                         final CharSequence message, final UndoListener listener) {
        return new UndoBar(activity).message(message).listener(listener).show();
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    public static UndoBarController show(final Activity activity,
                                         final CharSequence message) {
        return new UndoBar(activity).message(message).show();
    }

    @Deprecated
    /**
     * @Deprecated, use {@link com.cocosw.undobar.UndoBarController.UndoBar} clear() instead
     *
     * Hide all undo bar immediately
     *
     * @param activity The activity where the undobar in
     */
    public static void clear(@NonNull final Activity activity) {
        final UndoBarController v = UndoBarController.getView(activity);
        if (v != null) {
            v.setVisibility(View.GONE);
            v.mShowing = false;
            v.mHideHandler.removeCallbacks(v.mHideRunnable);
            if (v.listener instanceof AdvancedUndoListener) {
                if (v.currentMessage == null)
                    ((AdvancedUndoListener) v.listener).onClear(new Parcelable[]{});
                else {
                    Parcelable[] parcels = new Parcelable[v.mMessages.size() + 1];
                    parcels[0] = v.currentMessage.undoToken;
                    for (int i = 0; i < v.mMessages.size(); i++) {
                        parcels[i + 1] = v.mMessages.get(i).undoToken;
                    }
                    ((AdvancedUndoListener) v.listener).onClear(parcels);
                }
            }
        }
    }

    private static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    /**
     * Deprecated, Change the default In/Out animation
     * Please define your undobar style in your theme, do not use this anymore
     *
     * @param inAnimation
     * @param outAnimation
     */
    @Deprecated
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
        return listener;
    }


    private void hideUndoBar(final boolean immediate) {
        mHideHandler.removeCallbacks(mHideRunnable);
        final Message next = mMessages.poll();
        if (immediate) {
            setVisibility(View.GONE);
            currentMessage = null;
            mShowing = false;
            if (next != null)
                showUndoBar(next);
        } else {
            clearAnimation();
            Animation anim;
            if (currentMessage.style.outAnimation != null)
                anim = (currentMessage.style.outAnimation);
            else
                anim = (outAnimation);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    currentMessage = null;
                    mShowing = false;
                    if (next != null)
                        showUndoBar(next);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            startAnimation(anim);
            setVisibility(View.GONE);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle outState = new Bundle();
        final int count = mMessages.size();
        final Message[] messages = new Message[count];
        mMessages.toArray(messages);
        outState.putParcelableArray(SAVED_STATE, messages);
        outState.putParcelable(STATE_CURRENT_MESSAGE, currentMessage);
        return outState;
    }


    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof Bundle) {
            currentMessage = ((Bundle) state).getParcelable(STATE_CURRENT_MESSAGE);
            if (currentMessage != null) {
                Parcelable[] messages = ((Bundle) state).getParcelableArray(SAVED_STATE);
                for (Parcelable p : messages) {
                    mMessages.add((Message) p);
                }
            }
            return;
        }
        super.onRestoreInstanceState(state);
    }

    @SuppressWarnings("ConstantConditions")
    private void showUndoBar(@NonNull Message msg) {
        currentMessage = msg;
        mMessageView.setText(currentMessage.message, TextView.BufferType.SPANNABLE);
        if (currentMessage.style.titleRes > 0) {
            mButton.setVisibility(View.VISIBLE);
            findViewById(id.undobar_divider).setVisibility(View.VISIBLE);
            mButton.setText(currentMessage.style.titleRes);
            if (currentMessage.noIcon) {
                mButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            } else if (currentMessage.style.iconRes > 0) {
                Drawable drawable = getResources().getDrawable(currentMessage.style.iconRes);
                int iColor = mButton.getTextColors().getDefaultColor();

                if (currentMessage.colorDrawable) {
                    int red = (iColor & 0xFF0000) / 0xFFFF;
                    int green = (iColor & 0xFF00) / 0xFF;
                    int blue = iColor & 0xFF;

                    float[] matrix = {0, 0, 0, 0, red
                            , 0, 0, 0, 0, green
                            , 0, 0, 0, 0, blue
                            , 0, 0, 0, 1, 0};

                    ColorFilter colorFilter = new ColorMatrixColorFilter(matrix);

                    drawable.setColorFilter(colorFilter);
                }

                mButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }
        } else {
            mButton.setVisibility(View.GONE);
            findViewById(id.undobar_divider).setVisibility(View.GONE);
        }
        if (currentMessage.style.bgRes > 0)
            findViewById(id._undobar).setBackgroundResource(currentMessage.style.bgRes);

        mHideHandler.removeCallbacks(mHideRunnable);
        if (currentMessage.style.duration > 0) {
            mHideHandler.postDelayed(mHideRunnable, currentMessage.style.duration);
        }
        if (!currentMessage.immediate) {
            clearAnimation();
            if (currentMessage.style.inAnimation != null)
                startAnimation(currentMessage.style.inAnimation);
            else
                startAnimation(inAnimation);
        }
        setVisibility(View.VISIBLE);
        mShowing = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && currentMessage.translucent != 0) {
            if (currentMessage.translucent == 1 || mNavBarAvailable) {
                setPadding(0, 0, 0, getNavigationBarHeight(getContext()));
            }
        }
    }

    public interface UndoListener {

        /**
         * The callback function will be called when user press button in Undobar
         *
         * @param token
         */
        void onUndo(@Nullable Parcelable token);
    }

    /**
     * Advanced callback listener if you want to get notification when undobar is hided or cleared.
     */
    public interface AdvancedUndoListener extends UndoListener {
        /**
         * The callback function will be called when the Undobar fade out after duration without button clicked.
         *
         * @param token
         */
        void onHide(@Nullable Parcelable token);

        /**
         * The callback function will be called when the clear function been called
         */
        void onClear(@NonNull Parcelable[] token);
    }


    /**
     * UndoBar Builder
     */
    public static class UndoBar implements Parcelable {


        private Activity activity;
        private UndoListener listener;

        private UndoBarStyle style;
        private CharSequence message;
        private long duration;
        private Parcelable undoToken;

        private int translucent = -1;
        private boolean colorDrawable = true;
        private boolean noIcon = false;
        public boolean immediate;


        public UndoBar(@NonNull Activity activity) {
            this.activity = activity;
        }

        private void init() {
            style = null;
            message = null;
            duration = 0;
            undoToken = null;

            translucent = -1;
            colorDrawable = true;
            noIcon = false;
            immediate = false;
        }

        public UndoBar style(@NonNull UndoBarStyle style) {
            this.style = style;
            return this;
        }

        /**
         * Set the message to be displayed on the left of the undobar.
         *
         * @param message message
         * @return
         */
        public UndoBar message(@NonNull CharSequence message) {
            this.message = message;
            return this;
        }

        public UndoBar noicon(boolean b) {
            noIcon = b;
            return this;
        }

        /**
         * Set the message to be displayed on the left of the undobar.
         *
         * @param messageRes
         * @return
         */
        public UndoBar message(@StringRes int messageRes) {
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
        public UndoBar listener(@NonNull UndoListener mUndoListener) {
            this.listener = mUndoListener;
            return this;
        }


        /**
         * Sets a token for undobar which will be returned in listener
         *
         * @param undoToken
         * @return
         */
        public UndoBar token(@NonNull Parcelable undoToken) {
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
         * Whether the drawable in button should be rendered to the same color that the button text have.
         *
         * @param enable
         * @return
         */
        public UndoBar colorDrawable(boolean enable) {
            colorDrawable = enable;
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
            immediate = !anim;
            UndoBarController bar = UndoBarController.getBar(activity, this);
            Message msg = new Message(style, message, duration, undoToken, translucent, colorDrawable, noIcon, immediate);
            if (bar.mShowing)
                bar.addMessage(msg);
            else
                bar.showUndoBar(msg);
            init();
            return bar;
        }

        /**
         * Show undobar with animation
         *
         * @return
         */
        public UndoBarController show() {
            return show(true);
        }

        public void onSaveInstanceState(@NonNull Bundle saveState) {
            saveState.putParcelable("undobar", UndoBarController.getBar(activity, this).onSaveInstanceState());
        }

        public void onRestoreInstanceState(@NonNull Bundle loadState) {
            UndoBarController undobar = UndoBarController.getBar(activity, this);
            undobar.onRestoreInstanceState(loadState.getParcelable("undobar"));
            if (undobar.currentMessage != null) {
                undobar.showUndoBar(undobar.currentMessage);
            }
        }

        /**
         * Hide all undo bar immediately
         */
        @SuppressWarnings("deprecation")
        public void clear() {
            UndoBarController.clear(activity);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.style, 0);
            TextUtils.writeToParcel(this.message, dest, flags);
            dest.writeLong(this.duration);
            dest.writeParcelable(this.undoToken, 0);
            dest.writeInt(this.translucent);
            dest.writeByte(colorDrawable ? (byte) 1 : (byte) 0);
            dest.writeByte(noIcon ? (byte) 1 : (byte) 0);
        }

        private UndoBar(Parcel in) {
            this.style = in.readParcelable(UndoBarStyle.class.getClassLoader());
            this.message = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            this.duration = in.readLong();
            this.undoToken = in.readParcelable(Parcelable.class.getClassLoader());
            this.translucent = in.readInt();
            this.colorDrawable = in.readByte() != 0;
            this.noIcon = in.readByte() != 0;
        }

        public static final Parcelable.Creator<UndoBar> CREATOR = new Parcelable.Creator<UndoBar>() {
            public UndoBar createFromParcel(Parcel source) {
                return new UndoBar(source);
            }

            public UndoBar[] newArray(int size) {
                return new UndoBar[size];
            }
        };
    }


    private static class Message implements Parcelable {
        private UndoBarStyle style;
        private CharSequence message;
        private long duration;
        private Parcelable undoToken;
        private int translucent = -1;
        private boolean colorDrawable = true;
        private boolean noIcon = false;
        public boolean immediate;


        private Message(UndoBarStyle style, CharSequence message, long duration, Parcelable undoToken, int translucent, boolean colorDrawable, boolean noIcon, boolean immediate) {
            this.style = style;
            this.message = message;
            this.duration = duration;
            this.undoToken = undoToken;
            this.translucent = translucent;
            this.colorDrawable = colorDrawable;
            this.noIcon = noIcon;
            this.immediate = immediate;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.style, 0);
            TextUtils.writeToParcel(this.message, dest, flags);
            dest.writeLong(this.duration);
            dest.writeParcelable(this.undoToken, 0);
            dest.writeInt(this.translucent);
            dest.writeByte(colorDrawable ? (byte) 1 : (byte) 0);
            dest.writeByte(noIcon ? (byte) 1 : (byte) 0);
            dest.writeByte(immediate ? (byte) 1 : (byte) 0);
        }

        private Message(Parcel in) {
            this.style = in.readParcelable(UndoBarStyle.class.getClassLoader());
            this.message = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            this.duration = in.readLong();
            this.undoToken = in.readParcelable(Parcelable.class.getClassLoader());
            this.translucent = in.readInt();
            this.colorDrawable = in.readByte() != 0;
            this.noIcon = in.readByte() != 0;
            this.immediate = in.readByte() != 0;
        }

        public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
            public Message createFromParcel(Parcel source) {
                return new Message(source);
            }

            public Message[] newArray(int size) {
                return new Message[size];
            }
        };
    }


}
