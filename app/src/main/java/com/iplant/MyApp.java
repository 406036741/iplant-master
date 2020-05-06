package com.iplant;

import android.app.Application;

import com.iplant.model.Account;
import com.iplant.model.Group;
import com.iplant.model.ToolBox;
import com.iplant.presenter.db.DBManage;
import com.iplant.presenter.http.DataFetchModule;
import com.iplant.presenter.http.HttpRequestModule;
import com.iplant.presenter.image.ImageFetcherModule;

public class MyApp extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		
		init();
	}

	private void init() {
		HttpRequestModule.getInstance().init(this);
		DataFetchModule.getInstance().init(this);
		ImageFetcherModule.getInstance().init(this);
        //deleteDatabase("iplant.db");
		DBManage.create(getApplicationContext(), 1, "iplant.db", 
				new Class[]{ToolBox.class, Group.class, Account.class}, null);
	}
}
