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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cocosw.undobar.R.id;

public class UndoBarController extends LinearLayout {

	public interface UndoListener {
		void onUndo(Parcelable token);
	}

	private final TextView mMessageView;

	private final Handler mHideHandler = new Handler();

	private UndoListener mUndoListener;

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

	public UndoListener getUndoListener() {
		return mUndoListener;
	}

	public void hideUndoBar(final boolean immediate) {
		mHideHandler.removeCallbacks(mHideRunnable);
		if (immediate) {
			setVisibility(View.GONE);
			mUndoMessage = null;
			mUndoToken = null;

		} else {
			clearAnimation();
			startAnimation(UndoBarController
					.outToBottomAnimation(new android.view.animation.Animation.AnimationListener() {

						@Override
						public void onAnimationStart(final Animation animation) {
							// TODO 自动生成的方法存根

						}

						@Override
						public void onAnimationRepeat(final Animation animation) {
							// TODO 自动生成的方法存根

						}

						@Override
						public void onAnimationEnd(final Animation animation) {
							setVisibility(View.GONE);
							mUndoMessage = null;
							mUndoToken = null;

						}
					}));

		}
	}

	public static Animation outToBottomAnimation(
			final android.view.animation.Animation.AnimationListener animationlistener) {
		final TranslateAnimation translateanimation = new TranslateAnimation(2,
				0F, 2, 0F, 2, 0F, 2, 1F);
		translateanimation.setDuration(500L);
		translateanimation
				.setInterpolator(new AnticipateOvershootInterpolator());
		translateanimation.setAnimationListener(animationlistener);
		return translateanimation;
	}

	//
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

	public void setUndoListener(final UndoListener mUndoListener) {
		this.mUndoListener = mUndoListener;
	}

	public void showUndoBar(final boolean immediate,
			final CharSequence message, final Parcelable undoToken) {
		mUndoToken = undoToken;
		mUndoMessage = message;
		mMessageView.setText(mUndoMessage);

		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable,
				getResources().getInteger(R.integer.undobar_hide_delay));
		if (getVisibility() != View.VISIBLE) {
			setVisibility(View.VISIBLE);
			if (!immediate) {
				clearAnimation();
				startAnimation(UndoBarController.inFromBottomAnimation(null));
			}
		}
	}

	public static Animation inFromBottomAnimation(
			final android.view.animation.Animation.AnimationListener animationlistener) {
		final TranslateAnimation translateanimation = new TranslateAnimation(2,
				0F, 2, 0F, 2, 1F, 2, 0F);
		translateanimation.setDuration(500L);
		translateanimation.setInterpolator(new OvershootInterpolator());
		translateanimation.setAnimationListener(animationlistener);
		return translateanimation;
	}

	/**
	 * Quick method to insert a ShowcaseView into an Activity
	 * 
	 * @param viewToShowcase
	 *            View to showcase
	 * @param activity
	 *            Activity to insert into
	 * @param title
	 *            Text to show as a title. Can be null.
	 * @param detailText
	 *            More detailed text. Can be null.
	 * @param options
	 *            A set of options to customise the ShowcaseView
	 * @return the created ShowcaseView instance
	 */
	public static UndoBarController show(final Activity activity,
			final CharSequence message, final UndoListener listener,
			final Parcelable undoToken, final boolean immediate) {
		final View view = activity.findViewById(id._undobar);
		UndoBarController undo = null;
		if (view != null) {
			undo = (UndoBarController) view.getParent();
		}
		if (undo == null) {
			undo = new UndoBarController(activity, null);
			((ViewGroup) activity.findViewById(android.R.id.content))
					.addView(undo);
		}
		undo.setUndoListener(listener);
		undo.showUndoBar(immediate, message, undoToken);
		return undo;
	}

	public static UndoBarController show(final Activity activity,
			final int message, final UndoListener listener,
			final Parcelable undoToken, final boolean immediate) {
		return UndoBarController.show(activity, activity.getText(message),
				listener, undoToken, immediate);

	}
}
