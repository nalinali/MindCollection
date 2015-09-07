package com.taguage.whatson.siteclip;

import com.taguage.whatson.siteclip.dataObj.AppContext;
import com.taguage.whatson.siteclip.service.FloatService;
import com.taguage.whatson.siteclip.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageView;

public class BaseActivity extends FragmentActivity implements View.OnClickListener {

	FragmentManager fm;
	AppContext app;

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	public void setSvg(int viewid, int rid){
		ImageView iv=(ImageView) findViewById(viewid);
		Utils.parseSvg(rid, iv, this);
		iv.setOnClickListener(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		fm=this.getSupportFragmentManager();
		app=(AppContext) this.getApplicationContext();
		if(Utils.utils==null)Utils.getInstance().initUtils(this);
	}

	public ActionBar setActionBar(int title){
		ActionBar ab = getActionBar();
		if (ab != null) {
			ab.setDisplayShowHomeEnabled(true);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayUseLogoEnabled(false); 
			ab.setHomeButtonEnabled(true);
			ab.setTitle(title);
		}
		return ab;
	}
	
	public ActionBar setActionBar(String str){
		ActionBar ab = getActionBar();
		if (ab != null) {
			ab.setDisplayShowHomeEnabled(true);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayUseLogoEnabled(false); 
			ab.setHomeButtonEnabled(true);
			ab.setTitle(str);
		}
		return ab;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		
	}


}
