package com.taguage.whatson.siteclip.Dialog;

import com.taguage.whatson.siteclip.R;
import com.taguage.whatson.siteclip.dataObj.AppContext;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class DialogFilter extends DialogFragment  implements View.OnClickListener{

	public final static String TAG="DialogFilter";
	
	AppContext app;
	EditText et;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		app=(AppContext) this.getActivity().getApplicationContext();
		String temp=app.getSpString(R.string.key_backup_filter);
		
		final Dialog dialog = new Dialog(getActivity(),R.style.MDialog);  
		LayoutInflater inflater = LayoutInflater.from(getActivity());
	    View view = inflater.inflate(R.layout.dialog_edit_filter, null);  
	    et=(EditText) view.findViewById(R.id.et_filter);
	    if(!temp.equals("")){
	    	et.setText(temp);
	    	et.setSelection(temp.length());
	    }
	    
	    view.findViewById(R.id.btn_ok).setOnClickListener(this);
	    view.findViewById(R.id.btn_cancel).setOnClickListener(this);
	    
	    dialog.setTitle(R.string.dialog_title_set_tags_filter);
	    dialog.setContentView(view);
	    dialog.setCancelable(false);
	    dialog.setCanceledOnTouchOutside(false);
	    
		return dialog;
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()){
		case R.id.btn_ok:
			String r=et.getText().toString().trim();
			r=r.replaceAll("£¬", ",");
			app.setSpString(R.string.key_backup_filter, r);
			break;
		case R.id.btn_cancel:
			break;
		}
		this.dismiss();
	}

	
}
