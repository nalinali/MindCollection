package com.taguage.whatson.siteclip.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.taguage.whatson.siteclip.R;
import com.taguage.whatson.siteclip.adapter.LinksAdapter;
import com.taguage.whatson.siteclip.dataObj.AppContext;
import com.taguage.whatson.siteclip.dataObj.Constant;
import com.taguage.whatson.siteclip.dataObj.TargetSite;
import com.taguage.whatson.siteclip.db.DBManager;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.text.Html;
import android.text.TextUtils;
import android.util.Xml;

public class AsyncCrawl extends AsyncTask<String, Integer, Integer> {
	public static int DOUBAN_NOTE,
	WEIXIN,
	YUEHUI,
	QDAILY,
	IXIQI,
	YUEDUFM,
	BUNDPIC,
	ZHIHU_APP,
	ZHIHU_WAP,
	ZUIMEI,
	YOUSHE,
	HEXUN,
	SOHU_NEWS,
	CHUANSONGMEN,
	LOFTER,
	WEIXIN_MP,
	YOUDAO_SHARE,
	WEIBO_CARD,
	BBWC,
	SELF,
	BAIDUBAIKE,
	HUJIANG,
	BAIDU_TIEBA,
	HAIBAO,
	WEIXIN_SOGOU,
	WEIXIN_WECHAT,
	MEISHICHINA,
	HAODOU,
	MEISHIJ,
	LIEYUNWANG,
	DOUBAN_APP,
	DOUBAN_XIAOZHAN,
	REUTERS,
	ZHIHU_ZHUANLAN,
	IFENG,
	ZHONGCHOU;

	DBManager db=DBManager.getInstance();
	public static ArrayList<TargetSite> allsites=new ArrayList<TargetSite>();//顺序必须和最前面定义的常量顺序一致

	public static final int SUCCESS=0,FAIL=-1;
	public static final int YES=0,NO=-1;

	String targeturl;

	String ptitle, pcont;
	int url_source;
	Context ctx;
	Crawller crawller;
	AppContext app;

	public AsyncCrawl(Context ctx){
		//初始化使用
		this.ctx=ctx;
		app=(AppContext) ctx.getApplicationContext();
	}

	public AsyncCrawl(int us, Context ctx, Crawller crawller){
		this.url_source=us;
		this.crawller=crawller;
		this.ctx=ctx;
		app=(AppContext) ctx.getApplicationContext();
		setMode();
	}

