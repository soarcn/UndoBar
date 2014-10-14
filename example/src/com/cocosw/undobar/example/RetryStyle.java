package com.cocosw.undobar.example;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;

import com.cocosw.undobar.UndoBarController;
import com.cocosw.undobar.UndoBarController.UndoListener;
import com.cocosw.undobar.example.R.id;
import com.cocosw.undobar.example.R.layout;
import com.github.kevinsawicki.wishlist.ThrowableLoader;

/**
 * RetryStyle demo
 * 
 * Thanks Kevinsawicki for his wishlist lib, please check more from github
 * 
 * @author soar
 * 
 */
public class RetryStyle extends FragmentActivity implements UndoListener,
		LoaderCallbacks<Void> {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layout.retry);
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onUndo(final Parcelable token) {
		findViewById(id.progressBar).setVisibility(View.VISIBLE);
		getSupportLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<Void> onCreateLoader(final int arg0, final Bundle arg1) {
		return new ThrowableLoader<Void>(this, null) {

			@Override
			public Void loadData() throws Exception {
				Thread.sleep(1500);
				throw new Exception("Something is wrong");
			}
		};
	}

	@Override
	public void onLoadFinished(final Loader<Void> arg0, final Void arg1) {
		final ThrowableLoader<Void> loader = ((ThrowableLoader<Void>) arg0);
        //noinspection StatementWithEmptyBody
        if (loader.getException() != null) {

            new UndoBarController.UndoBar(this).message(loader.getException().getMessage()).style(UndoBarController.RETRYSTYLE).listener(this).show();

		} else {
			// if there is no exception
		}
		findViewById(id.progressBar).setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(final Loader<Void> arg0) {

	}
}
