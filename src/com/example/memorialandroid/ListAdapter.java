package com.example.memorialandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public abstract class ListAdapter<E>extends BaseAdapter{

	private final List<E> mData;

	public ListAdapter(List<E> map){
		mData = map;
	}

	@Override
	public int getCount(){
		return mData.size();
	}

	@Override
	public long getItemId(int position){
		return 0;
	}
	
	@Override
	public E getItem(int position){
		return mData.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		final View result;

		if(convertView == null){
			result = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.two_line_list_item ,
					parent, false);
		}
		else{
			result = convertView;
		}

		E element = getItem(position);

		((TextView)result.findViewById(android.R.id.text1)).setText(elementGetBack(element));
		((TextView)result.findViewById(android.R.id.text2)).setText(elementGetFront(element));

		return result;
	}

	abstract String elementGetFront(E element);

	abstract String elementGetBack(E element);

}
