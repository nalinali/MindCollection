package com.taguage.whatson.siteclip.adapter;

import org.json.JSONException;
import org.json.JSONObject;

import com.taguage.whatson.siteclip.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppsAdapter extends ArrayAdapter<JSONObject> {

	LayoutInflater inflater;
	
	private static class ViewHolder{
		ImageView iv_app;
		TextView tv_name, tv_summary;
	}
	
	public AppsAdapter(Context context, JSONObject[] objects) {
		super(context, 0, objects);
		// TODO Auto-generated constructor stub
		inflater=LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder viewHolder;
		if(convertView==null)
		{
			viewHolder=new ViewHolder();
			convertView=inflater.inflate(R.layout.item_rec_apps, null);

			viewHolder.tv_name=(TextView) convertView.findViewById(R.id.tv_name);
			viewHolder.tv_summary=(TextView) convertView.findViewById(R.id.tv_summary);
			viewHolder.iv_app=(ImageView) convertView.findViewById(R.id.iv_app);

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
			viewHolder.tv_name.setText(json.getString("name"));
			viewHolder.tv_summary.setText(json.getString("summary"));
			viewHolder.iv_app.setImageResource(json.getInt("img"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}
