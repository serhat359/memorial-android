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
	
	public static boolean runImport(AssetManager assetManager, Debugable activity)
			throws FileNotFoundException, IOException, Exception{

		String seperator = wordDefaultSeperator;

		InputStream fis = assetManager.open("kanji.txt");
		BufferedReader br = new BufferedReader(
				new InputStreamReader(fis, Charset.forName("UTF-8")));

		MainActivity.db.beginTransaction();
		
		try{
			for(String line = br.readLine(); line != null; line = br.readLine()){
				String[] tokens = line.split(seperator, 2);

				try{
					MainActivity.db.updateQuestion(tokens);
				}
				catch(ArrayIndexOutOfBoundsException e1){
					String message = "Could not split \"" + line + "\"\n" + "Format is: \"word"
							+ " - " + "word\"";
					activity.debug(message);
					break;
				}
			}

			MainActivity.db.commitTransaction();
			activity.debug("Update successful!");
		}
		catch(Exception e){
			MainActivity.db.rollBackTransaction();
			activity.debug("Update failed");
		}

		br.close();
		return true;
	}

}
