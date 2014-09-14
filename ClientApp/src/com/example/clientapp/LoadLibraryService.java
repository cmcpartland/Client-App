package com.example.clientapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class LoadLibraryService extends Service {
	private final IBinder mBinder = new LocalBinder();
	private Socket s;
	private PrintWriter socket_out;
	private BufferedReader socket_in;
	private int test = 0;
	
	public class LocalBinder extends Binder {
		LoadLibraryService getService() {
			return LoadLibraryService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public void send_message(String message) {
		socket_out.println(message);
	}
	
	public String receive_message() {
		String message = "";
		try {
			message = socket_in.readLine().replace("\n", "");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return message;
	}
	
	public void setTestInt(int n) {
		test = n;
	}
	
	public int getTestInt() {
		return test;
	}
	
	public String createSocket(String ip, int PORT) {
		try {
			s = new Socket(ip, PORT);
			socket_out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
			socket_in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			//String response = receive_message();
			//send_message("Hello, Server.");
			return "connected";
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return e.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return e.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
	}
}

	