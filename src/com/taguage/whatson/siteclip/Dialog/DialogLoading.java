package com.taguage.whatson.siteclip.Dialog;

import com.taguage.whatson.siteclip.R;
import com.taguage.whatson.siteclip.dataObj.Constant;
import com.taguage.whatson.siteclip.utils.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogLoading extends DialogFragment {

	public final static String TAG="DialogLoading";
	
	AnimationDrawable ad;
	
	final int TIME=100;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final Dialog dialog = new Dialog(getActivity(),R.style.MDialogNoTitle);  
		LayoutInflater inflater = LayoutInflater.from(getActivity());
	    View view = inflater.inflate(R.layout.dialog_loading, null, false);  
	    dialog.setContentView(view);
	    dialog.setCancelable(false);
	    dialog.setCanceledOnTouchOutside(false);
	    
	    ad=new AnimationDrawable();
	    ad.addFrame(getD(Constant.frameDrawable[0]), TIME);
	    ad.addFrame(getD(Constant.frameDrawable[1]), TIME);
	    ad.addFrame(getD(Constant.frameDrawable[2]), TIME);
	    ad.addFrame(getD(Constant.frameDrawable[3]), TIME);
	    ImageView v=(ImageView) view.findViewById(R.id.v);
	    v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	    v.setImageDrawable(ad);
	    ad.setOneShot(false);
	    ad.start();
	    
		return dialog;
	}
	
	private Drawable getD(int d){
		return this.getResources().getDrawable(d);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
		super.onDismiss(dialog);
		if(ad!=null)ad.stop();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		super.onCancel(dialog);
		if(ad!=null)ad.stop();
	}


	
	
	
}
