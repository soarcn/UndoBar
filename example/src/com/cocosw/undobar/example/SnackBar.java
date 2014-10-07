package com.cocosw.undobar.example;


import android.app.ListActivity;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cocosw.undobar.UndoBarController;

import java.util.Arrays;

public class SnackBar extends ListActivity implements UndoBarController.AdvancedUndoListener {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                Arrays.asList(new String[]{"Item 1", "Item 2", "Item 3",
                        "Item 4", "Item 5", "Item 6", "Item 7", "Item 8",
                        "Item 9", "Item 10", "Item 11", "Item 12", "Item 13",
                        "Item 14", "Item 15",})
        ));
    }

    @Override
    protected void onPause() {
        super.onPause();
        new UndoBarController.UndoBar(this).clear();
    }

    @Override
    protected void onListItemClick(final ListView l, final View v,
                                   final int position, final long id) {
        final Bundle b = new Bundle();
        b.putInt("index", position);

        new UndoBarController.UndoBar(this).message(getListAdapter().getItem(position)
                + " was selected").listener(this).noicon(true).token(b).show();
    }

    @Override
    public void onUndo(final Parcelable token) {
        if (token != null) {
            final int position = ((Bundle) token).getInt("index");
            Toast.makeText(this, "undo clicked, index " + position,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onHide(Parcelable token) {
        if (token != null) {
            final int position = ((Bundle) token).getInt("index");
            Toast.makeText(this, "UndoBar hided! index " + position,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClear() {

    }
}
