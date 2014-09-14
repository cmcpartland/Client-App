package com.example.clientapp;

import android.content.Context;
import android.util.Log;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class SongDatabaseSQL extends SQLiteOpenHelper {
	
	public static final String TABLE_SONGS = "songs";
	public static final String DB_PATH = "/data/data/com.example.clientapp/databases/";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ARTIST = "artist";
	public static final String COLUMN_ALBUM = "album";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_TRACKNO = "trackNo";
	public static final String COLUMN_ITUNESIDLOW = "iTunesIDlow";
	public static final String COLUMN_ITUNESIDHIGH = "iTunesIDhigh";
	
	private static final String DATABASE_NAME = "songs.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_SONGS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_ARTIST
			+ ", " + COLUMN_ALBUM
			+ ", " + COLUMN_NAME
			+ ", " + COLUMN_TRACKNO 
			+ ", " + COLUMN_ITUNESIDLOW 
			+ ", " + COLUMN_ITUNESIDHIGH + ");";
	
	public SongDatabaseSQL(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		boolean dbExist = checkDatabase(); 	
		if (!dbExist) 
			database.execSQL(DATABASE_CREATE);
	}
	
	private boolean checkDatabase(){
		 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = DB_PATH + TABLE_SONGS;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    	}catch(SQLiteException e){
 
    		//database does't exist yet.
 
    	}
 
    	if(checkDB != null){
 
    		checkDB.close();
 
    	}
 
    	return checkDB != null ? true : false;
    }
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(SongDatabaseSQL.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
		onCreate(db);
	}
}