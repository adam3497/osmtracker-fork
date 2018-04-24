package me.guillaumin.android.osmtracker.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import me.guillaumin.android.osmtracker.R;

/**
 * Created by adma9717 on 19/04/18.
 */

public class CustomAdapterListAvailable extends BaseAdapter {

    private Context context;
    private ArrayList<ItemListAvailableUtil> listItems;

    public CustomAdapterListAvailable(Context context, ArrayList<ItemListAvailableUtil> listItems) {
        this.context = context;
        this.listItems = listItems;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //instance the custom item in the list
        ItemListAvailableUtil itemListAvailableUtil = (ItemListAvailableUtil) getItem(position);

        //inflate the custom appearance item
        convertView = LayoutInflater.from(context).inflate(R.layout.item_available_layout, null);

        //reference the attr of each item
        TextView layoutTittle = (TextView) convertView.findViewById(R.id.txt_item_title);
        TextView layoutDescription = (TextView) convertView.findViewById(R.id.txt_item_description);;

        //put the title and description of the item
        layoutTittle.setText(itemListAvailableUtil.getAvailableLayoutName());
        layoutDescription.setText(itemListAvailableUtil.getAvailableLayoutDescription());

        return convertView;
    }
}
