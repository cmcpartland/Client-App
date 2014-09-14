package com.example.clientapp;

import com.example.clientapp.LoadLibraryService.LocalBinder;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ShowListActivity extends Activity {
//	boolean mBound = false;
	private ListView lv;
	private TextView header;
	private ProgressDialog dialog;
	private Handler lltHandler;
//	private LoadLibraryService lls;
	private ArrayAdapter<String> adapter;
	private View headerView;
	String itemType = "";
	String choice = "";
	String artistChoice = "";
	String albumChoice = "";
	String[] listItems;
	
//    /** Defines callbacks for service binding, passed to bindService() */
//    private ServiceConnection mConnection = new ServiceConnection() {
//  
//        @Override
//        public void onServiceConnected(ComponentName className,
//                IBinder service) {
//            // We've bound to LocalService, cast the IBinder and get LocalService instance
//            LocalBinder binder = (LocalBinder) service;
//            lls = binder.getService();
//            mBound = true;
//            dialogHide();
//            Toast.makeText(getApplicationContext(),"Service Connected", Toast.LENGTH_SHORT).show();
//            int response = lls.getTestInt();
//            Toast.makeText(getApplicationContext(), String.valueOf(response), Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            mBound = false;
//        }
//    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_library);
		dialog = new ProgressDialog(this);
		
		//Toast.makeText(this, "hello", Toast.LENGTH_LONG).show();
		lv = (ListView) findViewById(R.id.listView);
		header = (TextView) findViewById(R.id.header);
		
		
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String item = (String) parent.getItemAtPosition(position);
				choice = item;
				finish();
			}
		});
		
		lltHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String[] items = (String[])msg.obj;
				Context c = getApplicationContext();
				adapter = new ArrayAdapter<String>(c, R.layout.list_item_black, items);
				lv.setAdapter(adapter);
			}
		};
	}
	
	@Override
	public void finish() {
		Intent i = new Intent(getApplicationContext(), MainActivity.class);

		i.putExtra("item_type", itemType);
		i.putExtra("choice", choice);
		i.putExtra("artist_choice", artistChoice);
		i.putExtra("album_choice", albumChoice);
		setResult(RESULT_OK, i);
		super.finish();
	}
	
	void dialogShow(String message) {
	    dialog.setMessage(message);
	    dialog.show();
	}
	
	void dialogHide() {
	    dialog.dismiss();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		itemType = extras.getString("item_type");
		artistChoice = extras.getString("artist_choice");
		albumChoice = extras.getString("album_choice");
		listItems = extras.getStringArray("list_items");
		if (itemType.equals("album"))
			header.setText("Albums by " + artistChoice);
		else if (itemType.equals("song")) 
			header.setText("Songs on " + albumChoice);
		else
			header.setText("Artists");
		
//		lv.removeHeaderView(headerView);
//		headerView = getLayoutInflater().inflate(R.layout.header,  null);
//		lv.addHeaderView(headerView);
		loadList(listItems);
	}
	
//    @Override
//    protected void onStop() {
//        super.onStop();
//        // Unbind from the service
//        if (mBound) {
//            unbindService(mConnection);
//            mBound = false;
//        }
//    }
    
	private void loadList(String[] listItems) {
		Message msg = new Message();
		msg.obj = listItems;
		lltHandler.sendMessage(msg);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (itemType.equals("album")) {
				itemType = "none";
				artistChoice = "";
				albumChoice = "";
				finish();
			}
			else if (itemType.equals("song")) {
				itemType = "artist";
				choice = artistChoice;
				albumChoice = "";
				finish();
			}
			else {
				itemType = "pass";
				artistChoice = "";
				albumChoice = "";
				finish();
			}
			return true;	
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
    
}
