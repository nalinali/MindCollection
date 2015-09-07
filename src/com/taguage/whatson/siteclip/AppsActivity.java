package com.taguage.whatson.siteclip;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.taguage.whatson.siteclip.adapter.AppsAdapter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class AppsActivity extends BaseActivity implements AdapterView.OnItemClickListener{

	private final static int TAGUAGE=0, EASYMIND=TAGUAGE+1,
			EASYFAV=EASYMIND+1,ARCHITECT=EASYFAV+1,
			DIGIPIN=ARCHITECT+1,
			POCKER=DIGIPIN+1,
			MAJIANG=POCKER+1,SELECT=MAJIANG+1;

	private String[] names, summaries;
	private final static int[] logos=new int[]{
		R.drawable.app_taguage,R.drawable.app_easymap,R.drawable.app_fav,
		R.drawable.app_arch,R.drawable.app_digi,
		R.drawable.app_poker,R.drawable.app_mj,R.drawable.app_select
	};
	private final static String[] links=new String[]{
		"http://zhushou.360.cn/detail/index/soft_id/679981",
		"http://zhushou.360.cn/detail/index/soft_id/810276",
		"",
		"http://zhushou.360.cn/detail/index/soft_id/930114",
		"http://www.wandoujia.com/apps/com.taguage.whatson.memory",
		"http://www.wandoujia.com/apps/com.taguage.whatson.poker",
		"http://www.wandoujia.com/apps/com.taguage.whatson.majiang",
		"http://app.meizu.com/apps/public/detail?package_name=com.taguage.whatson.selector"
	};
	
	AppsAdapter adapter;
	JSONObject[] arr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setActionBar(R.string.ptitle_apps);
		setContentView(R.layout.activity_apps);
		try {
			setView();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setView() throws JSONException {
		// TODO Auto-generated method stub
		ListView lv=(ListView) findViewById(R.id.lv);
		names=getResources().getStringArray(R.array.app_names);
		summaries=getResources().getStringArray(R.array.app_summaries);
		int len=names.length;
		arr=new JSONObject[len];
		for(int i=0;i<len;i++){
			JSONObject json=new JSONObject();
			json.put("name", names[i]);
			json.put("summary", summaries[i]);
			json.put("link", links[i]);
			json.put("img", logos[i]);
			arr[i]=json;
		}
		
		adapter=new AppsAdapter(this, arr);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		JSONObject json;
		try {
			if(arg2>=0){
				json=arr[arg2];
				String url=json.getString("link");
				if(url.equals(""))return;
				
				Uri uri=Uri.parse(url);
				Intent intent=new Intent(Intent.ACTION_VIEW,uri);
				startActivity(intent);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
