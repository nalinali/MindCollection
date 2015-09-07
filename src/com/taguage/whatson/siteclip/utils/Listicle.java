package com.taguage.whatson.siteclip.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.taguage.whatson.siteclip.R;
import com.taguage.whatson.siteclip.db.DBManager;

public class Listicle {

	private static boolean[] isSelected;
	
	public static ArrayList<JSONObject> getListData(Context ctx){
		ArrayList<JSONObject> arr=new ArrayList<JSONObject>();
		DBManager db=DBManager.getInstance();			
		//查找已整理过的文章
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"_id","name","files","seq"
		}, null, null, null, null, "seq ASC");
		try {
			for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
				JSONObject json=new JSONObject();
				json.put("_id", c.getInt(c.getColumnIndex("_id")));
				json.put("title", c.getString(c.getColumnIndex("name")));
				String temp=c.getString(c.getColumnIndex("files"));
				if(temp!=null){
					String[] temps=temp.split(",");
					json.put("count", temps.length);
				}
				json.put("files", c.getString(c.getColumnIndex("files")));
				json.put("seq", c.getInt(c.getColumnIndex("seq")));
				arr.add(json);
			}						
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			c.close();
		}
		return arr;
	}
	
	public static void upToTop(int lid){
		DBManager db=DBManager.getInstance();	
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"_id","seq"
		}, null, null, null, null, "seq ASC");
		int count=1;
		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
			if(c.getInt(c.getColumnIndex("_id"))!=lid)db.updateData(DBManager.LISTICLE, "_id",
					c.getInt(c.getColumnIndex("_id")), 
					new String[]{ "seq" }, new Object[]{ count });
			count++;
		}		
		c.close();
		db.updateData(DBManager.LISTICLE, "_id",lid,
				new String[]{ "seq" }, new Object[]{ 0 });
	}
	
	public static void downToBottom(int lid){
		DBManager db=DBManager.getInstance();	
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"_id","seq"
		}, null, null, null, null, "seq ASC");
		int count=0;
		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
			if(c.getInt(c.getColumnIndex("_id"))!=lid)db.updateData(DBManager.LISTICLE, "_id",
					c.getInt(c.getColumnIndex("_id")), 
					new String[]{ "seq" }, new Object[]{ count });
			count++;
		}		
		c.close();
		db.updateData(DBManager.LISTICLE, "_id",lid,
				new String[]{ "seq" }, new Object[]{ count });
	}
	
	public static String getName(int id){
		DBManager db=DBManager.getInstance();	
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"_id","name"
		}, "_id="+id, null, null, null, null);
		c.moveToFirst();
		String s="";
		if(c.getCount()!=0)	s=c.getString(c.getColumnIndex("name"));
		c.close();
		return s;
	}
	
	public static void delListicle(int lid){
		DBManager db=DBManager.getInstance();	
		String files="";
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"_id","name","files","seq"
		}, "_id="+lid, null, null, null, null);
		c.moveToFirst();
		if(c.getCount()>0)files=c.getString(c.getColumnIndex("files"));
		c.close();
		db.del(DBManager.LISTICLE, "_id", lid);
		
		if(files.equals(""))return;		
		String[] clips=files.split(",");
		int len=clips.length;
		for(int i=0;i<len;i++){
			int fid=Integer.parseInt(clips[i]);
			updateLids(lid,fid);
		}
	}
	
	public static void updateLids(int lid, int fid){
		DBManager db=DBManager.getInstance();	
		Cursor c=db.getmDB().query(DBManager.MY_CLIP, new String[]{
				"_id","lid"
		}, "_id="+fid, null, null, null, null);
		c.moveToFirst();
		String lidstr=c.getString(c.getColumnIndex("lid"));
		c.close();
		
		String[] temp=lidstr.split(",");
		lidstr="";
		for(String s:temp){
			if(!s.equals(""+lid)){
				if(lidstr.equals(""))lidstr=s;
				else lidstr=lidstr+","+s;
			}
		}
		db.updateData(DBManager.MY_CLIP, "_id", fid, new String[]{
				"lid"
		}, new Object[]{
				lidstr
		});
	}
	
	public static int[] getExistingListicleIds(){
		DBManager db=DBManager.getInstance();	
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"_id","name","files","seq"
		}, null, null, null, null, "seq ASC");
		if(c.getCount()==0){
			c.close();
			return null;
		}
		int[] result=new int[c.getCount()];
		int count=0;
		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
			result[count]=c.getInt(c.getColumnIndex("_id"));
			count++;
		}
		c.close();
		return result;
	}
	
	//删除文件后从列表中同步数据
	public static void updateFids(int fid){
		DBManager db=DBManager.getInstance();	
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"_id","files"
		}, "files like '%#"+fid+"#'", null, null, null, null);
		if(c.getCount()>0){
			int lid=c.getInt(c.getColumnIndex("_id"));
			String files=c.getString(c.getColumnIndex("files"));
			files=files.replace("#"+fid+"#", "");
			files=files.replace(",,", ",");
			if(files.substring(files.length()-1, files.length()).equals(","))files=files.substring(0, files.length()-1);
			if(files.substring(0, 1).equals(","))files=files.substring(1, files.length());
			db.updateData(DBManager.LISTICLE, "_id", lid, new String[]{
					"files"
			}, new Object[]{
					files
			});
		}
		c.close();
	}
	
	public static int getMaxSeq(){
		int r=0;
		DBManager db=DBManager.getInstance();	
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"_id","seq"
		}, null, null, null, null, null);
		for(c.moveToFirst();!c.isAfterLast();c.moveToNext())r=Math.max(r, c.getInt(c.getColumnIndex("seq")));
		c.close();
		return r;
	}
	
	public static boolean isBelongToListicle(int fid, int lid){
		DBManager db=DBManager.getInstance();
		boolean r=false;
		Cursor c=db.getmDB().query(DBManager.MY_CLIP, new String[]{
			"_id","lid"	
		}, "_id="+fid, null, null, null, null);
		c.moveToFirst();
		if(c.getCount()>0)r=c.getString(c.getColumnIndex("lid")).contains("#"+lid+"#");
		c.close();
		return r;
	}
	
	public static void showEditDialog(Context ctx, final int fid, final OnSelect onSelect){
		final int[] lids=getExistingListicleIds();
		final int len=lids.length;
		String[] items=new String[len];
		isSelected=new boolean[len];
		boolean[] checkedItems=new boolean[len];
		for(int i=0;i<len;i++){
			checkedItems[i]=isBelongToListicle(fid, lids[i]);
			isSelected[i]=checkedItems[i];
		}
		
		DBManager db=DBManager.getInstance();		
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"_id","name","files","seq"
		}, null, null, null, null, "seq ASC");
		int count=0;
		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
			items[count]=c.getString(c.getColumnIndex("name"));
			count++;
		}
		c.close();		
		
		new AlertDialog.Builder(ctx).setTitle(ctx.getString(R.string.dialog_title_save_into_listicle))
		.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				// TODO Auto-generated method stub
				isSelected[which]=isChecked;
			}
		}).setPositiveButton(ctx.getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub	
				for(int j=0;j<len;j++){
					if(isSelected[j]){
						addFiles(lids[j],fid);
					}
					else removeFiles(lids[j], fid);
				}
				onSelect.onFinishSelect();
				
			}
		}).setNegativeButton(ctx.getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				onSelect.onFinishSelect();
			}
		}).setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				onSelect.onFinishSelect();
			}
		})
		.create().show();
	}
	
	public static void showSelectDialog(Context ctx, final int[] ids, final int fid, final OnSelect onSelect ){
		final int len=ids.length;
		String[] items=new String[len];
		boolean[] checkedItems=new boolean[len];
		isSelected=new boolean[len];
		DBManager db=DBManager.getInstance();		
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"_id","name","files","seq"
		}, null, null, null, null, "seq ASC");
		int count=0;
		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
			items[count]=c.getString(c.getColumnIndex("name"));
			count++;
		}
		c.close();		
		
		new AlertDialog.Builder(ctx).setTitle(ctx.getString(R.string.dialog_title_save_into_listicle))
			.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					// TODO Auto-generated method stub
					isSelected[which]=isChecked;
				}
			}).setPositiveButton(ctx.getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub	
					for(int j=0;j<len;j++){
						if(isSelected[j])addFiles(ids[j],fid);
					}
					onSelect.onFinishSelect();
					
				}
			}).setNegativeButton(ctx.getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					onSelect.onFinishSelect();
				}
			}).setNeutralButton(R.string.btn_add_listicle, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					onSelect.onAdd(fid);
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					onSelect.onFinishSelect();
				}
			})
			.create().show();
		
	}
	
	public static void showCreateAndSaveDialog(final int fid, Context ctx, final OnSelect onSelect){
		final View view=LayoutInflater.from(ctx).inflate(R.layout.inflate_input, null);
		final EditText et_input=(EditText) view.findViewById(R.id.et_input);
		
		new AlertDialog.Builder(ctx).setTitle(ctx.getString(R.string.dialog_title_create_listicle)).setView(view)
			.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String str=et_input.getEditableText().toString().trim();
					if(isValid(str)){
						DBManager db=DBManager.getInstance();
						String timestr=System.currentTimeMillis()+"";
						db.insertData(DBManager.LISTICLE, new String[]{
								"name","time","seq","files"
						}, new Object[]{
								str, timestr ,Listicle.getMaxSeq()+1,""
						});
						int lid=getLid(timestr);
						addFiles(lid, fid);
						onSelect.afterAdd();
					}
				}
			}).setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					onSelect.afterAdd();
				}
			}).show();
	}
	
	private static int getLid(String time){
		DBManager db=DBManager.getInstance();
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"_id","time"
		}, "time='"+time+"'", null, null, null, null);
		c.moveToFirst();
		int _id=c.getInt(c.getColumnIndex("_id"));
		c.close();
		return _id;
	}
	
	private static String addLids(String lids, int lid){
		if(lids.equals(""))return "#"+lid+"#";
		
		lids=lids+",#"+lid+"#";
		String[] temp=lids.split(",");
		HashMap<String,Void> hm=new HashMap<String,Void>();
		for(String str:temp)hm.put(str, null);
		
		Iterator<String> it=hm.keySet().iterator();
		String r="";
		while(it.hasNext()){
			String s=it.next();
			if(r.equals(""))r=s;
			else r=r+","+s;
		}
		return r;
	}
	
	public static  boolean isValid(String str){
		DBManager db=DBManager.getInstance();
		if(str.equals("")){
			Utils.getInstance().makeToast(R.string.info_listicle_name_empty);
			return false;
		}
		boolean check=str.matches("[\u4E00-\u9FA5A-Za-z0-9_]{1,}");
		if(!check){
			Utils.getInstance().makeToast(R.string.info_listicle_name_invalid);
			return false;
		}
		check=false;
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"name"
		}, null, null, null, null, null);
		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
			if(str.equals(c.getString(c.getColumnIndex("name"))))check=true;
		}
		c.close();
		if(check){
			Utils.getInstance().makeToast(R.string.info_listicle_name_duplicate);
			return false;
		}
		return true;
	}
	
	private static String addFids(String files, int fid){
		if(files.equals(""))return ""+fid;
		
		files=files+","+fid;
		String[] temp=files.split(",");
		HashMap<String,Void> hm=new HashMap<String,Void>();
		for(String str:temp)hm.put(str, null);
		
		Iterator<String> it=hm.keySet().iterator();
		String r="";
		while(it.hasNext()){
			String s=it.next();
			if(r.equals(""))r=s;
			else r=r+","+s;
		}
		return r;
	}
		
	
	public static void removeFiles(int lid, int fid){
		DBManager db=DBManager.getInstance();
		String files=getFiles(lid);
		String[] temp=files.split(",");
		HashMap<String,Void> hm=new HashMap<String,Void>();
		for(String str:temp){
			if(!str.equals(""+fid))hm.put(str, null);
		}
		
		Iterator<String> it=hm.keySet().iterator();
		String r="";
		while(it.hasNext()){
			String s=it.next();
			if(r.equals(""))r=s;
			else r=r+","+s;
		}
		db.updateData(DBManager.LISTICLE, "_id", lid, new String[]{
				"files"
		}, new Object[]{
				r
		});
		
		String lids=getLids(fid);
		temp=lids.split(",");
		hm=new HashMap<String,Void>();
		for(String str:temp){
			if(!str.equals("#"+lid+"#"))hm.put(str, null);
		}
		
		 it=hm.keySet().iterator();
		r="";
		while(it.hasNext()){
			String s=it.next();
			if(r.equals(""))r=s;
			else r=r+","+s;
		}
		db.updateData(DBManager.MY_CLIP, "_id", fid, new String[]{
				"lid"
		}, new Object[]{
			r
		});
	}
	
	public static void addFiles(int lid, int fid){
		String files=addFids(getFiles(lid), fid);		
		DBManager db=DBManager.getInstance();		
		db.updateData(DBManager.LISTICLE, "_id", lid, new String[]{
				"files"
		}, new Object[]{
				files
		});
		
		String lids=addLids(getLids(fid), lid);
		db.updateData(DBManager.MY_CLIP, "_id", fid, new String[]{
				"lid"
		}, new Object[]{
				lids
		});
		//MLog.e("","lids="+lids);
		//displayAll(1);
		
	}
	
	public static String getLids(int id){
		DBManager db=DBManager.getInstance();		
		Cursor c=db.getmDB().query(DBManager.MY_CLIP, new String[]{
				"_id","lid"
		}, "_id="+id, null, null, null, null);
		c.moveToFirst();
		String result=c.getString(c.getColumnIndex("lid"));
		c.close();
		return result;
		
	}
	
	public static String getFiles(int lid){
		DBManager db=DBManager.getInstance();		
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"files"
		}, "_id="+lid, null, null, null, null);
		if(c.getCount()==0){
			c.close();
			return "";
		}
		c.moveToFirst();
		String files=c.getString(c.getColumnIndex("files"));
		c.close();
		return files;
	}
	
	public static void displayAll(int type){
		DBManager db=DBManager.getInstance();
		if(type==0){
			Cursor c=db.getAll(DBManager.LISTICLE, new String[]{
					"_id","name","files"
			});
			for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
				MLog.e("","_id="+c.getInt(c.getColumnIndex("_id"))
						+" name="+c.getString(c.getColumnIndex("name"))
						+" files="+c.getString(c.getColumnIndex("files")));
			}
			c.close();
		}else if(type==1){
			Cursor c=db.getAll(DBManager.MY_CLIP, new String[]{
					"title","lid"
			});
			for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
				MLog.e("","title="+c.getString(c.getColumnIndex("title"))
						+" lid="+c.getString(c.getColumnIndex("lid")));
			}
			c.close();
			
		}
	}
	
	public static void clearAllFids(){
		DBManager db=DBManager.getInstance();
		int[] ids=null;
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"_id","files"
		}, null, null, null, null, null);
		if(c.getCount()>0){
			ids=new int[c.getCount()];
			int count=0;
			for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
				ids[count]=c.getInt(c.getColumnIndex("_id"));
			}
		}
		c.close();
		if(ids!=null){
			for(int fid:ids){
				db.updateData(DBManager.LISTICLE, "_id", fid, new String[]{ "files" }, new Object[]{ "" });
			}
		}
	}
	
	
	public static interface OnSelect{
		public void onFinishSelect();
		public void onAdd(int fid);
		public void afterAdd();
	}
}
