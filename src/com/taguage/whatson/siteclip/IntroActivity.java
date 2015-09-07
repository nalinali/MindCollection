package com.taguage.whatson.siteclip;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

public class IntroActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setActionBar(R.string.ptitle_tutorial);
		setContentView(R.layout.activity_tutorial);

		setView();
	}

	private void setView() {
		// TODO Auto-generated method stub
		WebView wv=(WebView) this.findViewById(R.id.wv);
		wv.getSettings().setDefaultTextEncodingName("utf-8");
		wv.getSettings().setUseWideViewPort(false);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadUrl("file:///android_asset/intro.html");
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
