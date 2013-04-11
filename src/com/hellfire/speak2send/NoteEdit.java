package com.hellfire.speak2send;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

public class NoteEdit extends Activity {
	private static final String TAG = "Speak2Send NoteEdit";
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	private static final int SEND_ALL_ACTIVITY_REQUEST_CODE = 5678;
	private EditText mBodyText;
	private Long mRowId;
	private NotesDbAdapter mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v(TAG, "onCreate");

		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();

		setContentView(R.layout.note_edit);
		
		mBodyText = (EditText) findViewById(R.id.body);

		ImageButton speakButton = (ImageButton) findViewById(R.id.speak);
		ImageButton confirmButton = (ImageButton) findViewById(R.id.confirm);
		ImageButton sendButton = (ImageButton) findViewById(R.id.send);
		
		mRowId = (savedInstanceState == null) ? null
				: (Long) savedInstanceState
						.getSerializable(NotesDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
					: null;
		}

		populateFields();

		// Check to see if a recognition activity is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			speakButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					startVoiceRecognitionActivity(VOICE_RECOGNITION_REQUEST_CODE);
				}
			});
		} else {
			speakButton.setEnabled(false);
//			speakButton.settext(R.string.no_voice);
		}

		confirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				setResult(RESULT_OK);
				finish();
			}
		});

		sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				final Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_SUBJECT, getMyTitle());
				i.putExtra(Intent.EXTRA_TEXT, mBodyText
						.getText().toString());
				startActivity(Intent.createChooser(i, getString(R.string.send)));
			}
		});
		
		//Close Soft InputMethod
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive())
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private String getMyTitle() {
		String s;
		int i;
		
		i = mBodyText.getText().toString().length();
		
		if (i < 20) {
			s = mBodyText.getText().toString();
		} else {
			s = mBodyText.getText().toString().substring(0, 20)+"...";
		}
		//to be add Date and Time  here.
		return s;
	}
	
	private void startVoiceRecognitionActivity(int n) {
		// 通过Intent传递语音识别的模式
		Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		// 语言模式和自由形式的语音识别
		i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		// 提示语音开始
		i.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.start_voice);
		i.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);
		// 开始执行我们的Intent、语音识别
		// i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivityForResult(i, n);
	}

	// 当语音结束时的回调函数onActivityResult
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(TAG, "onActivityResult");
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			// 取得语音的字符
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			String s = matches.get(0).toString();

			mBodyText.setText(mBodyText.getText().toString() + s + "\n");
			saveState();
		}
		
		if (requestCode == SEND_ALL_ACTIVITY_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			ArrayList<String> matches = data
				.getStringArrayListExtra(Intent.EXTRA_INTENT);
			Log.v(TAG, "SEND_ALL_ACTIVITY_REQUEST_CODE"+matches.toString());
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void populateFields() {
		if (mRowId != null) {
			Cursor note = mDbHelper.fetchNote(mRowId);
			startManagingCursor(note);
			mBodyText.setText(note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.v(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
		saveState();
		outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
	}

	@Override
	protected void onPause() {
		Log.v(TAG, "onPause");
		super.onPause();
		saveState();
	}

	@Override
	protected void onResume() {
		Log.v(TAG, "onResume");
		super.onResume();
		populateFields();

	}

	@Override
	protected void onStop() {
		Log.v(TAG, "onStop");
		super.onStop();

	}
	
	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		Log.v(TAG, "onRestart");
		super.onRestart();

	}

	@Override
	protected void onStart() {
		Log.v(TAG, "onStart");
		super.onStart();
		populateFields();

	}

	private void saveState() {
		String title = getMyTitle();
		String body = mBodyText.getText().toString();
		String date;
		Log.v(TAG, "saveState");

		Calendar calendar = Calendar.getInstance();
		Date d = calendar.getTime();
		date = d.toString();
		Log.v(TAG, date);
		
		if (!title.matches("") || !body.matches("")) {
 			if (mRowId == null) {
				long id = mDbHelper.createNote(title, body, date);
				if (id > 0) {
					mRowId = id;
				}
			} else {
				mDbHelper.updateNote(mRowId, title, body, date);
			}
		}
	}
}
