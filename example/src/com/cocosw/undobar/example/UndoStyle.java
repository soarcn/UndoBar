package com.cocosw.undobar.example;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cocosw.undobar.UndoBarController;

import java.util.Arrays;

public class UndoStyle extends AppCompatActivity implements UndoBarController.AdvancedUndoListener, AdapterView.OnItemClickListener {

    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_main);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(mAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                Arrays.asList(new String[]{"Item 1", "Item 2", "Item 3",
                        "Item 4", "Item 5", "Item 6", "Item 7", "Item 8",
                        "Item 9", "Item 10", "Item 11", "Item 12", "Item 13",
                        "Item 14", "Item 15",})
        ));
        listView.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, 0, 0, "Clear");
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        else
            new UndoBarController.UndoBar(this).clear();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

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
    public void onClear(@Nullable Parcelable[] tokens) {
        for (Parcelable token : tokens) {
            final int position = ((Bundle) token).getInt("index");
            Toast.makeText(this.getApplicationContext(), "UndoBar cleared! index " + position,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Bundle b = new Bundle();
        b.putInt("index", position);

        new UndoBarController.UndoBar(this).message(mAdapter.getItem(position)
                + " was selected").listener(this).token(b).show();
    }
}
