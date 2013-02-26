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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cocosw.undobar.R.drawable;
import com.cocosw.undobar.R.id;
import com.cocosw.undobar.R.string;

public class UndoBarController extends LinearLayout {

	public static UndoBarStyle UNDOSTYLE = new UndoBarStyle(
			drawable.ic_undobar_undo, string.undo);

	public static UndoBarStyle RETRYSTYLE = new UndoBarStyle(drawable.ic_retry,
			string.retry, -1);

	public interface UndoListener {
		void onUndo(Parcelable token);
	}

	private final TextView mMessageView;

	private final Handler mHideHandler = new Handler();

	private UndoListener mUndoListener;

	private UndoBarStyle style = UndoBarController.UNDOSTYLE;

	// State objects
	private Parcelable mUndoToken;

	private CharSequence mUndoMessage;
	private final Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			hideUndoBar(false);
		}
	};

	public UndoBarController(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.undobar, this, true);
		mMessageView = (TextView) findViewById(R.id.undobar_message);
		findViewById(R.id.undobar_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(final View view) {
						hideUndoBar(false);
						if (mUndoListener != null) {
							mUndoListener.onUndo(mUndoToken);
						}
					}
				});

		hideUndoBar(true);
	}

	/**
	 * Get callback listener
	 * 
	 * @return
	 */
	public UndoListener getUndoListener() {
		return mUndoListener;
	}

	private void hideUndoBar(final boolean immediate) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mUndoToken = null;
		if (immediate) {
			setVisibility(View.GONE);
		} else {
			clearAnimation();
			startAnimation(UndoBarController.outToBottomAnimation(null));
			setVisibility(View.GONE);
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

	// public void onRestoreInstanceState(final Bundle savedInstanceState) {
	// if (savedInstanceState != null) {
	// mUndoMessage = savedInstanceState.getCharSequence("undo_message");
	// mUndoToken = savedInstanceState.getParcelable("undo_token");
	//
	// if (mUndoToken != null || !TextUtils.isEmpty(mUndoMessage)) {
	// showUndoBar(true, mUndoMessage, mUndoToken);
	// }
	// }
	// }
	//
	// public void onSaveInstanceState(final Bundle outState) {
	// outState.putCharSequence("undo_message", mUndoMessage);
	// outState.putParcelable("undo_token", mUndoToken);
	// }

	private void setUndoListener(final UndoListener mUndoListener) {
		this.mUndoListener = mUndoListener;
	}

	private void showUndoBar(final boolean immediate,
			final CharSequence message, final Parcelable undoToken) {
		mUndoToken = undoToken;
		mUndoMessage = message;
		mMessageView.setText(mUndoMessage);

		if (style != null) {
			final Button button = (Button) findViewById(id.undobar_button);
			button.setText(style.titleRes);
			button.setCompoundDrawablesWithIntrinsicBounds(getResources()
					.getDrawable(style.iconRes), null, null, null);
			findViewById(id._undobar).setBackgroundResource(style.bgRes);
		}

		mHideHandler.removeCallbacks(mHideRunnable);
		if (style.duration > 0) {
			mHideHandler.postDelayed(mHideRunnable, style.duration);
		}
		if (!immediate) {
			clearAnimation();
			startAnimation(UndoBarController.inFromBottomAnimation(null));
		}
		setVisibility(View.VISIBLE);
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
	 * @param activity
	 *            activity to hold this view
	 * @param message
	 *            the message will be shown in left side in undobar
	 * @param listener
	 *            callback listener triggered after click undobar
	 * @param undoToken
	 *            Token info,will pass to callback to help you to undo
	 * @param immediate
	 *            show undobar immediately or show it with animation(duration is
	 *            0.5s)
	 * @param style
	 *            {@link UndoBarStyle}
	 * @return
	 */
	public static UndoBarController show(final Activity activity,
			final CharSequence message, final UndoListener listener,
			final Parcelable undoToken, final boolean immediate,
			final UndoBarStyle style) {
		UndoBarController undo = UndoBarController.getView(activity);
		if (undo == null) {
			undo = new UndoBarController(activity, null);
			((ViewGroup) activity.findViewById(android.R.id.content))
					.addView(undo);
		}
		undo.style = style;
		undo.setUndoListener(listener);
		undo.showUndoBar(immediate, message, undoToken);
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
			final UndoBarStyle style) {
		return UndoBarController.show(activity, message, listener, null, false,
				style);
	}

	public static UndoBarController show(final Activity activity,
			final CharSequence message, final UndoListener listener) {
		return UndoBarController.show(activity, message, listener, null, false,
				UndoBarController.UNDOSTYLE);
	}

	/**
	 * hide all undo bar immediately
	 * 
	 * @param activity
	 */
	public static void clear(final Activity activity) {
		final UndoBarController v = UndoBarController.getView(activity);
		if (v != null) {
			v.setVisibility(View.GONE);
		}
	}
}
