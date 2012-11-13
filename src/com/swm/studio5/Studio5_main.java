package com.swm.studio5;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.swm.studio5.rtsp.*;
import com.swm.studio5.tool.Setting;
import com.swm.studio5.ui.CallScreen;
import com.swm.studio5.ui.VideoCamera;

public class Studio5_main extends Activity {
	Context mContext = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studio5_main);
        
        // set default value
        ((EditText)findViewById(R.id.tv_remote_addr)).setText("127.0.0.1");
        ((EditText)findViewById(R.id.tv_remote_port)).setText("8080");
        ((EditText)findViewById(R.id.tv_local_port)).setText("8050");
        
        // proc event
        setEvents(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_studio5_main, menu);
        return true;
    }
    
    public void setEvents(Studio5_main v) {
    	Button b = (Button)v.findViewById(R.id.start);
    	b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// save setting
				Setting.remote_url = ((EditText)findViewById(R.id.tv_remote_addr)).getText().toString();
				Setting.remote_port = Integer.parseInt(((EditText)findViewById(R.id.tv_remote_port)).getText().toString());
				Setting.local_port = Integer.parseInt(((EditText)findViewById(R.id.tv_local_port)).getText().toString());
				
				// start VLC Connection
				Intent i = new Intent(mContext, VideoCamera.class);
				mContext.startActivity(i);
			}
		});
    }
}
