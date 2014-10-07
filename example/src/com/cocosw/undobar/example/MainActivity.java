package com.cocosw.undobar.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.cocosw.undobar.example.R.id;
import com.cocosw.undobar.example.R.layout;

public class MainActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layout.activity_main);
		findViewById(id.button1).setOnClickListener(this);
		findViewById(id.button2).setOnClickListener(this);
		findViewById(id.button3).setOnClickListener(this);
		findViewById(id.button4).setOnClickListener(this);
        findViewById(id.button5).setOnClickListener(this);
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case id.button1:
			startActivity(new Intent(this, UndoStyle.class));
			break;
		case id.button2:
			startActivity(new Intent(this, RetryStyle.class));
			break;
		case id.button3:
			startActivity(new Intent(this, MessageStyle.class));
			break;
		case id.button4:
			startActivity(new Intent(this, Customize.class));
			break;
        case id.button5:
            startActivity(new Intent(this, SnackBar.class));
            break;
		default:
			break;
		}

	}
}
