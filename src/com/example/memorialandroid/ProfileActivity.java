package com.example.memorialandroid;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class ProfileActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);

		final ProfileActivity me = this;

		List<String> profiles = MainActivity.db.getProfiles();
		ListAdapter adapter = ListAdapter.fromStrings(profiles);

		ListView listView = (ListView) findViewById(R.id.profileListView);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				String profile = ((TextView) view.findViewById(R.id.text_below)).getText().toString();

				MainActivity.db.setProfile(profile);
				me.finish();
			}

		});

		listView.setAdapter(adapter);
		registerForContextMenu(listView);
	}

	public void newProfile_click(View v){
		RunnableParam<String> okAction = new RunnableParam<String>() {
			@Override
			public void run(String profileName){
				MainActivity.db.createProfile(profileName);
			}
		};

		DialogInput.showDialog(ProfileActivity.this, "New Profile", "Profile Name", okAction);
	}
}
