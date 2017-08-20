package com.example.memorialandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogYesNo{

	public static void showDialog(Context context, String title, String message, final Runnable yesAction){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		builder.setTitle(title);
		builder.setMessage(message);
		
		builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			@Override
		    public void onClick(DialogInterface dialog, int which) {
				yesAction.run();
		        dialog.dismiss();
		    }
		});
		
		builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.dismiss();
		    }
		});
		
		AlertDialog alert = builder.create();
		alert.show();
	}
}
