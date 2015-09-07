package com.taguage.whatson.siteclip;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.taguage.whatson.siteclip.dataObj.Constant;
import com.taguage.whatson.siteclip.utils.MLog;
import com.taguage.whatson.siteclip.utils.Utils;
import com.xiaomi.mipush.sdk.MiPushClient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class PrefaceActivity extends BaseActivity {

	private final static int OK=0;
	Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case OK:
				startActivity(new Intent(PrefaceActivity.this, StartActivity.class));
				finish();
				break;
			}
		}
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);		
		
		setContentView(R.layout.activity_preface);
		setView();
		
		boolean isDenyMessage=app.getSpBoolean(R.string.key_dont_send_message);
		if(!isDenyMessage)MiPushClient.subscribe(this,Constant.SAVE_MESSAGE,null);
		else MiPushClient.subscribe(this,Constant.NO_MESSAGE,null);
		
		MLog.e("","tag="+MiPushClient.getAllTopic(this)+" key="+isDenyMessage);		
		
		handler.sendEmptyMessageDelayed(OK, 1500);
	}

	private void setView() {
		// TODO Auto-generated method stub
		RelativeLayout root=(RelativeLayout) findViewById(R.id.root);
		ImageView iv=new ImageView(this);
		LayoutParams params=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int[] size=Utils.getScreenSize(this);
		params.width=(int) (size[0]*0.5f);
		params.height=(int)(params.width*336/308);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		iv.setLayoutParams(params);

		Utils.parseSvg(R.raw.preface, iv, this);
		root.addView(iv);
		
	}

	
	
}
