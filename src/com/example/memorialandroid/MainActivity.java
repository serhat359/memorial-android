package com.example.memorialandroid;

import android.support.v4.app.FragmentActivity;

import java.io.*;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements Debugable{

	private static TextView debugView;
	private static boolean debugEnabled = false;

	private static DatabaseHandler db;
	static int numrows = -1;
	static Button show;
	static TextView qtext;
	static TextView atext;
	static Button[] buttons;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set Debug
		debugView = (TextView)findViewById(R.id.debug);
		debugView.setText("");

		//////////////////////////////////////////// Code Start

		debug("Activity created");

		createDatabase();
		prepareGUI();
		countRows();
		start();

		debug("Started the program");

		debug("Number of rows: " + numrows);
	}

	private void createDatabase(){
		db = new DatabaseHandler(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.id.action_settings:
				return true;

			case R.id.update:
				try{
					ActionHandler.runImport(getAssets(), this, db);
					start();
				}
				catch(Exception e){
					debug(e);
				}
				return true;

			case R.id.search:
				try{
					Intent launchNewIntent = new Intent(this, SearchActivity.class);
					startActivityForResult(launchNewIntent, 0);
				}
				catch(Exception e){
					debug(e);
				}
				return false;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void prepareGUI(){
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");

		show = (Button)findViewById(R.id.showAnswer);
		atext = (TextView)findViewById(R.id.answerView);
		qtext = (TextView)findViewById(R.id.questionView);

		qtext.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "MSMINCHO.TTF"));

		buttons = new Button[] { (Button)findViewById(R.id.veryRarely),
				(Button)findViewById(R.id.rarely), (Button)findViewById(R.id.often),
				(Button)findViewById(R.id.veryOften), };
	}

	public void countRows(){
		try{
			numrows = db.getCount();

			if(numrows <= 1){
				debug("Importing records");
				String sqlQuery = getAssetContent("importSql.txt");
				db.importRecords(sqlQuery, this);
				numrows = db.getCount();
				debug("New count is: " + numrows);
			}
		}
		catch(Exception e){
			debug(e.getMessage());
		}
	}

	public void start(){
		buttonsEnabled(false);
		if(numrows == 0){
			show.setEnabled(false);
			qtext.setText("");
			atext.setText("");
		}
		else{
			show.setEnabled(true);
			atext.setText("");
			try{
				qtext.setText(db.getQuestion(numrows));
			}
			catch(Exception e){
				debug(e.getMessage());
			}
		}
	}

	public static String getStackTrace(Exception e){
		StringBuilder sb = new StringBuilder();

		sb.append(e.getMessage() + '\n');

		for(StackTraceElement i: e.getStackTrace()){
			sb.append("Line " + i.getLineNumber() + " in " + i.getMethodName() + '\n');
		}

		return sb.toString();
	}

	public void buttonsEnabled(boolean b){
		for(int i = buttons.length - 1; i >= 0; i--)
			buttons[i].setEnabled(b);
	}

	@SuppressWarnings("unused")
	public void showAnswer_click(View v){
		show.setEnabled(false);
		buttonsEnabled(true);
		atext.setText(db.getAnswer());
	}

	public void rateClicked(View v){
		Button button = (Button)v;

		int degree = Integer.parseInt((String)button.getTag());

		buttonsEnabled(false);

		db.setDegree(degree, this);

		debug("you selected " + degree);

		start();
	}

	public void debug(String message){
		if(debugEnabled)
			debugView.setText(debugView.getText().toString() + '\n' + message);
	}

	private String getAssetContent(String fileName){
		try{
			InputStream fis = getResources().getAssets().open(fileName);

			String text = readStream(fis);

			return text;
		}
		catch(IOException e){
			return e.getMessage();
		}
	}

	@Override
	public AssetManager getAssets(){
		return getResources().getAssets();
	}

	// TODO dump ekle

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

	public void print(String message){
		debug(message);
	}

	public void debug(Exception e){
		String message = getStackTrace(e);

		debug(message);
	}
}
