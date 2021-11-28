package com.example.memorialandroid;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import android.content.res.AssetManager;

public class ActionHandler {

	private static final String wordDefaultSeperator = " – ";

	public static boolean runUpdate(AssetManager assetManager, Debugable activity, DatabaseHandler db)
			throws FileNotFoundException, IOException, Exception{

		InputStream fis = assetManager.open(db.getAssetName() + ".txt");
		return runUpdate(fis, activity, db);
	}
	
	public static boolean runUpdate(String fileName, Debugable activity, DatabaseHandler db)
			throws FileNotFoundException, IOException, Exception {

		InputStream fis = new FileInputStream(fileName);
		return runUpdate(fis, activity, db);
	}
	
	private static boolean runUpdate(InputStream fis, Debugable activity, DatabaseHandler db)
			throws FileNotFoundException, IOException, Exception{
		BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

		db.updateEntries(br, activity, wordDefaultSeperator);

		br.close();
		return true;
	}

}
