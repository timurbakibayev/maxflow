package com.gii.maxflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import java.util.ArrayList;

/**
 * Created by Acer on 9/7/2016.
 */
public class AdapterMulti extends BaseAdapter {
    private static final String TAG = "KolesaAdapter";
    Context ctx;
    LayoutInflater lInflater;
    String filter = "";
    ArrayList<Circle> objects;
    ArrayList<Circle> filteredObjects = new ArrayList<>();
    public ArrayList<String> selectedCircles = new ArrayList<>();

    AdapterMulti(Context context, ArrayList<Circle> circles, ArrayList<String> currentSelected) {
        ctx = context;
        objects = circles;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //selectedIds = new ArrayList<>(Arrays.asList(currentValue.split(";")));
        selectedCircles = currentSelected;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Circle getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    ArrayList<String> firstTimes = new ArrayList<>();

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.kolesa_multi_list_item, parent, false);
        }

        final Circle p = getItem(position);
        CheckBox workCheckBox = (CheckBox) view.findViewById(R.id.checkbox1);
        workCheckBox.setText(p.name.trim());
        workCheckBox.setChecked(selectedCircles.contains(p.id));
        workCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean b = ((CheckBox)view).isChecked();
                if (b) {
                    selectedCircles.add(p.id);
                } else {
                    for (String selectedCircle : selectedCircles) {
                        if (selectedCircle.equals(p.id)) {
                            selectedCircles.remove(selectedCircle);
                            return;
                        }
                    }

                }
            }
        });

        return view;
    }
}
