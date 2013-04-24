package com.github.kevinsawicki.wishlist;

import android.content.Context;
import android.os.Bundle;


/**
 * Loader that support throwing an exception when loading in the background
 * 
 * @param <D>
 */
public abstract class ThrowableLoader<D> extends AsyncLoader<D> {

	private final D data;

	private Exception exception;

	private Bundle bundle;

	/**
	 * Create loader for context and seeded with initial data
	 * 
	 * @param context
	 * @param data
	 */
	public ThrowableLoader(final Context context, final D data) {
		super(context);

		this.data = data;
	}

	@Override
	public D loadInBackground() {
		exception = null;
		try {
			return loadData();
		} catch (final Exception e) {
			exception = e;
			return data;
		}
	}

	/**
	 * @return exception
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * Clear the stored exception and return it
	 * 
	 * @return exception
	 */
	public Exception clearException() {
		final Exception throwable = exception;
		exception = null;
		return throwable;
	}

	/**
	 * Load data
	 * 
	 * @return data
	 * @throws Exception
	 */
	public abstract D loadData() throws Exception;

	public Bundle getBundle() {
		return bundle;
	}

	public Bundle clearBundle() {
		return bundle;
	}

	public ThrowableLoader<D> setBundle(final Bundle bundle) {
		this.bundle = bundle;
		return this;
	}

}
