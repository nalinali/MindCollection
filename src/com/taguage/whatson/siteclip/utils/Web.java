package com.taguage.whatson.siteclip.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.taguage.whatson.siteclip.R;
import com.taguage.whatson.siteclip.dataObj.AppContext;
import com.taguage.whatson.siteclip.dataObj.Constant;
import com.taguage.whatson.siteclip.db.DBManager;

public class Web {

	AQuery aq;
	static Web web, webpost;
	CallBack loginCallBack,postTagCallBack,refreshTokenCallBack;
	CallBack callback;

	public Web(){
		aq=new AQuery(Utils.getInstance().getCtx());
	}

	public static Web getInstance(){
		if(web==null)web=new Web();
		return web;
	}

	public void sendTaguage(String title, String cont, String token,  AjaxCallback<JSONObject> cb){		
		String url="http://api.taguage.com/tag/add";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("tag", title);
		params.put("cont", cont);

		cb.url(url);
		cb.params(params);
		cb.header("taguage-auth-token", token);
		aq.ajax(cb);
	}

	public void refreshToken(String url, Map<String, String> params, CallBack callback){
		this.refreshTokenCallBack=callback;
		aq.ajax(url, params, JSONObject.class, this,"refreshUserInfo");
	}

	public void refreshUserInfo(String url, JSONObject json, AjaxStatus status){		
		if(json!=null){
			refreshTokenCallBack.onSuccess(json);
		}else{
			refreshTokenCallBack.onFail();
		}
	}
	
	public void get(String url, AjaxCallback<JSONObject> callback){
		aq.ajax(url, JSONObject.class, callback);
	}
	

	public void requestLogin(String email, String pw, String uuid, CallBack callback){

		String url="http://api.taguage.com/account/login";
		loginCallBack=callback;

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("email", email);
		params.put("pswd", pw);
		params.put("uuid", uuid);

		aq.ajax(url, params, JSONObject.class, this,"setUserInfo");
	}

	public void setUserInfo(String url, JSONObject json, AjaxStatus status){
		if(json!=null){
			loginCallBack.onSuccess(json);
		}else{
			loginCallBack.onFail();
		}
	}
	
	private String comma(String str){
		str=str.replaceAll(",", ".");
		str=str.replaceAll("，", ".");
		return str;
	}

	public interface CallBack{
		public void onSuccess(JSONObject json);
		public void onFail();
		public void onStart();
	}

