package com.taguage.whatson.siteclip.Dialog;

import org.json.JSONException;
import org.json.JSONObject;

import com.taguage.whatson.siteclip.EssayActivity;
import com.taguage.whatson.siteclip.OptionsActivity;
import com.taguage.whatson.siteclip.R;
import com.taguage.whatson.siteclip.dataObj.AppContext;
import com.taguage.whatson.siteclip.utils.Utils;
import com.taguage.whatson.siteclip.utils.Web;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;



public class DialogLogin extends DialogFragment implements View.OnClickListener{

	EditText et_pw;
	AutoCompleteTextView ac_email;
	AppContext app;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateDialog(savedInstanceState);
		app=(AppContext) this.getActivity().getApplicationContext();
		String temp=app.getSpString(R.string.key_taguage_used_emails);
		
		final Dialog dialog = new Dialog(getActivity(),R.style.MDialog);  
		LayoutInflater inflater = LayoutInflater.from(getActivity());
	    View view = inflater.inflate(R.layout.dialog_login, null, false);  
	    ac_email=(AutoCompleteTextView) view.findViewById(R.id.et_email);
	    et_pw=(EditText) view.findViewById(R.id.et_pw);
	    dialog.setContentView(view);
	    dialog.setCancelable(false);
	    dialog.setTitle(R.string.dialog_title_login_taguage);
	    view.findViewById(R.id.tv_register).setOnClickListener(this);
	    view.findViewById(R.id.btn_ok).setOnClickListener(this);
	    view.findViewById(R.id.btn_cancel).setOnClickListener(this);
	    
	    if(!temp.equals("")){
	    	String[] emails=temp.split(",");
	    	ArrayAdapter<String> titleAdapter = new ArrayAdapter<String>(this.getActivity(),
					android.R.layout.simple_dropdown_item_1line, emails);
	    	ac_email.setThreshold(0);
	    	ac_email.setAdapter(titleAdapter);
	    }
	    
	    return dialog;
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.btn_ok:
			if(validateInput())requestLogin(ac_email.getText().toString().trim(), 
					Utils.makeMD5(et_pw.getText().toString()));
			break;
		case R.id.btn_cancel:
			this.dismiss();
			break;
		case R.id.tv_register:
			Intent intent = new Intent();        
	        intent.setAction("android.intent.action.VIEW");    
	        Uri content_url = Uri.parse("http://www.taguage.com/register");   
	        intent.setData(content_url);  
	        startActivity(intent);
			break;
		}
	}
	
	private void requestLogin(final String email, String pw) {
		// TODO Auto-generated method stub
		String uuid=Utils.getUUID(getActivity());
		Web.getInstance().requestLogin(email, pw,uuid,
				new Web.CallBack() {
					
					@Override
					public void onSuccess(JSONObject json) {
						// TODO Auto-generated method stub
						try {
							app.setSpString(R.string.key_taguage_nick, json.getString("nickname"));
							app.setSpString(R.string.key_taguage_token, json.getString("tokenid"));
							app.setSpString(R.string.key_taguage_uid, json.getString("uid"));
							app.setSpLong(R.string.key_taguage_expire, json.getLong("expire"));
							
							String existingEmails=app.getSpString(R.string.key_taguage_used_emails);
							if(!existingEmails.contains(email)){
								if(existingEmails.equals(""))existingEmails=email;
								else existingEmails=existingEmails+","+email;
								app.setSpString(R.string.key_taguage_used_emails, existingEmails);
							}
							
							Activity ac=DialogLogin.this.getActivity();
							if(ac instanceof OptionsActivity){
								((OptionsActivity) ac).setUserInfo();
							}else if(ac instanceof EssayActivity){
								((EssayActivity) ac).upload();
							}
							DialogLogin.this.dismiss();
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Utils.getInstance().makeToast(R.string.info_fail_in_login);
						}
						
						DialogLogin.this.dismiss();
					}

					@Override
					public void onFail() {
						// TODO Auto-generated method stub
						Utils.getInstance().makeToast(R.string.info_fail_in_login);
						DialogLogin.this.dismiss();
					}

					@Override
					public void onStart() {
						// TODO Auto-generated method stub
						
					}
				});
	}

	private boolean validateInput(){
		String emailstr=ac_email.getEditableText().toString().trim();
		if(Utils.validateEmail(emailstr))return true;
		else {
			Utils.getInstance().makeToast(R.string.info_invalid_email);
			return false;
		}
	}
	
}
