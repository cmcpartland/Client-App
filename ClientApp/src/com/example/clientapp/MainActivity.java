package com.example.clientapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.Socket;

import com.example.clientapp.LoadLibraryService.LocalBinder;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public class MainActivity extends Activity {
	private Button  exit_button, prev_button, play_pause_button, next_button;
	private ProgressDialog dialog;
	private ProgressDialog loadingBar;
	private TextView tv, tvArtist, tvAlbum, tvSong;
	private boolean song_playing = false;
	boolean mBound = false;
	//Handler is used so that View can be changed outside of the UI thread
	private Handler mHandler, lbHandler;
	private LoadLibraryService lls;
	private SongsDataSource datasource;
	private String artist = "";
	private String album = "";
	private String song = "";
	private String[] previousList;
	private boolean inMenu = false;
	private UpdateStateTask ust;
	
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            lls = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tv = (TextView) findViewById(R.id.textView1);
		tvArtist = (TextView) findViewById(R.id.tvArtist);
		tvAlbum = (TextView) findViewById(R.id.tvAlbum);
		tvSong = (TextView) findViewById(R.id.tvSong);
		exit_button = (Button) findViewById(R.id.exit);
		prev_button = (Button) findViewById(R.id.previous);
		next_button = (Button) findViewById(R.id.next);
		play_pause_button = (Button) findViewById(R.id.play_pause);
		dialog = new ProgressDialog(this);
		dialog.setCanceledOnTouchOutside(false);
		loadingBar = new ProgressDialog(this);
		loadingBar.setCanceledOnTouchOutside(false);
		loadingBar.setMessage("Loading songs from iTunes");
		loadingBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				//Update TextView with status
				String m = (String)msg.obj;
				String old_status = tv.getText().toString();
				tv.setText(old_status + m + "\n");
			}
		};
		
		lbHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				loadingBar.incrementProgressBy(1);
			}
		};
					
		datasource = new SongsDataSource(this);
		datasource.open();
		
		Intent intent = new Intent(this, LoadLibraryService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		connectToServer();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, LoadLibraryService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		datasource.open();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = new Intent(this, LoadLibraryService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		if (!inMenu) {
			UpdateStateTask ust = new UpdateStateTask();
			ust.execute("pass");
		}
		datasource.open();
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        datasource.close();
    }
    @Override
    protected void onStop() {
        super.onStop();
        datasource.close();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	sendMessage("exiting");
    }
	
    void loadingBarShow() {
    	loadingBar.show();
    }
    
    void loadingBarHide() {
    	loadingBar.dismiss();
    }
    
	void dialogShow(String message) {
	    dialog.setMessage(message);
	    dialog.show();
	}
	
	void dialogHide() {
	    dialog.dismiss();
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
        	sendMessage("decrease_volume");
        }
        return true;
    }
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
			sendMessage("increase_volume");
		}
		return true;
	}
	
	
	private void sendMessage(String message) {
//		socket_out.println(message);
		lls.send_message(message);
	}
	
	private String receiveMessage() {
		String response = lls.receive_message();
		return response;
	}
	
	private void updateStatus(String message) {
		Message msg = new Message();
		msg.obj = message;
		mHandler.sendMessage(msg);
	}
	
	private void connectToServer() {
		String ip = "192.168.1.2";
		String port = "8888";
		updateStatus("Connecting to server on IP: " + ip + "; PORT: " + port);
		ConnectTask ct = new ConnectTask();
		ct.execute(new String[] {ip, port});		
	}
	
	private void updateState() {
		if (song_playing) 	{
			play_pause_button.setText("Pause");
			tvArtist.setText(artist);
			tvAlbum.setText(album);
			tvSong.setText(song);
		}
		else 
			play_pause_button.setText("Play");
	}

	private void loadList(String type, String[] listItems, String artistChoice, String albumChoice) {	
		inMenu = true;
		Intent i = new Intent(this, ShowListActivity.class);
		i.putExtra("previous_list", previousList);
		i.putExtra("list_items", listItems);
		i.putExtra("item_type", type);
		i.putExtra("artist_choice", artistChoice);
		i.putExtra("album_choice", albumChoice);
		startActivityForResult(i, 1);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (data.hasExtra("item_type") && data.hasExtra("choice") && data.hasExtra("artist_choice") && data.hasExtra("album_choice")) {
				String itemType = data.getStringExtra("item_type");
				previousList = data.getStringArrayExtra("previous_list");
				
				if (itemType.equals("none")) {
					String[] artists = datasource.getArtists();
					inMenu = true;
					loadList("artist", artists, "", "");
				}
				else if (itemType.equals("artist")) {
					//Display albums from artist
					inMenu = true;
					String artistChoice = data.getStringExtra("choice");
					String[] artist_albums = datasource.getAlbumsByArtist(data.getStringExtra("choice"));
					loadList("album", artist_albums, artistChoice, "");
				}
				else if (itemType.equals("album")) {
					inMenu = true;
					String artistChoice = data.getStringExtra("artist_choice");
					String[] album_songs = datasource.getSongsByAlbum(artistChoice, data.getStringExtra("choice"));
					loadList("song", album_songs, artistChoice, data.getStringExtra("choice"));
				}
				else if (itemType.equals("song")) {
					String artistChoice = data.getStringExtra("artist_choice");
					String albumChoice = data.getStringExtra("album_choice");
					String songChoice = data.getStringExtra("choice");
//					tvArtist.setText(artistChoice);
//					tvAlbum.setText(albumChoice);
//					tvSong.setText(songChoice);
					String IDs = datasource.getITunesIDsBySong(artistChoice, albumChoice, songChoice);
					song_playing = true;
//					play_pause_button.setText("Pause");
					sendMessage("play_music");
					sendMessage(IDs);
					inMenu = false;
					android.os.SystemClock.sleep(350);
				}
				else if (itemType.equals("pass")) {
					inMenu = false;
				}
				
			}
		}
	}
	
