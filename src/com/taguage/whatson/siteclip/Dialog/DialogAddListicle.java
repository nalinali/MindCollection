package com.taguage.whatson.siteclip.Dialog;

import com.taguage.whatson.siteclip.ListicleActivity;
import com.taguage.whatson.siteclip.R;
import com.taguage.whatson.siteclip.db.DBManager;
import com.taguage.whatson.siteclip.utils.Listicle;
import com.taguage.whatson.siteclip.utils.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class DialogAddListicle extends DialogFragment implements View.OnClickListener {

	public final static String TAG="DialogAddListicle";

	EditText et;
	DBManager db=DBManager.getInstance();
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final Dialog dialog = new Dialog(getActivity(),R.style.MDialog);  
		LayoutInflater inflater = LayoutInflater.from(getActivity());
	    View view = inflater.inflate(R.layout.dialog_input, null); 
	    et=(EditText) view.findViewById(R.id.et_input);
	    
	    view.findViewById(R.id.btn_cancel).setOnClickListener(this);
	    view.findViewById(R.id.btn_ok).setOnClickListener(this);
	    
	    dialog.setTitle(R.string.dialog_title_create_listicle);
	    dialog.setContentView(view);
	    dialog.setCancelable(false);
	    
		return dialog;
	}
	
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.btn_cancel:
			this.dismiss();
			break;
		case R.id.btn_ok:
			if(Listicle.isValid(et.getEditableText().toString().trim())){
				addListicle();
				Activity ac=this.getActivity();
				if(ac instanceof ListicleActivity){
					((ListicleActivity) ac).updateList();
				}
				this.dismiss();
			}			
			break;
		}
	}

	private void addListicle() {
		// TODO Auto-generated method stub
		String str=et.getEditableText().toString().trim();
		db.insertData(DBManager.LISTICLE, new String[]{
				"name","time","seq","files"
		}, new Object[]{
				str, System.currentTimeMillis()+"",Listicle.getMaxSeq()+1,""
		});
	}
	
	
	
}
