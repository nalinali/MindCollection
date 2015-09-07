package com.taguage.whatson.siteclip.dataObj;

import com.taguage.whatson.siteclip.R;

public class Constant {

	public static boolean DEBUG=false;
	public final static String DIR="/taguage/clip";
	public final static String NO_MESSAGE="no_message";
	public final static String SAVE_MESSAGE="save_url";

	public final static int[] frameDrawable=new int[]{
			R.drawable.ani_loading00,
			R.drawable.ani_loading01,
			R.drawable.ani_loading02,
			R.drawable.ani_loading03
	};
	
	public static String[] feature=new String[8];
	public static String[] featureKey=new String[]{
		"art","zhuangbility","stupid","food","soup","politics_economy","sophisticated","animation"
	};
	public static String[] featureKeyChinese=new String[]{
		"文艺","装逼","二逼","吃货","鸡汤","政经","高冷","二次元"
	};
	
	public final static String defaultFolder="默认文件夹";
	public final static String defaultTag="";
	
	public static final String UPLOAD_NOT_YET="no",
			UPLOAD_ALREADY="yes";
	
	public static final String STAR_HV="yes", STAR_NO="";
	
}
