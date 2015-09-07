package com.taguage.whatson.siteclip.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.taguage.whatson.siteclip.R;
import com.taguage.whatson.siteclip.dataObj.AppContext;
import com.taguage.whatson.siteclip.dataObj.Constant;
import com.taguage.whatson.siteclip.db.DBManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Utils {

	public final static String TAG="Utils";

	private static Toast mToast;

	public static Utils utils;

	private Context ctx;

	private int offsetY;

	public void initUtils(Context ctx)
	{
		this.ctx=ctx;
		MediaScannerWrapper.getInstance().initMediaScannerWrapper(ctx);

		AsyncCrawl ac=new AsyncCrawl(ctx);
		if(!checkDBExist())ac.initDB();
		else {
			if(updateDB())ac.initDB();
		}
		ac.initSiteInfo();
		ac.createSiteList();
		FileUtils.initialDir();
		initFeature();
		initialListicle();
	}

	public static Utils getInstance()
	{
		if(utils==null)utils=new Utils();		
		return utils;
	}
	
	public void setCtx(Context ctx){
		this.ctx=ctx;
	}
	
	private void initialListicle(){
		AppContext app=(AppContext) ctx.getApplicationContext();
		DBManager db=DBManager.getInstance();
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				
		}, null, null, null, null, null);
		int l=c.getCount();
		c.close();
		if(l==0 && !app.getSpBoolean(R.string.key_hv_initial_listicle)){
			String[] items=app.getResources().getStringArray(R.array.initial_listicles);
			int len=items.length;
			for(int i=0;i<len;i++){
				long time=System.currentTimeMillis();
				db.insertData(DBManager.LISTICLE, new String[]{
						"name","time","seq","files"
				}, new Object[]{
						items[i],time+"",i,""
				});
			}
			app.setSpBoolean(R.string.key_hv_initial_listicle, true);
		}
	}

	private boolean checkDBExist(){
		DBManager db=DBManager.getInstance();
		Cursor c=db.getmDB().rawQuery("select * from sqlite_master where type=\"table\" and name=\""+DBManager.SITE+"\"", null);
		if(c.moveToFirst()){
			c.close();
			return true;
		}
		return false;
	}

	public void makeToast(Object id)
	{
		if(ctx==null)return;
		
		String str = null;
		if(id instanceof String)
			str = (String) id;
		else
			str=ctx.getString((Integer)id);
		if(mToast == null) {  
			mToast=Toast.makeText(ctx, str, 200);
		}  
		mToast.setGravity(Gravity.CENTER, 0, offsetY);
		mToast.setText(str);
		mToast.show();

	}


	public Context getCtx()
	{
		return ctx;
	}

	private void initFeature(){
		String dict=Utils.getFromAssets("dict.txt", ctx);
		try {
			JSONObject json=new JSONObject(dict);
			Constant.feature[0]=removeDuplicate(json.getString("art"));
			Constant.feature[1]=removeDuplicate(json.getString("zhuangbility"));
			Constant.feature[2]=removeDuplicate(json.getString("stupid"));
			Constant.feature[3]=removeDuplicate(json.getString("food"));
			Constant.feature[4]=removeDuplicate(json.getString("soup"));
			Constant.feature[5]=removeDuplicate(json.getString("politics_economy"));
			Constant.feature[6]=removeDuplicate(json.getString("sophisticated"));
			Constant.feature[7]=removeDuplicate(json.getString("animation"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void countFeature(int tid, String cont){
		JSONObject json=new JSONObject();
		DBManager db=DBManager.getInstance();
		int count=0;
		for(String group:Constant.feature){
			group=group.replaceAll("，", ",");
			String[] words=group.split(",");
			int total=0;
			for(String str:words){
				Pattern p=Pattern.compile(str);
				Matcher m=p.matcher(cont);
				int k=0;
				while(m.find()){
					k++;
				}
				//if(k!=0)MLog.e("",str+"="+k);
				total+=k;
			}
			try {
				json.put(Constant.featureKey[count], total);
				count++;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		db.updateData(DBManager.MY_CLIP, "_id", tid, new String[]{
				"feature"
		}, new String[]{
				json.toString()
		});
		if(Constant.DEBUG)FileUtils.writeFile(json.toString(),"feature");
	}


	public boolean updateDB(){
		DBManager db=DBManager.getInstance();

		//channel_id
		boolean isHaveColumn=db.checkColumnExists(db.getmDB(), DBManager.SITE, "channel_id");
		if(!isHaveColumn){
			db.getmDB().execSQL("ALTER TABLE "+DBManager.SITE+" ADD COLUMN channel_id TEXT");
			Cursor cursor=db.getmDB().query(DBManager.SITE,new String[]{ "_id","channel_id"}, "channel_id IS null", null, null, null, null);
			for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
				long id=cursor.getLong(cursor.getColumnIndex("_id"));
				db.updateData(DBManager.SITE, "_id", id, 
						new String[]{"channel_id"}, new String[]{""});
			}
			cursor.close();
		}
		//priority
		isHaveColumn=db.checkColumnExists(db.getmDB(), DBManager.SITE, "priority");
		if(!isHaveColumn){
			db.getmDB().execSQL("ALTER TABLE "+DBManager.SITE+" ADD COLUMN priority TEXT");
			Cursor cursor=db.getmDB().query(DBManager.SITE,new String[]{ "_id","priority"}, "priority IS null", null, null, null, null);
			for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
				long id=cursor.getLong(cursor.getColumnIndex("_id"));
				db.updateData(DBManager.SITE, "_id", id, 
						new String[]{"priority"}, new String[]{"0"});
			}
			cursor.close();
		}
		//feature 特征词
		isHaveColumn=db.checkColumnExists(db.getmDB(), DBManager.MY_CLIP, "feature");
		if(!isHaveColumn){
			db.getmDB().execSQL("ALTER TABLE "+DBManager.MY_CLIP+" ADD COLUMN feature TEXT");
			Cursor cursor=db.getmDB().query(DBManager.MY_CLIP,new String[]{ "_id","feature"}, "feature IS null", null, null, null, null);
			for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
				long id=cursor.getLong(cursor.getColumnIndex("_id"));
				db.updateData(DBManager.MY_CLIP, "_id", id, 
						new String[]{"feature"}, new String[]{""});
			}
			cursor.close();
		}
		//wrapper
		isHaveColumn=db.checkColumnExists(db.getmDB(), DBManager.SITE, "wrapper");
		if(!isHaveColumn){
			db.getmDB().execSQL("ALTER TABLE "+DBManager.SITE+" ADD COLUMN wrapper TEXT");
			Cursor cursor=db.getmDB().query(DBManager.SITE,new String[]{ "_id","wrapper"}, "wrapper IS null", null, null, null, null);
			for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
				long id=cursor.getLong(cursor.getColumnIndex("_id"));
				db.updateData(DBManager.SITE, "_id", id, 
						new String[]{"wrapper"}, new String[]{""});
			}
			cursor.close();
		}
		//wrapper_style
		isHaveColumn=db.checkColumnExists(db.getmDB(), DBManager.SITE, "wrapper_style");
		if(!isHaveColumn){
			db.getmDB().execSQL("ALTER TABLE "+DBManager.SITE+" ADD COLUMN wrapper_style TEXT");
			Cursor cursor=db.getmDB().query(DBManager.SITE,new String[]{ "_id","wrapper_style"}, "wrapper_style IS null", null, null, null, null);
			for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
				long id=cursor.getLong(cursor.getColumnIndex("_id"));
				db.updateData(DBManager.SITE, "_id", id, 
						new String[]{"wrapper_style"}, new String[]{""});
			}
			cursor.close();
		}
		
		//user-agent
		isHaveColumn=db.checkColumnExists(db.getmDB(), DBManager.SITE, "user_agent");
		if(!isHaveColumn){
			db.getmDB().execSQL("ALTER TABLE "+DBManager.SITE+" ADD COLUMN user_agent TEXT");
			Cursor cursor=db.getmDB().query(DBManager.SITE,new String[]{ "_id","user_agent"}, "user_agent IS null", null, null, null, null);
			for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
				long id=cursor.getLong(cursor.getColumnIndex("_id"));
				db.updateData(DBManager.SITE, "_id", id, 
						new String[]{"user_agent"}, new String[]{""});
			}
			cursor.close();
		}
		
		//lid
		isHaveColumn=db.checkColumnExists(db.getmDB(), DBManager.MY_CLIP, "lid");
		if(!isHaveColumn){
			db.getmDB().execSQL("ALTER TABLE "+DBManager.MY_CLIP+" ADD COLUMN lid TEXT");
			Cursor cursor=db.getmDB().query(DBManager.MY_CLIP,new String[]{ "_id","lid"}, "lid IS null", null, null, null, null);
			for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
				long id=cursor.getLong(cursor.getColumnIndex("_id"));
				db.updateData(DBManager.MY_CLIP, "_id", id, 
						new String[]{"lid"}, new String[]{""});
			}
			cursor.close();
		}
		
		//keep_title
		isHaveColumn=db.checkColumnExists(db.getmDB(), DBManager.SITE, "keep_title");
		if(!isHaveColumn){
			db.getmDB().execSQL("ALTER TABLE "+DBManager.SITE+" ADD COLUMN keep_title TEXT");
			Cursor cursor=db.getmDB().query(DBManager.SITE,new String[]{ "_id","keep_title"}, "keep_title IS null", null, null, null, null);
			for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
				long id=cursor.getLong(cursor.getColumnIndex("_id"));
				db.updateData(DBManager.SITE, "_id", id, 
						new String[]{"keep_title"}, new String[]{""});
			}
			cursor.close();
		}

		//检查json更新		
		AppContext app=(AppContext) ctx.getApplicationContext();
		int localVer=app.getSpInt(R.string.key_data_ver);
		String crawStr=Utils.getFromAssets("siteparams.txt", ctx);
		JSONObject jsonMain;
		try {
			jsonMain = new JSONObject(crawStr);
			int ver=jsonMain.getInt("ver");
			if(localVer>=ver)return false;	

			db.getmDB().delete(DBManager.SITE, null, null);

			return true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {  

		Bitmap bitmap = Bitmap.createBitmap(  
				drawable.getIntrinsicWidth(),  
				drawable.getIntrinsicHeight(),  
				drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);  
		Canvas canvas = new Canvas(bitmap);  
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());  
		drawable.draw(canvas);  
		return bitmap;  
	}


	public Bitmap createBm(int id){
		return BitmapFactory.decodeResource(ctx.getResources(), id);
	}

	public Bitmap resizeBm(Bitmap srcBitmap, float scaleWidth,
			float scaleHeight,int maxSize) {
		int srcWidth = srcBitmap.getWidth();
		int srcHeight = srcBitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcWidth,
				srcHeight, matrix, true);
		if (resizedBitmap != null) {
			return resizedBitmap;
		} else {
			return srcBitmap;
		}

	}



	public void setToastPosY(int toastPosY) {
		// TODO Auto-generated method stub
		this.offsetY = toastPosY;
	}



	public void openBrowser(String url)
	{		
		Uri uri=Uri.parse(url);
		Intent i=new Intent(Intent.ACTION_VIEW,uri);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(i);
	}

	public static int dip2px(Context context, float dipValue){ 
		final float scale = context.getResources().getDisplayMetrics().density; 
		return (int)(dipValue * scale + 0.5f); 
	} 

	public static int px2dip(Context context, float pxValue){ 
		final float scale = context.getResources().getDisplayMetrics().density; 
		return (int)(pxValue / scale + 0.5f); 
	} 

	//get string from assets raw
	public static String getFromAssets(String fileName,Context ctx){ 
		try { 
			InputStreamReader inputReader = new InputStreamReader( ctx.getResources().getAssets().open(fileName) ); 
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line="";
			String Result="";
			while((line = bufReader.readLine()) != null)
				Result += line;
			return Result;
		} catch (Exception e) { 
			e.printStackTrace(); 
		}
		return "";
	} 

	//convert str to utf8
	public static String convert2utf8(String str){
		try {
			byte[] utf8Bytes = str.getBytes("UTF8");
			String roundTrip = new String(utf8Bytes, "UTF8");
			return roundTrip;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;
	}

	@SuppressLint("SimpleDateFormat")
	public  static String getTimeStr(){
		Calendar cld=Calendar.getInstance(TimeZone.getDefault());
		Date date=cld.getTime();
		SimpleDateFormat format=new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}


	public static void closeKeyboard(Activity ctx,EditText et){
		InputMethodManager imm = (InputMethodManager)ctx.getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(et.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@SuppressWarnings("deprecation")
	public static int getAndroidSDKVersion() {
		int version = 0;
		try {
			version = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (NumberFormatException e) {

		}
		return version;
	}

	public static void parseSvg(int source, ImageView iv, Context ctx){
		SVG svg=SVGParser.getSVGFromResource(ctx.getResources(), source);
		iv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		iv.setImageDrawable(svg.createPictureDrawable());
	}

	public static Drawable getDrawableFromSvg(int source,Context ctx){
		return SVGParser.getSVGFromResource(ctx.getResources(), source).createPictureDrawable();
	}



	public static boolean validateEmail(String email)
	{
		return Pattern.compile("^[_\\.0-9a-zA-Z+-]+@([0-9a-zA-Z]+[0-9a-zA-Z-]*\\.)+[a-zA-Z]{2,4}$").matcher(email).find();
	}

	public static String makeMD5(String s)
	{
		String result=null;

		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(s.getBytes());
			byte b[]=md.digest();

			int i;

			StringBuffer buf=new StringBuffer("");
			for(int k=0;k<b.length;k++)
			{
				i=b[k];
				if(i<0)i+=256;
				if(i<16)buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			result=buf.toString();

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return result;
	}

	public static File bitmapToFile(String dir,String fileName ,Bitmap bm,
			int quality,Context ctx)
	{   	
		String status=Environment.getExternalStorageState();
		if(status.equals(Environment.MEDIA_MOUNTED))
		{
			File d=new File(dir);
			if(!d.exists())d.mkdir();
			File file=new File(d,fileName);
			if(file.exists())file.delete(); 

			try {
				file.createNewFile();
				FileOutputStream fOut=new FileOutputStream(file);
				bm.compress(Bitmap.CompressFormat.PNG, quality, fOut);			
				fOut.flush();
				fOut.close();					

				//MediaStore.Images.Media.insertImage(ctx.getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
				//MLog.e("","path="+file.getAbsolutePath());

				//MLog.i(TAG, file.getAbsolutePath())
				//ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file)));

				return file;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}catch(OutOfMemoryError outE){
				Log.e("","out of memory when exporting image!");
				return null;
				//feedback_out_of_memory
				/*if(ctx instanceof Activity)Crouton.makeText((Activity)ctx, R.string.feedback_out_of_memory, Style.ALERT).show();
				else Utils.getInstance().makeToast( R.string.feedback_out_of_memory);*/
			}
		}else{
			/*if(ctx instanceof Activity)Crouton.makeText((Activity)ctx, R.string.feedback_no_sdcard, Style.ALERT).show();
			else Utils.getInstance().makeToast( R.string.feedback_no_sdcard);*/
		}    	
		return null;
	}

	static public String getUUID(Context ctx) {
		String uuid;
		synchronized (Utils.class) {
			final String androidId = Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID);
			try {
				if (!"9774d56d682e549c".equals(androidId)) {
					uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8")).toString()+Utils.makeMD5("selectionisimportant");
				} else {
					final String deviceId = ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
					uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")).toString() : UUID.randomUUID().toString();
					uuid=uuid+Utils.makeMD5("savefavoriteessays");
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return uuid;
	}

	public static int[] getScreenSize(Activity ac){
		DisplayMetrics dm = new DisplayMetrics();
		ac.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return new int[]{
				dm.widthPixels,
				dm.heightPixels
		};
	}

	public static String removeHtml(String htmlStr)
	{
		if(htmlStr==null)return "";

		String regEx_html="<[^>]+>";  

		htmlStr=htmlStr.replaceAll(regEx_html, "");
		return htmlStr.trim();
	}

	public static boolean isHtml(String str){
		if(str==null)return false;
		Pattern p=Pattern.compile("<[^>]+>");  
		Matcher m=p.matcher(str);
		return (m.find());
	}

	public static String removeDuplicate(String words){
		ArrayList<String> arr=new ArrayList<String>();
		String[] temp=words.split(",");
		for(String str:temp){
			arr.add(str);
		}
		return removeDuplicate(arr);
	}

	public static String removeDuplicate(ArrayList<String> tagslist){
		HashMap<String, Void> hm=new HashMap<String, Void>();
		for(String str:tagslist){
			hm.put(str, null);
		}
		Set<String> set=hm.keySet();
		if(set.isEmpty())return "";

		String tags="";
		Iterator<String> it=set.iterator();
		while(it.hasNext()){
			if(tags.equals(""))tags= it.next();
			else tags= it.next()+","+tags;
			
		}
		return tags;
	}

	public static ArrayList<String> str2Arr(String str){
		ArrayList<String> arr=new ArrayList<String>();
		if(str==null || str.equals("") || !str.contains(","))return arr;
		String[] temp=str.split(",");
		for(String s:temp)arr.add(s);

		return arr;
	}
	
	public static boolean isBackground(Context context) {
	    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
	    for (RunningAppProcessInfo appProcess : appProcesses) {
	         if (appProcess.processName.equals(context.getPackageName())) {
	                if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
	                          Log.i("后台", appProcess.processName);
	                          return true;
	                }else{
	                          Log.i("前台", appProcess.processName);
	                          return false;
	                }
	           }
	    }
	    return false;
	}


}
