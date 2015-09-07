package com.taguage.whatson.siteclip.dataObj;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.text.Html;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.taguage.whatson.siteclip.utils.AsyncCrawl;
import com.taguage.whatson.siteclip.utils.FileUtils;
import com.taguage.whatson.siteclip.utils.MLog;
import com.taguage.whatson.siteclip.utils.Web;

public class TargetSite {


	public int id;
	public String site;
	public String tag_rex;
	public Pattern link_pattern;
	public String link_prefix;
	public String home_page;
	public String title_rex;
	public String cont_rex;
	public String auth_rex;
	public int cont_len;
	public String source;
	public String channel_id;
	public int priority;
	public String wrapper;
	public String wrapper_style;

	public String extra_rex;
	public String pre_tag="";
	public String ua="";
	public boolean isDelTitle;

	public String resultTitle, resultCont;

	private static String UA="Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1650.63 Safari/537.36";

	public int crawl(int ok, int fail, String url, boolean isShort){
		if(!ua.equals("") && ua!=null)UA=ua;
		try {
			Document doc;
			if(isShort){
				Response resp=Jsoup.connect(url).userAgent(UA).followRedirects(true).execute();
				doc=resp.parse();
			}else{
				doc=Jsoup.connect(url).userAgent(UA).timeout(10000).get();
			}
			resultTitle=resultCont="";

			/*MLog.e("","title_rex="+title_rex);
			MLog.e("","cont_rex="+cont_rex);
			MLog.e("","auth_rex="+auth_rex);
			MLog.e("","extra_rex="+extra_rex);
			MLog.e("","source="+source+" url="+url);*/

			if(cont_rex.contains(" ")){
				String ctemp=cont_rex.trim();
				String[] cgp=ctemp.split(" ");
				if(cgp[1].equals("all")){
					cont_len=-1;
					cont_rex=cgp[0];
				}
			}

			Elements eletitle=doc.select(this.title_rex),
					eleauth=null,
					elecont=doc.select(this.cont_rex),
					eleextra=null;



			if(Constant.DEBUG)FileUtils.writeFile(doc.html(), "clip");

			if(!auth_rex.equals(""))eleauth=doc.select(this.auth_rex);
			if(!extra_rex.equals(""))eleextra=doc.select(this.extra_rex);

			if(eletitle.size()>0 ){
				resultTitle=eletitle.get(0).html();
				if(elecont.size()>0){
					elecont=addStyleForTable(elecont);
					if(cont_len==-1){
						for(Element ele:elecont){
							resultCont=resultCont+ele.html();
						}
					}else resultCont=elecont.get(0).html();
				}
				if(!auth_rex.equals("")){
					if(eleauth.size()>0)resultCont="<p>"+eleauth.get(0).html()+"</p>"+resultCont;
				}
				if(!extra_rex.equals("")){
					eleextra=addStyleForTable(eleextra);
					if(eleextra.size()>0)resultCont=resultCont+eleextra.get(0).html();
				}				

				return ok;
			}else{
				MLog.e("","没有匹配到title");
				return fail;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			MLog.e("","没有请求到数据");
			return fail;
		}
	}

	public int crawDoubanApp(int ok, int fail, String url, TargetSite tZhan, TargetSite tNote){
		String fstr="dispatch?uri=";
		int p=url.indexOf(fstr);
		String link="http://www.douban.com"+url.substring(p+fstr.length(), url.length());
		MLog.e("", url);
		MLog.e("", link);

		int[] r=AsyncCrawl.verifyLink(link, false);
		if(r[0]==AsyncCrawl.YES){
			if(r[1]==AsyncCrawl.DOUBAN_NOTE){
				title_rex=tNote.title_rex;
				cont_rex=tNote.cont_rex;
				int re=crawl(ok, fail,link, false);
				return re;
			}else if(r[1]==AsyncCrawl.DOUBAN_XIAOZHAN){
				title_rex=tZhan.title_rex;
				cont_rex=tZhan.cont_rex;
				return crawl(ok, fail, link, false);
			}
		}

		return fail;
	}

	public int crawBBWC(int ok, int fail, String url){

		try {
			Document doc=Jsoup.connect(url).userAgent(UA).timeout(3000).get();
			Elements frame=doc.select("iframe#verticalContent");
			if(frame.size()>0){
				url=frame.attr("src");
			}

			doc=Jsoup.connect(url).userAgent(UA).timeout(3000).get();
			resultTitle=resultCont="";

			//处理图片链接
			Pattern p=Pattern.compile("issue_\\d+/articles/\\d+");
			Matcher m=p.matcher(url);
			if(m.find()){
				String pre="http://s4.cdn.bb.bbwc.cn/"+m.group();
				Elements imgs=doc.select("img");
				if(imgs.size()>0){
					for(Element img:imgs){
						String raw=img.attr("data-src");
						raw=raw.replace("uploadfile", pre);
						img.attr("src",raw);
					}
				}
			}

			//开始提取
			Elements eletitle=doc.select(this.title_rex),
					eleauth=null,
					elecont=doc.select(this.cont_rex),
					eleextra=null;

			if(Constant.DEBUG)FileUtils.writeFile(doc.html(), "clip");

			if(!auth_rex.equals(""))eleauth=doc.select(this.auth_rex);
			if(!extra_rex.equals(""))eleextra=doc.select(this.extra_rex);

			if(eletitle.size()>0 ){
				resultTitle=eletitle.get(0).html();
				if(elecont.size()>0){
					elecont=addStyleForTable(elecont);
					resultCont=elecont.get(0).html();
				}
				if(!auth_rex.equals("")){
					if(eleauth.size()>0)resultCont="<p>"+eleauth.get(0).html()+"</p>"+resultCont;
				}
				if(!extra_rex.equals("")){
					eleextra=addStyleForTable(eleextra);
					if(eleextra.size()>0)resultCont=resultCont+eleextra.get(0).html();
				}				

				return ok;
			}else{
				MLog.e("","没有匹配到title");
				return fail;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fail;
	}



	private Elements addStyleForTable(Elements pcont){

		Elements td=pcont.select("td");
		for(Element d:td){
			d.attr("style", "border: 1px solid #aaa;width:auto");
			d.removeAttr("class");
		}
		return pcont;
	}

	public String reviseBase(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eles=doc.select("form");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("input");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("textarea");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("link");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("head");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("noscript");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("p");
		for(Element ele:eles){
			ele.removeAttr("class");
			ele.removeAttr("id");
			if(ele.text().equals("")){
				String s=ele.html();
				if(!s.contains("<img"))ele.remove();
			}
		}
		eles=doc.select("div");
		for(Element ele:eles){
			ele.removeAttr("class");
			ele.removeAttr("id");
			if(ele.text().equals("")){
				String s=ele.html();
				if(!s.contains("<img"))ele.remove();
			}
		}
		eles=doc.select("a");
		for(Element ele:eles){
			ele.removeAttr("class");
			ele.removeAttr("id");
			if(ele.text().equals("")){
				String s=ele.html();
				if(!s.contains("<img"))ele.remove();
			}
		}
		eles=doc.select("section");
		for(Element ele:eles){
			ele.removeAttr("class");
			ele.removeAttr("id");
			if(ele.text().equals("")){
				String s=ele.html();
				if(!s.contains("<img"))ele.remove();
			}
		}
		eles=doc.select("h1");
		for(Element ele:eles){
			ele.removeAttr("class");
			ele.removeAttr("id");
			if(ele.text().equals("")){
				String s=ele.html();
				if(!s.contains("<img"))ele.remove();
			}
		}
		eles=doc.select("h2");
		for(Element ele:eles){
			ele.removeAttr("class");
			ele.removeAttr("id");
			if(ele.text().equals("")){
				String s=ele.html();
				if(!s.contains("<img"))ele.remove();
			}
		}



		return doc.html();
	}


	public String reviseContForLieyunwang(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eles=doc.select("div#share-box");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div[id^=BAIDU]");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("iframe[id^=360_HOT]");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.n_article");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div#comment-box");
		for(Element ele:eles){
			ele.remove();
		}

		return doc.html();
	}

	public String reviseContForHaibao(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eles=doc.select("div.hbh5_page");
		for(Element ele:eles){
			ele.remove();
		}

		return doc.html();
	}

	public String reviseContForTieba(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eles=doc.select("div.BAIDU_CLB_AD");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("ul.p_mtail");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("ul.p_props_tail");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.thread_recommend");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.j_lzl_container");
		for(Element ele:eles){
			ele.remove();
		}
		return doc.html();
	}

	public String reviseContForHaoDou(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eles=doc.select("div[id^=BAIDU]");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.topbar");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div[class*=header]");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div#loading");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.app_btnbox");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.adpic");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div#Adc");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.footer");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("fieldset");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("script");
		for(Element ele:eles){
			ele.remove();
		}
		return doc.html();
	}

