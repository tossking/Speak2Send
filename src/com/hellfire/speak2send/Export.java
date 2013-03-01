package com.hellfire.speak2send;

import java.util.Calendar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Export extends Activity {
	private static final String TAG = "Speak2Send Export";
	private Button mEmailButton;
	private Button mOKButton;
	private TextView mText;
	private String path;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.export);

		Bundle bundle = getIntent().getExtras();
		path = bundle.getString("path");// 读出数据
		Log.v(TAG, "path=" + path);

		mText = (TextView) findViewById(R.id.export_content);
		mText.setText(getResources().getString(R.string.export_text) + path);
		mEmailButton = (Button) findViewById(R.id.export_email);
		mEmailButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				exportEmail(path);
			}
		});

		mOKButton = (Button) findViewById(R.id.export_ok);
		mOKButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
	}

	private void exportEmail(String p) {
		Log.v(TAG, "exportEmail");
		final Intent i = new Intent(android.content.Intent.ACTION_SEND);
		i.setType("text/email");
		i.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources()
				.getString(R.string.export_email_title)
				+ " " + Calendar.getInstance().getTime().toString());
		i.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(
				R.string.export_email_body));
		i.putExtra(android.content.Intent.EXTRA_STREAM, Uri
				.parse("file://" + p));
		startActivity(Intent.createChooser(i, getResources().getString(
				R.string.select_email)));

		setResult(RESULT_OK);
	}
}
