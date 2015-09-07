package com.taguage.whatson.siteclip;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.taguage.whatson.siteclip.Dialog.DialogFilter;
import com.taguage.whatson.siteclip.adapter.OptionsAdapter;
import com.taguage.whatson.siteclip.dataObj.Constant;

public class BackupOptionsActivity extends BaseActivity  implements AdapterView.OnItemClickListener{

	private final static int AUTO=0, PRIVACY=AUTO+1,
			TAG_MODE=PRIVACY+1,SOURCE=TAG_MODE+1, FILTER=SOURCE+1;

	private String[] titles, autoinfo, privacyinfo, taginfo, sourceinfo;
	boolean isManual, isPrivate, isSimpleTag, isSource;
	ArrayList<JSONObject> arr=new ArrayList<JSONObject>();
	OptionsAdapter adapter;
	ListView lv;
	
	DialogFilter dialogFilter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setActionBar(R.string.ptitle_backup);
		
		isManual=app.getSpBoolean(R.string.key_backup_manual);
		isPrivate=app.getSpBoolean(R.string.key_backup_private);
		isSimpleTag=app.getSpBoolean(R.string.key_backup_simple_tag);
		isSource=!app.getSpBoolean(R.string.key_backup_source);

		setContentView(R.layout.activity_options);
		setView();
	}
	
	private void setView() {
		// TODO Auto-generated method stub
		titles=getResources().getStringArray(R.array.backup_params);
		autoinfo=getResources().getStringArray(R.array.upload_mode);
		privacyinfo=getResources().getStringArray(R.array.upload_privacy);
		taginfo=getResources().getStringArray(R.array.upload_tag);
		sourceinfo=getResources().getStringArray(R.array.upload_source);
		
		String temp=app.getSpString(R.string.key_backup_filter);

		lv=(ListView) findViewById(R.id.lv);
		arr.clear();
		adapter=new OptionsAdapter(this, arr, false);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		int len=titles.length;
		for(int i=0;i<len;i++){
			JSONObject json=new JSONObject();
			try {
				json.put("title", titles[i]);
				String subtitle="";
				if(i==AUTO)subtitle=(isManual)?autoinfo[1]:autoinfo[0];
				else if(i==PRIVACY)subtitle=(isPrivate)?privacyinfo[1]:privacyinfo[0];
				else if(i==TAG_MODE)subtitle=(isSimpleTag)?taginfo[1]:taginfo[0];
				else if(i==SOURCE)subtitle=(isSource)?sourceinfo[0]:sourceinfo[1];
				else if(i==FILTER){
					if(temp.equals(""))subtitle=getString(R.string.attach_tags_filters);
					else {
						int l=Math.min(16, temp.length());
						subtitle=temp.substring(0,l);
					}
				}
				json.put("sub", subtitle);
				arr.add(json);
				adapter.notifyDataSetChanged();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

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

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		switch(arg2){
		case AUTO:
			setUploadMode(adapter.getItem(arg2));
			break;
		case PRIVACY:
			setUploadPrivacy(adapter.getItem(arg2));
			break;
		case TAG_MODE:
			setUploadTag(adapter.getItem(arg2));
			break;
		case SOURCE:
			setUploadSource(adapter.getItem(arg2));
			break;
		case FILTER:
			if(dialogFilter==null)dialogFilter=new DialogFilter();
			dialogFilter.show(fm, DialogFilter.TAG);
			break;
		}
	}
	
	private void setUploadSource(final JSONObject json) {
		// TODO Auto-generated method stub
		boolean mode=app.getSpBoolean(R.string.key_backup_source);
		int checkedItem=(!mode)?0:1;
		final String[] items=this.getResources().getStringArray(R.array.upload_source);
		new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_title_set_upload_source))
		.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					if(which==0){
						app.setSpBoolean(R.string.key_backup_source, false);
						json.put("sub", items[0]);
					}else {
						app.setSpBoolean(R.string.key_backup_source, true);
						json.put("sub", items[1]);
					}
					adapter.notifyDataSetChanged();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dialog.dismiss();
			}
		}).show();
	}
	
	private void setUploadTag(final JSONObject json) {
		// TODO Auto-generated method stub
		boolean mode=app.getSpBoolean(R.string.key_backup_simple_tag);
		int checkedItem=(!mode)?0:1;
		final String[] items=this.getResources().getStringArray(R.array.upload_tag);
		new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_title_set_upload_tags))
		.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					if(which==0){
						app.setSpBoolean(R.string.key_backup_simple_tag, false);
						json.put("sub", items[0]);
					}else {
						app.setSpBoolean(R.string.key_backup_simple_tag, true);
						json.put("sub", items[1]);
					}
					adapter.notifyDataSetChanged();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dialog.dismiss();
			}
		}).show();
	}

	private void setUploadPrivacy(final JSONObject json) {
		// TODO Auto-generated method stub
		boolean mode=app.getSpBoolean(R.string.key_backup_private);
		int checkedItem=(!mode)?0:1;
		final String[] items=this.getResources().getStringArray(R.array.upload_privacy);
		new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_title_set_upload_privacy))
		.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					if(which==0){
						app.setSpBoolean(R.string.key_backup_private, false);
						json.put("sub", items[0]);
					}else {
						app.setSpBoolean(R.string.key_backup_private, true);
						json.put("sub", items[1]);
					}
					adapter.notifyDataSetChanged();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dialog.dismiss();
			}
		}).show();
	}

	private void setUploadMode(final JSONObject json) {
		// TODO Auto-generated method stub
		boolean mode=app.getSpBoolean(R.string.key_backup_manual);
		int checkedItem=(!mode)?0:1;
		final String[] items=this.getResources().getStringArray(R.array.upload_mode);
		new AlertDialog.Builder(this).setTitle(getString(R.string.dialog_title_set_upload_mode))
		.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					if(which==0){
						app.setSpBoolean(R.string.key_backup_manual, false);
						json.put("sub", items[0]);
					}else {
						app.setSpBoolean(R.string.key_backup_manual, true);
						json.put("sub", items[1]);
					}
					adapter.notifyDataSetChanged();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dialog.dismiss();
			}
		}).show();
	}
}
