package com.example.memorialandroid;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

public class SearchActivity extends Activity {

	CheckBox wholeWordCheckBox;
	EditText searchEditText;

	final Handler composingHandler = new Handler();
	final Runnable composingRunnable = new Runnable() {
		@Override
		public void run(){
			performSearch(searchEditText.getText().toString(), wholeWordCheckBox.isChecked());
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		wholeWordCheckBox = (CheckBox) findViewById(R.id.checkbox_wholeWord);
		wholeWordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				composingHandler.removeCallbacks(composingRunnable);
				composingHandler.postDelayed(composingRunnable, 500);
			}
		});

		searchEditText = (EditText) findViewById(R.id.searchInDictionary);
		searchEditText.addTextChangedListener(new TextWatcher() {
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

	public void performSearch(String s, boolean isWholeWord){
		if(s.length() > 0){
			ListView listView = (ListView) findViewById(R.id.searchResultListView);

			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@SuppressLint("NewApi")
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id){
					String frontMessage = ((TextView) view.findViewById(android.R.id.text2)).getText().toString();

					Functions.copyStringToClipboard(SearchActivity.this, frontMessage);
					Toast.makeText(SearchActivity.this, "Text copied", Toast.LENGTH_SHORT).show();

					DialogMessage.showDialog(SearchActivity.this, frontMessage);
				}

			});

			ArrayList<Card> cards = getDB().getSearchResult(s, isWholeWord);

			ListAdapter adapter = new ListAdapter(cards);

			listView.setAdapter(adapter);
			registerForContextMenu(listView);
		}
	}

	private DatabaseHandler getDB(){
		return MainActivity.db;
	}

}