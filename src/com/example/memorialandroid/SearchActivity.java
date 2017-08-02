package com.example.memorialandroid;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

public class SearchActivity extends Activity{

	EditText searchEditText;

	final Handler composingHandler = new Handler();
	final Runnable composingRunnable = new Runnable(){
		@Override
		public void run(){
			performSearch(searchEditText.getText().toString());
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		searchEditText = (EditText)findViewById(R.id.searchInDictionary);
		searchEditText.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(final Editable s){
				composingHandler.removeCallbacks(composingRunnable);
				composingHandler.postDelayed(composingRunnable, 500);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after){
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count){
			}
		});
	}

	public void performSearch(String s){
		ListView listView = (ListView)findViewById(R.id.searchResultListView);

		ArrayList<Card> favorites = getDB().getSearchResult(s);

		ListAdapter adapter = new ListAdapter(favorites);

		listView.setAdapter(adapter);
		registerForContextMenu(listView);
	}

	private DatabaseHandler getDB(){
		return MainActivity.db;
	}

}