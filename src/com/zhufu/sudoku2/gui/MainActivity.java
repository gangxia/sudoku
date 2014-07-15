package com.zhufu.sudoku2.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.InterstitialAd;
import com.zhufu.sudoku2.R;
import com.zhufu.sudoku2.utils.AndroidUtils;


public class MainActivity extends Activity implements OnClickListener,
		AdListener {
	RelativeLayout share, about,  exit, start;

	Context context;

	private InterstitialAd interstitial;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		context = this;
		
		initView();
		initData();

	}

	public void initView() {

		start = (RelativeLayout) findViewById(R.id.start);
		start.setOnClickListener(this);
	
		share = (RelativeLayout) findViewById(R.id.share);
		share.setOnClickListener(this);

		about = (RelativeLayout) findViewById(R.id.about);
		about.setOnClickListener(this);
		
		exit = (RelativeLayout) findViewById(R.id.exit);
		exit.setOnClickListener(this);

	}

	String frist;
	SharedPreferences sharedPreferences;

	public void initData() {
	
		// 制作插页式广告
		interstitial = new InterstitialAd(this, "a1534ff55a29ddf");

		// 创建广告请求
		AdRequest adRequest = new AdRequest();

		// 开始加载插页式广告
		interstitial.loadAd(adRequest);

		// 将广告监听器设为使用以下回调
		interstitial.setAdListener(this);
		sharedPreferences = getSharedPreferences("frist", MODE_PRIVATE);
		frist = sharedPreferences.getString("frist", "");

		if (frist.length() == 0) {

			try {
				copyBigDataToSD();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void copyBigDataToSD() throws IOException {
		String path = Environment.getExternalStorageDirectory().getPath()
				+ "/.android/all-folders-2012-03-24.opensudoku";
		File file = new File(path);
		if (new File(path).exists()) {

		} else {
			new File(Environment.getExternalStorageDirectory().getPath()
					+ "/.android/").mkdirs();
			file.createNewFile();
		}
		InputStream myInput;
		OutputStream myOutput = new FileOutputStream(path);
		myInput = this.getAssets().open("all-folders-2012-03-24.opensudoku");
		byte[] buffer = new byte[1024];
		int length = myInput.read(buffer);
		while (length > 0) {
			myOutput.write(buffer, 0, length);
			length = myInput.read(buffer);
		}

		myOutput.flush();
		myInput.close();
		myOutput.close();
		Intent intent = new Intent(context, SudokuImportActivity.class);
		Uri uri = Uri.parse("file://" + path);
		intent.setData(uri);

		startActivity(intent);
		Editor editor = sharedPreferences.edit();
		editor.putString("frist", "no");
		editor.commit();

	}

	public void onRestart() {
		super.onRestart();

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
	

		
		case R.id.share:
			Intent intent = new Intent(Intent.ACTION_SEND);

			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share));
			intent.putExtra(
					Intent.EXTRA_TEXT,
					getString(R.string.share_text)
							+ "https://play.google.com/store/apps/details?id=com.zhufu.sudoku");
			startActivity(Intent.createChooser(intent,
					getString(R.string.share)));
			break;
		case R.id.start:

			Intent intent3 = new Intent(this, FolderListActivity.class);
			startActivity(intent3);
			break;
		case R.id.about:
			LayoutInflater factory = LayoutInflater.from(MainActivity.this);
			final View aboutView = factory.inflate(R.layout.about, null);
			TextView versionLabel = (TextView) aboutView
					.findViewById(R.id.version_label);
			String versionName = AndroidUtils
					.getAppVersionName(getApplicationContext());
			versionLabel.setText(getString(R.string.version, versionName));
			new AlertDialog.Builder(MainActivity.this)
					.setIcon(R.drawable.opensudoku_logo_72)
					.setTitle(R.string.app_name).setView(aboutView)
					.setPositiveButton("OK", null).create().show();
			break;

		case R.id.exit:
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(getString(R.string.exit) + "?")
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface paramDialogInterface,
										int paramInt) {
									// TODO Auto-generated method stub
									Intent intent = new Intent(
											Intent.ACTION_MAIN);
									intent.addCategory(Intent.CATEGORY_HOME);
									intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
									android.os.Process
											.killProcess(android.os.Process
													.myPid());

								}
							})
					.setNegativeButton(android.R.string.no,
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface paramDialogInterface,
										int paramInt) {
									// TODO Auto-generated method stub
									return;
								}
							}).create();
			dialog.show();
			break;
		default:
			break;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if ((keyCode == KeyEvent.KEYCODE_BACK)
				&& (event.getAction() == KeyEvent.ACTION_DOWN)
				&& (event.getRepeatCount() == 0)) {
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(getString(R.string.exit) + "?")
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface paramDialogInterface,
										int paramInt) {
									// TODO Auto-generated method stub
									Intent intent = new Intent(
											Intent.ACTION_MAIN);
									intent.addCategory(Intent.CATEGORY_HOME);
									intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
									android.os.Process
											.killProcess(android.os.Process
													.myPid());

								}
							})
					.setNegativeButton(android.R.string.no,
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface paramDialogInterface,
										int paramInt) {
									// TODO Auto-generated method stub
									return;
								}
							}).create();
			dialog.show();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private String getLanguageEnv() {
		Locale l = Locale.getDefault();
		String language = l.getLanguage();
		String country = l.getCountry().toLowerCase();
		if ("zh".equals(language)) {
			if ("cn".equals(country)) {
				language = "zh-CN";
			} else if ("tw".equals(country)) {
				language = "zh-TW";
			}
		} else if ("pt".equals(language)) {
			if ("br".equals(country)) {
				language = "pt-BR";
			} else if ("pt".equals(country)) {
				language = "pt-PT";
			}
		}
		return language;
	}

	@Override
	public void onDismissScreen(com.google.ads.Ad arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFailedToReceiveAd(com.google.ads.Ad arg0, ErrorCode arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLeaveApplication(com.google.ads.Ad arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPresentScreen(com.google.ads.Ad arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReceiveAd(com.google.ads.Ad arg0) {
		// TODO Auto-generated method stub
		if (arg0 == interstitial) {
			interstitial.show();
		}
	}

}
