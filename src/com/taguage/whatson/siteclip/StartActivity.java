package com.taguage.whatson.siteclip;

import com.taguage.whatson.siteclip.Dialog.DialogLoading;
import com.taguage.whatson.siteclip.Dialog.DialogTutorial;
import com.taguage.whatson.siteclip.db.DBManager;
import com.taguage.whatson.siteclip.service.FloatService;
import com.taguage.whatson.siteclip.utils.AsyncCrawl;
import com.taguage.whatson.siteclip.utils.Listicle;
import com.taguage.whatson.siteclip.utils.MLog;
import com.taguage.whatson.siteclip.utils.Utils;
import com.umeng.update.UmengUpdateAgent;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class StartActivity extends BaseActivity implements AsyncCrawl.Crawller, Listicle.OnSelect{

	EditText et_link;
	
	DBManager db;
	boolean isFromShare,isCrawling;
	String pcont, ptitle;
	int tid;
	
	DialogLoading dialogLoading;
	DialogTutorial dialogTutorial;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar ab=this.getActionBar();
		ab.hide();
		
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.update(this);
		db=DBManager.getInstance();

		setContentView(R.layout.activity_start);
		setView();
		
		analysisIntent(getIntent());
		
		if(!app.getSpBoolean(R.string.key_tutorial_home)){
			dialogTutorial=new DialogTutorial();
			Bundle b=new Bundle();
			b.putInt("type", DialogTutorial.HOME);
			dialogTutorial.setArguments(b);
			dialogTutorial.show(fm, "dialog");
			app.setSpBoolean(R.string.key_tutorial_home, true);
		}
		
		if(app.getSpBoolean(R.string.key_open_float_window))startService(new Intent(this, FloatService.class));
		
	}
	

	private void analysisIntent(Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction()!=null){
			//if(Utils.getInstance().getCtx()==null)Utils.getInstance().setCtx(this);
			
			if(intent.getAction().equals(Intent.ACTION_SEND)){
				isFromShare=false;
				MLog.e("","share type="+intent.getType());
				if(intent.getType().equals("image/*") || intent.getType().equals("image/jpeg") 
						|| intent.getType().equals("text/plain") || intent.getType().equals("text/*")){
					Bundle extras=intent.getExtras();
					String str=(String) extras.getCharSequence(Intent.EXTRA_TEXT);
					MLog.e("","share str="+str);
					if(str==null)str="";
					
					String url=AsyncCrawl.extractUrl(str);
					if(str.equals(""))Utils.getInstance().makeToast(R.string.info_no_supported_url);
					else {
						et_link.setText(url);
						isFromShare=true;
						checkAndSend();
					}
				}
			}
		}else{
			Bundle b=intent.getExtras();
			if(b!=null){
				if(b.containsKey("url")){
					et_link.setText(b.getString("url"));
					if(b.containsKey("isClose")){
						isFromShare=true;
						checkAndSend();
					}
				}
			}
		}
	}

	private void setView() {
		// TODO Auto-generated method stub
		setSvg(R.id.iv_app_name, R.raw.t_app_name);
		setSvg(R.id.iv_send,R.raw.btn_send);
		setSvg(R.id.iv_help,R.raw.btn_link);
		setSvg(R.id.iv_list,R.raw.btn_list);
		setSvg(R.id.iv_options,R.raw.btn_options);
		setSvg(R.id.iv_stats,R.raw.btn_statistics);
		
		findViewById(R.id.tv_list).setOnClickListener(this);
		findViewById(R.id.tv_options).setOnClickListener(this);
		findViewById(R.id.tv_stats).setOnClickListener(this);

		et_link=(EditText) findViewById(R.id.et_link);
		
		if(!app.getSpBoolean(R.string.key_tutorial_intro)){
			startActivity(new Intent(this, IntroActivity.class));
			app.setSpBoolean(R.string.key_tutorial_intro, true);
		}
	}

	private void checkAndSend(){
		String url=et_link.getText().toString().trim();
		if(url.equals(""))return;
		url=AsyncCrawl.extractUrl(url);
		AsyncCrawl.prepareToCrawl(url, this, this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch(v.getId()){
		case R.id.iv_send:
			checkAndSend();
			break;
		case R.id.iv_list:
			visitEssayList();
			break;
		case R.id.tv_list:
			visitEssayList();
			break;
		case R.id.iv_stats:
			visitGraph();
			break;
		case R.id.tv_stats:
			visitGraph();
			break;
		case R.id.iv_options:
			startActivity(new Intent(this,OptionsActivity.class));
			break;
		case R.id.tv_options:
			startActivity(new Intent(this,OptionsActivity.class));
			break;
		case R.id.iv_help:
			startActivity(new Intent(this,LinksActivity.class));
			break;
		}
	}
	
	private void visitGraph(){
		Cursor c=db.getAll(DBManager.MY_CLIP, new String[]{
			"_id"	
		});
		int size=c.getCount();
		c.close();
		if(size==0)Utils.getInstance().makeToast(R.string.info_no_essays_saved);
		else {
			startActivity(new Intent(this,StatActivity.class));
		}
	}
	
	private void visitEssayList() {
		// TODO Auto-generated method stub ListicleActivity
		//Intent intent=new Intent(this,EssayActivity.class);
		Intent intent=new Intent(this,ListicleActivity.class);
		startActivity(intent);
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
		this.tid=tid;
		
		int[] ids=Listicle.getExistingListicleIds();
		if(ids!=null  && !app.getSpBoolean(R.string.key_no_listicle_dialog))	Listicle.showSelectDialog(this, ids, tid, this);
		else onFinishSelect();
		
	}

	@Override
	public void onError() {
		// TODO Auto-generated method stub
		isCrawling=false;
		if(dialogLoading!=null)dialogLoading.dismiss();
		Utils.getInstance().makeToast(R.string.info_fail_to_get_data);
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
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();		
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
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
		et_link.setText("");	
		if(isFromShare)finish();	
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
