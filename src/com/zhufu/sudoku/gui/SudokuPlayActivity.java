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

package com.zhufu.sudoku.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.zhufu.sudoku.R;
import com.zhufu.sudoku.db.SudokuDatabase;
import com.zhufu.sudoku.game.SudokuGame;
import com.zhufu.sudoku.game.SudokuGame.OnPuzzleSolvedListener;
import com.zhufu.sudoku.gui.inputmethod.IMControlPanel;
import com.zhufu.sudoku.gui.inputmethod.IMControlPanelStatePersister;
import com.zhufu.sudoku.gui.inputmethod.IMSingleNumber;
import com.zhufu.sudoku.utils.AndroidUtils;

/*
 */
public class SudokuPlayActivity extends Activity {

	public static final String EXTRA_SUDOKU_ID = "sudoku_id";

	private static final int DIALOG_RESTART = 1;
	private static final int DIALOG_WELL_DONE = 2;
	private static final int DIALOG_CLEAR_NOTES = 3;
	private static final int DIALOG_UNDO_TO_CHECKPOINT = 4;

	private static final int REQUEST_SETTINGS = 1;

	private long mSudokuGameID;
	private SudokuGame mSudokuGame;

	private SudokuDatabase mDatabase;

	private Handler mGuiHandler;

	private ViewGroup mRootLayout;
	private SudokuBoardView mSudokuBoard;
	private TextView mTimeLabel;

	private IMControlPanel mIMControlPanel;
	private IMControlPanelStatePersister mIMControlPanelStatePersister;
	// private IMPopup mIMPopup;
	private IMSingleNumber mIMSingleNumber;
	// private IMNumpad mIMNumpad;

	private boolean mShowTime = true;
	private GameTimer mGameTimer;
	private GameTimeFormat mGameTimeFormatter = new GameTimeFormat();
	private boolean mFullScreen;
	private boolean mFillInNotesEnabled = false;

	Button back, restart;
	TextView name;

	private RelativeLayout layout;

	private AdView adView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// theme must be set before setContentView

		AndroidUtils.setThemeFromPreferences(this);

		setContentView(R.layout.sudoku_play);
		layout = (RelativeLayout) findViewById(R.id.adsdkContent);
		adView = new com.google.ads.AdView(this, AdSize.SMART_BANNER,
				"a1534ff55a29ddf");

		// 查询LinearLayout，假设其已指定
		// 属性android:id="@+id/mainLayout"

		// 在其中添加adView
		layout.addView(adView);

