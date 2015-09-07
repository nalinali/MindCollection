package com.taguage.whatson.siteclip.Dialog;

import com.taguage.whatson.siteclip.ListicleActivity;
import com.taguage.whatson.siteclip.R;
import com.taguage.whatson.siteclip.db.DBManager;
import com.taguage.whatson.siteclip.utils.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class DialogEditListicle extends DialogFragment implements View.OnClickListener{

	public final static String TAG="DialogEditListicle";
	
	public static int LISTICLE_NAME=0;
	
	EditText et;
	DBManager db=DBManager.getInstance();
	int type=-1,lid=-1;
	String cont;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void setArguments(Bundle args) {
		// TODO Auto-generated method stub
		super.setArguments(args);
		if(args.containsKey("type"))type=args.getInt("type");
		if(args.containsKey("cont"))cont=args.getString("cont");
		if(args.containsKey("lid"))lid=args.getInt("lid");
	}



	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final Dialog dialog = new Dialog(getActivity(),R.style.MDialog);  
		LayoutInflater inflater = LayoutInflater.from(getActivity());
	    View view = inflater.inflate(R.layout.dialog_edit, null); 
	    et=(EditText) view.findViewById(R.id.et_edit);
	    
	    if(type==LISTICLE_NAME){
	    	dialog.setTitle(R.string.dialog_title_edit_listicle);
	    	et.setText(cont);
	    }
	    view.findViewById(R.id.btn_cancel).setOnClickListener(this);
	    view.findViewById(R.id.btn_ok).setOnClickListener(this);
	    
	    dialog.setContentView(view);
	    dialog.setCancelable(false);
	    
		return dialog;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.btn_cancel:
			dismiss();
			break;
		case R.id.btn_ok:
			editName();
			dismiss();
			break;
		}
	}
	
	private  boolean isValid(){
		String str=et.getEditableText().toString().trim();
		if(str.equals("")){
			Utils.getInstance().makeToast(R.string.info_listicle_name_empty);
			return false;
		}
		boolean check=str.matches("[\u4E00-\u9FA5A-Za-z0-9_]{1,}");
		if(!check){
			Utils.getInstance().makeToast(R.string.info_listicle_name_invalid);
			return false;
		}
		check=false;
		Cursor c=db.getmDB().query(DBManager.LISTICLE, new String[]{
				"name"
		}, null, null, null, null, null);
		for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
			if(str.equals(c.getString(c.getColumnIndex("name"))))check=true;
		}
		c.close();
		if(check){
			Utils.getInstance().makeToast(R.string.info_listicle_name_duplicate);
			return false;
		}
		return true;
	}

	private void editName() {
		// TODO Auto-generated method stub
		Activity ac=this.getActivity();
		if(ac instanceof ListicleActivity){
			if(isValid()){
				String name=et.getEditableText().toString().trim();
				db.updateData(DBManager.LISTICLE, "_id", lid, new String[]{
						"name"
				}, new Object[]{
						name
				});
			}
			((ListicleActivity) ac).updateList();
		}
	}
	
	
}
