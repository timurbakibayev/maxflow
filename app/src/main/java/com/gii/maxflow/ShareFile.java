package com.gii.maxflow;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShareFile extends AppCompatActivity {
    private static String TAG = "ShareFile";
    static ListView shareListView;
    ArrayList<AccessRight> accessRights = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accessRights = GIIApplication.gii.accessRights;
        if (accessRights == null) {
            return;
        }
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
//        {
//            AccessRight accessRight = new AccessRight();
//            accessRight.permitToEmail = "ttt@ggg.com";
//            accessRight.filter = "Taxi,Maxi";
//            accessRight.circleIds.add(GIIApplication.gii.circle.get(0).id);
//            accessRight.circleIds.add(GIIApplication.gii.circle.get(1).id);
//            accessRight.circleIds.add(GIIApplication.gii.circle.get(2).id);
//            accessRights.add(accessRight);
//        }
//        {
//            AccessRight accessRight = new AccessRight();
//            accessRight.permitToEmail = "shashlik@google.com";
//            accessRight.filter = "Shashlik,Mashlik";
//            accessRights.add(accessRight);
//        }

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
                    final AccessRight accessRight = new AccessRight();
                    accessRight.permitToEmail = email;
                    accessRight.owner = GII.ref.getAuth().getUid();
                    accessRight.ownerEmail = GII.ref.getAuth().getProviderData().get("email").toString();
                    accessRight.filename = GIIApplication.gii.properties.computeFileNameWithoutXML();
                    accessRight.sentOk = false;

                    GII.ref.child("users").orderByChild("email")
                            .equalTo(email)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChildren()) {
                                        Map<String, String> map = new HashMap<String, String>();
                                        DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                        Log.w(TAG,"Found user id by email:" + firstChild.getKey().toString());
                                        accessRight.permitTo = firstChild.getKey().toString();
                                        refreshData();
                                    } else
                                        Log.e(TAG, "Nothing found: " +  dataSnapshot.toString());
                                }
                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    Log.e("Firebase","The read failed 13: " + firebaseError.getMessage());
                                }
                            });

                    accessRights.add(accessRight);

                    ((BaseAdapter) shareListView.getAdapter()).notifyDataSetChanged();
                    newRuleDialog.dismiss();
                } else
                    ((EditText)newRuleDialog.findViewById(R.id.emailEditText)).setError("Email");
            }
        });
        newRuleDialog.show();
    }

    public static void getAccessRights() {
        String pathToFile = "maxflow/" + GII.ref.getAuth().getUid() + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML();
        if (!GIIApplication.gii.properties.owner.equals(""))
            pathToFile = "maxflow/" + GIIApplication.gii.properties.owner + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML();
        GII.ref.child(pathToFile + "/shared/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GIIApplication.gii.accessRights = new ArrayList<>();
                if (dataSnapshot.hasChildren())
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            AccessRight a = snapshot.getValue(AccessRight.class);
                            GIIApplication.gii.accessRights.add(a);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ERROR");
                            e.printStackTrace();
                        }
                    }
                if (shareListView != null && shareListView.getAdapter() != null)
                    ((BaseAdapter) shareListView.getAdapter()).notifyDataSetChanged();
                GIIApplication.gii.recalculateAll();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "onCancelled: " + firebaseError.getMessage());
            }
        });
    }

    public void refreshData() {
        if (!GIIApplication.gii.properties.owner.equals(""))
            return;
        //fireBase rule: no ownership = no access to write
        for (AccessRight accessRight : accessRights) {
            if (!accessRight.permitTo.trim().equals("")) {
                if (!accessRight.delete) {
                    accessRight.sentOk = true;
                    GII.ref.child("maxflow/" + GII.ref.getAuth().getUid() + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML() + "/shared/" + accessRight.permitTo).setValue(accessRight);
                    GII.ref.child("shared/" + accessRight.permitTo + "/" + GII.ref.getAuth().getUid() + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML()).setValue(accessRight);
                } else {
                    GII.ref.child("maxflow/" + GII.ref.getAuth().getUid() + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML() + "/shared/" + accessRight.permitTo).setValue(null);
                    GII.ref.child("shared/" + accessRight.permitTo + "/" + GII.ref.getAuth().getUid() + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML()).setValue(null);
                }
            }
        }
        //GIIApplication.gii.accessRights = accessRights;
    }


}