/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
//		initialize_buttons();
	}*/
	
	public void onTextViewClicked(View v) {
		if (v.getId() == R.id.tvArtist) {
			String artist = (String)tvArtist.getText();
			String[] albums = datasource.getAlbumsByArtist(artist);
			loadList("album", albums, artist, "");
		}
		else if (v.getId() == R.id.tvAlbum) {
			String artist = (String)tvArtist.getText();
			String album = (String)tvAlbum.getText();
			String[] songs = datasource.getSongsByAlbum(artist,  album);
			loadList("song", songs, artist, album);
		}
	}
	                                    
		
	public void onButtonClicked(View v) {

		if (v.getId() == R.id.exit) {
			sendMessage("exiting");
			datasource.close();
			onStop();
			onDestroy();
		}
		else if (v.getId() == R.id.SyncLibrary) {
			LoadLibraryTask llt = new LoadLibraryTask();
			llt.execute("params");
		}
		else if (v.getId() == R.id.ViewLibrary) {
			loadList("artist", datasource.getArtists(), "", "");
		}
		else {
			if (v.getId() == R.id.previous){
				sendMessage("prev_track");
			}
			else if (v.getId() == R.id.play_pause) {
				if (song_playing) {
					sendMessage("pause");
					play_pause_button.setText("Play");
					song_playing = false;
				}
				else {
					sendMessage("play");
					play_pause_button.setText("Pause");
					song_playing = true;
				}
			}
			else if (v.getId() == R.id.next) {
				sendMessage("next_track");
			}
			android.os.SystemClock.sleep(350);
			if (ust != null) {
				ust.cancel(true);
			}
			ust = new UpdateStateTask();
			ust.execute("pass");
			
		}
	}


	class ConnectTask extends AsyncTask<String, Void, String> {
		/*AsyncTask<alpha, beta, gamma>
		 * alpha: input parameter type for doInBackground
		 * beta: input paramter type for onProgres (if this is used)
		 * gamma: return type of doInBackground
		*/
		protected void onPreExecute()  {
			dialogShow("Connecting to server...");
		}
		
		protected String doInBackground(String... socket_params) {
			String ip = socket_params[0];
			String PORT = socket_params[1];
			String connected_bool = "connected";
			int port = Integer.parseInt(PORT);
			//Wait a second for LoadLibraryService to bind
			android.os.SystemClock.sleep(1000);
			try {
				if (mBound) {
					String connected = lls.createSocket(ip, port);
					updateStatus("Connected: " + connected);
				}
			} catch (Exception e) {
				updateStatus(e.toString());
				connected_bool = "not_connected";
				e.printStackTrace();
			}
			return connected_bool;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			updateStatus("Task finished.");
			dialogHide();
			updateState();
			//connect_button.setEnabled(false);
		}
	}
	
	class UpdateStateTask extends AsyncTask<String, Void, String> {
		protected void onPreExecute() {
		}
		
		protected String doInBackground(String... strings) {
			sendMessage("get_state");
			String state = receiveMessage();
			String[] stateAttributes = state.split(";");
			if (stateAttributes[0].equals("1")) {
				song_playing = true;
				artist = stateAttributes[1];
				album = stateAttributes[2];
				song = stateAttributes[3];
			}
			else
				song_playing = false;
			return "finished";
		}
		
		@Override
		protected void onPostExecute(String result) {
			updateState();
		}
	}
		
	
	class LoadLibraryTask extends AsyncTask<String, Void, String> {
		protected void onPreExecute() {			
			loadingBarShow();
		}
		
		protected String doInBackground(String... inputs) {
			sendMessage("get_num_of_songs");
			String response = receiveMessage();
			int numOfSongs = Integer.parseInt(response);
			loadingBar.setMax(numOfSongs);
			sendMessage("load_library");
			String track_list = receiveMessage();
			String[] songAttributes = track_list.split("//");
			for (String song : songAttributes) {
				String[] attributes = song.split(";");
				//Add song to database
				datasource.createSong(attributes[0], attributes[1], attributes[2], attributes[3], attributes[4], attributes[5]);
				lbHandler.sendMessage(lbHandler.obtainMessage());
			}
			
			String[] artists = datasource.getArtists();
			loadList("artist", artists, "", "");
			
			return "finished";
		}
		
		@Override
		protected void onPostExecute(String result) {
			loadingBarHide();
			
		}
	}
}


