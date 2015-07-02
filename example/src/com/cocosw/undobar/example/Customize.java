package com.cocosw.undobar.example;

import android.R.string;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cocosw.undobar.UndoBarController;
import com.cocosw.undobar.UndoBarController.UndoListener;
import com.cocosw.undobar.UndoBarStyle;
import com.cocosw.undobar.example.R.drawable;
import com.cocosw.undobar.example.R.id;

public class Customize extends AppCompatActivity implements UndoListener {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final UndoBarStyle style = new UndoBarStyle(drawable.button, string.ok,
                drawable.g, 2000);
        setContentView(R.layout.activity_customize);
		findViewById(id.button1).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
                new UndoBarController.UndoBar(Customize.this).message("Hello World!").style(style).show();
			}
		});
	}

	@Override
	public void onUndo(final Parcelable token) {
		Toast.makeText(this, "Hello Geek", Toast.LENGTH_SHORT).show();
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
