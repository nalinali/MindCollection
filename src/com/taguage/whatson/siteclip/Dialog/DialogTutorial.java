package com.taguage.whatson.siteclip.Dialog;

import com.taguage.whatson.siteclip.R;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class DialogTutorial extends DialogFragment {

	public final static int HOME=0, ESSAY=HOME+1, ESSAY_NO=ESSAY+1, SITELIST=ESSAY_NO+1,LISTICLE=SITELIST+1;
	
	int type;
	int[] imgs=new int[]{
			R.drawable.tutorial_home,
			R.drawable.tutorial_essay,
			R.drawable.tutorial_essay_no_cont,
			R.drawable.tutorial_sitelist,
			R.drawable.tutorial_listicle
	};
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		Bundle b=this.getArguments();
		if(b!=null){
			if(b.containsKey("type"))type=b.getInt("type");
			type=type%imgs.length;
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final Dialog dialog = new Dialog(getActivity(),R.style.MDialogNoTitle);  
		
		RelativeLayout rl=new RelativeLayout(this.getActivity());
		ImageView iv=new ImageView(this.getActivity());
		iv.setImageResource(imgs[type]);
		LayoutParams lp_rl=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT),
				lp_iv=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp_iv.addRule(RelativeLayout.CENTER_IN_PARENT)	;
		rl.setLayoutParams(lp_rl);
		iv.setLayoutParams(lp_iv);
		rl.addView(iv);
		
		dialog.setContentView(rl);
		
		return dialog;
	}

	
}
