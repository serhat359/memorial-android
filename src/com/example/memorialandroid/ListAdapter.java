package com.example.memorialandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends BaseAdapter {

	private final List<ListAdapterObject> mData;

	private ListAdapter(List<ListAdapterObject> list) {
		mData = list;
	}

	public static ListAdapter fromStrings(List<String> strings){
		ArrayList<ListAdapterObject> list = new ArrayList<ListAdapterObject>();

		for(String s : strings){
			list.add(new ListAdapterObject(s, s));
		}

		return new ListAdapter(list);
	}

	public static ListAdapter fromCards(List<Card> map){
		ArrayList<ListAdapterObject> list = new ArrayList<ListAdapterObject>();

		for(Card card : map){
			list.add(new ListAdapterObject(card.back, card.front));
		}

		return new ListAdapter(list);
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
	public ListAdapterObject getItem(int position){
		return mData.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		final View result;

		if(convertView == null){
			result = LayoutInflater.from(parent.getContext()).inflate(R.layout.two_line_list_item, parent,
					false);
		}
		else{
			result = convertView;
		}

		ListAdapterObject element = getItem(position);

		((TextView) result.findViewById(R.id.text_above)).setText(element.textAbove);
		((TextView) result.findViewById(R.id.text_below)).setText(element.textBelow);

		return result;
	}

}
