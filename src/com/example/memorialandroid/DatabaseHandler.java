package com.example.memorialandroid;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper{

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "cardsManager";
	private static final String TABLE_CARDS = "CARDS";
	private static final String COL_REMAINING = "REMAINING";
	private static final String COL_FRONT = "FRONT";
	private static final String COL_BACK = "BACK";

	private Cursor cursor;

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

	public void execQuery(String query){
		SQLiteDatabase db = this.getWritableDatabase();

		db.execSQL(query);
	}

	public Cursor execSelectQuery(String query){
		SQLiteDatabase db = this.getWritableDatabase();

		Cursor cursor = db.rawQuery(query, null);

		return cursor;
	}

	public void beginTransaction(){
		SQLiteDatabase db = this.getWritableDatabase();

		db.beginTransaction();
	}

	public void commitTransaction(){
		SQLiteDatabase db = this.getWritableDatabase();

		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void rollBackTransaction(){
		SQLiteDatabase db = this.getWritableDatabase();

		db.endTransaction();
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

		while(true){
			int n = (int)(Math.random() * numrows);

			cursor = db.rawQuery("select * from cards limit " + n + ",1", null);

			cursor.moveToFirst();

			int rem = cursor.getInt(cursor.getColumnIndex(COL_REMAINING));
			if(rem == 0)
				break;
			db.execSQL("update cards set remaining='" + (rem - 1) + "' where front='"
					+ cursor.getString(cursor.getColumnIndex(COL_FRONT)) + "'");
		}

		return cursor.getString(cursor.getColumnIndex(COL_FRONT));
	}

	public void importRecords(String sql, Debugable method){
		SQLiteDatabase db = this.getWritableDatabase();

		try{
			db.beginTransaction();
			db.execSQL("delete from " + TABLE_CARDS);

			for(String s: sql.split("\n")){
				db.execSQL(s);
			}

			db.setTransactionSuccessful();
			db.endTransaction();
		}
		catch(Exception e){
			method.print(e.getMessage());
		}
	}

	public String getAnswer(){
		return cursor.getString(cursor.getColumnIndex(COL_BACK));
	}

	public void setDegree(int degree, Debugable method){
		SQLiteDatabase db = this.getWritableDatabase();

		try{
			db.execSQL("update cards set remaining='" + degree + "' where front='"
					+ cursor.getString(cursor.getColumnIndex(COL_FRONT)) + "'");
		}
		catch(Exception e){
			method.print(e.getMessage());
		}
	}

	public void updateQuestion(String[] tokens){
		Cursor rs = MainActivity.db
				.execSelectQuery("SELECT back FROM cards WHERE front='" + tokens[0] + "'");

		if(!rs.moveToFirst()){
			MainActivity.db.execQuery("insert into cards (front, back, remaining)" + " values ('"
					+ tokens[0] + "', '" + tokens[1] + "', '0')");
		}
		else{
			String oldBack = rs.getString(rs.getColumnIndex(COL_BACK));

			if(!oldBack.equalsIgnoreCase(tokens[1])){
				MainActivity.db.execSelectQuery("update cards set back = '" + tokens[1]
						+ "' where front = '" + tokens[0] + "'");
			}
		}
	}
}
