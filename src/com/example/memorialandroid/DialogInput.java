package com.example.memorialandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

public class DialogInput {

	public static void showDialog(Context context, String title, String message, final RunnableParam<String> okAction){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle(title);
		builder.setMessage(message);

		final EditText view = new EditText(context);
		builder.setView(view);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which){
				okAction.run(view.getText().toString());
				dialog.dismiss();
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.dismiss();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();

	}
}
