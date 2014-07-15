/* 
 * Copyright (C) 2009 Roman Masek
 * 
 * This file is part of OpenSudoku.
 * 
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.zhufu.sudoku2.gui;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.zhufu.sudoku2.R;
import com.zhufu.sudoku2.db.FolderColumns;
import com.zhufu.sudoku2.db.SudokuDatabase;
import com.zhufu.sudoku2.game.FolderInfo;
import com.zhufu.sudoku2.gui.FolderDetailLoader.FolderDetailCallback;

/**
 * List of puzzle's folder. This activity also serves as root activity of
 * application.
 * 
 * @author romario
 * 
 */
public class FolderListActivity extends ListActivity {

	private static final int DIALOG_ABOUT = 0;
	private static final int DIALOG_ADD_FOLDER = 1;
	private static final int DIALOG_RENAME_FOLDER = 2;
	private static final int DIALOG_DELETE_FOLDER = 3;

	private static final String TAG = "FolderListActivity";

	private Cursor mCursor;
	private SudokuDatabase mDatabase;
	private FolderListViewBinder mFolderListBinder;

	// input parameters for dialogs
	private TextView mAddFolderNameInput;
	private TextView mRenameFolderNameInput;
	private long mRenameFolderID;
	private long mDeleteFolderID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.folder_list);
		ImageView back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
		// Inform the list we provide context menus for items
		getListView().setOnCreateContextMenuListener(this);

		mDatabase = new SudokuDatabase(getApplicationContext());
		mCursor = mDatabase.getFolderList();
		startManagingCursor(mCursor);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.folder_list_item, mCursor, new String[] {
						FolderColumns.NAME, FolderColumns._ID }, new int[] {
						R.id.name, R.id.detail });
		mFolderListBinder = new FolderListViewBinder(this);
		adapter.setViewBinder(mFolderListBinder);

		setListAdapter(adapter);

	}

	@Override
	protected void onStart() {
		super.onStart();

		updateList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDatabase.close();
		mFolderListBinder.destroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong("mRenameFolderID", mRenameFolderID);
		outState.putLong("mDeleteFolderID", mDeleteFolderID);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);

		mRenameFolderID = state.getLong("mRenameFolderID");
		mDeleteFolderID = state.getLong("mDeleteFolderID");
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(this, SudokuListActivity.class);
		i.putExtra(SudokuListActivity.EXTRA_FOLDER_ID, id);
		startActivity(i);
	}

	private void updateList() {
		mCursor.requery();
	}

	private static class FolderListViewBinder implements ViewBinder {
		private Context mContext;
		private FolderDetailLoader mDetailLoader;

		public FolderListViewBinder(Context context) {
			mContext = context;
			mDetailLoader = new FolderDetailLoader(context);
		}

		@Override
		public boolean setViewValue(View view, Cursor c, int columnIndex) {

			switch (view.getId()) {
			case R.id.name:
				((TextView) view).setText(c.getString(columnIndex));
				break;
			case R.id.detail:
				final long folderID = c.getLong(columnIndex);
				final TextView detailView = (TextView) view;
				detailView.setText(mContext.getString(R.string.loading));
				mDetailLoader.loadDetailAsync(folderID,
						new FolderDetailCallback() {
							@Override
							public void onLoaded(FolderInfo folderInfo) {
								if (folderInfo != null)
									detailView.setText(folderInfo
											.getDetail(mContext));
							}
						});
			}

			return true;
		}

		public void destroy() {
			mDetailLoader.destroy();
		}
	}

}
