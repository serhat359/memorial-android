package com.example.memorialandroid;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper{

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "cardsManager";
	private static final String TABLE_CARDS = "CARDS";

	public DatabaseHandler(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		String createTableQuery = "CREATE TABLE " + TABLE_CARDS
				+ " (FRONT TEXT NOT NULL,  BACK TEXT NOT NULL,  REMAINING INT NOT NULL)";

		db.execSQL(createTableQuery);

		String indexQuery = "CREATE UNIQUE INDEX frontIndex ON " + TABLE_CARDS + " (front)";

		db.execSQL(indexQuery);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARDS);
		onCreate(db);
	}
	
	public int getCount(){
		SQLiteDatabase db = this.getWritableDatabase();
		
		Cursor cursor = db.rawQuery("select count(*) from cards", null); 
		
		cursor.moveToFirst();
		
		int count = cursor.getInt(0);
		
		return count;
	}

	public String getQuestion(int numrows){
		SQLiteDatabase db = this.getWritableDatabase();
		
		Cursor cursor;
		
		while(true){
			int n = (int)(Math.random() * numrows);
			cursor = db.rawQuery("select * from cards limit " + n + ",1", null);
			int rem = Integer.parseInt(cursor.getString(3));
			if(rem == 0)
				break;
			db.execSQL("update cards set remaining='" + (rem - 1)
					+ "' where front='" + cursor.getString(1) + "'");
		}
		
		return cursor.getString(1);
	}
}
