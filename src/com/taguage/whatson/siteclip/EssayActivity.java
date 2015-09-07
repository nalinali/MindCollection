package com.taguage.whatson.siteclip;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.taguage.whatson.siteclip.Dialog.DialogLoading;
import com.taguage.whatson.siteclip.Dialog.DialogLogin;
import com.taguage.whatson.siteclip.Dialog.DialogTutorial;
import com.taguage.whatson.siteclip.adapter.EssayAdapter;
import com.taguage.whatson.siteclip.dataObj.Constant;
import com.taguage.whatson.siteclip.db.DBManager;
import com.taguage.whatson.siteclip.utils.Listicle;
import com.taguage.whatson.siteclip.utils.MLog;
import com.taguage.whatson.siteclip.utils.Utils;
import com.taguage.whatson.siteclip.utils.Web;
import com.taguage.whatson.siteclip.view.BaseSwipeListViewListener;
import com.taguage.whatson.siteclip.view.SwipeListView;
import com.taguage.whatson.siteclip.view.SwipeListViewListener;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class EssayActivity extends BaseActivity implements Listicle.OnSelect{

	DBManager db;
	EssayAdapter adapter;
	Cursor c;
	SwipeListView lv;
	boolean isTokenValid;
	int uploadid,lid=-1,pos=0;
	
	Handler handler;
	DialogLogin dialogLogin;
	DialogLoading dialogLoading;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		db=DBManager.getInstance();
		//setActionBar(R.string.ptitle_essay_list);
		Bundle b=getIntent().getExtras();
		if(b!=null){
			if(b.containsKey("lid"))lid=b.getInt("lid");
			//setActionBar(Listicle.getName(lid));
		}
		ActionBar ab = getActionBar();
		ab.hide();
		
		setContentView(R.layout.activity_essay_list);
		
		
		handler=new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch(msg.what){
				case EssayAdapter.DEL:
					if(lv!=null){
						lv.closeOpenedItems();
						int id=msg.arg1;
						db.del(DBManager.MY_CLIP, "_id", id);
						Listicle.updateFids(id);
						updatelist();
					}
					break;
				case EssayAdapter.UPLOAD:
					if(lv!=null){
						lv.closeOpenedItems();
						uploadid=msg.arg1;
						checkToken();
					}
					break;
				case EssayAdapter.LISTICLE:
					if(lv!=null)lv.closeOpenedItems();
					Listicle.showEditDialog(EssayActivity.this, msg.arg1, EssayActivity.this);
					break;
				}
			}			
		};
		
		setView();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			break;
		/*case R.id.action_clear:
			showClearConfirm();
			break;*/
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void showClearConfirm() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(this).setTitle(R.string.dialog_title_confirm_to_clear)
		.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				db.getmDB().delete(DBManager.MY_CLIP, null, null);
				updatelist();
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

	public void upload() {
		// TODO Auto-generated method stub		
		String token=app.getSpString(R.string.key_taguage_token);		
		
		Web.getInstance().getTags(token, uploadid, new Web.CallBack() {
			
			@Override
			public void onSuccess(JSONObject json) {
				// TODO Auto-generated method stub
				dialogLoading.dismiss();
				Utils.getInstance().makeToast(R.string.info_success_backup);
				DBManager db=DBManager.getInstance();
				db.updateData(DBManager.MY_CLIP, "_id", uploadid, new String[]{
						"upload"
				}, new String[]{
						Constant.UPLOAD_ALREADY+""
				});
				updatelist();
				Utils.getInstance().makeToast(R.string.info_success_backup);
				
			}
			
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				if(dialogLoading==null)dialogLoading=new DialogLoading();
				dialogLoading.show(fm, DialogLoading.TAG);
			}
			
			@Override
			public void onFail() {
				// TODO Auto-generated method stub
				Utils.getInstance().makeToast(R.string.info_fail_backup);
				dialogLoading.dismiss();
			}
		});
	}
	
	public void checkToken(){
		refreshtoken();
		if(isTokenValid)upload();
		else{
			if(dialogLogin==null)dialogLogin=new DialogLogin();
			dialogLogin.show(fm, "dialog");
		}
		Log.e("","isTokenValid="+isTokenValid);
	}
	
	private void refreshtoken() {
		isTokenValid=false;
		long expire = app.getSpLong(R.string.key_taguage_expire);
		if(expire>0&&expire-System.currentTimeMillis()>1000*600){
			isTokenValid=true;
			return;
		}
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("token", app.getSpString(R.string.key_taguage_token));
		params.put("fr", 2+"");
		params.put("uuid", Utils.getUUID(this));		
		
		Web.getInstance().refreshToken("http://api.taguage.com/account/refreshtoken", params, new Web.CallBack() {
			
			@Override
			public void onSuccess(JSONObject json) {
				// TODO Auto-generated method stub
				try {
					app.setSpString(R.string.key_taguage_token, json.getString("tokenid"));
					app.setSpString(R.string.key_taguage_uid, json.getString("uid"));
					app.setSpLong(R.string.key_taguage_expire, json.getLong("expire"));
					isTokenValid=true;
					upload();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFail() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(adapter!=null)updatelist();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		/*menu.clear();
		MenuInflater inflater=this.getMenuInflater();
		inflater.inflate(R.menu.menu_essays, menu);*/
		return super.onPrepareOptionsMenu(menu);
	}

	private void setView() {
		// TODO Auto-generated method stub
		db=DBManager.getInstance();
		c=db.getmDB().query(DBManager.MY_CLIP, new String[]{
				"_id",
				"source",
				"sourceurl",
				"title",
				"cont",
				"abstract",
				"folder",
				"tags",
				"time",
				"star",
				"upload"
		}, null, null, null, null, "_id DESC");
		lv=(SwipeListView) findViewById(R.id.lv);
		
		View head=LayoutInflater.from(this).inflate(R.layout.head_essays, null);
		lv.addHeaderView(head);
		TextView tv_essay=(TextView) head.findViewById(R.id.tv_essay);
		if(lid!=-1){
			tv_essay.setText(Listicle.getName(lid));
		}else tv_essay.setText(R.string.ptitle_essay_list);
		
		lv.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
		lv.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
		int[] deviceSize=Utils.getScreenSize(this);
		lv.setOffsetLeft( deviceSize[0] / 4);
		
		adapter=new EssayAdapter(this, c, true, handler);
		lv.setAdapter(adapter);
		lv.setSwipeListViewListener(new MSwipeListViewListener());
		
	}
	
	private void updatelist(){
		db=DBManager.getInstance();
		String condt=null;
		if(lid!=-1){
			condt="lid like '%#"+lid+"#%'";
		}
		c=db.getmDB().query(DBManager.MY_CLIP, new String[]{
				"_id",
				"source",
				"sourceurl",
				"title",
				"cont",
				"abstract",
				"folder",
				"tags",
				"time",
				"star",
				"upload"
		}, condt, null, null, null, "_id DESC");
		adapter.changeCursor(c);
		lv.setSelection(pos);
		MLog.e("","essay pos="+pos);
		
		if(adapter.getCount()==0){
			if(!app.getSpBoolean(R.string.key_tutorial_essaylist_no_cont)){
				DialogTutorial dialogTutorial=new DialogTutorial();
				Bundle b=new Bundle();
				b.putInt("type", DialogTutorial.ESSAY_NO);
				dialogTutorial.setArguments(b);
				dialogTutorial.show(fm, "dialog");
				app.setSpBoolean(R.string.key_tutorial_essaylist_no_cont, true);
			}
		}else{
			if(!app.getSpBoolean(R.string.key_tutorial_essaylist)){
				DialogTutorial dialogTutorial=new DialogTutorial();
				Bundle b=new Bundle();
				b.putInt("type", DialogTutorial.ESSAY);
				dialogTutorial.setArguments(b);
				dialogTutorial.show(fm, "dialog");
				app.setSpBoolean(R.string.key_tutorial_essaylist, true);
			}
		}
	}



	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		c.close();
	}
	
	class MSwipeListViewListener extends BaseSwipeListViewListener{

		@Override
		public void onClickFrontView(int position) {
			super.onClickFrontView(position);
			pos=position-1;
			
			Cursor c=(Cursor) adapter.getItem(position-1);
			Intent intent=new Intent(EssayActivity.this,PreviewActivity.class);
			Bundle b=new Bundle();
			b.putString("title", c.getString(c.getColumnIndex("title")));
			b.putString("cont",c.getString(c.getColumnIndex("cont")));
			intent.putExtras(b);
			c.close();
			startActivity(intent);	
		}

		@Override
		public void onDismiss(int[] reverseSortedPositions) {
			
		}
	}

	@Override
	public void onFinishSelect() {
		// TODO Auto-generated method stub
		updatelist();
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