		// 启动一般性请求并在其中加载广告
		adView.loadAd(new AdRequest());
		name = (TextView) findViewById(R.id.name);
		back = (Button) findViewById(R.id.back);
		layout = (RelativeLayout) findViewById(R.id.adsdkContent);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		restart = (Button) findViewById(R.id.restart);
		restart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(SudokuPlayActivity.this)
						.setIcon(android.R.drawable.ic_menu_rotate)
						.setTitle(R.string.app_name)
						.setMessage(R.string.restart_confirm)
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										// Restart game
										mSudokuGame.reset();
										mSudokuGame.start();
										mSudokuBoard.setReadOnly(false);
										if (mShowTime) {
											mGameTimer.start();
										}
									}
								}).setNegativeButton(android.R.string.no, null)
						.create().show();
			}
		});
		mRootLayout = (ViewGroup) findViewById(R.id.root_layout);
		mSudokuBoard = (SudokuBoardView) findViewById(R.id.sudoku_board);
		mTimeLabel = (TextView) findViewById(R.id.time_label);

		mDatabase = new SudokuDatabase(getApplicationContext());

		mGameTimer = new GameTimer();

		mGuiHandler = new Handler();

		// create sudoku game instance
		if (savedInstanceState == null) {
			// activity runs for the first time, read game from database
			mSudokuGameID = getIntent().getLongExtra(EXTRA_SUDOKU_ID, 0);
			mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
		} else {
			// activity has been running before, restore its state
			mSudokuGame = new SudokuGame();
			mSudokuGame.restoreState(savedInstanceState);
			mGameTimer.restoreState(savedInstanceState);
		}

		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
			mSudokuGame.start();
		} else if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			mSudokuGame.resume();
		}

		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_COMPLETED) {
			mSudokuBoard.setReadOnly(true);
		}

		mSudokuBoard.setGame(mSudokuGame);
		mSudokuGame.setOnPuzzleSolvedListener(onSolvedListener);

		// mHintsQueue.showOneTimeHint("welcome", R.string.welcome,
		// R.string.first_run_hint);

		mIMControlPanel = (IMControlPanel) findViewById(R.id.input_methods);
		mIMControlPanel.initialize(mSudokuBoard, mSudokuGame);

		mIMControlPanelStatePersister = new IMControlPanelStatePersister(this);

		// mIMPopup = mIMControlPanel
		// .getInputMethod(IMControlPanel.INPUT_METHOD_POPUP);
		mIMSingleNumber = mIMControlPanel
				.getInputMethod(IMControlPanel.INPUT_METHOD_SINGLE_NUMBER);
		// mIMNumpad = mIMControlPanel
		// .getInputMethod(IMControlPanel.INPUT_METHOD_NUMPAD);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// read game settings
		SharedPreferences gameSettings = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		int screenPadding = gameSettings.getInt("screen_border_size", 0);
		mRootLayout.setPadding(screenPadding, screenPadding, screenPadding,
				screenPadding);

		mFillInNotesEnabled = gameSettings.getBoolean("fill_in_notes_enabled",
				false);

		mSudokuBoard.setHighlightWrongVals(gameSettings.getBoolean(
				"highlight_wrong_values", true));
		mSudokuBoard.setHighlightTouchedCell(gameSettings.getBoolean(
				"highlight_touched_cell", true));

		mShowTime = gameSettings.getBoolean("show_time", true);
		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			mSudokuGame.resume();

			if (mShowTime) {
				mGameTimer.start();
			}
		}
		mTimeLabel.setVisibility(mFullScreen && mShowTime ? View.VISIBLE
				: View.GONE);

		// mIMPopup.setEnabled(gameSettings.getBoolean("im_popup", true));
		mIMSingleNumber.setEnabled(gameSettings.getBoolean("im_single_number",
				true));
		// mIMNumpad.setEnabled(gameSettings.getBoolean("im_numpad", true));
		// mIMNumpad.setMoveCellSelectionOnPress(gameSettings.getBoolean(
		// "im_numpad_move_right", false));
		// mIMPopup.setHighlightCompletedValues(gameSettings.getBoolean(
		// "highlight_completed_values", true));
		// mIMPopup.setShowNumberTotals(gameSettings.getBoolean(
		// "show_number_totals", false));
		mIMSingleNumber.setHighlightCompletedValues(gameSettings.getBoolean(
				"highlight_completed_values", true));
		mIMSingleNumber.setShowNumberTotals(gameSettings.getBoolean(
				"show_number_totals", false));
		// mIMNumpad.setHighlightCompletedValues(gameSettings.getBoolean(
		// "highlight_completed_values", true));
		// mIMNumpad.setShowNumberTotals(gameSettings.getBoolean(
		// "show_number_totals", false));

		mIMControlPanel.activateFirstInputMethod(); // make sure that some input
													// method is activated
		mIMControlPanelStatePersister.restoreState(mIMControlPanel);

		updateTime();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus) {
			// FIXME: When activity is resumed, title isn't sometimes hidden
			// properly (there is black
			// empty space at the top of the screen). This is desperate
			// workaround.
			if (mFullScreen) {
				mGuiHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						getWindow()
								.clearFlags(
										WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
						mRootLayout.requestLayout();
					}
				}, 1000);
			}

		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// we will save game to the database as we might not be able to get back
		mDatabase.updateSudoku(mSudokuGame);

		mGameTimer.stop();
		mIMControlPanel.pause();
		mIMControlPanelStatePersister.saveState(mIMControlPanel);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mDatabase.close();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		mGameTimer.stop();

		if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
			mSudokuGame.pause();
		}

		mSudokuGame.saveState(outState);
		mGameTimer.saveState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_SETTINGS:
			restartActivity();
			break;
		}
	}

	/**
	 * Restarts whole activity.
	 */
	private void restartActivity() {
		startActivity(getIntent());
		finish();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_WELL_DONE:
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(R.string.well_done)
					.setMessage(
							getString(R.string.congrats, mGameTimeFormatter
									.format(mSudokuGame.getTime())))
					.setPositiveButton(android.R.string.ok, null).create();
		case DIALOG_RESTART:
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_menu_rotate)
					.setTitle(R.string.app_name)
					.setMessage(R.string.restart_confirm)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Restart game
									mSudokuGame.reset();
									mSudokuGame.start();
									mSudokuBoard.setReadOnly(false);
									if (mShowTime) {
										mGameTimer.start();
									}
								}
							}).setNegativeButton(android.R.string.no, null)
					.create();
		case DIALOG_CLEAR_NOTES:
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_menu_delete)
					.setTitle(R.string.app_name)
					.setMessage(R.string.clear_all_notes_confirm)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									mSudokuGame.clearAllNotes();
								}
							}).setNegativeButton(android.R.string.no, null)
					.create();
		case DIALOG_UNDO_TO_CHECKPOINT:
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_menu_delete)
					.setTitle(R.string.app_name)
					.setMessage(R.string.undo_to_checkpoint_confirm)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									mSudokuGame.undoToCheckpoint();
								}
							}).setNegativeButton(android.R.string.no, null)
					.create();

		}
		return null;
	}

	/**
	 * Occurs when puzzle is solved.
	 */
	private OnPuzzleSolvedListener onSolvedListener = new OnPuzzleSolvedListener() {

		@Override
		public void onPuzzleSolved() {
			mSudokuBoard.setReadOnly(true);
			showDialog(DIALOG_WELL_DONE);
		}

	};

	/**
	 * Update the time of game-play.
	 */
	void updateTime() {
		if (mShowTime) {
			name.setText(mGameTimeFormatter.format(mSudokuGame.getTime()));
			mTimeLabel
					.setText(mGameTimeFormatter.format(mSudokuGame.getTime()));
		} else {
			name.setText(R.string.app_name);
		}

	}

	// This class implements the game clock. All it does is update the
	// status each tick.
	private final class GameTimer extends Timer {

		GameTimer() {
			super(1000);
		}

		@Override
		protected boolean step(int count, long time) {
			updateTime();

			// Run until explicitly stopped.
			return false;
		}

	}

}
