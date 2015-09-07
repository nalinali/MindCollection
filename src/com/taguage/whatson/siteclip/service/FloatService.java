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

	//���帡�����ڲ���  
    LinearLayout mFloatLayout;  
    WindowManager.LayoutParams wmParams;  
    //���������������ò��ֲ����Ķ���  
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
            //�Ƴ���������  
            mWindowManager.removeView(mFloatLayout);  
        } 
	}

	private void createFloatView(){
		MLog.e("","-------start float service-------");
		wmParams = new WindowManager.LayoutParams();  
        //��ȡ����WindowManagerImpl.CompatModeWrapper  
        mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);  
        //����window type  
        wmParams.type = LayoutParams.TYPE_PHONE;   
        //����ͼƬ��ʽ��Ч��Ϊ����͸��  
        wmParams.format = PixelFormat.RGBA_8888;   
        //���ø������ڲ��ɾ۽���ʵ�ֲ���������������������ɼ����ڵĲ�����  
        wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;        
        //������������ʾ��ͣ��λ��Ϊ����ö�  
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;         
        // ����Ļ���Ͻ�Ϊԭ�㣬����x��y��ʼֵ�������gravity  
        wmParams.x = 0;  
        wmParams.y = 300;  
  
         // �����������ڳ������� 
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        
        LayoutInflater inflater = LayoutInflater.from(getApplication());  
        //��ȡ����������ͼ���ڲ���  
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_get_clipboard, null);  
        //���mFloatLayout  
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
