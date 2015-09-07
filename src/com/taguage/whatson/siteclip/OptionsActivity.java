package com.taguage.whatson.siteclip;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.taguage.whatson.siteclip.Dialog.DialogLogin;
import com.taguage.whatson.siteclip.adapter.OptionsAdapter;
import com.taguage.whatson.siteclip.dataObj.Constant;
import com.taguage.whatson.siteclip.db.DBManager;
import com.taguage.whatson.siteclip.service.FloatService;
import com.taguage.whatson.siteclip.utils.Listicle;
import com.taguage.whatson.siteclip.utils.Utils;
import com.xiaomi.mipush.sdk.MiPushClient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class OptionsActivity extends BaseActivity implements AdapterView.OnItemClickListener{

	private final static int TAGUAGE=0, UPLOAD=TAGUAGE+1,MESSAGE=UPLOAD+1,LISTICLE=MESSAGE+1,
			FLOAT=LISTICLE+1,CLEAR_ALL=FLOAT+1,TUTORIAL=CLEAR_ALL+1,RECOMMEND=TUTORIAL+1;

	private String[] titles, subtitles;
	ArrayList<JSONObject> arr=new ArrayList<JSONObject>();
	OptionsAdapter adapter;
	ListView lv;
	
	boolean isDenyMessage;

	DialogLogin dialogLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setActionBar(R.string.ptitle_options);

		setContentView(R.layout.activity_options);
		setView();
	}

	private void setView() {
		// TODO Auto-generated method stub		
		titles=getResources().getStringArray(R.array.menu_options);
		subtitles=getResources().getStringArray(R.array.initial_submenu_options);

		lv=(ListView) findViewById(R.id.lv);
		arr.clear();
		adapter=new OptionsAdapter(this, arr, true);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		
		updateList();
		
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
	
	private void updateList(){
		arr.clear();
		
		isDenyMessage=app.getSpBoolean(R.string.key_dont_send_message);
		String[] msg=getResources().getStringArray(R.array.message_options);
		
		int len=subtitles.length;
		for(int i=0;i<len;i++){
			JSONObject json=new JSONObject();
			try {
				json.put("title", titles[i]);
				if(i==TAGUAGE && !app.getSpString(R.string.key_taguage_nick).equals("")){
					String s=getString(R.string.attach_curreont_account),
							nick=app.getSpString(R.string.key_taguage_nick);
					json.put("sub", s.replace("x", nick));
				} else if(i==MESSAGE){
					if(isDenyMessage){
						json.put("sub", msg[1]);
						MiPushClient.subscribe(this,Constant.SAVE_MESSAGE,null);
					}
					else {
						json.put("sub", msg[0]);
						MiPushClient.subscribe(this,Constant.NO_MESSAGE,null);
					}
				}else if(i==FLOAT){
					String[] fstrs=app.getResources().getStringArray(R.array.float_options);
					if(app.getSpBoolean(R.string.key_open_float_window))json.put("sub", fstrs[1]);
					else json.put("sub", fstrs[0]);
				}else if(i==LISTICLE){
					String[] lstrs=app.getResources().getStringArray(R.array.listicle_options);
					if(!app.getSpBoolean(R.string.key_no_listicle_dialog))json.put("sub", lstrs[0]);
					else json.put("sub", lstrs[1]);
				}
				else json.put("sub", subtitles[i]);
				arr.add(json);
				adapter.notifyDataSetChanged();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void setMessageOptions(){
		app.setSpBoolean(R.string.key_dont_send_message, !app.getSpBoolean(R.string.key_dont_send_message));
		updateList();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		switch(arg2){
		case TAGUAGE:
			if(dialogLogin==null)dialogLogin=new DialogLogin();
			dialogLogin.show(fm, "dialog");
			break;
		case UPLOAD:
			startActivity(new Intent(this,BackupOptionsActivity.class));
			break;
		case MESSAGE:
			setMessageOptions();
			break;
		case LISTICLE:
			tweakListicle();
			break;
		case FLOAT:
			switchFloat();
			break;
		case CLEAR_ALL:
			showClearConfirm();
			break;
		case TUTORIAL:
			startActivity(new Intent(this, IntroActivity.class));
			break;
		case RECOMMEND:
			startActivity(new Intent(this, AppsActivity.class));
			break;
		}
	}
	

	private void tweakListicle() {
		// TODO Auto-generated method stub
		app.setSpBoolean(R.string.key_no_listicle_dialog, !app.getSpBoolean(R.string.key_no_listicle_dialog));
		updateList();
	}

	private void showClearConfirm() {
		// TODO Auto-generated method stub
		final DBManager db=DBManager.getInstance();
		new AlertDialog.Builder(this).setTitle(R.string.dialog_title_confirm_to_clear)
		.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				db.getmDB().delete(DBManager.MY_CLIP, null, null);
				Listicle.clearAllFids();
				dialog.dismiss();
			}
		}).setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).show();
	}

	private void switchFloat() {
		// TODO Auto-generated method stub
		app.setSpBoolean(R.string.key_open_float_window, !app.getSpBoolean(R.string.key_open_float_window));
		if(!app.getSpBoolean(R.string.key_open_float_window)) stopService(new Intent(this, FloatService.class));
		else {
			//Utils.getInstance().makeToast(R.string.info_float_open);
			startService(new Intent(this, FloatService.class));
		}
		updateList();
	}

	public void setUserInfo(){
		String s=getString(R.string.attach_curreont_account),
				nick=app.getSpString(R.string.key_taguage_nick);
		if(nick.equals(""))return;

		JSONObject json=arr.get(TAGUAGE);
		try {
			json.put("sub", s.replace("x", nick));
			adapter.notifyDataSetChanged();
			Utils.getInstance().makeToast(R.string.info_success_login);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
