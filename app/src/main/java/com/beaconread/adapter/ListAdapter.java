package com.beaconread.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beaconread.ListActivity;
import com.beaconread.R;

import java.util.List;

/**
 * Adapter class for beacon list
 *
 * @author Rakesh
 */
public class ListAdapter extends BaseAdapter {

    Activity mActivity;
    List<String> data;

    public ListAdapter(Activity mActivity, List<String> data) {
        this.mActivity = mActivity;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parentp) {
        ViewHolder holder = null;

        LayoutInflater mInflater = (LayoutInflater)
                mActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_list, null);
            holder = new ViewHolder();
            holder.txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
            holder.imgDel = (ImageView) convertView.findViewById(R.id.imgDel);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.txtTitle.setText(data.get(position));
        holder.imgDel.setTag(position + "");
        holder.imgDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = Integer.parseInt(view.getTag().toString());
                ((ListActivity) mActivity).removeItem(pos);
                notifyDataSetChanged();

            }
        });

        return convertView;
    }

    private class ViewHolder {
        ImageView imgDel;
        TextView txtTitle;
    }
}
