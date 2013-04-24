package com.cocosw.undobar.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.cocosw.undobar.UndoBarController;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class MessageStyle extends Activity {

	private EditText mEmailView;
	private EditText mPasswordView;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_message_style);

		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText("Geek");
		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setVisibility(View.GONE);
		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(final View view) {
						attemptLogin();
					}

				});
	}

	private void attemptLogin() {
		UndoBarController.show(this, "Hello " + mEmailView.getText());
	}

}
