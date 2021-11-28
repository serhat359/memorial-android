package com.example.memorialandroid;

import android.support.v4.app.FragmentActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("InlinedApi")
public class MainActivity extends FragmentActivity implements Debugable {

	private static TextView debugView;
	private static boolean debugEnabled = false;

	public static MainActivity instance;
	public static DatabaseHandler db;
	public static Typeface fontFamily;
	static int numrows = -1;
	static Button show;
	static TextView qtext;
	static TextView atext;
	static Button[] buttons;

	private static final int FILE_SELECT_FOR_EXPORT = 0;
	private static final int FILE_SELECT_FOR_IMPORT = 1;

	private static final int PERMISSION_REQUEST_CODE = 1;
	private static final int SEARCH_REQUEST_CODE = 2;
	private static final int PROFILE_REQUEST_CODE = 3;

	private Runnable permissionGrantedAction = null;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		instance = this;

		// Set Debug
		debugView = (TextView) findViewById(R.id.debug);
		debugView.setText("");

		//////////////////////////////////////////// Code Start

		debug("Activity created");

		createDatabase();
		prepareGUI();
		initializeDb();
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
		switch (item.getItemId()) {
		// case R.id.action_settings:
		// return true;

		case R.id.update:
			try{
				ActionHandler.runUpdate(getAssets(), this, db);
				Toast.makeText(this, "Update Completed", Toast.LENGTH_SHORT).show();
				start();
			}
			catch (Exception e){
				debug(e);
			}
			return true;

		case R.id.search:
			try{
				Intent launchNewIntent = new Intent(this, SearchActivity.class);
				startActivityForResult(launchNewIntent, SEARCH_REQUEST_CODE);
			}
			catch (Exception e){
				debug(e);
			}
			return false;

		case R.id.profile:
			try{
				Intent launchNewIntent = new Intent(this, ProfileActivity.class);
				startActivityForResult(launchNewIntent, PROFILE_REQUEST_CODE);
			}
			catch (Exception e){
				debug(e);
			}
			return true;

		case R.id.export:
			requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, new Runnable() {
				@Override
				public void run(){
					try{
						showFileChooser(FILE_SELECT_FOR_EXPORT);
					}
					catch (Exception e){
						debug(e);
					}
				}
			});

			return false;

