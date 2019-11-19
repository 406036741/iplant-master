package com.iplant.presenter.view.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.view.WindowManager;

public class SystemDialog {
	public static void show(Context c, String title, String msg){
		AlertDialog dialog = new AlertDialog.Builder(c).setPositiveButton("知道了", null).create();
		dialog.setMessage(msg);
		dialog.setTitle(title);
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
	}
}
