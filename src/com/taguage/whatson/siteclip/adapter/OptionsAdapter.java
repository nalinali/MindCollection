package com.taguage.whatson.siteclip.adapter;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.taguage.whatson.siteclip.R;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OptionsAdapter extends ArrayAdapter<JSONObject> {

	Context ctx;
	LayoutInflater inflater;
	String version="";
	boolean isMainOption;
	
	private static class ViewHolder{
		TextView tv_title;
		TextView tv_sub;
	}
	
	public OptionsAdapter(Context context, List<JSONObject> objects, boolean isMainOption) {
		super(context, 0, objects);
		// TODO Auto-generated constructor stub
		this.isMainOption=isMainOption;
		ctx=context;
		inflater = ((Activity) context).getLayoutInflater();
		
		PackageManager pkm=context.getPackageManager();
		PackageInfo info;
		try {
			info = pkm.getPackageInfo(context.getPackageName(), 0);
			version="V"+info.versionName+" ";
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder viewHolder;
		if(convertView==null)
		{
			viewHolder=new ViewHolder();
			convertView=inflater.inflate(R.layout.item_options, null);

			viewHolder.tv_title=(TextView) convertView.findViewById(R.id.tv_title);
			viewHolder.tv_sub=(TextView) convertView.findViewById(R.id.tv_sub);

			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder) convertView.getTag();
		}
		
		setData(position,viewHolder);
		return convertView;
	}

	private void setData(int position, ViewHolder viewHolder) {
		// TODO Auto-generated method stub
		JSONObject itemObj=this.getItem(position);
		
		try {
			viewHolder.tv_title.setText(itemObj.getString("title"));
			if(position!=this.getCount()-2)viewHolder.tv_sub.setText(itemObj.getString("sub"));
			else {
				if(isMainOption)viewHolder.tv_sub.setText(version);
				else viewHolder.tv_sub.setText(itemObj.getString("sub"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
