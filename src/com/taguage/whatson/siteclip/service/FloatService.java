package com.taguage.whatson.siteclip.service;

import com.taguage.whatson.siteclip.R;
import com.taguage.whatson.siteclip.StartActivity;
import com.taguage.whatson.siteclip.utils.MLog;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class FloatService extends Service implements View.OnClickListener, View.OnTouchListener {

	//定义浮动窗口布局  
    LinearLayout mFloatLayout;  
    WindowManager.LayoutParams wmParams;  
    //创建浮动窗口设置布局参数的对象  
    WindowManager mWindowManager; 
    
    ImageView iv;
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		createFloatView();
	}



	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mFloatLayout != null)  
        {  
            //移除悬浮窗口  
            mWindowManager.removeView(mFloatLayout);  
        } 
	}

	private void createFloatView(){
		MLog.e("","-------start float service-------");
		wmParams = new WindowManager.LayoutParams();  
        //获取的是WindowManagerImpl.CompatModeWrapper  
        mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);  
        //设置window type  
        wmParams.type = LayoutParams.TYPE_PHONE;   
        //设置图片格式，效果为背景透明  
        wmParams.format = PixelFormat.RGBA_8888;   
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）  
        wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;        
        //调整悬浮窗显示的停靠位置为左侧置顶  
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;         
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity  
        wmParams.x = 0;  
        wmParams.y = 300;  
  
         // 设置悬浮窗口长宽数据 
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        
        LayoutInflater inflater = LayoutInflater.from(getApplication());  
        //获取浮动窗口视图所在布局  
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_get_clipboard, null);  
        //添加mFloatLayout  
        mWindowManager.addView(mFloatLayout, wmParams);  
        
        iv=(ImageView) mFloatLayout.findViewById(R.id.iv);
        iv.measure(View.MeasureSpec.makeMeasureSpec(0,  
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec  
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        iv.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.iv){
			ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);  
			String url="";
			if (clipboardManager.hasPrimaryClip()){  
			    if(clipboardManager.getPrimaryClip().getItemAt(0).getText()!=null)
			    	url=clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
			    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, "")); 
			}  
						
			Intent intent=new Intent(this,StartActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Bundle b=new Bundle();
			b.putBoolean("isClose", true);
			b.putString("url", url);
			intent.putExtras(b);
			startActivity(intent);
		}
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

}
