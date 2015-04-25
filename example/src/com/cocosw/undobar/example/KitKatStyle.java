package com.cocosw.undobar.example;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cocosw.undobar.UndoBarController;

/**
 * Project: UndoBar
 * Created by LiaoKai(soarcn) on 2014/11/16.
 */
public class KitKatStyle extends AppCompatActivity implements UndoBarController.UndoListener {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_customize);
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                new UndoBarController.UndoBar(KitKatStyle.this).message("KitKat!").listener(KitKatStyle.this).show();
            }
        });
    }

    @Override
    public void onUndo(final Parcelable token) {
        Toast.makeText(this, "Hello KitKat", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
