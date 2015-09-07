package com.taguage.whatson.siteclip.adapter;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.taguage.whatson.siteclip.ListicleActivity;
import com.taguage.whatson.siteclip.R;
import com.taguage.whatson.siteclip.utils.MLog;
import com.taguage.whatson.siteclip.utils.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListicleAdapter extends ArrayAdapter<JSONObject> implements View.OnClickListener {
	
	LayoutInflater inflater;
	Drawable d_top,d_bottom,d_edit,d_del;
	Context ctx;
	Handler handler;
	
	private static class ViewHolder{
		ImageView btn_top, btn_bottom, btn_edit, btn_del;
		TextView tv_title,tv_seq;
	}

	public ListicleAdapter(Context ctx, List<JSONObject> objects, Handler handler) {
		super(ctx, 0, objects);
		// TODO Auto-generated constructor stub
		this.ctx=ctx;
		this.handler=handler;
		inflater=LayoutInflater.from(ctx);
		d_top=Utils.getDrawableFromSvg(R.raw.btn_top, ctx);
		d_bottom=Utils.getDrawableFromSvg(R.raw.btn_bottom, ctx);
		d_edit=Utils.getDrawableFromSvg(R.raw.btn_edit, ctx);
		d_del=Utils.getDrawableFromSvg(R.raw.btn_del, ctx);
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder viewHolder;
		if(convertView==null)
		{
			viewHolder=new ViewHolder();
			convertView=inflater.inflate(R.layout.item_listicle, null);

			viewHolder.tv_title=(TextView) convertView.findViewById(R.id.tv_title);
			viewHolder.tv_seq=(TextView) convertView.findViewById(R.id.tv_seq);
			viewHolder.btn_top=(ImageView) convertView.findViewById(R.id.iv_top);
			viewHolder.btn_bottom=(ImageView) convertView.findViewById(R.id.iv_bottom);
			viewHolder.btn_edit=(ImageView) convertView.findViewById(R.id.iv_edit);
			viewHolder.btn_del=(ImageView) convertView.findViewById(R.id.iv_del);
			
			viewHolder.btn_bottom.setLayerType(View.LAYER_TYPE_SOFTWARE, null);	
			viewHolder.btn_del.setLayerType(View.LAYER_TYPE_SOFTWARE, null);	
			viewHolder.btn_edit.setLayerType(View.LAYER_TYPE_SOFTWARE, null);	
			viewHolder.btn_top.setLayerType(View.LAYER_TYPE_SOFTWARE, null);	
			
			viewHolder.btn_bottom.setImageDrawable(d_bottom);
			viewHolder.btn_del.setImageDrawable(d_del);
			viewHolder.btn_edit.setImageDrawable(d_edit);
			viewHolder.btn_top.setImageDrawable(d_top);
			
			viewHolder.btn_bottom.setOnClickListener(this);
			viewHolder.btn_del.setOnClickListener(this);
			viewHolder.btn_edit.setOnClickListener(this);
			viewHolder.btn_top.setOnClickListener(this);

			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder) convertView.getTag();
		}
		setData(position, viewHolder);
		return convertView;
	}
	
	private void setData(int position, ViewHolder viewHolder){
		JSONObject json=this.getItem(position);
		try {
			viewHolder.tv_title.setText(json.getString("title"));
			viewHolder.tv_seq.setText(""+(position+1));
			
			viewHolder.btn_bottom.setTag(position);
			viewHolder.btn_del.setTag(position);
			viewHolder.btn_edit.setTag(position);
			viewHolder.btn_top.setTag(position);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id=(Integer) v.getTag();
		JSONObject json=this.getItem(id);
		Message msg=handler.obtainMessage();
		try {
			msg.arg1=json.getInt("_id");
			switch(v.getId()){
			case R.id.iv_bottom:
				msg.what=ListicleActivity.DOWN;
				handler.sendMessage(msg);
				break;
			case R.id.iv_top:
				msg.what=ListicleActivity.UP;
				handler.sendMessage(msg);
				break;
			case R.id.iv_edit:
				msg.what=ListicleActivity.EDIT;
				handler.sendMessage(msg);
				break;
			case R.id.iv_del:
				msg.what=ListicleActivity.DEL;
				handler.sendMessage(msg);
				break;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
