package com.example.memorialandroid;

import android.support.v4.app.FragmentActivity;

import java.io.*;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends FragmentActivity{

	static TextView debugView;

	static DatabaseHandler db;
	static int numrows = -1;
	static Button show;
	static TextView qtext;
	static TextView atext;
	static ValButton[] buttons;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set Debug
		debugView = (TextView)findViewById(R.id.debug);

		// Actual Code Start
		String sampleText = getTextOfSample("sample.txt");

		debug(sampleText);

		//////////////////////////////////////////// Code Start

		debug("Program started");

		createDatabase();

		debug("Created database");

		prepareGUI();

		debug("prepared the GUI");

		countRows();

		debug("Counted the rows");

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
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id == R.id.action_settings){
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void prepareGUI(){
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");

		/*
		// question panel
		qtext = new MyTextField("");
		qtext.setFont(new Font("MS Mincho", Font.PLAIN, 64));
		q.add(qtext, BorderLayout.CENTER);
		
		atext = new MyTextField("");
		atext.setFont(new Font("MS PMincho", Font.PLAIN, 36));
		a.add(atext, BorderLayout.CENTER);
		
		// grade panel
		buttons = new MyButton[] { new MyButton("Very Rarely", 10), new MyButton("Rarely", 3),
				new MyButton("Often", 1), new MyButton("Very Often", 0) };
		for(int i = 0; i < buttons.length; i++)
			grade.add(buttons[i]);*/

		show = (Button)findViewById(R.id.showAnswer);
		atext = (TextView)findViewById(R.id.answerView);
		qtext = (TextView)findViewById(R.id.questionView);

		buttons = new ValButton[] { new ValButton((Button)findViewById(R.id.veryRarely), 10),
				new ValButton((Button)findViewById(R.id.rarely), 3),
				new ValButton((Button)findViewById(R.id.often), 1),
				new ValButton((Button)findViewById(R.id.veryOften), 0) };
	}

	public static void countRows(){
		try{
			numrows = db.getCount();
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

	public static void buttonsEnabled(boolean b){
		for(int i = buttons.length - 1; i >= 0; i--)
			buttons[i].button.setEnabled(b);
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
