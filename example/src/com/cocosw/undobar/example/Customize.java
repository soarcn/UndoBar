package com.cocosw.undobar.example;

import android.R.string;
import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.cocosw.undobar.UndoBarController;
import com.cocosw.undobar.UndoBarController.UndoListener;
import com.cocosw.undobar.UndoBarStyle;
import com.cocosw.undobar.example.R.drawable;
import com.cocosw.undobar.example.R.id;

public class Customize extends Activity implements UndoListener {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final UndoBarStyle style = new UndoBarStyle(drawable.button, string.ok,
				drawable.g, 2000).setAnim(AnimationUtils.loadAnimation(this,android.R.anim.fade_in),AnimationUtils.loadAnimation(this,android.R.anim.fade_out));
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

}
