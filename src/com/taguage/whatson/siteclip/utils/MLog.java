package com.taguage.whatson.siteclip.utils;

import com.taguage.whatson.siteclip.dataObj.Constant;

import android.util.Log;

public class MLog  {

	public void MLog()
	{
		
	}
	
	public final static void s(String str){
		System.out.print(str);
	}
	
	public final static void v(String tag,Object obj)
	{
		if(Constant.DEBUG)Log.v(tag,obj.toString());
	}
	
	public final static void i(String tag,Object obj)
	{
		if(Constant.DEBUG)Log.i(tag,obj.toString());
	}
	
	public final static void e(String tag,Object obj)
	{
		if(Constant.DEBUG)Log.e(tag,obj.toString());
	}
}