		case R.id.importFromFile:
			DialogYesNo.showDialog(this, "Confirm Import", "This action will delete all data, continue?",
					new Runnable() {
						@Override
						public void run(){
							requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, new Runnable() {
								@Override
								public void run(){
									try{
										showFileChooser(FILE_SELECT_FOR_IMPORT, "text/plain");
									}
									catch (Exception e){
										debug(e);
									}
								}

							});
						}
					});

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		switch (requestCode) {
		case FILE_SELECT_FOR_EXPORT:
			if(resultCode == RESULT_OK){
				// Get the path of the chosen file
				// TODO this method chooses a file when it should be choosing a folder, fix it
				String path = Functions.getPath(this, data.getData());

				String parentFolder = new File(path).getParent();

				try{
					exportDBToFolder(parentFolder);
				}
				catch (IOException e){
					debug(e.getMessage());
				}
			}
			break;
		case FILE_SELECT_FOR_IMPORT:
			if(resultCode == RESULT_OK){
				// Get the path of the chosen file
				String path = Functions.getPath(this, data.getData());

				if(path == null)
					debug("Error: Path is null");

				try{
					importDBFromFile(path);
				}
				catch (IOException e){
					debug(e.getMessage());
				}
			}
			break;
		case PROFILE_REQUEST_CODE:
			countRows();
			start();
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void prepareGUI(){
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");

		show = (Button) findViewById(R.id.showAnswer);
		atext = (TextView) findViewById(R.id.answerView);
		qtext = (TextView) findViewById(R.id.questionView);

		fontFamily = Typeface.createFromAsset(getResources().getAssets(), "MSMINCHO.TTF");

		qtext.setTypeface(fontFamily);

		buttons = new Button[] { (Button) findViewById(R.id.veryRarely), (Button) findViewById(R.id.rarely),
				(Button) findViewById(R.id.often), (Button) findViewById(R.id.veryOften), };
	}

	public void initializeDb(){
		try{
			db.checkProfile();
		}
		catch (Exception e){
			debug(e.getMessage());
		}
	}

	public void countRows(){
		try{
			numrows = db.getCount();
		}
		catch (Exception e){
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
			catch (Exception e){
				debug(e.getMessage());
			}
		}
	}

	public static String getStackTrace(Exception e){
		StringBuilder sb = new StringBuilder();

		sb.append(e.getMessage() + '\n');

		for(StackTraceElement i : e.getStackTrace()){
			sb.append("Line " + i.getLineNumber() + " in " + i.getMethodName() + '\n');
		}

		return sb.toString();
	}

	public void buttonsEnabled(boolean b){
		for(int i = buttons.length - 1; i >= 0; i--)
			buttons[i].setEnabled(b);
	}

	public void showAnswer_click(View v){
		show.setEnabled(false);
		buttonsEnabled(true);
		atext.setText(db.getAnswer());
	}

	public void rateClicked(View v){
		Button button = (Button) v;

		int degree = Integer.parseInt((String) button.getTag());

		buttonsEnabled(false);

		db.setDegree(degree, this);

		debug("you selected " + degree);

		start();
	}

	@SuppressLint("NewApi")
	public void requestPermission(String permissionID, Runnable onGettingPermission){

		final boolean isAboveKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		if(!isAboveKitKat){
			onGettingPermission.run();
		}
		else{
			permissionGrantedAction = onGettingPermission;

			requestPermissions(new String[] { permissionID }, PERMISSION_REQUEST_CODE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
		switch (requestCode) {
		case PERMISSION_REQUEST_CODE:
			if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
				permissionGrantedAction.run();
			}
			else{
				Toast.makeText(this, "Could not get the permission for that", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void debug(String message){
		if(debugEnabled)
			debugView.setText(debugView.getText().toString() + '\n' + message);
	}

	@Override
	public AssetManager getAssets(){
		return getResources().getAssets();
	}

	private void importDBFromFile(String filePath) throws IOException{
			debug("Importing records");
			boolean isSuccessful = db.importRecords(filePath, this);
			numrows = db.getCount();
			debug("New count is: " + numrows);

			if(isSuccessful)
				Toast.makeText(this, "Successfully imported", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "Import failed", Toast.LENGTH_SHORT).show();

			start();
		}

	private void exportDBToFolder(String directory) throws IOException{
		Date todaysDate = Calendar.getInstance().getTime();

		String fileName = "Memorial Backup "
				+ new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todaysDate) + ".txt";

		File filePath = new File(directory, fileName);

		ArrayList<Card> allCards = db.getAllCards();

		boolean isSuccessful = false;
		Writer out = null;
		try{
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath.getAbsolutePath()), "UTF-8"));

			Locale locale = Locale.getDefault();
			
			for(Card card : allCards){
				String formatted = db.getInsertQuery(card, locale);

				out.write(formatted);
			}

			isSuccessful = true;
		}
		catch (Exception e){
			debug(e.getMessage());
		}
		finally{
			out.close();
		}

		if(isSuccessful){
			Toast.makeText(this, "Exported to " + fileName, Toast.LENGTH_SHORT).show();
		}
	}

	private void showFileChooser(int requestCode){
		showFileChooser(requestCode, "*/*");
	}

	private void showFileChooser(int requestCode, String fileType){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(fileType);
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		try{
			startActivityForResult(Intent.createChooser(intent, "Select a folder to save the file"), requestCode);
		}
		catch (android.content.ActivityNotFoundException ex){
			Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
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
