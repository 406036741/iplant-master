package com.iplant.presenter.view.activity;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.iplant.R;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

/**
 * @author lildu
 *	网页界面
 */
public class AboutActivity extends BaseActivity {
	@Bind(R.id.tv_bartitle)
	TextView mTitle;
	
	@Bind(R.id.web)
	WebView mWebView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_about);
		
		initView();
	}

	private void initView() {
		ButterKnife.bind(this);		
		
		mTitle.setText("关于");
		
		//初始化webview
		WebSettings setting = mWebView.getSettings();
		setting.setJavaScriptEnabled(true);
		setting.setAppCacheEnabled(true);
		setting.setBuiltInZoomControls(false);
		setting.setUseWideViewPort(true);
		
		mWebView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return super.shouldOverrideUrlLoading(view, url);
			}
		});
		
		mWebView.setWebChromeClient(new WebChromeClient());
		
		mWebView.loadUrl("http://www.shris.com.cn/");
	}
}
