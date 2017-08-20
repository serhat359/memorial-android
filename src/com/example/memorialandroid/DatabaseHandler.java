package com.example.memorialandroid;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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

	private Cursor questionCursor;

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

		cursor.close();

		return count;
	}

	public String getQuestion(int numrows){
		SQLiteDatabase db = this.getWritableDatabase();

		while(true){
			int n = (int)(Math.random() * numrows);

			questionCursor = db.rawQuery("select * from cards limit " + n + ",1", null);
			questionCursor.moveToFirst();

			int rem = questionCursor.getInt(questionCursor.getColumnIndex(COL_REMAINING));
			if(rem == 0)
				break;
			db.execSQL("update cards set remaining='" + (rem - 1) + "' where front='"
					+ questionCursor.getString(questionCursor.getColumnIndex(COL_FRONT)) + "'");
		}

		return questionCursor.getString(questionCursor.getColumnIndex(COL_FRONT));
	}

	public String getAnswer(){
		return questionCursor.getString(questionCursor.getColumnIndex(COL_BACK));
	}

	public void setDegree(int degree, Debugable method){
		SQLiteDatabase db = this.getWritableDatabase();

		try{
			db.execSQL("update cards set remaining='" + degree + "' where front='"
					+ questionCursor.getString(questionCursor.getColumnIndex(COL_FRONT)) + "'");
		}
		catch(Exception e){
			method.debug(e.getMessage());
		}
	}

	public boolean importRecords(String filePath, Debugable method) throws IOException{
		SQLiteDatabase db = this.getWritableDatabase();

		boolean isSuccessful = false;
		
		BufferedReader br = null;
		try{
			db.beginTransaction();
			db.execSQL("delete from " + TABLE_CARDS);

			method.debug("deleted all records");
			
			br = new BufferedReader(new FileReader(filePath));
			String line = null;
			while((line = br.readLine()) != null){
				db.execSQL(line);
			}
			
			method.debug("inserted all records");

			db.setTransactionSuccessful();
			db.endTransaction();
			
			isSuccessful = true;
		}
		catch(Exception e){
			method.debug(e.getMessage());
			
			db.endTransaction();
		}
		finally{
			if(br != null)
				br.close();
		}
		
		return isSuccessful;
	}

	private void updateQuestion(String[] tokens, SQLiteDatabase db){
		Cursor rs = db.rawQuery("SELECT back FROM cards WHERE front='" + tokens[0] + "'", null);

		if(!rs.moveToFirst()){
			db.execSQL("insert into cards (front, back, remaining)" + " values ('" + tokens[0] + "', '" + tokens[1]
					+ "', '0')");
		}
		else{
			String oldBack = rs.getString(rs.getColumnIndex(COL_BACK));

			if(!oldBack.equalsIgnoreCase(tokens[1])){
				db.execSQL("update cards set back = '" + tokens[1] + "' where front = '" + tokens[0] + "'");
			}
		}

		rs.close();
	}

	public void updateEntries(BufferedReader br, Debugable activity, String seperator){
		SQLiteDatabase db = this.getWritableDatabase();

		db.beginTransaction();

		try{
			for(String line = br.readLine(); line != null; line = br.readLine()){
				String[] tokens = line.split(seperator, 2);

				try{
					updateQuestion(tokens, db);
				}
				catch(ArrayIndexOutOfBoundsException e1){
					String message = "Could not split \"" + line + "\"\n" + "Format is: \"word" + " - " + "word\"";
					activity.debug(message);
					break;
				}
			}

			db.setTransactionSuccessful();
			db.endTransaction();
			activity.debug("Update successful!");
		}
		catch(Exception e){
			db.endTransaction();
			activity.debug("Update failed");
			activity.debug(e.getMessage());
		}

	}

	public ArrayList<Card> getSearchResult(String q){
		String query = "SELECT * FROM cards WHERE back LIKE '%" + q + "%' limit 10";

		ArrayList<Card> list = runSelectQuery(query);

		return list;
	}

	public ArrayList<Card> getAllCards(){
		String query = "SELECT * FROM cards";

		ArrayList<Card> list = runSelectQuery(query);

		return list;
	}

	private ArrayList<Card> runSelectQuery(String query){
		ArrayList<Card> cards = new ArrayList<Card>();

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		if(cursor.moveToFirst()){
			do{
				Card card = cursorToCard(cursor);
				cards.add(card);
			}
			while(cursor.moveToNext());
		}

		cursor.close();

		return cards;
	}

	private Card cursorToCard(Cursor cursor){
		Card card = new Card();
		card.front = cursor.getString(0);
		card.back = cursor.getString(1);
		card.remaining = cursor.getInt(2);
		return card;
	}
}
