package com.example.memorialandroid;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "cardsManager";
	private static final String TABLE_CARDS = "CARDS";
	private static final String TABLE_PROFILE = "PROFILES";
	private static final String COL_REMAINING = "REMAINING";
	private static final String COL_FRONT = "FRONT";
	private static final String COL_BACK = "BACK";

	private Cursor questionCursor;
	private String currentTableName = TABLE_CARDS;
	private String currentAssetName = "kanji";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		createTable(db, TABLE_CARDS);
	}

	public void createTable(SQLiteDatabase db, String tableName){
		String createTableQuery = "CREATE TABLE " + tableName
				+ " (FRONT TEXT NOT NULL,  BACK TEXT NOT NULL,  REMAINING INT NOT NULL)";

		db.execSQL(createTableQuery);

		String indexQuery = "CREATE UNIQUE INDEX " + tableName + "_frontIndex ON " + tableName + " (front)";

		db.execSQL(indexQuery);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARDS);
		onCreate(db);
	}

	public int getCount(){
		SQLiteDatabase db = this.getWritableDatabase();

		Cursor cursor = db.rawQuery("select count(*) from " + getTableName(), null);
		cursor.moveToFirst();

		int count = cursor.getInt(0);

		cursor.close();

		return count;
	}

	public String getQuestion(int numrows){
		SQLiteDatabase db = this.getWritableDatabase();

		while (true){
			int n = (int) (Math.random() * numrows);

			questionCursor = db.rawQuery("select * from " + getTableName() + " limit " + n + ",1", null);
			questionCursor.moveToFirst();

			int rem = questionCursor.getInt(questionCursor.getColumnIndex(COL_REMAINING));
			if(rem == 0)
				break;
			db.execSQL("update " + getTableName() + " set remaining='" + (rem - 1) + "' where front='"
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
			db.execSQL("update " + getTableName() + " set remaining='" + degree + "' where front='"
					+ questionCursor.getString(questionCursor.getColumnIndex(COL_FRONT)) + "'");
		}
		catch (Exception e){
			method.debug(e.getMessage());
		}
	}

	public boolean importRecords(String filePath, Debugable method) throws IOException{
		SQLiteDatabase db = this.getWritableDatabase();

		boolean isSuccessful = false;

		BufferedReader br = null;
		try{
			db.beginTransaction();
			db.execSQL("delete from " + getTableName());

			method.debug("deleted all records");

			br = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = br.readLine()) != null){
				db.execSQL(line);
			}

			method.debug("inserted all records");

			db.setTransactionSuccessful();
			db.endTransaction();

			isSuccessful = true;
		}
		catch (Exception e){
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
		Cursor rs = db.rawQuery("SELECT back FROM " + getTableName() + " WHERE front='" + tokens[0] + "'", null);

		if(!rs.moveToFirst()){
			db.execSQL("insert into " + getTableName() + " (front, back, remaining)" + " values ('" + tokens[0] + "', '"
					+ tokens[1] + "', '0')");
		}
		else{
			String oldBack = rs.getString(rs.getColumnIndex(COL_BACK));

			if(!oldBack.equals(tokens[1])){
				db.execSQL("update " + getTableName() + " set back = '" + tokens[1] + "' where front = '" + tokens[0]
						+ "'");
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
				catch (ArrayIndexOutOfBoundsException e1){
					String message = "Could not split \"" + line + "\"\n" + "Format is: \"word" + " - " + "word\"";
					activity.debug(message);
					break;
				}
			}

			db.setTransactionSuccessful();
			db.endTransaction();
			activity.debug("Update successful!");
		}
		catch (Exception e){
			db.endTransaction();
			activity.debug("Update failed");
			activity.debug(e.getMessage());
		}

	}

	public ArrayList<Card> getSearchResult(String q, boolean isWholeWord){
		ArrayList<Card> list;

		if(!isWholeWord){
			String arg = "%" + q + "%";

			String query = "SELECT * FROM " + getTableName() + " WHERE back LIKE ? or front like ?";

			list = runSelectQuery(query, new String[] { arg, arg });
		}
		else{
			String arg1 = q + " %";
			String arg2 = "% " + q;
			String arg3 = "% " + q + " %";
			String arg4 = q;

			String where1 = "(front LIKE ? or front LIKE ? or front LIKE ? or front LIKE ?)";
			String where2 = "(back  LIKE ? or back  LIKE ? or back  LIKE ? or back  LIKE ?)";

			String query = String.format("SELECT * FROM " + getTableName() + " WHERE (%s or %s)", where1, where2);

			list = runSelectQuery(query, new String[] { arg1, arg2, arg3, arg4, arg1, arg2, arg3, arg4 });
		}

		return list;
	}

	public ArrayList<Card> getAllCards(){
		String query = "SELECT * FROM " + getTableName() + "";

		ArrayList<Card> list = runSelectQuery(query);

		return list;
	}

	public ArrayList<String> getProfiles(){
		SQLiteDatabase db = this.getWritableDatabase();

		assureProfilesTableExists(db);

		String selectQuery = "SELECT NAME FROM " + TABLE_PROFILE;
		ArrayList<String> list = runSelectQueryString(selectQuery);

		if(list.isEmpty()){
			insertProfile(db, "Japanese", TABLE_CARDS, "kanji");
			list.add("Japanese");
		}

		return list;
	}

	public void checkProfile(){
		SQLiteDatabase db = this.getWritableDatabase();

		assureProfilesTableExists(db);

		String query = "select * from " + TABLE_PROFILE + " where ISSELECTED = 1";
		resetProfile(db, query, null);
	}

	public void createProfile(String profileName){
		SQLiteDatabase db = this.getWritableDatabase();

		String tableName = "prof_" + profileName;

		createTable(db, tableName);
		insertProfile(db, profileName, tableName, "kanji_" + profileName.toLowerCase(Locale.US));
	}

	public void setProfile(String profile){
		SQLiteDatabase db = this.getWritableDatabase();

		db.execSQL("update " + TABLE_PROFILE + " set ISSELECTED = 0 where ISSELECTED = 1");
		db.execSQL("update " + TABLE_PROFILE + " set ISSELECTED = 1 where NAME = ?", new String[] { profile });

		String profileQuery = "select * from " + TABLE_PROFILE + " where NAME = ?";

		resetProfile(db, profileQuery, new String[] { profile });
	}

	public String getAssetName(){
		return this.currentAssetName;
	}

	private String getTableName(){
		return this.currentTableName;
	}

	private void assureProfilesTableExists(SQLiteDatabase db){
		String query = "CREATE TABLE IF NOT EXISTS " + TABLE_PROFILE
				+ "(NAME TEXT NOT NULL,  TABLE_NAME TEXT NOT NULL,  ASSET_NAME TEXT NOT NULL,  ISSELECTED INT NOT NULL)";

		db.execSQL(query);
	}

	private void resetProfile(SQLiteDatabase db, String query, String[] params){
		Cursor cursor = db.rawQuery(query, params);

		if(cursor.moveToFirst()){
			this.currentTableName = cursor.getString(cursor.getColumnIndex("TABLE_NAME"));
			this.currentAssetName = cursor.getString(cursor.getColumnIndex("ASSET_NAME"));
		}

		cursor.close();
	}

	private void insertProfile(SQLiteDatabase db, String profileName, String tableName, String assetName){
		String resetQuery = "update " + TABLE_PROFILE + " set ISSELECTED = 0";
		db.execSQL(resetQuery);

		String insertQuery = "Insert into " + TABLE_PROFILE + "(NAME, TABLE_NAME, ASSET_NAME, ISSELECTED) "
				+ "values ('" + profileName + "', '" + tableName + "', '" + assetName + "', 1)";
		db.execSQL(insertQuery);

		this.currentTableName = tableName;
		this.currentAssetName = assetName;
	}

	private ArrayList<Card> runSelectQuery(String query){
		return runSelectQuery(query, null);
	}

	private ArrayList<Card> runSelectQuery(String query, String[] args){
		ArrayList<Card> cards = new ArrayList<Card>();

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, args);

		if(cursor.moveToFirst()){
			do{
				Card card = cursorToCard(cursor);
				cards.add(card);
			}
			while (cursor.moveToNext());
		}

		cursor.close();

		return cards;
	}

	private ArrayList<String> runSelectQueryString(String query){
		ArrayList<String> items = new ArrayList<String>();

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		if(cursor.moveToFirst()){
			do{
				String s = cursor.getString(0);
				items.add(s);
			}
			while (cursor.moveToNext());
		}

		cursor.close();

		return items;
	}

	private Card cursorToCard(Cursor cursor){
		Card card = new Card();
		card.front = cursor.getString(0);
		card.back = cursor.getString(1);
		card.remaining = cursor.getInt(2);
		return card;
	}
}
