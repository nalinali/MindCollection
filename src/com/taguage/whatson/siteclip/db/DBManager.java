package com.taguage.whatson.siteclip.db;

import com.taguage.whatson.siteclip.utils.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBManager {

	public static final String TAG="DBManager";
	public static final String DB_NAME = "taguage_web_clip.db";
	private static int DB_VERSION = 12;

	public static final String MY_CLIP="my_clip";
	public static final String SITE="site_info",
			LISTICLE="listicles";

	private DatabaseHelper helper=null;
	private SQLiteDatabase mDB=null;

	private Context ctx=null;
	
	public static DBManager db;
	
	public DBManager()
	{
		ctx=Utils.getInstance().getCtx();
	}
	
	public static DBManager getInstance()
	{
		if(db==null){
			db=new DBManager();
			db.open();
		}
		return db;
	}

	
	public void open() throws SQLException
	{
		helper=new DatabaseHelper(ctx);
		mDB=helper.getWritableDatabase();	
	}
	
	public void close()
	{
		if(helper!=null)helper.close();
	}
	
	/**
	* 方法2：检查表中某列是否存在
	* @param db
	* @param tableName 表名
	* @param columnName 列名
	* @return
	*/
	public boolean checkColumnExists(SQLiteDatabase db, String tableName
	       , String columnName) {
	    boolean result = false ;
	    Cursor cursor = null ;

	    try{
	        cursor = db.rawQuery( "select * from sqlite_master where name = ? and sql like ?"
	           , new String[]{tableName , "%" + columnName + "%"} );
	        result = null != cursor && cursor.moveToFirst() ;
	    }catch (Exception e){
	        Log.e(TAG,"checkColumnExists2..." + e.getMessage()) ;
	    }finally{
	        if(null != cursor && !cursor.isClosed()){
	            cursor.close() ;
	        }
	    }

	    return result ;
	}
	
	
	
	/**
	 * insert strings into db
	 * @param tableName
	 * @param keys
	 * @param data
	 * @return
	 */
	public long insertData(String tableName, String[] keys, Object[] data)
	{
		ContentValues cv=new ContentValues();
		int count=keys.length;
		for(int i=0;i<count;i++){
			if(data[i] instanceof String)cv.put(keys[i], data[i].toString());
			else if(data[i] instanceof Integer)cv.put(keys[i], (Integer)data[i]);
		}
		return mDB.insert(tableName, null, cv);
	}
	
	/**
	 * del data
	 * @param tableName
	 * @param keyId
	 * @param rowId
	 * @return
	 */
	public boolean del(String tableName, String keyId, long rowId)
	{
		return mDB.delete(tableName, keyId+"="+rowId, null)>0;
	}
	
	public Cursor getAll(String tableName, String[] columns)
	{
		return mDB.query(tableName, columns,null,null,null,null,null);
	}
	
	public Cursor getData(String tableName,String keyId, String[] columns, long rowId) throws SQLException
	{
		Cursor c = mDB.query(tableName, columns, keyId+"="+rowId, null, null, null, null);
		if(c!=null)c.moveToFirst();
//		c.close();
		return c;
	}
	
	public boolean updateData(String tableName, String keyId, long rowId, String[] keys, Object[] data)
	{
		int count=keys.length;
		ContentValues cv=new ContentValues();
		for(int i=0;i<count;i++)
		{
			if(data[i] instanceof String)cv.put(keys[i], data[i].toString());
			else if(data[i] instanceof Integer)cv.put(keys[i], (Integer)data[i]);
		}
		return mDB.update(tableName, cv, keyId+"="+rowId, null)>0;
	}
	
	public SQLiteDatabase getmDB() {
		return mDB;
	}
	
	public void recreateDB()
	{
		helper.onUpgrade(mDB, 0, 0);
	}
	
	
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		/**
		 * database tools
		 * @param context
		 */
		public DatabaseHelper(Context context)
		{
			super(context, DB_NAME, null, DB_VERSION);
		}

		

		public void onUpgrade(SQLiteDatabase db, int i, int j)
		{
			onCreate(db);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			
			new Thread()
			{

				@Override
				public void run() {
					// TODO Auto-generated method stub
					super.run();
					db.execSQL("CREATE TABLE IF NOT EXISTS "+MY_CLIP
							+"( _id INTEGER PRIMARY KEY, source TEXT, sourceurl TEXT, " +
							"time TEXT, title TEXT, cont TEXT, abstract TEXT," +
									"edittime TEXT, tags TEXT,folder TEXT,star TEXT," +
									" upload TEXT, feature TEXT, lid TEXT);");
					db.execSQL("CREATE TABLE IF NOT EXISTS "+SITE
							+"( _id INTEGER PRIMARY KEY, site TEXT, link_pattern TEXT, " +
							"link_prefix TEXT, home_page TEXT, title_rex TEXT, auth_rex TEXT," +
									"cont_rex TEXT, extra_rex TEXT,tag_rex TEXT," +
									" source TEXT, channel_id TEXT, priority TEXT, wrapper TEXT, " +
									"wrapper_style TEXT, user_agent TEXT, keep_title TEXT);");
					db.execSQL("CREATE TABLE IF NOT EXISTS "+LISTICLE
							+"( _id INTEGER PRIMARY KEY, name TEXT, time TEXT,  seq INTEGER, files TEXT);");
				}
				
			}.start();

			
			
		}
	}
}
