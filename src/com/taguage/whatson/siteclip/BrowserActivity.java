package com.taguage.whatson.siteclip;

import com.taguage.whatson.siteclip.Dialog.DialogLoading;
import com.taguage.whatson.siteclip.dataObj.Constant;
import com.taguage.whatson.siteclip.db.DBManager;
import com.taguage.whatson.siteclip.utils.AsyncCrawl;
import com.taguage.whatson.siteclip.utils.Listicle;
import com.taguage.whatson.siteclip.utils.MLog;
import com.taguage.whatson.siteclip.utils.Utils;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class BrowserActivity extends BaseActivity implements AsyncCrawl.Crawller, Listicle.OnSelect {

	String url,sitename;
	ActionBar ab;
	WebView wv;
	
	String pcont, ptitle;
	int tid;

	DialogLoading dialogLoading;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ab=setActionBar(R.string.ptitle_read);
		setContentView(R.layout.activity_browser);

		Bundle b=getIntent().getExtras();
		if(b!=null){
			if(b.containsKey("url"))url=b.getString("url");
			if(b.containsKey("name")){
				sitename=b.getString("name");
				ab.setTitle(sitename);			
			}
			setView();
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(dialogLoading!=null && dialogLoading.isAdded()){
			if(!Utils.isBackground(this))dialogLoading.dismiss();
		}
	}

	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(wv!=null)wv.destroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {	
		String curl=wv.getUrl().replaceAll("#.*", "");
		curl=curl.replaceAll("\\?.*", "");

		if(curl.equals("http://m.guokr.com/"))curl=url;
		else if(curl.equals("http://www.qdaily.com/webapp/homes"))curl=url;
		
		if (keyCode == KeyEvent.KEYCODE_BACK && !curl.equals(url)) {
			wv.goBack();
			return false;
		}else {
			finish();
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.clear();
		MenuInflater inflater=this.getMenuInflater();
		inflater.inflate(R.menu.menu_browser, menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			break;
		case R.id.action_save:
			crawlPage();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void crawlPage(){
		MLog.e("","webview url="+wv.getUrl());
		
		String currentUrl=AsyncCrawl.extractUrl(wv.getUrl());
		
		MLog.e("","currentUrl="+currentUrl);
		
		if(currentUrl.equals("")){
			Utils.getInstance().makeToast(R.string.info_not_support_this_page);
			return;
		}
		
		AsyncCrawl.prepareToCrawl(currentUrl, this, this);

	}


	private void setView() {
		// TODO Auto-generated method stub
		wv=(WebView) this.findViewById(R.id.wv);
		wv.getSettings().setDefaultTextEncodingName("utf-8");
		wv.getSettings().setUseWideViewPort(false);
		//wv.setWebChromeClient(new WebChromeClient());
		wv.getSettings().setJavaScriptEnabled(true);

		final ProgressBar pbar=(ProgressBar)findViewById(R.id.pbar);
		wv.setWebChromeClient(new WebChromeClient(){

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				// TODO Auto-generated method stub
				super.onProgressChanged(view, newProgress);
				pbar.setVisibility(View.VISIBLE);
				pbar.setProgress(newProgress);
			}

		});
		wv.setWebViewClient(new WebViewClient(){

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				view.loadUrl(url);
				MLog.e("","webview url="+url);
				return true;
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				// TODO Auto-generated method stub
				super.onReceivedError(view, errorCode, description, failingUrl);
				Utils.getInstance().makeToast(R.string.info_fail_to_load_webpage);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				pbar.setVisibility(View.GONE);
			}

		});
		wv.loadUrl(url);
	}

	@Override
	public void onCrawling() {
		// TODO Auto-generated method stub
		if(dialogLoading==null)dialogLoading=new DialogLoading();
		dialogLoading.show(fm, DialogLoading.TAG);
	}

	@Override
	public void onFinished(String ptitle, String pcont, int tid) {
		// TODO Auto-generated method stub
		if(dialogLoading!=null && dialogLoading.isAdded()){
			if(!Utils.isBackground(this))dialogLoading.dismiss();
		}
		
		this.ptitle=ptitle;
		this.pcont=pcont;
		this.tid=tid;
		
		Utils.getInstance().makeToast(R.string.info_saved_to_list);
		
		int[] ids=Listicle.getExistingListicleIds();
		if(ids!=null  && !app.getSpBoolean(R.string.key_no_listicle_dialog))	Listicle.showSelectDialog(this, ids, tid, this);
		else onFinishSelect();
	}

	@Override
	public void onError() {
		// TODO Auto-generated method stub
		if(dialogLoading!=null)dialogLoading.dismiss();
		Utils.getInstance().makeToast(R.string.info_fail_to_get_data);
	}

	@Override
	public void onFinishSelect() {
		// TODO Auto-generated method stub
		Intent intent=new Intent(this,PreviewActivity.class);
		Bundle b=new Bundle();
		b.putString("title", ptitle);
		b.putString("cont",pcont);
		b.putInt("tid", tid);
		intent.putExtras(b);
		startActivity(intent);
	}

	@Override
	public void onAdd(int fid) {
		// TODO Auto-generated method stub
		Listicle.showCreateAndSaveDialog(fid, this, this);
	}

	@Override
	public void afterAdd() {
		// TODO Auto-generated method stub
		onFinishSelect();
	}


}
