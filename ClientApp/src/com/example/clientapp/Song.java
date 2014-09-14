package com.example.clientapp;

public class Song {
	private long id;
	private String name;
	private String artist;
	private String album;
	private String trackNumber;
	private String iTunesNumber; 
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	
	public String getTrackNumber() {
		return trackNumber;
	}
	public void setTrackNumber(String trackNumber) {
		this.trackNumber = trackNumber;
	}
	
	public String getITunesNumber() {
		return iTunesNumber;
	}
	public void setITunesNumber(String iTunesNumber) {
		this.iTunesNumber = iTunesNumber;
	}
	@Override
	public String toString() {
		return artist + "." + album + "." + name + ";";
	}
}