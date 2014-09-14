package com.example.clientapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SongsDataSource {
	private SQLiteDatabase database;
	private SongDatabaseSQL dbHelper;
	private String[] allColumns = { SongDatabaseSQL.COLUMN_ID,
			SongDatabaseSQL.COLUMN_ARTIST,
			SongDatabaseSQL.COLUMN_ALBUM,
			SongDatabaseSQL.COLUMN_NAME,
			SongDatabaseSQL.COLUMN_TRACKNO,
			SongDatabaseSQL.COLUMN_ITUNESIDLOW,
			SongDatabaseSQL.COLUMN_ITUNESIDHIGH };
	
	public SongsDataSource(Context context) {
		dbHelper = new SongDatabaseSQL(context);
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public Song createSong(String artist, String album, String name, String trackNumber, String IDlow, String IDhigh) {
		ContentValues values = new ContentValues();
		values.put(SongDatabaseSQL.COLUMN_ARTIST, artist);
		values.put(SongDatabaseSQL.COLUMN_ALBUM, album);
		values.put(SongDatabaseSQL.COLUMN_NAME, name);
		values.put(SongDatabaseSQL.COLUMN_TRACKNO, trackNumber);
		values.put(SongDatabaseSQL.COLUMN_ITUNESIDLOW, IDlow);
		values.put(SongDatabaseSQL.COLUMN_ITUNESIDHIGH, IDhigh);
		
		long insertId = database.insert(SongDatabaseSQL.TABLE_SONGS, "nullColumnHack", values);
		Cursor cursor;
		try {
			cursor = database.query(SongDatabaseSQL.TABLE_SONGS, allColumns, SongDatabaseSQL.COLUMN_ID + " = " + insertId, null, null, null, null);
		} catch(Exception e) {
			cursor = null;
			e.printStackTrace();
		}
		cursor.moveToFirst();
		Song newSong = cursorToSong(cursor);
		cursor.close();
		return newSong;
	}
	
	public void deleteSong(Song song) {
		long id = song.getId();
		System.out.println("Song deleted with id: " + id);
		database.delete(SongDatabaseSQL.TABLE_SONGS, 
				SongDatabaseSQL.COLUMN_ID + " = " + id, null);
	}
	
	public String[] getArtists() {
		database = dbHelper.getReadableDatabase();
		List<String> artists = new ArrayList<String>();
		
		Cursor cursor = database.query(SongDatabaseSQL.TABLE_SONGS,
				new String[] {SongDatabaseSQL.COLUMN_ARTIST}, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String artist = cursor.getString(0);
			artists.add(artist);
			cursor.moveToNext();
		}
		Set<String> temp = new HashSet<String>(artists);
		String[] uniqueArtists = temp.toArray(new String[temp.size()]);
		Arrays.sort(uniqueArtists);
		return uniqueArtists;
	}
	
	public String[] getAlbumsByArtist(String artist) {
		database = dbHelper.getReadableDatabase();
		List<String> albums = new ArrayList<String>();
		
		Cursor cursor = null;
		try {
			cursor = database.query(SongDatabaseSQL.TABLE_SONGS, 
				new String[] { SongDatabaseSQL.COLUMN_ALBUM }, SongDatabaseSQL.COLUMN_ARTIST + " = ?", new String[] {artist}, null, null, null);
		} catch(Exception e) {
			e.printStackTrace();
		}
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String album = cursor.getString(0);
			albums.add(album);
			cursor.moveToNext();
		}
		Set<String> temp = new HashSet<String>(albums);
		String[] uniqueAlbums = temp.toArray(new String[temp.size()]);
		Arrays.sort(uniqueAlbums);
		return uniqueAlbums;
	}
	
	public String[] getSongsByAlbum(String artist, String album) {
		database = dbHelper.getReadableDatabase();
		List<String> songs = new ArrayList<String>();
		
		Cursor cursor = null;
		try {
			cursor = database.query(SongDatabaseSQL.TABLE_SONGS, 
				new String[] { SongDatabaseSQL.COLUMN_NAME },
				SongDatabaseSQL.COLUMN_ARTIST + " = ? and " + SongDatabaseSQL.COLUMN_ALBUM + " = ?", 
				new String[] {artist, album}, null, null, null);
		} catch(Exception e) {
			e.printStackTrace();
		}
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String song = cursor.getString(0);
			songs.add(song);
			cursor.moveToNext();
		}
		return songs.toArray(new String[songs.size()]);
	}
	
	public String getITunesIDsBySong(String artist, String album, String song) {
		database = dbHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = database.query(SongDatabaseSQL.TABLE_SONGS, new String[] {SongDatabaseSQL.COLUMN_ITUNESIDLOW, SongDatabaseSQL.COLUMN_ITUNESIDHIGH },
				SongDatabaseSQL.COLUMN_ARTIST + " = ? and " +
				SongDatabaseSQL.COLUMN_ALBUM + " = ? and " +
				SongDatabaseSQL.COLUMN_NAME + " = ?",
				new String[] {artist, album, song}, null, null, null);
		} catch(SQLException e) {
			e.printStackTrace();
		}
		cursor.moveToFirst();
		return cursor.getString(0) + "; " + cursor.getString(1) + ";";
	}
	
	public List<Song> getAllSongs() {
		List<Song> songs = new ArrayList<Song>();
		
		Cursor cursor = database.query(SongDatabaseSQL.TABLE_SONGS, 
				allColumns, null, null, null, null, null);
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Song song = cursorToSong(cursor);
			songs.add(song);
			cursor.moveToNext();
		}
		cursor.close();
		return songs;
	}
		                            
	
	private Song cursorToSong(Cursor cursor) {
		Song song = new Song();
		song.setId(cursor.getLong(0));
		song.setArtist(cursor.getString(1));
		song.setAlbum(cursor.getString(2));
		song.setName(cursor.getString(3));
		song.setTrackNumber(cursor.getString(4));
		song.setITunesNumber(cursor.getString(5));
		
		return song;
	}
	
}
