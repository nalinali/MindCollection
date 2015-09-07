package com.taguage.whatson.siteclip;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.taguage.whatson.siteclip.Dialog.DialogTutorial;
import com.taguage.whatson.siteclip.adapter.LinksAdapter;
import com.taguage.whatson.siteclip.utils.Utils;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.widget.AdapterView;
import android.widget.ListView;

public class LinksActivity extends BaseActivity {

	WebView wv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setActionBar(R.string.ptitle_links);
		
		setContentView(R.layout.activity_links);
		setView();
		
		if(!app.getSpBoolean(R.string.key_tutorial_sitelist)){
			DialogTutorial dialogTutorial=new DialogTutorial();
			Bundle b=new Bundle();
			b.putInt("type", DialogTutorial.SITELIST);
			dialogTutorial.setArguments(b);
			dialogTutorial.show(fm, "dialog");
			app.setSpBoolean(R.string.key_tutorial_sitelist, true);
		}
	}

	@SuppressLint("JavascriptInterface")
	private void setView() {
		// TODO Auto-generated method stub
		wv=(WebView) this.findViewById(R.id.wv);
		wv.getSettings().setDefaultTextEncodingName("utf-8");
		wv.getSettings().setUseWideViewPort(false);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.addJavascriptInterface(new JSInteraction(), "linksActivity");
		wv.setWebChromeClient(new WebChromeClient());
		wv.setWebViewClient(new WebViewClient());
		wv.loadUrl("file:///android_asset/portal.html");
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
	
	
	final class JSInteraction{
		@JavascriptInterface
		public void jump(String url, String site){
			Bundle b=new Bundle();
			b.putString("url", url);
			b.putString("name", site);
			Intent intent=new Intent(LinksActivity.this,BrowserActivity.class);
			intent.putExtras(b);
			startActivity(intent);
		}
	}

	/*@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if(arg2<1)return;
		JSONObject json=adapter.getItem(arg2-1);
		try {
			Bundle b=new Bundle();
			b.putString("url", json.getString("url"));
			b.putString("site", json.getString("site"));
			Intent intent=new Intent(this,BrowserActivity.class);
			intent.putExtras(b);
			startActivity(intent);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

	
}