	public String reviseContForReuters(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eles=doc.select("div.footer");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.lnk");
		for(Element ele:eles){
			ele.remove();
		}
		return doc.html();
	}

	public String reviseContForMeishiChina(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eles=doc.select("div[id^=BAIDU]");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.topbar");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.header");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.flist");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.s2");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.tag_con_a");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.ca");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.ibox2");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.footer");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("fieldset");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("p.ca_r");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div.ibox>a>img");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("script");
		for(Element ele:eles){
			ele.remove();
		}
		return doc.html();
	}

	public String reviseContForHujiang(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eles=doc.select("div#article_app");
		for(Element ele:eles){
			ele.remove();
		}
		eles=doc.select("div#div_tab_shuangyu");
		for(Element ele:eles){
			ele.remove();
		}
		return doc.html();
	}

	public String reviseContForBaiduBaike(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eles=doc.select("a#lemma-edit");
		for(Element ele:eles){
			ele.parent().remove();
		}
		eles=doc.select("div#collectBtn");
		for(Element ele:eles){
			ele.parent().remove();
		}
		return doc.html();
	}

	public String[] combineForYoudao(String pcont){
		String[] r=new String[]{
				"",""
		};
		r[0]=getStartAndEnd("\"tl\":\"","\",\"mt\"", pcont);
		r[1]=getStartAndEnd("\"content\":\"",",\"tl\":",pcont);

		/*try {
			JSONObject json=new JSONObject(pcont);
			r[0]=json.getString("tl");
			r[1]=json.getString("content");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return r;
	}

	private String getStartAndEnd(String st, String en, String raw){
		int s=raw.indexOf(st), e=raw.indexOf(en);
		if(s==-1 || e==-1 || s>=e)return "";		
		return raw.substring(s+st.length(), e);
	}

	public String reviseImgForChuansongmen(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eleimages=doc.select("img");
		for(Element img:eleimages){
			String source=img.attr("data-src");
			if(!source.equals(""))img.attr("src", source);
		}
		return doc.html();
	}
	
	public String reviseImgForSelf(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eleimages=doc.select("img");
		for(Element img:eleimages){
			String source=img.attr("data-src");
			if(!source.equals(""))img.attr("src", source);
		}
		return doc.html();
	}

	public String getRealHtmlForLofter(String pcont){
		pcont=Html.fromHtml(pcont).toString();		
		return pcont;
	}

	public String reviseImgForMeishij(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eleimages=doc.select("img");
		for(Element img:eleimages){
			Attributes attrs=img.attributes();
			String source=attrs.get("data-original");
			if(!source.equals(""))img.attr("src", source);
		}
		return doc.html();
	}

	public String reviseImgForZuiMei(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eleimages=doc.select("img");
		for(Element img:eleimages){
			Attributes attrs=img.attributes();
			String source=attrs.get("data-original");
			img.attr("src", source);
		}
		return doc.html();
	}

	public String reviseImgForZhiHuApp(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements noeles=doc.select("noscript");
		for(Element no:noeles){
			Elements eleimages=no.getElementsByTag("img");
			for(Element img:eleimages){
				Attributes attrs=img.attributes();
				String source=attrs.get("src");
				img.parent().before("<img src=\""+source+"\" />");
			}
			no.remove();
		}
		Elements eleimages=doc.select("img");
		for(Element img:eleimages){
			String source=img.attr("data-original"),s2=img.attr("data-actualsrc");
			if(!source.equals(""))img.attr("src", source);
			if(!s2.equals(""))img.attr("src",s2);
		}
		return doc.html();
	}
	
	public String reviseImgForZhongcou(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eleimages=doc.select("img");
		for(Element img:eleimages){
			String source=img.attr("data-src");
			img.attr("src", source);
		}
		return doc.html();
	}
	
	public String reviseImgForFenghuang(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eleimages=doc.select("img");
		for(Element img:eleimages){
			String source=img.attr("original");
			img.attr("src", source);
		}
		return doc.html();
	}

	public String reviseImgForHexun(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eleimages=doc.select("img");
		for(Element img:eleimages){
			String source=img.attr("original");
			img.attr("src", source);
		}
		return doc.html();
	}

	public String reviseImgForSohuNews(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eleimages=doc.select("img");
		for(Element img:eleimages){
			Attributes attrs=img.attributes();
			if(attrs.hasKey("data-src")){
				String source=attrs.get("data-src");
				img.attr("src", source);				
			}
		}
		return doc.html();
	}

	public String reviseImgForBundpic(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eleimages=doc.select("div#list_image_div>img"),
				divs=doc.select("div#list_image_div");

		if(divs.size()>0 && eleimages.size()>0){
			eleimages.get(0).removeAttr("style");
			divs.get(0).removeAttr("style");
		}


		return doc.html();
	}

	public String reviseImgForIxiqi(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eleimages=doc.select("img");
		for(Element img:eleimages){
			Attributes attrs=img.attributes();
			String source=attrs.get("data-original");
			img.attr("src", source);
		}
		return doc.html();
	}

	public String reviseImgForQdaily(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eleimages=doc.select("img");
		for(Element img:eleimages){
			Attributes attrs=img.attributes();
			String source=attrs.get("src");
			img.attr("src", "http://qdaily.com/"+source);
		}
		return doc.html();
	}

	public String reviseImgForYuehui(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eleimages=doc.select("input[name=\"hiddenimg\"]");
		if(eleimages.size()>0){
			for(Element img:eleimages){
				Attributes attrs=img.attributes();
				String source=attrs.get("value");
				img.parent().before("<img src=\""+source+"\" />");
			}
		}
		return doc.html();
	}

	public String reviseImgForWX(String pcont){
		if(pcont==null)return "";

		Document doc=Jsoup.parse(pcont);
		Elements eleimages=doc.select("img");
		if(eleimages.size()>0){
			for(Element img:eleimages){
				String source=img.attr("data-src");
				int pos=source.lastIndexOf("/")+1;
				source=source.substring(0, pos);
				img.removeAttr("data-s");
				img.removeAttr("data-src");
				img.removeAttr("data-w");
				img.attr("src", source+"640");		
				img.attr("max-width","640");
			}
		}		
		Elements elesrp=doc.select("script");
		Elements divs=doc.select("div");
		if(elesrp.size()>0 && divs.size()>0){

			for(Element ele:elesrp){
				String s=ele.html();
				Pattern p=Pattern.compile("(?<=(var\\scover\\s=\\s\"))\\S+(?=\")");
				Matcher m=p.matcher(s);
				if(m.find()){
					String nimg="<img src=\""+m.group()+"\"/>";
					divs.get(0).before(nimg);
				}
			}
		}
		return doc.html();
	}
}
