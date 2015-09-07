package com.taguage.whatson.siteclip;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.taguage.whatson.siteclip.Dialog.DialogLoading;
import com.taguage.whatson.siteclip.dataObj.Constant;
import com.taguage.whatson.siteclip.db.DBManager;
import com.taguage.whatson.siteclip.utils.AsyncCrawl;
import com.taguage.whatson.siteclip.utils.FileUtils;
import com.taguage.whatson.siteclip.utils.Listicle;
import com.taguage.whatson.siteclip.utils.MLog;
import com.taguage.whatson.siteclip.utils.Utils;
import com.taguage.whatson.siteclip.utils.Web;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

public class PreviewActivity extends BaseActivity  implements AsyncCrawl.Crawller, Listicle.OnSelect{

	String ptitle,pcont,token;
	WebView wv;
	DialogLoading dialogLoading;
	boolean isFromPush,isNight;
	boolean isCrawling;
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		token=app.getSpString(R.string.key_taguage_token);

		setActionBar(R.string.ptitle_preview);
		setContentView(R.layout.activity_preview);
		setView();
		
		//从其它程序传来的信息
		Intent intent=getIntent();
		if(intent.getAction()!=null){
			//if(Utils.getInstance().getCtx()==null)Utils.getInstance().setCtx(this);
			
			MLog.e("","action="+intent.getAction());
			if(intent.getAction().equals(Intent.ACTION_VIEW)){
				
				if(intent.getScheme().equals("http") || intent.getScheme().equals("https")){
					String str = intent.getDataString().trim();
					
					extractAndCraw(str);
				}
			}else if(intent.getAction().equals(Intent.ACTION_SEND)){
				Bundle extras=intent.getExtras();
				if(extras!=null){
					MLog.e("","type="+intent.getType());
					String title=intent.getStringExtra(Intent.EXTRA_SUBJECT),
							cont=intent.getStringExtra(Intent.EXTRA_TEXT);
					if(Constant.DEBUG)FileUtils.writeFile(title+cont, "email");
					if(title!=null && cont!=null){
						if(cont.contains("来自ZAKER：http://www.myzaker.com"))poccessPlainText(title, cont);
						
					}
				}				
			}else if( intent.getAction().equals(Intent.ACTION_SENDTO)){
				Bundle extras=intent.getExtras();
				if(extras!=null){
					String title=intent.getStringExtra(Intent.EXTRA_SUBJECT),
							cont=intent.getStringExtra(Intent.EXTRA_TEXT);
					MLog.e("","sendto="+title+cont);
					//if(Constant.DEBUG)FileUtils.writeFile(title+cont, "email");
					extractAndCraw(title+cont);
				}
			}
		}else MLog.e("","no intent");

		
		//自家传来的信息
		Bundle b=getIntent().getExtras();
		if(b!=null){			
			//从推送消息进来的url
			if(b.containsKey("url")){
				String url=b.getString("url");
				url=AsyncCrawl.extractUrl(url);
				if(url.equals(""))Utils.getInstance().makeToast(R.string.info_no_supported_url);
				else{
					isFromPush=true;
					int[] r=AsyncCrawl.verifyLink(url, true);
					if(r[0]==AsyncCrawl.YES){
						AsyncCrawl crawl=new AsyncCrawl(r[1], this, this);
						crawl.execute(url);
					}
				}
				
				return;
			}
			
			if(b.containsKey("title") && b.containsKey("cont")){
				ptitle=b.getString("title");
				pcont=b.getString("cont");	
				loadPage();
			}

			if(b.containsKey("tid")){
				int tid=b.getInt("tid");						

				boolean isManual=app.getSpBoolean(R.string.key_backup_manual);
				if(!isManual)uploadCont(tid);				
			}
		}
	}
	
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(!isCrawling){
			if(dialogLoading!=null && dialogLoading.isAdded()){
				if(!Utils.isBackground(this))dialogLoading.dismiss();
			}			
		}
	}
	
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(wv!=null)wv.destroy();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	private void extractAndCraw(String cont){
		String url=AsyncCrawl.extractUrl(cont);
		if(url.equals(""))Utils.getInstance().makeToast(R.string.info_no_supported_url);
		else{
			AsyncCrawl.prepareToCrawl(url, this, this);
		}
	}
	
	//目前仅解析Zaker文章
	private void poccessPlainText(String title, String cont){
		if(!Utils.isHtml(cont)){
			if(cont.contains("\n")){
				String[] temp=cont.split("\n");
				String str="";
				for(String s:temp){
					str=str+"<p>"+s+"</p>";
				}
				cont=str;
			}
		}
		AsyncCrawl crawl=new AsyncCrawl(-1, this, this);
		crawl.saveToDB("Zaker", "", title, cont);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.clear();
		MenuInflater inflater=this.getMenuInflater();
		if(!isNight)inflater.inflate(R.menu.menu_preview_day, menu);
		else inflater.inflate(R.menu.menu_preview_night, menu);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			break;
		case R.id.action_switch_day_and_night:
			isNight=!isNight;
			loadPage();
			this.invalidateOptionsMenu();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void uploadCont(final int tid) {
		// TODO Auto-generated method stub
		if(token.equals(""))return;
		
		Web.getInstance().getTags(token, tid, new Web.CallBack() {

			@Override
			public void onSuccess(JSONObject json) {
				// TODO Auto-generated method stub
				Utils.getInstance().makeToast(R.string.info_success_backup);
				DBManager db=DBManager.getInstance();
				db.updateData(DBManager.MY_CLIP, "_id", tid, new String[]{
						"upload"
				}, new String[]{
						Constant.UPLOAD_ALREADY+""
				});
			}

			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				Utils.getInstance().makeToast(R.string.info_auto_upload);
			}

			@Override
			public void onFail() {
				// TODO Auto-generated method stub

			}
		});

	}

	private void setView() {
		// TODO Auto-generated method stub
		wv=(WebView) this.findViewById(R.id.wv);
		wv.getSettings().setDefaultTextEncodingName("utf-8");
		wv.getSettings().setUseWideViewPort(false);
		wv.getSettings().setJavaScriptEnabled(true);

	}

	private void loadPage(){
		String bodyStyle="",contStyle="color:#b4b5b5;",hdStyle="color:#212121;",aStyle="color:auto;";
		if(isNight){
			bodyStyle=";background:#212121 !important";
			contStyle="color:#e6e6e6 !important;";
			hdStyle="color:#e6e6e6 !important;";
			aStyle="color:#f09300 !important;";
		}
		int[] size=Utils.getScreenSize(this);
		String html="<style>body{width:"+(Utils.px2dip(this, size[0])-20)
				+"px"+bodyStyle+"}img{max-width:100%; height:auto !important}" +
						"video{max-width:100%; height:auto !important}" 
				+"a{"+aStyle+"}</style>"
				+ "<body><h2 style="+hdStyle+">"
				+ptitle+"<h2>"+"<div style=\"font-size:16px; line-height:1.8em; "+contStyle+"font-weight:normal\">"
				+pcont+"</div></body>";
		
		wv.loadDataWithBaseURL(null,html, "text/html", "utf-8",null);
	}

	@Override
	public void onCrawling() {
		// TODO Auto-generated method stub		
		isCrawling=true;
		if(dialogLoading==null)dialogLoading=new DialogLoading();
		dialogLoading.show(fm, DialogLoading.TAG);
	}

	@Override
	public void onFinished(String ptitle, String pcont, int tid) {
		// TODO Auto-generated method stub
		isCrawling=false;
		if(dialogLoading!=null && dialogLoading.isAdded()){
			if(!Utils.isBackground(this))dialogLoading.dismiss();
		}
		this.ptitle=ptitle;
		this.pcont=pcont;
		loadPage();
		
		if(!isFromPush){
			boolean isManual=app.getSpBoolean(R.string.key_backup_manual);
			if(!isManual)uploadCont(tid);					
		}
		
		int[] ids=Listicle.getExistingListicleIds();
		if(ids!=null && !app.getSpBoolean(R.string.key_no_listicle_dialog))	Listicle.showSelectDialog(this, ids, tid, this);
		else onFinishSelect();
		
	}

	@Override
	public void onError() {
		// TODO Auto-generated method stub
		isCrawling=false;
		dialogLoading.dismiss();
		Utils.getInstance().makeToast(R.string.info_fail_to_get_data);
	}

	@Override
	public void onFinishSelect() {
		// TODO Auto-generated method stub
		
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
