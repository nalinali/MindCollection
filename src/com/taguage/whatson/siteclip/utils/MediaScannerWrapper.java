package com.taguage.whatson.siteclip.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

public class MediaScannerWrapper implements  
MediaScannerConnection.MediaScannerConnectionClient{
    private MediaScannerConnection mConnection;
    private String mPath;
    private String mMimeType;
    private Context ctx;
    
    private static MediaScannerWrapper msw;
    
    public static MediaScannerWrapper getInstance()
    {
    	if(msw==null)msw=new MediaScannerWrapper();
    	return msw;
    }
    
    public void initMediaScannerWrapper(Context ctx)
    {
    	this.ctx=ctx;
    	
    }

    // filePath - where to scan; 
    // mime type of media to scan i.e. "image/jpeg". 
    // use "*/*" for any media
    public void setWrapper(String filePath,String mime){
        mPath = filePath;
        mMimeType = mime;
        
    }

    // do the scanning
    public void scan(){
    	if(mConnection==null)mConnection =new MediaScannerConnection(ctx,this);    
    	mConnection.connect();
    }

    // start the scan when scanner is ready
    public void onMediaScannerConnected(){
        try {
			if(mPath!=null)mConnection.scanFile(mPath, mMimeType);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        //Log.w("MediaScannerWrapper","media file scanned: "+ mPath);
    }

	@Override
	public void onScanCompleted(String path, Uri uri) {
		// TODO Auto-generated method stub
		 mConnection.disconnect();
	}

   
}