	public void createSiteList(){
		String infoStr=Utils.getFromAssets("siteinfo.txt", ctx);
		ArrayList<JSONObject> arr=new ArrayList<JSONObject>();
		ArrayList<JSONObject> resultarr=new ArrayList<JSONObject>();
		HashMap<String,ArrayList<Integer>> keys=new HashMap<String,ArrayList<Integer>>();
		try {
			JSONObject jsonMain=new JSONObject(infoStr);

			JSONArray jarr=jsonMain.getJSONArray("list");
			int len=jarr.length();
			for(int i=0;i<len;i++){
				JSONObject json=jarr.getJSONObject(i);
				ArrayList<Integer> items;
				if(keys.containsKey(json.get("type")))items=keys.get(json.get("type"));
				else items=new ArrayList<Integer>();
				items.add(i);
				keys.put(json.getString("type"), items);
				arr.add(json);
			}

			Set<String> set=keys.keySet();
			for(String str:set){
				JSONObject title=new JSONObject();
				title.put("site", str);
				resultarr.add(title);

				ArrayList<Integer> jkeys=keys.get(str);
				for(int num:jkeys){
					JSONObject js=jarr.getJSONObject(num);
					resultarr.add(js);
				}
			}
			LinksAdapter.siteArr=resultarr;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initDB(){		
		DBManager db=DBManager.getInstance();
		AppContext app=(AppContext) ctx.getApplicationContext();
		String crawStr=Utils.getFromAssets("siteparams.txt", ctx);
		try {
			JSONObject jsonMain = new JSONObject(crawStr);			
			JSONArray jarr=jsonMain.getJSONArray("list");
			int len=jarr.length();
			for(int i=0;i<len;i++){
				JSONObject json=jarr.getJSONObject(i);
				db.insertData(DBManager.SITE, new String[]{
						"site","link_pattern","link_prefix","home_page","title_rex",
						"auth_rex","cont_rex","extra_rex","tag_rex","source","channel_id",
						"priority","wrapper","wrapper_style","user_agent","keep_title"
				}, new String[]{
						json.getString("site").trim(),
						json.getString("link_pattern").trim(),
						json.getString("link_prefix").trim(),
						json.getString("home_page").trim(),
						json.getString("title_rex").trim(),
						json.getString("auth_rex").trim(),
						json.getString("cont_rex").trim(),
						json.getString("extra_rex").trim(),
						json.getString("tag_rex").trim(),
						json.getString("source").trim(),
						json.getString("channel_id").trim(),
						json.getString("priority").trim(),
						json.getString("wrapper").trim(),
						json.getString("wrapper_style").trim(),
						json.getString("user_agent").trim(),
						json.getString("keep_title").trim()
				});
				int ver=jsonMain.getInt("ver");
				app.setSpInt(R.string.key_data_ver, ver);
				//MLog.e("","json.getString(\"channel_id\")="+json.getString("channel_id"));
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void initSiteInfo(){
		Cursor c=db.getAll(DBManager.SITE, new String[]{
				"_id","site","link_pattern","link_prefix","home_page","title_rex",
				"auth_rex","cont_rex","extra_rex","tag_rex","source",
				"channel_id","priority","wrapper","wrapper_style","user_agent","keep_title"
		});
		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
			TargetSite ts=new TargetSite();
			ts.id=c.getInt(c.getColumnIndex("_id"))-1;
			ts.site=c.getString(c.getColumnIndex("site"));
			if(!c.getString(c.getColumnIndex("link_pattern")).equals(""))
				ts.link_pattern=Pattern.compile(c.getString(c.getColumnIndex("link_pattern")));
			ts.link_prefix=c.getString(c.getColumnIndex("link_prefix"));
			ts.home_page=c.getString(c.getColumnIndex("home_page"));
			ts.title_rex=c.getString(c.getColumnIndex("title_rex"));
			ts.auth_rex=c.getString(c.getColumnIndex("auth_rex"));
			ts.cont_rex=c.getString(c.getColumnIndex("cont_rex"));
			ts.extra_rex=c.getString(c.getColumnIndex("extra_rex"));
			ts.tag_rex=c.getString(c.getColumnIndex("tag_rex"));
			ts.source=c.getString(c.getColumnIndex("source"));
			ts.channel_id=c.getString(c.getColumnIndex("channel_id"));
			ts.priority=Integer.parseInt(c.getString(c.getColumnIndex("priority")));			
			ts.wrapper=c.getString(c.getColumnIndex("wrapper"));
			ts.wrapper_style=c.getString(c.getColumnIndex("wrapper_style"));
			ts.ua=c.getString(c.getColumnIndex("user_agent"));
			ts.isDelTitle=(c.getString(c.getColumnIndex("keep_title")).equals("no"));
			allsites.add(ts);

			if(ts.channel_id.equals("douban_wap_note"))DOUBAN_NOTE=ts.id;
			else if(ts.channel_id.equals("weixin"))WEIXIN=ts.id;
			else if(ts.channel_id.equals("yhouse_wap"))YUEHUI=ts.id;
			else if(ts.channel_id.equals("qdaily_wap"))QDAILY=ts.id;
			else if(ts.channel_id.equals("ixiqi_wap"))IXIQI=ts.id;
			else if(ts.channel_id.equals("yuedu_wap"))YUEDUFM=ts.id;
			else if(ts.channel_id.equals("bundpic_wap"))BUNDPIC=ts.id;
			else if(ts.channel_id.equals("zhihu_app"))ZHIHU_APP=ts.id;
			else if(ts.channel_id.equals("zhihu_wap"))ZHIHU_WAP=ts.id;
			else if(ts.channel_id.equals("zuimeia_wap_android"))ZUIMEI=ts.id;
			else if(ts.channel_id.equals("uisdc_wap"))YOUSHE=ts.id;
			else if(ts.channel_id.equals("hexun_wap"))HEXUN=ts.id;
			else if(ts.channel_id.equals("sohu_news_wap"))SOHU_NEWS=ts.id;
			else if(ts.channel_id.equals("chuansongme_wap"))CHUANSONGMEN=ts.id;
			else if(ts.channel_id.equals("lofter"))LOFTER=ts.id;
			else if(ts.channel_id.equals("weixin_mp"))WEIXIN_MP=ts.id;
			else if(ts.channel_id.equals("youdao_share"))YOUDAO_SHARE=ts.id;
			else if(ts.channel_id.equals("weibo_opinion"))WEIBO_CARD=ts.id;
			else if(ts.channel_id.equals("bbwc_app"))BBWC=ts.id;
			else if(ts.channel_id.equals("self_wap"))SELF=ts.id;
			else if(ts.channel_id.equals("baidubaike_wap"))BAIDUBAIKE=ts.id;
			else if(ts.channel_id.equals("hujiang_app"))HUJIANG=ts.id;
			else if(ts.channel_id.equals("baidu_tieba"))BAIDU_TIEBA=ts.id;
			else if(ts.channel_id.equals("haibao_wap"))HAIBAO=ts.id;
			else if(ts.channel_id.equals("weixin_sogou"))WEIXIN_SOGOU=ts.id;
			else if(ts.channel_id.equals("weixin_wechat"))WEIXIN_WECHAT=ts.id;			
			else if(ts.channel_id.equals("meishichina_wap"))MEISHICHINA=ts.id;
			else if(ts.channel_id.equals("haodou_wap"))HAODOU=ts.id;
			else if(ts.channel_id.equals("meishij_wap"))MEISHIJ=ts.id;
			else if(ts.channel_id.equals("lieyunwang_wap"))LIEYUNWANG=ts.id;
			else if(ts.channel_id.equals("douban_app"))DOUBAN_APP=ts.id;
			else if(ts.channel_id.equals("douban_xiaozhan"))DOUBAN_XIAOZHAN=ts.id;
			else if(ts.channel_id.equals("reuters_wap"))REUTERS=ts.id;
			else if(ts.channel_id.equals("zhihu_zhuanlan"))ZHIHU_ZHUANLAN=ts.id;
			else if(ts.channel_id.equals("fenghuang_wap"))IFENG=ts.id;
			else if(ts.channel_id.equals("zhongchou_wap"))ZHONGCHOU=ts.id;
		}

		c.close();
	}

	private static boolean validPrefix(String str, String prefix){
		Pattern p=Pattern.compile(prefix);
		Matcher m=p.matcher(str);
		return m.find();
	}


	public static String extractUrl(String str){
		Matcher m=null;
		for(TargetSite site:allsites){
			if(site.link_prefix!=null && site.link_pattern!=null){
				/*MLog.e("",str);
				MLog.e("",site.channel_id+"___"+site.link_prefix);
				MLog.e("",site.link_pattern.toString());
				MLog.e("","--------------------");	*/	
				if(site.id==YUEDUFM){
					if(str.contains(site.link_prefix)){
						m=site.link_pattern.matcher(str);
						break;
					}
				}else if(validPrefix(str, site.link_prefix) && !str.equals(site.link_prefix)){
					m=site.link_pattern.matcher(str);
					//MLog.e("","link_pattern="+site.link_pattern+" str="+str);
					MLog.e("",site.link_pattern+".matcher("+str+")");
					//break;
				}
			}
		}

		if(m==null){
			MLog.e("","m=null!");
			return "";
		}
		if(m.find())return m.group();
		else {
			MLog.e("","not found!");
			return "";
		}
	}

	public static int[] verifyLink(String link,boolean isMainThread){

		for(TargetSite site:allsites){
			if(site.link_pattern.matcher(link).find())return new int[]{
					YES,site.id
			};
		}

		if(isMainThread)Utils.getInstance().makeToast(R.string.info_invalid_link);
		return new int[]{
				NO,NO
		};
	}

	private void setMode(){
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.detectDiskReads()
		.detectDiskWrites()
		.detectNetwork() // 这里可以替换为detectAll() 就包括了磁盘读写和网络I/O
		.penaltyLog() //打印logcat，当然也可以定位到dropbox，通过文件保存相应的log
		.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		.detectLeakedSqlLiteObjects() //探测SQLite数据库操作
		.penaltyLog() //打印logcat
		.penaltyDeath()
		.build()); 
	}

	public static void prepareToCrawl(String url, Context ctx, Crawller cr){
		int[] r=verifyLink(url, true);
		//MLog.e("","background="+r[0]+"----"+r[1]+"   wc="+AsyncCrawl.WEIBO_CARD);
		if(r[0]==AsyncCrawl.YES){
			AsyncCrawl crawl=new AsyncCrawl(r[1], ctx, cr);
			if(r[1]==AsyncCrawl.WEIBO_CARD)crawl.specialCrawWeiboCard(url);
			else if(r[1]==AsyncCrawl.ZHIHU_ZHUANLAN){
				crawl.specialCrawForZhihuZhuanlan(url);
			}
			else if(url.contains("wapbaike")){
				url=url.replace("wapbaike", "baike");
				crawl.execute(url);	
			}
			else crawl.execute(url);	
		}
	}

	@Override
	protected Integer doInBackground(String... params) {
		// TODO Auto-generated method stub

		pcont="";
		ptitle="";
		int r=FAIL;

		targeturl=params[0];
		TargetSite ts=allsites.get(url_source);

		if(url_source==YOUDAO_SHARE){
			String sample="http://note.youdao.com/yws/public/note/ID?keyfrom=public";
			targeturl=targeturl.replace("http://note.youdao.com/share/?id=", "");
			String[] temp=targeturl.split("&");
			targeturl=sample.replace("ID", temp[0]);
			r=ts.crawl(SUCCESS, FAIL, targeturl, false);
		}
		else if(url_source==BBWC){
			r=ts.crawBBWC(SUCCESS, FAIL, targeturl);
		}
		else if(url_source==SELF){
			int pos=targeturl.lastIndexOf(".html");
			if(pos!=-1)targeturl=targeturl.substring(0, pos)+".full.html";
			r=ts.crawl(SUCCESS, FAIL, targeturl, false);
		}
		else if(url_source==DOUBAN_APP){
			r=ts.crawDoubanApp(SUCCESS, FAIL, targeturl, allsites.get(DOUBAN_XIAOZHAN), allsites.get(DOUBAN_NOTE));
		}
		else if(url_source==IFENG){
			if(targeturl.indexOf("all=1")==-1)targeturl=targeturl+"&all=1";
			r=ts.crawl(SUCCESS, FAIL, targeturl, false);
		}
		else r=ts.crawl(SUCCESS, FAIL, targeturl, false);

		pcont=ts.resultCont;
		ptitle=ts.resultTitle;
		
		if(ts.isDelTitle)pcont=pcont.replace(ptitle, "");

		pcont=ts.reviseBase(pcont);
		pcont=pcont.replaceAll("<html>", "");
		pcont=pcont.replaceAll("</html>", "");
		pcont=pcont.replaceAll("<p></p>", "");
		pcont=pcont.replaceAll("<div></div>", "");
		pcont=pcont.replaceAll("(<br>){2,}", "<br>");
		pcont=pcont.replaceAll("(<br/>){2,}", "<br/>");

		if(!ts.wrapper.equals("")){
			if(ts.wrapper.equals("table"))pcont="<"+ts.wrapper+" cellpadding=\"0\" cellspacing=\"0\""
					+" style=\""+ts.wrapper_style+"\">"+pcont+"</"+ts.wrapper+">";
			else pcont="<"+ts.wrapper+" style=\""+ts.wrapper_style+"\">"+pcont+"</"+ts.wrapper+">";
		}

		if(ts.id==WEIXIN || ts.id == WEIXIN_MP || ts.id == WEIXIN_SOGOU  || ts.id == WEIXIN_WECHAT)
			pcont=ts.reviseImgForWX(pcont);
		else if(ts.id==YUEHUI)pcont=ts.reviseImgForYuehui(pcont);
		else if(ts.id==QDAILY)pcont=ts.reviseImgForQdaily(pcont);
		else if(ts.id==IXIQI)pcont=ts.reviseImgForIxiqi(pcont);
		else if(ts.id==BUNDPIC)pcont=ts.reviseImgForBundpic(pcont);
		else if(ts.id==HEXUN)pcont=ts.reviseImgForHexun(pcont);
		else if(ts.id==ZHIHU_APP || ts.id==ZHIHU_WAP)pcont=ts.reviseImgForZhiHuApp(pcont);
		else if(ts.id==ZUIMEI)pcont=ts.reviseImgForZuiMei(pcont);
		else if(ts.id==SOHU_NEWS)pcont=ts.reviseImgForSohuNews(pcont);
		else if(ts.id==CHUANSONGMEN)pcont=ts.reviseImgForChuansongmen(pcont);
		else if(ts.id==LOFTER)pcont=ts.getRealHtmlForLofter(pcont);
		else if(ts.id==YOUDAO_SHARE){
			String[] temp=ts.combineForYoudao(pcont);
			pcont=temp[1];
			ptitle=temp[0];
		}
		else if(ts.id==SELF)pcont=ts.reviseImgForSelf(pcont);
		else if(ts.id==BAIDUBAIKE)pcont=ts.reviseContForBaiduBaike(pcont);
		else if(ts.id==HUJIANG)pcont=ts.reviseContForHujiang(pcont);
		else if(ts.id==BAIDU_TIEBA)pcont=ts.reviseContForTieba(pcont);
		else if(ts.id==HAIBAO)pcont=ts.reviseContForHaibao(pcont);
		else if(ts.id==MEISHICHINA)pcont=ts.reviseContForMeishiChina(pcont);
		else if(ts.id==HAODOU)pcont=ts.reviseContForHaoDou(pcont);
		else if(ts.id==MEISHIJ)pcont=ts.reviseImgForMeishij(pcont);
		else if(ts.id==LIEYUNWANG)pcont=ts.reviseContForLieyunwang(pcont);
		else if(ts.id==REUTERS)pcont=ts.reviseContForReuters(pcont);
		else if(ts.id==IFENG)pcont=ts.reviseImgForFenghuang(pcont);
		else if(ts.id==ZHONGCHOU)pcont=ts.reviseImgForZhongcou(pcont);

		if(Constant.DEBUG)FileUtils.writeFile(pcont, "after");

		return r;
	}
	@Override
	protected void onPostExecute(Integer result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if(pcont==null){
			crawller.onError();
			return;
		}

		if(result==SUCCESS){
			saveToDB(allsites.get(url_source).source,targeturl,null,null);			
		}else{
			crawller.onError();
		}
	}

	public void saveToDB(String sourceStr, String targeturl, String title, String cont){
		if(title!=null)ptitle=title;
		if(cont!=null)pcont=cont;

		pcont=pcont.replaceAll("<script[^>]*>[\\s\\S]+?</script>","");
		pcont=pcont.replaceAll("<style[^>]*>[\\s\\S]+?</style>","");
		ptitle=Html.fromHtml(ptitle).toString();

		DBManager db=DBManager.getInstance();
		db=DBManager.getInstance();
		db.insertData(DBManager.MY_CLIP, new String[]{
				"source",
				"sourceurl",
				"title",
				"cont",
				"abstract",
				"folder",
				"tags",
				"time",
				"star",
				"upload",
				"lid"
		}, new String[]{
				sourceStr,
				targeturl,
				Utils.removeHtml(ptitle),
				pcont, 
				genAbs(),
				Constant.defaultFolder,
				extractTags(),
				Utils.getTimeStr(),
				Constant.STAR_NO,
				Constant.UPLOAD_NOT_YET,
				""
		});

		Cursor c=db.getAll(DBManager.MY_CLIP, new String[]{
				"_id"
		});
		c.moveToLast();
		int tid=c.getInt(c.getColumnIndex("_id"));
		c.close();

		Utils.getInstance().countFeature(tid, Utils.removeHtml(pcont));

		crawller.onFinished(ptitle, pcont, tid);
	}

	private String extractTags(){
		if(app.getSpBoolean(R.string.key_backup_simple_tag))return "";

		ArrayList<String> tagslist=new ArrayList<String>();
		String[] rex=new String[]{
				"《[^《]+》",
				"(?<=\\[)[\\u4E00-\\u9FA5a-zA-Z“”\"]{2,10}(?=\\])",
				"(?<=【)[\\u4E00-\\u9FA5a-zA-Z“”\"]{2,10}(?=】)",
				"(?<=(打造))[\\u4E00-\\u9FA5a-zA-Z“”\"a-zA-Z0-9]{2,10}秘诀",
				"(?<=(打造))[\\u4E00-\\u9FA5a-zA-Z“”\"a-zA-Z0-9]{2,10}方法",
				"(?<=(<p>))[\\u4E00-\\u9FA5a-zA-Z“”\"]{2,10}(?=(</p>))",
				"(?<=(<strong>))[\\u4E00-\\u9FA5a-zA-Z“”\"]{2,10}(?=(</strong>))",
				"(?<=(<div>))[\\u4E00-\\u9FA5a-zA-Z“”\"]{2,10}(?=(</div>))"
		};
		for(String re:rex){
			Pattern p=Pattern.compile(re);
			Matcher m1=p.matcher(ptitle),
					m2=p.matcher(pcont);
			while(m1.find()){
				String temp1=m1.group();
				temp1=Utils.removeHtml(temp1);
				tagslist.add(comma(temp1));
			}
			int count=0;
			while(m2.find()){
				if(url_source==YOUSHE){
					if(count==0)continue;
				}

				String temp2=m2.group();
				temp2=comma(temp2);
				temp2=Utils.removeHtml(temp2);
				tagslist.add(comma(temp2));
				count++;
			}	
		}

		return Utils.removeDuplicate(tagslist);
	}	


	private String comma(String str){
		str=str.replaceAll(",", ".");
		str=str.replaceAll("，", ".");
		return str;
	}

	private String genAbs(){
		String temp=Utils.removeHtml(pcont);
		temp=temp.replaceAll("\\n", "");
		temp=temp.replaceAll("\\s{2,}", "");
		temp=temp.replaceAll("&nbsp;", " ");
		if(temp.length()<=80)return temp;
		else return temp.substring(0,80)+"...";
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		crawller.onCrawling();
	}	

	public void specialCrawForZhihuZhuanlan(String url){
		pcont="";
		ptitle="";

		targeturl=url;
		final TargetSite ts=allsites.get(url_source);
		
		Pattern pa=Pattern.compile("http://zhuanlan\\.zhihu\\.com/[a-zA-Z0-9-_]+/\\d+");
		Matcher mr=pa.matcher(targeturl);
		if(mr.find()){
			String tmp=mr.group();
			int s=tmp.lastIndexOf("/"),e=tmp.length();
			String num=tmp.substring(s+1, e);
			String name=targeturl.replace("http://zhuanlan.zhihu.com/", "");
			int ne=name.indexOf("/");
			name=name.substring(0, ne);
			String nurl="http://zhuanlan.zhihu.com/api/columns/"+name+"/posts/"+num;
			crawller.onCrawling();
			Web.getInstance().get(nurl, new AjaxCallback<JSONObject>(){

				@Override
				public void callback(String url, JSONObject json,
						AjaxStatus status) {
					// TODO Auto-generated method stub
					super.callback(url, json, status);
					try {
						ptitle=json.getString("title");
						pcont=json.getString("content");
						saveToDB(ts.source, url, ptitle, pcont);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						crawller.onError();
					}
				}

				@Override
				public void failure(int code, String message) {
					// TODO Auto-generated method stub
					super.failure(code, message);
					crawller.onError();
				}

			});
		}else {
			crawller.onError();
			MLog.e("","no match");
		}

	}

	public void specialCrawWeiboCard(String url){
		pcont="";
		ptitle="";

		targeturl=url;
		final TargetSite ts=allsites.get(url_source);
		Pattern p=Pattern.compile("(?<=(cid=))[0-9a-zA-Z]+");
		Matcher m=p.matcher(url);
		if(!m.find()){
			crawller.onError();
			return;
		}

		crawller.onCrawling();
		url="http://card.weibo.com/article/aj/articleshow?cid="+m.group();		
		Web.getInstance().get(url, new AjaxCallback<JSONObject>(){

			@Override
			public void callback(String url, JSONObject object,
					AjaxStatus status) {
				// TODO Auto-generated method stub
				super.callback(url, object, status);
				//MLog.e("","status="+status.getCode()+" object="+object);
				//if(Constant.DEBUG)FileUtils.writeFile(object.toString(), "weibocard");
				try {
					JSONObject data=(JSONObject) object.get("data");
					String resultTitle=data.getString("title");
					String resultCont=data.getString("article");
					saveToDB(ts.source, url, resultTitle, resultCont);
					MLog.e("",resultCont);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					crawller.onError();
				}
			}

			@Override
			public void failure(int code, String message) {
				// TODO Auto-generated method stub
				super.failure(code, message);
				MLog.e("","code="+code+" message="+message);
				crawller.onError();
			}

		});

	}



	public interface Crawller{
		public void onCrawling();
		public void onFinished(String ptitle, String pcont, int tid);		
		public void onError();
	}

}
