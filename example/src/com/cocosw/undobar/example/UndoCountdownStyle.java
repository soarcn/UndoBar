package com.cocosw.undobar.example;

import java.util.Arrays;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cocosw.undobar.UndoBarController;
import com.cocosw.undobar.UndoBarController.CountDownFormatter;
import com.cocosw.undobar.UndoBarController.UndoListener;
import com.cocosw.undobar.UndoBarStyle;

public class UndoCountdownStyle extends ListActivity implements UndoListener
{

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1,
				Arrays.asList(new String[] { "Item 1", "Item 2", "Item 3",
						"Item 4", "Item 5", "Item 6", "Item 7", "Item 8",
						"Item 9", "Item 10", "Item 11", "Item 12", "Item 13",
						"Item 14", "Item 15", })));
	}

	@Override
	protected void onListItemClick(final ListView l, final View v,
			final int position, final long id) {
		final Bundle b = new Bundle();
		b.putInt("index", position);
        UndoBarStyle undoBarStyle = new UndoBarStyle(com.cocosw.undobar.R.drawable.ic_undobar_undo,
                R.string.undo, 5000).setCountDownFormatter(new MyFormatCountDownCallback());
        UndoBarController.show(this, getListAdapter().getItem(position) + " was selected", this, b,
                false, undoBarStyle);
	}

	@Override
	public void onUndo(final Parcelable token) {
		if (token != null) {
			final int position = ((Bundle) token).getInt("index");
			Toast.makeText(this, "undo clicked, index " + position,
					Toast.LENGTH_SHORT).show();
		}
	}

    private class MyFormatCountDownCallback implements CountDownFormatter {

        @Override
        public String getCountDownString(final long millisUntilFinished) {
            int seconds = (int) Math.ceil(millisUntilFinished / 1000.0);

            if (seconds > 0) {
                return getResources().getQuantityString(R.plurals.countdown_seconds, seconds,
                        seconds);
            }
            return getString(R.string.countdown_dismissing);
        }
    }
}
