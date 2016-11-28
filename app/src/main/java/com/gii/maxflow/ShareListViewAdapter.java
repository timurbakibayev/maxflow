package com.gii.maxflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Timur_hnimdvi on 28-Nov-16.
 */
public class ShareListViewAdapter extends BaseAdapter {
    ArrayList<AccessRights> accessRightses = new ArrayList<>();

    public ShareListViewAdapter(Context context, ArrayList<AccessRights> accessRightses) {
        this.accessRightses = accessRightses;
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return accessRightses.size();
    }

    @Override
    public AccessRights getItem(int position) {
        return accessRightses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    LayoutInflater lInflater;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.shared_list_item, parent, false);
        }

        final AccessRights p = getItem(position);

        ((TextView)view.findViewById(R.id.userEmail)).setText(p.permitToEmail);
        ((TextView)view.findViewById(R.id.accessFilter)).setText(p.filter);
        String circles = "";
        int i = 0;
        for (Circle circle : p.circles) {
            i++;
            circles += circle.name;
            if (i < p.circles.size())
                circles += ", ";
        }
        if (i == 0) {
            circles = view.getContext().getString(R.string.share_all_circles).toString();
        }

        ((TextView)view.findViewById(R.id.accessCircles)).setText(circles);

        return view;
    }
}
