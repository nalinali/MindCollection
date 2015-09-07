package com.taguage.whatson.siteclip.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.TimeZone;

import android.util.Log;

import com.taguage.whatson.siteclip.dataObj.Constant;


public class FileUtils {

	public static boolean checkSDCard(){
		//get sdcard's status
				String SDState = android.os.Environment.getExternalStorageState();
				//if readable and writable
				if(SDState.equals(android.os.Environment.MEDIA_MOUNTED))return true;
				
				return false;
	}
	
	public static void initialDir(){
		if(checkSDCard()){
			String SDPath =android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
			String path=SDPath+Constant.DIR;
			File filetemplate=new File(path);
			if(!filetemplate.exists()){
				filetemplate.mkdirs();
			}
		}
	}
	
	public static void writeFile(String str,String filename){
		if(str==null)return;
		
		String SDPath =android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
		//create file path, absolute path
		try {			
			Writer out = new BufferedWriter(new OutputStreamWriter(					
				    new FileOutputStream(SDPath+Constant.DIR+"/"+filename+".txt"), "UTF-8"));
				try {
				    out.write(str);
				} finally {
				    out.close();
				}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getFileList(String dirstr){
		String result = ""; 
		   File[] files = new File(dirstr).listFiles(); 
		   if(files==null)return "";
		   if(files.length==0)return "";
		   for (File file : files) { 
		     if (file.getName().indexOf(".txt") >= 0) { 
		       result += file.getPath() + "\n"; 
		     } 
		   } 
		 return result;		
	}
	
	 public static String ReadTxtFile(String strFilePath)
	    {
	        String path = strFilePath;
	        String content = ""; //文件内容字符串
	            //打开文件
	            File file = new File(path);
	            //如果path是传递过来的参数，可以做一个非目录的判断
	            if (file.isDirectory())
	            {
	                
	            }
	            else
	            {
	                try {
	                    InputStream instream = new FileInputStream(file); 
	                    if (instream != null) 
	                    {
	                        InputStreamReader inputreader = new InputStreamReader(instream);
	                        BufferedReader buffreader = new BufferedReader(inputreader);
	                        String line;
	                        //分行读取
	                        while (( line = buffreader.readLine()) != null) {
	                            content += line + "\n";
	                        }                
	                        instream.close();
	                    }
	                }
	                catch (java.io.FileNotFoundException e) 
	                {
	                    Log.i("TestFile", "The File doesn't not exist.");
	                } 
	                catch (IOException e) 
	                {
	                     Log.i("TestFile", e.getMessage());
	                }
	            }
	            return content;
	    }
}