	//自己写的请求包	
	public void getTags(String token, int tid, Web.CallBack cb){
		String url="http://cloud.taguage.com/tagrecom2";
		
		DBManager db=DBManager.getInstance();
		AppContext app=(AppContext) Utils.getInstance().getCtx().getApplicationContext();
		
		Cursor c=db.getmDB().query(DBManager.MY_CLIP, new String[]{
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
		}, "_id="+tid, null, null, null, null);
		if(c.getCount()==0){
			Log.e("","数据库里没有这条记录");
			return;
		}
		
		c.moveToFirst();
		String cont=c.getString(c.getColumnIndex("title"))+c.getString(c.getColumnIndex("cont"));		
		String pretag=c.getString(c.getColumnIndex("tags"));
		String title=c.getString(c.getColumnIndex("title"));
		String source=c.getString(c.getColumnIndex("source"));
		String sourceurl=c.getString(c.getColumnIndex("sourceurl"));
		if(pretag!=null && !pretag.equals("")){
			if(!app.getSpBoolean(R.string.key_backup_source))pretag=pretag+","+source;
		}else {
			if(!app.getSpBoolean(R.string.key_backup_source))pretag=source;
			else pretag="";
		}
		
		
		c.close();
		String oricont=cont;

		cont=Utils.removeHtml(oricont);
		cont=TextUtils.htmlEncode(cont);
		cont=cont.replaceAll("\\n", "");
		cont=cont.replaceAll("\\r", "");
		cont=cont.replaceAll("&amp", "");
		cont=cont.replaceAll("&quot", "");
		cont=cont.replaceAll("&nbsp", "");
		cont=cont.replaceAll("&lt;", "");
		cont=cont.replaceAll("&gt;", "");

		if(cont.length()>1000)cont=cont.substring(0, 1000);

		this.callback=cb;
		JSONObject jsonbase=new JSONObject(),
				jsoncont=new JSONObject();
		try {
			jsonbase.put("url", url);
			jsonbase.put("pretag", pretag);
			jsonbase.put("token", token);
			jsonbase.put("title", title);
			jsonbase.put("sourceurl", sourceurl);
			jsonbase.put("ori", oricont);
			jsoncont.put("content", cont);
			JSONObject[] j=new JSONObject[]{
					jsonbase,jsoncont
			};
			new AsyncPost().execute(j);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private class AsyncPost extends AsyncTask<JSONObject, Void, JSONObject>{


		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if(callback!=null)callback.onStart();
		}

		@Override
		protected JSONObject doInBackground(JSONObject... params) {
			// TODO Auto-generated method stub
			AppContext app=(AppContext) Utils.getInstance().getCtx().getApplicationContext();
			boolean isSimpleTag=app.getSpBoolean(R.string.key_backup_simple_tag);
			JSONObject jresp=null;
			HttpPost httpPost=null;
			HttpClient client=null;
			String tag="";
			StringEntity entity=null;
			String respStr="";
			HttpResponse resp=null;
			HttpEntity respEntity=null;

			try {				
				client=new DefaultHttpClient();
				JSONObject jbase=params[0];
				
				String url=jbase.getString("url"), token=jbase.getString("token"),
						oricont=jbase.getString("ori"),pretag=jbase.getString("pretag"),
								title=jbase.getString("title"),sourceurl=jbase.getString("sourceurl");

				if(!isSimpleTag){
					httpPost=new HttpPost(url);		
					httpPost.addHeader("taguage-auth-token", token);				

					entity=new StringEntity(params[1].toString(),"UTF-8");
					entity.setContentEncoding("UTF-8");
					entity.setContentType("application/json");
					httpPost.setEntity(entity);

					resp=client.execute(httpPost);
					
					Log.e("","开始提取标签");

					if(resp.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
						respEntity=resp.getEntity();
						respStr=EntityUtils.toString(respEntity);
						JSONObject jsonTag=new JSONObject(respStr);

						Log.e("","标签---"+respStr);

						JSONArray tagarr=jsonTag.getJSONArray("list");
						int len=tagarr.length();

						for(int i=0;i<len;i++)tag=(i==0)?tagarr.getString(i):tag+","+tagarr.getString(i);
						tag=(pretag.equals(""))?tag:tag+","+comma(title)+","+pretag;
					}else{
						if(resp!=null)Log.e("","fail---"+resp.getStatusLine().getStatusCode());
						else Log.e("","fail---resp=null");
						tag=comma(title)+","+pretag;
					}
				}else tag=comma(title)+","+pretag;
				
				//如果用户设置了过滤词
				String temp=app.getSpString(R.string.key_backup_filter);
				if(!temp.equals("")){
					String[] filters=temp.split(",");
					for(String f:filters){
						tag=tag.replaceAll(f+",*", "");
					}
				}
				
				//检查重复标签
				tag=Utils.removeDuplicate(Utils.str2Arr(tag));
				
				//标签不能超过200字
				if(tag.length()>200){
					tag=tag.substring(0, 200);
					int pos=tag.lastIndexOf(",");
					if(pos>0)tag=tag.substring(0, pos);
				}
				
				//最后检查标签是否为空
				if(tag.trim().equals(""))tag="思维点藏";
				
				//添加文章来源
				oricont=oricont+"<p/><p><a href=\""+sourceurl+"\">文章来源</a></p>";
				
				//上传点博
				url="http://api.taguage.com/tag/add/";
				httpPost=new HttpPost(url);		
				httpPost.addHeader("taguage-auth-token", token);	


				boolean isPrivate=app.getSpBoolean(R.string.key_backup_private);

				List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();    
				pairList.add(new BasicNameValuePair("tag", tag));
				pairList.add(new BasicNameValuePair("cont", oricont));
				pairList.add(new BasicNameValuePair("private", isPrivate+""));
				httpPost.setEntity(new UrlEncodedFormEntity(pairList, "UTF-8"));
				
				//Log.e("","tag="+tag);
				//Log.e("",isPrivate+"");
				//Log.e("","token="+token);
				//Log.e("",oricont);
				
				if(Constant.DEBUG)FileUtils.writeFile(oricont, "uploadcont");

				resp=client.execute(httpPost);

				if(resp.getStatusLine().getStatusCode()==HttpStatus.SC_OK){						
					respEntity=resp.getEntity();
					respStr=EntityUtils.toString(respEntity);
					Log.e("","respStr="+respStr);
					jresp=new JSONObject(respStr);

				}else{
					//发点博不成功
					Log.e("","发点博失败"+resp.getStatusLine().getStatusCode());
					return null;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if(client!=null && client.getConnectionManager()!=null){
					client.getConnectionManager().shutdown();
				}
			}
			return jresp;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(result==null){
				callback.onFail();
			}else{
				callback.onSuccess(result);
			}
		}



	}


}
