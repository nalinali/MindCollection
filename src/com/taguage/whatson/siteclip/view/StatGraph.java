package com.taguage.whatson.siteclip.view;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Random;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.taguage.whatson.siteclip.R;
import com.taguage.whatson.siteclip.dataObj.Constant;
import com.taguage.whatson.siteclip.utils.MLog;
import com.taguage.whatson.siteclip.utils.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class StatGraph extends View {
	
	private final static int S_WIDTH=880, S_HEIGHT=1600;
	private static int screenWidth,screenHeight,TEXT_SIZE=32;
	private static Bitmap mainBm=null;
	private final static Point mainBmPos=new Point(200,183);
	private static int initMargin;
	
	private String[] keywords=new String[8],
			kindexes=new String[8];
	private String waterIndex;
	
	private int[] posOffset=new int[2];
	private int[] totalOffset=new int[2];
	private int[] downOffset=new int[2];
	private int[] lastPos=new int[2];
	private final static int moveThreshold=1;
	private final float minScaleRatio=0.6f;

	private static int lastPointerCount;
	private float baseValue,ratio=1f,totalRatio=1f;
	
	Paint bgPaint, tPaintWhite, tPaintColor,framePaint;
	Point itemPos=new Point();
	Random ra=new Random();
	int pass=ra.nextInt(10);
	
	String[] tColors=new String[]{"#f7b52c","#036eb7","#6ead31","#00913a",
			"#009fe8","#e3007f","#8086d1","#aa9b43","#eeaa00","#4a8aff","#8f00aa"};
	String[] occupy=new String[8];
	Point[] occupyPos=new Point[8], indexPos=new Point[8];
	
	OnSavingBitmap onSavingBitmap;
	boolean isSavingBitmap;
	
	Context ctx;
	
	public StatGraph(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		ctx=context;
		init();
	}

	public StatGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		ctx=context;
		init();
	}
	
	private void init(){
		int[] screenSize=Utils.getScreenSize((Activity) ctx);
		screenWidth=screenSize[0];
		totalOffset[1]=0;
		totalRatio=1;
		initMargin=(screenWidth-S_WIDTH)>>1;
		totalOffset[0]=initMargin;		
		
		occupyPos=new Point[]{
				new Point(485,0),new Point(485,435),
				new Point(0,480),new Point(485,250),
				new Point(485,90),new Point(0,0),
				new Point(0,59),new Point(0,244)
		};
		
		SVG svg=SVGParser.getSVGFromResource(ctx.getResources(), R.raw.event_main);
		mainBm=Utils.drawableToBitmap(svg.createPictureDrawable());
		
		bgPaint=new Paint();
		bgPaint.setColor(Color.parseColor("#3e3a39"));
		
		tPaintWhite=new Paint();
		tPaintWhite.setAntiAlias(true);
		tPaintWhite.setTextAlign(Align.LEFT);
		tPaintWhite.setColor(Color.WHITE);
		tPaintWhite.setTextSize(TEXT_SIZE);
		
		framePaint=new Paint();
		framePaint.setAntiAlias(true);
		framePaint.setColor(Color.WHITE);
		framePaint.setStyle(Style.STROKE);
		framePaint.setStrokeWidth(1);
		
		tPaintColor= new Paint();
		tPaintColor.setAntiAlias(true);
		tPaintColor.setTextAlign(Align.LEFT);
		tPaintColor.setTextSize(TEXT_SIZE);
		
	}
	
	private String getResultWords(String str){
		str=str.replace("%", "");
		float raw=Float.parseFloat(str);
		int level=0;
		if(raw==1000)level=0;
		else if(raw>=70 && raw<=100)level=0;
		else if(raw<70 && raw>=50)level=1;
		else if(raw<50 && raw>=30)level=2;
		else if(raw<30 && raw>=20)level=3;
		else if(raw<20 && raw>=10)level=4;
		else level=5;
		String[] resultWords=ctx.getResources().getStringArray(R.array.result_words);
		return resultWords[level];
	}
	
	public void setVals(Bundle b){
		String words=b.getString("words"), index=b.getString("index");
		keywords=words.split(",");
		kindexes=index.split(",");
		waterIndex=b.getString("water");
		
		String temp=ctx.getString(R.string.attach_occupied);
		for(int i=0;i<occupy.length;i++){
			occupy[i]=temp.replace("x", keywords[i]);			
		}
		pass=ra.nextInt(10);
		
		postInvalidate();
	}
	
	private Path indexBgPath(Point p){
		Path pt=new Path();
		pt.moveTo(p.x, p.y);
		pt.lineTo(p.x+160, p.y);
		pt.lineTo(p.x+180, p.y+30);
		pt.lineTo(p.x+160, p.y+60);
		pt.lineTo(p.x, p.y+60);
		pt.lineTo(p.x, p.y);
		
		return pt;
	}
	
	private void drawMultiline(Canvas m_canvas,String str, float x, float y, Paint paint)
	{		
		String[] group=str.split("\n");
		for (String line: group)
		{
			m_canvas.drawText(line, x, y, paint);
			y+=paint.getTextSize()+5;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		drawCont(canvas);
	}
	
	private void drawCont(Canvas canvas){
		canvas.drawColor(Color.parseColor("#3e3a39"));		
		canvas.scale(totalRatio, totalRatio);
		
		if(waterIndex!=null){
			tPaintWhite.setTextAlign(Align.CENTER);
			tPaintWhite.setTextSize(TEXT_SIZE<<1);
			canvas.drawText(ctx.getString(R.string.attach_title_saved_essays), 
					totalOffset[0]+(S_WIDTH>>1), 
					totalOffset[1]+(TEXT_SIZE<<2),
					tPaintWhite);
			tPaintWhite.setTextAlign(Align.LEFT);
			tPaintWhite.setTextSize(TEXT_SIZE);
			
			canvas.drawBitmap(mainBm, mainBmPos.x+totalOffset[0], mainBmPos.y+totalOffset[1], tPaintWhite);
			
			for(int i=0;i<occupyPos.length;i++){
				tPaintColor.setColor(Color.parseColor(tColors[i]));
				Point p=occupyPos[i];
				if(p.x==0)tPaintColor.setTextAlign(Align.RIGHT);
				else tPaintColor.setTextAlign(Align.LEFT);
				canvas.drawText(occupy[i], 
						mainBmPos.x+totalOffset[0]+p.x, 
						mainBmPos.y+totalOffset[1]+p.y+TEXT_SIZE, 
						tPaintColor);
				
				//指数指标				
				if(i%2==0)itemPos.x=totalOffset[0]+30;
				else itemPos.x=(mainBm.getWidth()>>1)+totalOffset[0]+mainBmPos.x;
				int offestY=(i%2==0)?i>>1:i-1>>1;
				itemPos.y=mainBmPos.y+mainBm.getHeight()+totalOffset[1]+80*offestY+80;
				canvas.drawPath(indexBgPath(itemPos), tPaintColor);
				String key=occupy[i].replace("被", "");
				key=key.replace("占领", "");
				canvas.drawText(key+"                "+kindexes[i], 
						itemPos.x+10, itemPos.y+40, tPaintWhite);
			}
			
			//计算指标
			itemPos.y+=200;
			itemPos.x=totalOffset[0]+40;
			tPaintWhite.setTextSize(TEXT_SIZE*0.8f);
			canvas.drawText(ctx.getString(R.string.attach_water), 
					itemPos.x, itemPos.y, tPaintWhite);
			
			canvas.drawText(ctx.getString(R.string.attach_water_up1), 
					itemPos.x+230+60, itemPos.y-70, tPaintWhite);
			canvas.drawText(ctx.getString(R.string.attach_water_up2), 
					itemPos.x+230+60, itemPos.y-30, tPaintWhite);
			canvas.drawText(ctx.getString(R.string.attach_water_down1), 
					itemPos.x+230+20, itemPos.y+30, tPaintWhite);
			canvas.drawText(ctx.getString(R.string.attach_water_down2), 
					itemPos.x+230+20, itemPos.y+70, tPaintWhite);
			canvas.drawLine(itemPos.x+230, itemPos.y-10, itemPos.x+230+300, itemPos.y-10, tPaintWhite);
			canvas.drawText("=", itemPos.x+230+300+30, itemPos.y, tPaintWhite);
			
			tPaintWhite.setTextSize(TEXT_SIZE<<1);
			if(waterIndex.equals("1000.00%"))canvas.drawText("出错了", 
					itemPos.x+230+300+30+30,
					itemPos.y,
					tPaintWhite);
			else canvas.drawText(waterIndex, 
					itemPos.x+230+300+30+30,
					itemPos.y,
					tPaintWhite);
			
			//评语 getResultWords
			tPaintColor.setColor(Color.parseColor(tColors[1]));
			canvas.drawRect(itemPos.x, itemPos.y+130, 
					itemPos.x+(TEXT_SIZE<<1)+20, 
					itemPos.y+(TEXT_SIZE*3)+200+20, tPaintColor);
			tPaintColor.setColor(Color.parseColor(tColors[2]));
			tPaintColor.setTextAlign(Align.LEFT);
			tPaintColor.setTextSize(TEXT_SIZE<<1);
			drawMultiline(canvas, ctx.getString(R.string.attach_judgement), 
					itemPos.x+10,
					itemPos.y+200+10, tPaintColor);
			tPaintColor.setTextSize(TEXT_SIZE*1.5f);
			tPaintColor.setColor(Color.parseColor(tColors[0]));
			drawMultiline(canvas, getResultWords(waterIndex), 
					itemPos.x+120,
					itemPos.y+180, tPaintColor);
			tPaintColor.setTextSize(TEXT_SIZE);
			int[] r=getDots(waterIndex);
			float dv=((TEXT_SIZE<<1)+20)/5f;
			for(int i=0;i<r.length-1;i++){				
				tPaintColor.setColor(Color.parseColor(tColors[r[i]]));
				canvas.drawRect(itemPos.x+i*dv, 
						itemPos.y+(TEXT_SIZE*3)+200+10,
						itemPos.x+(i+1)*dv,
						itemPos.y+(TEXT_SIZE*3)+200+20,
						tPaintColor);
			}
			tPaintColor.setColor(Color.parseColor(tColors[r[5]]));
			canvas.drawRect(itemPos.x, 
					itemPos.y+130,
					itemPos.x+(TEXT_SIZE<<1)+20,
					itemPos.y+130+10,
					tPaintColor);
			
			tPaintWhite.setTextSize(TEXT_SIZE);
			canvas.drawRect(totalOffset[0], totalOffset[1], totalOffset[0]+S_WIDTH, totalOffset[1]+S_HEIGHT, framePaint);
			canvas.drawLine(totalOffset[0],
					totalOffset[1]+S_HEIGHT-60, 
					totalOffset[0]+S_WIDTH, 
					totalOffset[1]+S_HEIGHT-60, framePaint);
			
			//署名
			tPaintWhite.setTextAlign(Align.RIGHT);
			canvas.drawText(ctx.getString(R.string.attach_auth), 
					totalOffset[0]+S_WIDTH-40,
					totalOffset[1]+S_HEIGHT-20, tPaintWhite);
			tPaintWhite.setTextAlign(Align.LEFT);
			
			
		}
	}
	
	private int[] getDots(String str){
		int[] r=new int[6];
		if(str.equals("1000.00%"))return r;
		String[] temp=str.split("\\.");		
		if(temp.length==2){
			int n=Integer.parseInt(temp[0]);
			DecimalFormat df=new DecimalFormat("000");
			String s=df.format(n);
			r[0]=(Integer.parseInt(s.substring(0,1))+pass)%10;
			r[3]=(Integer.parseInt(s.substring(1,2))+pass+1)%10;
			r[2]=(Integer.parseInt(s.substring(2,3))+pass+2)%10;
			r[1]=(Integer.parseInt(temp[1].substring(0,1))+pass+3)%10;
			r[4]=(Integer.parseInt(temp[1].substring(1,2))+pass+4)%10;
			r[5]=pass;
		}
		
		return r;		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub		
		switch(event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			lastPos[0]=(int) event.getX();
			lastPos[1]=(int) event.getY();
			downOffset[0]=(int) event.getX();
			downOffset[1]=(int) event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			if(event.getPointerCount()==1){
				movePos(new int[]{(int) (event.getX()-lastPos[0]),(int) (event.getY()-lastPos[1])});
				lastPos[0]=(int) event.getX();
				lastPos[1]=(int) event.getY();			
				lastPointerCount=1;
			}else{
				lastPointerCount=2;
				float sX=event.getX(0)-event.getX(1);
				float sY=event.getY(0)-event.getY(1);
				float dist=(float) Math.sqrt(sX*sX+sY*sY)/totalRatio;
				if(baseValue==0){
					baseValue=dist;
				}else{
					if(Math.abs(baseValue-dist)>moveThreshold){
						ratio=dist/baseValue;
						if(ratio<0.98)ratio=0.98f;
						if(ratio>1.02)ratio=1.02f;
						totalRatio*=ratio;						

						if(totalRatio>1.5)totalRatio=1.5f;
						else if(totalRatio<minScaleRatio){
							totalRatio=minScaleRatio;
						}		
					}
				}			
			}
			break;
		case MotionEvent.ACTION_UP:
			baseValue=0;
			lastPos[0]=(int) event.getX();
			lastPos[1]=(int) event.getY();
			break;
		case MotionEvent.ACTION_CANCEL:
			baseValue=0;
			lastPos[0]=(int) event.getX();
			lastPos[1]=(int) event.getY();
			break;
		}
		postInvalidate();
		return true;
	}
	
	public void movePos(int[] dist){
		if(dist.length==2){
			posOffset=dist;
		}else return;

		if(lastPointerCount==2)return;
		totalOffset[0]+=(posOffset[0]<<1);
		totalOffset[1]+=(posOffset[1]<<1);		
		
		postInvalidate();
	}
	
	public void saveAsBitmap(){
		totalRatio=1;
		new SaveBmTask().execute();
	}
	
	private Bitmap getBm(){
		totalOffset[1]=0;
		totalOffset[0]=0;
		totalRatio=1;
		postInvalidate();
		Bitmap bm=Bitmap.createBitmap(S_WIDTH,S_HEIGHT, Config.ARGB_8888);
		Canvas mCanvas=new Canvas(bm);
		drawCont(mCanvas);
		mCanvas.drawBitmap(bm, 0, 0, null);
		return bm;
	}
	
	public void setOnSavingBitmap(OnSavingBitmap onSavingBitmap){
		this.onSavingBitmap=onSavingBitmap;
	}
	public interface OnSavingBitmap{
		public void onSaveStart();
		public void onSaveEnd(File bmFile);
		public void onSaveFailed();
	}
	
	private class SaveBmTask extends AsyncTask<Void,Void,Boolean>{

		File bmFile;
		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(result){
				if(onSavingBitmap!=null && bmFile!=null)onSavingBitmap.onSaveEnd(bmFile);
			}
			else if(onSavingBitmap!=null){
				onSavingBitmap.onSaveFailed();
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if(onSavingBitmap!=null)onSavingBitmap.onSaveStart();
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			isSavingBitmap=true;
			String SDPath =android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
			String filename="brain_index.png";
			Bitmap bm=getBm();
			bmFile=Utils.bitmapToFile(SDPath+Constant.DIR+"/", filename, bm, 100,ctx);
			isSavingBitmap=false;
			if(bmFile==null)return false;
			return true;
		}
	}

}
