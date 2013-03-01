package com.hellfire.speak2send;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class Speak2Send extends ListActivity {
	private static final String TAG = "Speak2Send";
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;

	private static final int INSERT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int EXPORT_ID = Menu.FIRST + 2;
	private static final int ABOUT_ID = Menu.FIRST + 3;
	private static final int VOICE_ID = Menu.FIRST + 4;
	private static final int TTS_ID = Menu.FIRST + 5;

	private NotesDbAdapter mDbHelper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notes_list);
		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();
		fillData();
		ListView lv = getListView();
		// lv.setTextFilterEnabled(true);
		registerForContextMenu(lv);
	}

	private void fillData() {
		// Get all of the rows from the database and create the item list
		Cursor notesCursor = mDbHelper.fetchAllNotes();
		startManagingCursor(notesCursor);

		// Create an array to specify the fields we want to display in the list
		// (only TITLE)
		String[] from = new String[] { NotesDbAdapter.KEY_BODY };

		// and an array of the fields we want to bind those fields to (in this
		// case just text1)
		int[] to = new int[] { R.id.text1 };

		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter notes = new SimpleCursorAdapter(this,
				R.layout.notes_row, notesCursor, from, to);
		setListAdapter(notes);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, INSERT_ID, 0, R.string.menu_insert).setShortcut('3', 'a')
				.setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, EXPORT_ID, 1, R.string.menu_export).setShortcut('4', 'e')
				.setIcon(android.R.drawable.ic_menu_upload);
		menu.add(0, ABOUT_ID, 2, R.string.menu_about).setShortcut('5', 'b')
				.setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, VOICE_ID, 3, R.string.menu_voice).setShortcut('6', 'v')
				.setIcon(android.R.drawable.ic_menu_search);
//		menu.add(0, TTS_ID, 4, R.string.menu_tts).setShortcut('7', 't')
//				.setIcon(android.R.drawable.ic_menu_search);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case INSERT_ID:
			createNote();
			return true;
		case ABOUT_ID:
			Intent i = new Intent(this, About.class);
			startActivity(i);
			return true;
		case EXPORT_ID:
			exportToSD();
			return true;
		case VOICE_ID:
			// Uri uri =
			// Uri.parse("market://search?q=pname:com.hellfire.speak2send");
			Uri uri = Uri
					.parse("market://search?q=pname:com.google.android.voicesearch");
			Intent it = new Intent(Intent.ACTION_VIEW, uri);
			try {
				startActivity(it);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		case TTS_ID:
			Uri uritts = Uri.parse("market://search?q=pname:com.google.tts");
			Intent tts = new Intent(Intent.ACTION_VIEW, uritts);
			try {
				startActivity(tts);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void exportToSD() {
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			// 获取SD卡目录
			File sdDir = Environment.getExternalStorageDirectory();
			FileWriter fw;
			File myDir;
			try {
				myDir = new File(sdDir.toString() + "/Speak2Send");
				if (!myDir.exists()) {
					if (myDir.mkdirs())
						Log.i(TAG, "Mkdir Success" + myDir.toString());
					else
						Log.w(TAG, "Mkdir Failed" + myDir.toString());
				} else
					Log.i(TAG, "Mkdir exists");

				if (myDir.exists()) {
					String fname = myDir.toString() + "/" + getDate() + ".txt";
					fw = new FileWriter(fname, true);

					Cursor cur = mDbHelper.fetchAllNotes();
					if (cur != null) {
						startManagingCursor(cur);
						cur.moveToFirst();

						while (!cur.isAfterLast()) {
							String id = cur.getString(cur
									.getColumnIndex(NotesDbAdapter.KEY_ROWID));
							fw.write(id + "\r\n");
							String d = cur.getString(cur
									.getColumnIndex(NotesDbAdapter.KEY_DATE));
							fw.write(d + "\r\n");
							String t = cur.getString(cur
									.getColumnIndex(NotesDbAdapter.KEY_TITLE));
							fw.write(t + "\r\n");
							String b = cur.getString(cur
									.getColumnIndex(NotesDbAdapter.KEY_BODY));
							fw.write(b + "\r\n\r\n");
							cur.moveToNext();
						}
					}
					// 关闭文件
					fw.close();
					Intent i = new Intent(this, Export.class);
					Bundle mBundle = new Bundle();
					mBundle.putString("path", fname);// 压入数据
					i.putExtras(mBundle);
					startActivity(i);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getDate() {
		Calendar calendar = Calendar.getInstance();
		Date d = calendar.getTime();
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd_hh-mm-ss");
		String date = sDateFormat.format(d);
		Log.i(TAG, "getDate()" + date);
		return date;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete).setIcon(
				android.R.drawable.ic_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			mDbHelper.deleteNote(info.id);
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void createNote() {
		Intent i = new Intent(this, NoteEdit.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, NoteEdit.class);
		i.putExtra(NotesDbAdapter.KEY_ROWID, id);
		startActivityForResult(i, ACTIVITY_EDIT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		fillData();
	}
}
