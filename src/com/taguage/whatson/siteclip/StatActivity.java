package com.taguage.whatson.siteclip;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.taguage.whatson.siteclip.Dialog.DialogLoading;
import com.taguage.whatson.siteclip.dataObj.Constant;
import com.taguage.whatson.siteclip.db.DBManager;
import com.taguage.whatson.siteclip.utils.MLog;
import com.taguage.whatson.siteclip.utils.Utils;
import com.taguage.whatson.siteclip.view.StatGraph;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class StatActivity extends BaseActivity implements StatGraph.OnSavingBitmap {

	DialogLoading dialogLoading;
	StatGraph sgraph;
	File shareFile;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setActionBar(R.string.ptitle_stat);		

		setContentView(R.layout.activity_stat);
		sgraph=(StatGraph) findViewById(R.id.graph);
		sgraph.setOnSavingBitmap(this);
		
		new AsyncFeature().execute();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.clear();
		MenuInflater inflater=this.getMenuInflater();
		inflater.inflate(R.menu.menu_stats, menu);
		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			break;
		case R.id.action_share:
			if(shareFile==null)genBmAndShare();
			else shareImg();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void genBmAndShare() {
		// TODO Auto-generated method stub
		sgraph.saveAsBitmap();
	}

	private static class Words{
		public String name;
		public float count;
		public String cnName;
	}

	private class AsyncFeature extends AsyncTask<Void, Void, Boolean>{
		Bundle b;
		Words[] total;

		@Override
		protected Boolean doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			
			DBManager db=DBManager.getInstance();
			Cursor c=null;
			try {
				total=new Words[8];
				for(int i=0;i<total.length;i++){
					total[i]=new Words();
					total[i].name=Constant.featureKey[i];
					total[i].cnName=Constant.featureKeyChinese[i];
				}
				//检查是否有未索引过的
				c=db.getmDB().query(DBManager.MY_CLIP, new String[]{
						"_id","feature","cont","title"
				}, "feature =''", null, null, null, null);
				if(c.getCount()>0){
					for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
						String pcont=c.getString(c.getColumnIndex("cont"))+c.getString(c.getColumnIndex("title"));
						pcont=Utils.removeHtml(pcont);
						Utils.getInstance().countFeature(c.getInt(c.getColumnIndex("_id")), pcont);						
					}
				}
				c.close();
				//开始计算结果
				c=db.getmDB().query(DBManager.MY_CLIP, new String[]{
						"_id","feature","upload","cont","title"
				}, "feature !=''", null, null, null, null);

				for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
					Utils.getInstance().countFeature(c.getInt(c.getColumnIndex("_id")), 
							Utils.removeHtml(c.getString(c.getColumnIndex("cont"))+c.getString(c.getColumnIndex("title"))));
				}

				for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
					String f=c.getString(c.getColumnIndex("feature"));
					if(f==null || f.equals(""))continue;
					JSONObject json=new JSONObject(f);
					for(int i=0;i<total.length;i++){
						total[i].count+=Integer.parseInt(json.getString(total[i].name));			
					}
				}
				c.close();

				//调节参数
				for(int i=0;i<total.length;i++){
					if(total[i].name.equals(Constant.featureKey[1]) || 
							total[i].name.equals(Constant.featureKey[2]) || 
							total[i].name.equals(Constant.featureKey[4])){
						total[i].count=total[i].count/2f;
					}
				}

				DecimalFormat df = new DecimalFormat("0.00");
				b=new Bundle();
				String index="";
				float all=0;
				for(Words w:total){
					all+=w.count;
				}
				if(all==0)all=100;				

				float d=total[0].count+total[3].count*0.7f+total[4].count*0.4f+total[5].count+total[6].count+total[7].count,
						t=total[1].count+total[2].count+total[4].count*0.6f+total[3].count*0.3f;
				float result;
				if(d==0)result=1000;
				else result=t/d*100;


				String rstr=df.format(result)+"%";

				b.putString("water", rstr);

				sort();
				String words="";
				for(Words w:total){
					if(words.equals(""))words=w.cnName;
					else words=words+","+w.cnName;
				}
				b.putString("words", words);
				for(Words w:total){
					float f=w.count/all*100;
					String fstr=df.format(f);
					if(index.equals(""))index=fstr+"%";
					else index=index+","+fstr+"%";
				}
				b.putString("index", index);
				return true;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch(Exception e){
				if(c!=null)c.close();
			}
			return false;
		}

		private void sort(){
			for(int i=0;i<total.length-1;i++){
				for(int j=i+1;j<total.length;j++){
					if (total[i].count<total[j].count){
						Words temp=total[i];
						total[i]=total[j];
						total[j]=temp;
					}
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			dialogLoading.dismiss();
			if(result){
				sgraph.setVals(b);
			}else{
				Utils.getInstance().makeToast(R.string.info_data_error);
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if(dialogLoading==null)dialogLoading=new DialogLoading();
			dialogLoading.show(fm, DialogLoading.TAG);
		}


	}
	
	private void shareImg(){
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile));
		intent.setType("image/*");
		startActivity(Intent.createChooser(intent, getResources().getText(R.string.info_share_image))); 


		/*Intent it = new Intent(Intent.ACTION_SEND);
		it.setType("image/*");
		List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(it, 0);
		if (!resInfo.isEmpty()) {
			List<Intent> targetedShareIntents = new ArrayList<Intent>();
			for (ResolveInfo info : resInfo) {
				Intent targeted = new Intent(Intent.ACTION_SEND);
				targeted.setType("image/*");
				ActivityInfo activityInfo = info.activityInfo;

				if (activityInfo.packageName.contains("com.tencent.mm") 
						|| activityInfo.name.contains("com.sina.weibo") 
						|| activityInfo.name.contains("me.imid.fuubo")
						|| activityInfo.name.contains("com.weico")) {
					targeted.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile));
					targeted.setType("image/*");
					targeted.setPackage(activityInfo.packageName);
					targetedShareIntents.add(targeted);
				}
			}

			Intent chooserIntent = Intent.createChooser(targetedShareIntents.get(0),
					getResources().getText(R.string.info_share_image));
			if (chooserIntent == null) {
				Utils.getInstance().makeToast("intent is null");
				return;
			}	

			try {
				startActivity(chooserIntent);
			} catch (android.content.ActivityNotFoundException ex) {
				Utils.getInstance().makeToast("start intent failed!");
			}
		}*/
	}

	@Override
	public void onSaveStart() {
		// TODO Auto-generated method stub
		if(dialogLoading==null)dialogLoading=new DialogLoading();
		dialogLoading.show(fm, DialogLoading.TAG);
	}

	@Override
	public void onSaveEnd(File bmFile) {
		// TODO Auto-generated method stub
		if(dialogLoading!=null)dialogLoading.dismiss();
		shareFile=bmFile;

		/*Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(bmFile), "image/*");
		startActivity(intent);*/

		shareImg();

	}

	@Override
	public void onSaveFailed() {
		// TODO Auto-generated method stub
		if(dialogLoading!=null)dialogLoading.dismiss();
		Utils.getInstance().makeToast(R.string.info_fail_save_bm);

	}

}
