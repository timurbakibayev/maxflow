package com.gii.maxflow;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Timur_hnimdvi on 28-Nov-16.
 */
public class ShareListViewAdapter extends BaseAdapter {
    ArrayList<AccessRight> accessRightses = new ArrayList<>();
    ShareFile context;

    public ShareListViewAdapter(ShareFile context, ArrayList<AccessRight> accessRightses) {
        this.accessRightses = accessRightses;
        this.context = context;
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return accessRightses.size();
    }

    @Override
    public AccessRight getItem(int position) {
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

        final AccessRight p = getItem(position);

        ((TextView)view.findViewById(R.id.userEmail)).setText(p.permitToEmail);
        ((TextView)view.findViewById(R.id.accessFilter)).setText(p.filter);
        view.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteShare(p);
            }
        });
        view.findViewById(R.id.edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editShare(p);
            }
        });
        view.findViewById(R.id.noAccessTextView).setVisibility(p.delete?View.VISIBLE:View.GONE);
        String circles = "";
        int i = 0;
        for (String circle : p.circleIds) {
            i++;
            circles += GIIApplication.gii.circleById(circle).name;
            if (i < p.circleIds.size())
                circles += ", ";
        }
        if (i == 0) {
            circles = view.getContext().getString(R.string.share_all_circles).toString();
        }

        ((TextView)view.findViewById(R.id.accessCircles)).setText(circles);

        return view;
    }
    private void deleteShare(AccessRight p) {
        p.delete = !p.delete;
        notifyDataSetChanged();
        refreshData();
        return;
        /*
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete_circle))
                .setMessage(context.getString(R.string.are_you_sure))
                .setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        p.delete = true;
                        notifyDataSetChanged();
                    }
                }).setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();*/
    }

    private void refreshData() {
        context.refreshData();
    }

    private void editShare(final AccessRight p) {
        final Dialog lookUpDialogMulti = new Dialog(context);
        lookUpDialogMulti.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        lookUpDialogMulti.setContentView(context.getLayoutInflater().inflate(R.layout.edit_share_dialog
                , null));
        ArrayList<Circle> circlesNotDeleted = new ArrayList<>();
        for (Circle circle : GIIApplication.gii.circle) {
            if (!circle.deleted)
                circlesNotDeleted.add(circle);
        }
        AdapterMulti adapterMulti = new AdapterMulti(context,circlesNotDeleted,p.circleIds);
        ((ListView)lookUpDialogMulti.findViewById(R.id.circlesListView)).setAdapter(adapterMulti);
        ((EditText)lookUpDialogMulti.findViewById(R.id.filterEditText)).setText(p.filter);
        (lookUpDialogMulti.findViewById(R.id.saveButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p.filter = ((EditText)lookUpDialogMulti.findViewById(R.id.filterEditText)).getText().toString();
                refreshData();
                notifyDataSetChanged();
                lookUpDialogMulti.dismiss();
            }
        });
        lookUpDialogMulti.show();
    }
}
