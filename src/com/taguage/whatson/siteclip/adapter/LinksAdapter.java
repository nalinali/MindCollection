package com.taguage.whatson.siteclip.adapter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.taguage.whatson.siteclip.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LinksAdapter extends ArrayAdapter<JSONObject> {
	
	public static ArrayList<JSONObject> siteArr;

	Context ctx;
	LayoutInflater inflater;
	
	private static class ViewHolder{
		public TextView tv_site, tv_url,tv_channel;
	}
	
	public LinksAdapter(Context context, List<JSONObject> objects) {
		super(context, 0, objects);
		// TODO Auto-generated constructor stub
		this.ctx=context;
		inflater=LayoutInflater.from(context);		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder viewHolder;
		if(convertView==null)
		{
			viewHolder=new ViewHolder();
			convertView=inflater.inflate(R.layout.item_supported_site, null);

			viewHolder.tv_site=(TextView) convertView.findViewById(R.id.tv_site);
			viewHolder.tv_url=(TextView) convertView.findViewById(R.id.tv_url);
			viewHolder.tv_channel=(TextView) convertView.findViewById(R.id.tv_channel);

			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder) convertView.getTag();
		}
		
		setData(position,viewHolder);
		return convertView;
	}

	private void setData(int position, ViewHolder viewHolder) {
		// TODO Auto-generated method stub
		JSONObject json=this.getItem(position);
		try {
			if(json.has("url")){
				viewHolder.tv_site.setText(json.getString("site"));
				viewHolder.tv_url.setText(json.getString("url"));			
				viewHolder.tv_channel.setVisibility(View.GONE);
				viewHolder.tv_site.setVisibility(View.VISIBLE);
				viewHolder.tv_url.setVisibility(View.VISIBLE);
			}else{
				viewHolder.tv_channel.setText(json.getString("site"));
				viewHolder.tv_channel.setVisibility(View.VISIBLE);
				viewHolder.tv_site.setVisibility(View.GONE);
				viewHolder.tv_url.setVisibility(View.GONE);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
