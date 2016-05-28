package com.example.memorialandroid;

import android.support.v4.app.FragmentActivity;

import java.io.*;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends FragmentActivity{

	static TextView debugView;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set Debug
		debugView = (TextView)findViewById(R.id.debug);

		// Actual Code Start
		String sampleText = getTextOfSample("sample.txt");

		debug(sampleText);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id == R.id.action_settings){
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private static void debug(String message){
		debugView.setText(message);
	}

	private String getTextOfSample(String fileName){
		try{
			InputStream fis = getResources().getAssets().open(fileName);

			String text = readStream(fis);

			return text;
		}
		catch(IOException e){
			// TODO Auto-generated catch block
			return e.getMessage();
		}
	}

	private static String readStream(InputStream is){
		try{
			StringBuilder sb = new StringBuilder(is.available());
			Reader r = new InputStreamReader(is, "UTF-8");
			int c = 0;
			while((c = r.read()) != -1){
				sb.append((char)c);
			}
			return sb.toString();
		}
		catch(IOException e){
			throw new RuntimeException(e);
		}
	}
}
