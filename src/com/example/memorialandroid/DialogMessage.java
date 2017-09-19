package com.example.memorialandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

public class DialogMessage{
	public static void showDialog(Context context, String message){

		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle("Message");

		TextView myMsg = new TextView(context);
		myMsg.setText(message);
		myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		myMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
		myMsg.setTypeface(MainActivity.fontFamily);
		if(Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD
				|| Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD_MR1){
			myMsg.setTextColor(Color.WHITE);
		}

		builder.setView(myMsg);

		builder.setNegativeButton("OK", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.dismiss();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}
}
