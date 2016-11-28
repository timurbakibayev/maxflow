package com.gii.maxflow;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ShareFile extends AppCompatActivity {

    ListView shareListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        ArrayList<AccessRights> accessRightses = new ArrayList<>();
        shareListView = (ListView)findViewById(R.id.sharingListView);
        {
            AccessRights accessRights = new AccessRights();
            accessRights.permitToEmail = "ttt@ggg.com";
            accessRights.filter = "Taxi,Maxi";
            accessRights.circles.add(new Circle("0","Circle1"));
            accessRights.circles.add(new Circle("0","Circle2"));
            accessRights.circles.add(new Circle("0","Circle3"));
            accessRightses.add(accessRights);
        }


        BaseAdapter shareListViewAdapter = new ShareListViewAdapter(this, accessRightses);

        shareListView.setAdapter(shareListViewAdapter);
    }

}
