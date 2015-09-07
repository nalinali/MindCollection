package com.taguage.whatson.siteclip.adapter;

import com.taguage.whatson.siteclip.R;
import com.taguage.whatson.siteclip.dataObj.Constant;
import com.taguage.whatson.siteclip.db.DBManager;
import com.taguage.whatson.siteclip.utils.Utils;
import com.taguage.whatson.siteclip.view.SwipeListView;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EssayAdapter extends CursorAdapter {

	public final static int DEL=0;
	public final static int UPLOAD=1,LISTICLE=2;
	
	DBManager db=DBManager.getInstance();
	Handler handler;
	LayoutInflater inflater;
	Context ctx;
	SwipeListView slv;
	Drawable[] dr;
	int[] raw=new int[]{
			R.raw.btn_del, R.raw.btn_upload, R.raw.btn_upload_already, R.raw.btn_listicle
	};
	
	public EssayAdapter(Context ctx, Cursor c, boolean autoRequery, Handler handler) {
		super(ctx, c, autoRequery);
		// TODO Auto-generated constructor stub
		this.ctx=ctx;
		this.handler=handler;
		inflater=LayoutInflater.from(ctx);
		dr=new Drawable[raw.length];
		for(int i=0;i<dr.length;i++){
			dr[i]=Utils.getDrawableFromSvg(raw[i], ctx);
		}
	}

	@Override
	public void bindView(View view, Context ctx, final Cursor c) {
		// TODO Auto-generated method stub
		final TextView tv_title, tv_source, tv_time, tv_abstract;
		tv_title=(TextView) view.findViewById(R.id.tv_title);
		tv_source=(TextView) view.findViewById(R.id.tv_source);
		tv_time=(TextView) view.findViewById(R.id.tv_time);
		tv_abstract=(TextView) view.findViewById(R.id.tv_abstract);
		
		tv_title.setText(c.getString(c.getColumnIndex("title")));
		tv_source.setText(c.getString(c.getColumnIndex("source")));
		tv_time.setText(c.getString(c.getColumnIndex("time")));
		tv_abstract.setText(c.getString(c.getColumnIndex("abstract")));
		
		final ImageView iv_del, iv_upload, iv_listicle;
		iv_upload=(ImageView) view.findViewById(R.id.iv_upload);
		iv_upload.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		iv_upload.setTag(c.getInt(c.getColumnIndex("_id")));
		if(c.getString(c.getColumnIndex("upload")).equals(Constant.UPLOAD_ALREADY))
			iv_upload.setImageDrawable(dr[2]);
		else iv_upload.setImageDrawable(dr[1]);
		
		iv_upload.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(handler==null)return;
				Message msg=handler.obtainMessage();
				msg.what=UPLOAD;
				msg.arg1=(Integer) iv_upload.getTag();
				
				Cursor cur=db.getmDB().query(DBManager.MY_CLIP, new String[]{
						"_id","upload"
				}, "_id="+msg.arg1, null, null, null, null);
				cur.moveToFirst();
				if(cur.getCount()!=0){
					if(cur.getString(cur.getColumnIndex("upload")).equals(Constant.UPLOAD_NOT_YET))handler.sendMessage(msg);		
				}
				
				cur.close();
				
			}
		});
		
		iv_del=(ImageView) view.findViewById(R.id.iv_del);		
		iv_del.setLayerType(View.LAYER_TYPE_SOFTWARE, null);		
		iv_del.setImageDrawable(dr[0]);
		iv_del.setTag(c.getInt(c.getColumnIndex("_id")));
		iv_del.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(handler==null)return;
				Message msg=handler.obtainMessage();
				msg.what=DEL;
				msg.arg1=(Integer) iv_del.getTag();
				handler.sendMessage(msg);
			}
		});
		
		iv_listicle=(ImageView) view.findViewById(R.id.iv_listicle);
		iv_listicle.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		iv_listicle.setImageDrawable(dr[3]);
		iv_listicle.setTag(c.getInt(c.getColumnIndex("_id")));
		iv_listicle.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if(handler==null)return;
						Message msg=handler.obtainMessage();
						msg.what=LISTICLE;
						msg.arg1=(Integer) iv_del.getTag();
						handler.sendMessage(msg);
					}
				});
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.item_article, null);
	}

}
