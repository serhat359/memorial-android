package com.example.memorialandroid;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import android.content.res.AssetManager;

public class ActionHandler{

	private static final String wordDefaultSeperator = " – ";

	public static boolean runUpdate(AssetManager assetManager, Debugable activity, DatabaseHandler db)
			throws FileNotFoundException, IOException, Exception{

		String seperator = wordDefaultSeperator;

		InputStream fis = assetManager.open("kanji.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

		db.updateEntries(br, activity, seperator);
		
		br.close();
		return true;
	}

}
