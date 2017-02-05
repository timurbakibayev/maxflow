package com.gii.maxflow;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class ShareFile extends AppCompatActivity {

    ListView shareListView;
    ArrayList<AccessRight> accessRights = new ArrayList<>();

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

        findViewById(R.id.buttonAddNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewRule();
            }
        });

        shareListView = (ListView)findViewById(R.id.sharingListView);
        {
            AccessRight accessRight = new AccessRight();
            accessRight.permitToEmail = "ttt@ggg.com";
            accessRight.filter = "Taxi,Maxi";
            accessRight.circleIds.add(GIIApplication.gii.circle.get(0).id);
            accessRight.circleIds.add(GIIApplication.gii.circle.get(1).id);
            accessRight.circleIds.add(GIIApplication.gii.circle.get(2).id);
            accessRights.add(accessRight);
        }
        {
            AccessRight accessRight = new AccessRight();
            accessRight.permitToEmail = "shashlik@google.com";
            accessRight.filter = "Shashlik,Mashlik";
            accessRights.add(accessRight);
        }


        BaseAdapter shareListViewAdapter = new ShareListViewAdapter(this, accessRights);

        shareListView.setAdapter(shareListViewAdapter);
    }

    private void addNewRule() {
        final Dialog newRuleDialog = new Dialog(this);
        newRuleDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        newRuleDialog.setContentView(this.getLayoutInflater().inflate(R.layout.add_new_rule_share_dialog
                , null));
        //((EditText)lookUpDialogMulti.findViewById(R.id.filterEditText)).setHint();
        (newRuleDialog.findViewById(R.id.addNewRuleButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ((EditText)newRuleDialog.findViewById(R.id.emailEditText)).getText().toString();
                if (email.indexOf("@") > 0) {
                    AccessRight accessRight = new AccessRight();
                    accessRight.permitToEmail = email;
                    accessRights.add(accessRight);
                    refreshData();
                    ((BaseAdapter) shareListView.getAdapter()).notifyDataSetChanged();
                    newRuleDialog.dismiss();
                } else
                    ((EditText)newRuleDialog.findViewById(R.id.emailEditText)).setError("Email");
            }
        });
        newRuleDialog.show();

    }

    public void refreshData() {
        //todo:save all data, share what's needed
    }


}
