package com.taguage.whatson.siteclip;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.taguage.whatson.siteclip.Dialog.DialogAddListicle;
import com.taguage.whatson.siteclip.Dialog.DialogEditListicle;
import com.taguage.whatson.siteclip.Dialog.DialogTutorial;
import com.taguage.whatson.siteclip.adapter.ListicleAdapter;
import com.taguage.whatson.siteclip.db.DBManager;
import com.taguage.whatson.siteclip.utils.Listicle;
import com.taguage.whatson.siteclip.utils.MLog;
import com.taguage.whatson.siteclip.utils.Utils;
import com.taguage.whatson.siteclip.view.BaseSwipeListViewListener;
import com.taguage.whatson.siteclip.view.SwipeListView;

public class ListicleActivity extends BaseActivity {

	public final static int DEL=0, EDIT=1, UP=2, DOWN=3;
	
	SwipeListView lv;
	Handler handler;
	
	ListicleAdapter adapter;
	ArrayList<JSONObject> arr=new ArrayList<JSONObject>();
	DialogAddListicle dialogAddListicle;
	DialogEditListicle dialogEditListicle;
	DialogTutorial dialogTutorial;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		ActionBar ab=this.getActionBar();
		ab.hide();
		
		setContentView(R.layout.activity_listicle);
		
		handler=new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if(lv!=null)lv.closeOpenedItems();
				switch(msg.what){
				case DEL:
					showDelConfirm(msg.arg1);
					break;
				case EDIT:
					editListicleName(msg.arg1);					
					break;
				case UP:
					Listicle.upToTop(msg.arg1);
					updateList();
					break;
				case DOWN:
					Listicle.downToBottom(msg.arg1);
					updateList();
					break;
				}
			}
			
		};
		
		setView();
		
		if(!app.getSpBoolean(R.string.key_tutorial_listicle)){
			dialogTutorial=new DialogTutorial();
			Bundle b=new Bundle();
			b.putInt("type", DialogTutorial.LISTICLE);
			dialogTutorial.setArguments(b);
			dialogTutorial.show(fm, "dialog");
			app.setSpBoolean(R.string.key_tutorial_listicle, true);
		}
		
	}
	
	private void showDelConfirm(final int lid){
		new AlertDialog.Builder(this).setTitle(R.string.dialog_title_del_listicle)
		.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Listicle.delListicle(lid);
				dialog.dismiss();
				updateList();
			}
		}).setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).show();
	}
	
	private void editListicleName(int lid){
		if(dialogEditListicle==null)dialogEditListicle=new DialogEditListicle();
		Bundle b=new Bundle();
		b.putInt("type", DialogEditListicle.LISTICLE_NAME);
		b.putString("cont", Listicle.getName(lid));
		b.putInt("lid", lid);
		dialogEditListicle.setArguments(b);
		dialogEditListicle.show(fm, DialogEditListicle.TAG);
	}

	private void setView() {
		// TODO Auto-generated method stub
		lv=(SwipeListView) findViewById(R.id.lv);
		lv.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
		lv.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
		lv.setSwipeListViewListener(new MSwipeListViewListener());
		
		View head=LayoutInflater.from(this).inflate(R.layout.head_listicle, null);
		lv.addHeaderView(head);
		ImageView iv_all, iv_add;
		iv_all=(ImageView) head.findViewById(R.id.iv_all);
		iv_add=(ImageView) head.findViewById(R.id.iv_add);
		Utils.parseSvg(R.raw.btn_add	, iv_add, this);
		Utils.parseSvg(R.raw.btn_all	, iv_all, this);
		iv_all.setOnClickListener(this);
		iv_add.setOnClickListener(this);
		
		arr=Listicle.getListData(this);
		adapter=new ListicleAdapter(this, arr, handler);
		lv.setAdapter(adapter);	
	}
	
	public void updateList(){
		ArrayList<JSONObject> narr=Listicle.getListData(this);
		arr.clear();
		for(JSONObject jn:narr){
			arr.add(jn);
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		/*menu.clear();
		MenuInflater inflater=this.getMenuInflater();
		inflater.inflate(R.menu.menu_listicle, menu);*/
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			break;
		/*case R.id.action_add:
			if(dialogAddListicle==null)dialogAddListicle=new DialogAddListicle();
			dialogAddListicle.show(fm, DialogAddListicle.TAG);
			break;*/
		}
		return super.onOptionsItemSelected(item);
	}
	
	class MSwipeListViewListener extends BaseSwipeListViewListener{

		@Override
		public void onClickFrontView(int position) {
			super.onClickFrontView(position);
			
			JSONObject json=adapter.getItem(position-1);
			if(json!=null){
				try {
					int lid=json.getInt("_id");
					Intent intent=new Intent(ListicleActivity.this, EssayActivity.class);
					Bundle b=new Bundle();
					b.putInt("lid", lid);
					intent.putExtras(b);
					startActivity(intent);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}

		@Override
		public void onDismiss(int[] reverseSortedPositions) {
			
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch(v.getId()){
		case R.id.iv_all:
			Intent intent=new Intent(ListicleActivity.this, EssayActivity.class);
			startActivity(intent);
			break;
		case R.id.iv_add:
			if(dialogAddListicle==null)dialogAddListicle=new DialogAddListicle();
			dialogAddListicle.show(fm, DialogAddListicle.TAG);
			break;
		}
	}
	
	
	
}
