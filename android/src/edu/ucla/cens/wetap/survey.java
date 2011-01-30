package edu.ucla.cens.wetap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.widget.TextView;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.ImageView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.CheckBox;
import android.widget.TableRow;
import android.app.AlertDialog;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import edu.ucla.cens.wetap.survey_db;
import edu.ucla.cens.wetap.survey_db.survey_db_row;


public class survey extends Activity
{
    //a whole bunch of private members
    private String TAG = "Survey";
    private ArrayList<ArrayList<CheckBox>> group_box_list = new ArrayList<ArrayList<CheckBox>>();
    private Button take_picture;
    private Button submit_button;
    //private Button clear_history;
    private ImageView image_thumbnail;
    private String filename = "";
    private survey_db sdb;
    private SharedPreferences preferences;
    //these are just reference integers
    private final int GB_INDEX_OPER = 0;
    private final int GB_INDEX_TASTE = 1;
    private final int GB_INDEX_FLOW = 2;
    private final int GB_INDEX_WHEEL = 3;
    private final int GB_INDEX_CHILD = 4;
    private final int GB_INDEX_REFILL = 5;
    private final int GB_INDEX_REFILL_AUX = 6;
    private final int GB_INDEX_VIS = 7;
    private final int GB_INDEX_LOC = 8;
    private final int GB_INDEX_TYPE = 9;    //EDIT

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        //calls Activity's create with the passed Bundle savedInstanceState
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey);    //creates a window to view the user interface (the survey one)

        //sets the member "preferences" to the return. preferences is a SharedPreferences object
        preferences = getSharedPreferences(getString(R.string.preferences), Activity.MODE_PRIVATE);
        // allow users to collect data even if they are not yet authenticated
        // let the survey_upload service make sure they are auth'd before
        // uploading... (lets users collect data without internet conn)
        //if (!preferences.getBoolean("authenticated", false)) {
        //    Log.d(TAG, "exiting (not authenticated)");
        //    survey.this.finish();
        //    return;
        //}

        sdb = new survey_db(this);

        /* start location service */
        startService (new Intent(survey.this, light_loc.class));
        preferences.edit().putBoolean ("light_loc", true).commit ();

        //log that you created the gps listener and survey
        Log.d(TAG, "gps listener and db are started");

        //new Arraylist of Checkboxs - this is a temporary variable
        ArrayList<CheckBox> lcb;

        //each segment of code will initiate lcb as a new arraylist and then add it to
        //the arraylist of arraylists
        //each arraylist will have multiple "checkBox" objects and will log
        //that the checkBoxes have been added to the ArraylistArraylist
        //groups are : operable, taste, flow, access wheelchair, access child, refill, alternate
        //visibility, location

        // add operable boxes
        lcb = new ArrayList<CheckBox>();
        lcb.add( (CheckBox) findViewById(R.id.operable_functioning) );
        lcb.add( (CheckBox) findViewById(R.id.operable_broken) );
        lcb.add( (CheckBox) findViewById(R.id.operable_needs_repair) );
        group_box_list.add(lcb);
        Log.d(TAG, "added operable boxes");

        // add taste boxes
        lcb = new ArrayList<CheckBox>();
        lcb.add( (CheckBox) findViewById(R.id.taste_same) );
        lcb.add( (CheckBox) findViewById(R.id.taste_good) );
        lcb.add( (CheckBox) findViewById(R.id.taste_bad) );
        lcb.add( (CheckBox) findViewById(R.id.taste_other) );
        group_box_list.add(lcb);
        Log.d(TAG, "added taste boxes");

        // add flow boxes
        lcb = new ArrayList<CheckBox>();
        lcb.add( (CheckBox) findViewById(R.id.flow_strong) );
        lcb.add( (CheckBox) findViewById(R.id.flow_trickle) );
        lcb.add( (CheckBox) findViewById(R.id.flow_too_strong) );
        lcb.add( (CheckBox) findViewById(R.id.flow_cant_answer) );
        lcb.add( (CheckBox) findViewById(R.id.flow_medium) );
        group_box_list.add(lcb);
        Log.d(TAG, "added flow boxes");

        // add access wheelchair box:
        lcb = new ArrayList<CheckBox>();
        lcb.add( (CheckBox) findViewById(R.id.question_5_option_0) );
        group_box_list.add(lcb);
        Log.d(TAG, "added wheelchair box");

        // add access child box:
        lcb = new ArrayList<CheckBox>();
        lcb.add( (CheckBox) findViewById(R.id.question_5_option_1) );
        group_box_list.add(lcb);
        Log.d(TAG, "added child box");

        // add access refill box:
        lcb = new ArrayList<CheckBox>();
        lcb.add( (CheckBox) findViewById(R.id.question_5_option_2) );
        group_box_list.add(lcb);
        Log.d(TAG, "added refill box");

        // add alternate accessibility questions
        lcb = new ArrayList<CheckBox>();
        lcb.add( (CheckBox) findViewById(R.id.question_6_option_0) );
        lcb.add( (CheckBox) findViewById(R.id.question_6_option_1) );
        lcb.add( (CheckBox) findViewById(R.id.question_6_option_3) );
        lcb.add( (CheckBox) findViewById(R.id.question_6_option_2) );
        group_box_list.add(lcb);
        Log.d(TAG, "added alternate accessibility boxes");

        //add type boxes  EDIT
        lcb = new ArrayList<CheckBox>();
        lcb.add( (CheckBox) findViewById(R.id.type_drinking) );
        lcb.add( (CheckBox) findViewById(R.id.type_dispensing) );
        group_box_list.add(lcb);
        Log.d(TAG, "added type boxes");

        // add visibility boxes
        lcb = new ArrayList<CheckBox>();
        lcb.add( (CheckBox) findViewById(R.id.visibility_visible) );
        lcb.add( (CheckBox) findViewById(R.id.visibility_hidden) );
        group_box_list.add(lcb);
        Log.d(TAG, "added visibility boxes");

        // add location boxes
        lcb = new ArrayList<CheckBox>();
        lcb.add( (CheckBox) findViewById(R.id.location_indoor) );
        lcb.add( (CheckBox) findViewById(R.id.location_outdoors) );
        group_box_list.add(lcb);
        Log.d(TAG, "added location boxes");

        //create some buttons: submit and take picture, log the added buttons

        // add submit button
        submit_button = (Button) findViewById(R.id.upload_button);

        // add picture button
        take_picture = (Button) findViewById(R.id.picture_button);

        // add clear history button
        //clear_history = (Button) findViewById(R.id.clear_history_button);
        Log.d(TAG, "added buttons");

        // add image thumbnail view
        image_thumbnail = (ImageView) findViewById(R.id.thumbnail);

        // add check box listeners
      //get an arraylist from the arraylist of arraylists
        for (int j = 0; j < group_box_list.size(); j++) {
            lcb = group_box_list.get(j);
            //iterate through each individual arraylist
            for (int i = 0; i < lcb.size(); i++) {
                CheckBox cb = (CheckBox) lcb.get(i);
                //create listeners for all checkboxs
                cb.setOnClickListener(check_box_listener);
                //check_box_listener is defined later on in this file
            }
        }

        // add submit button listener
        submit_button.setOnClickListener(submit_button_listener);

        // add take picture button listener
        take_picture.setOnClickListener(take_picture_listener);

        // add clear history button listener
        //clear_history.setOnClickListener(clear_history_listener);

        // restore previous state (if available)
        if (savedInstanceState != null && savedInstanceState.getBoolean("started")) {
            for (int i = 0; i < group_box_list.size(); i++) {
                lcb = group_box_list.get(i);
                int k = savedInstanceState.getInt(Integer.toString(i));

                for (int j = 0; j < lcb.size(); j++) {
                    CheckBox cb = (CheckBox) lcb.get(j);
                    if (j == k) {
                        cb.setChecked(true);
                        update_checkbox_status (cb);
                        break;
//                    } else {
//                        cb.setChecked(false);
                    }
                }
            }

            filename = savedInstanceState.getString("filename");
            if ((null != filename) && (!filename.toString().equals(""))) {
                Bitmap bm = BitmapFactory.decodeFile(filename);
                if (bm != null) {
                    take_picture.setText("Retake Picture");
                    image_thumbnail.setVisibility(View.VISIBLE);
                    image_thumbnail.setImageBitmap(bm);
                }
            }
        }

        return;
    }
    //end of OnCreate() function

    @Override
    //this function should only be called once, the first time the options menu is displayed
    public boolean onCreateOptionsMenu (Menu m) {
        //calls Activity's onCreateOptions
        super.onCreateOptionsMenu (m);

        //then adds more to the menu (this is what shows up when we press the middle "Menu" button
        m.add (Menu.NONE, 0, Menu.NONE, "Home").setIcon (android.R.drawable.ic_menu_revert);
        m.add (Menu.NONE, 1, Menu.NONE, "Map").setIcon (android.R.drawable.ic_menu_mapmode);
        m.add (Menu.NONE, 2, Menu.NONE, "About").setIcon (android.R.drawable.ic_menu_info_details);
        m.add (Menu.NONE, 3, Menu.NONE, "Instructions").setIcon (android.R.drawable.ic_menu_help);
        m.add (Menu.NONE, 4, Menu.NONE, "Statistics").setIcon(android.R.drawable.ic_menu_sort_by_size);
        return true;
    }

    @Override
    //this function is called when a menu item has been selected
    public boolean onOptionsItemSelected (MenuItem index) {
        //will tell us which button was pressed
        Context ctx = survey.this;
        //initializes new Intent
        Intent i;
        //display these pages if they are clicked
        switch (index.getItemId()) {
            case 0:
                i = new Intent (ctx, home.class);
                break;
            case 1:
                i = new Intent (ctx, map.class);
                break;
            case 2:
                i = new Intent (ctx, about.class);
                break;
            case 3:
                i = new Intent (ctx, instructions.class);
                break;
            case 4:
                i = new Intent (ctx,statistics.class);
                break;
            default:
                return false;
        }
        //start the Activity and finish(?) the survey
        ctx.startActivity (i);
        this.finish();
        return true;
    }

    //call this function in OnCreate if the gps is not enabled
    private void alert_no_gps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //display this popup message to the user
        builder.setMessage("Yout GPS seems to be disabled, You need GPS to run this application. do you want to enable it?")
               //you cannot "cancel" this message
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   //if "yes" enable, then enable the gps by doing something
                    public void onClick(final DialogInterface dialog, final int id) {
                        survey.this.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 3);
                    }
                })
                //if no they do not want to enable the gps
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        //finish the survey
                        survey.this.finish();
                    }
                });
        final AlertDialog alert = builder.create();
        //now show the alert
        alert.show();
    }

    // if this activity gets killed for any reason, save the status of the
    // check boxes so that they are filled in the next time it gets run
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("started", true);
        savedInstanceState.putString("filename", filename);
        List<CheckBox> lcb;
        CheckBox cb;

        for (int i = 0; i < group_box_list.size(); i++) {
            lcb = group_box_list.get(i);
            for (int j = 0; j < lcb.size(); j++) {
                cb = (CheckBox) lcb.get(j);
                if (cb.isChecked()) {
                    savedInstanceState.putInt(Integer.toString(i), j);
                }
            }
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    //this is called by a later function
    public void update_checkbox_status (CheckBox cb) {
        List<CheckBox> lcb;
        boolean checked = cb.isChecked();

        //q5_o2 is the access refill question
        //need to read up on TableRow
        if (R.id.question_5_option_2 == cb.getId()) {
            TableRow tr = (TableRow) findViewById(R.id.question_6_row);
            tr.setVisibility(checked ? View.GONE : View.VISIBLE);
            return;
            //hide the "Why couldn't you refill" question if
            //the user has checked the they were able to refill
           /*if you were able to refill, then the fountain cannot be broken
                     *therefore skip the if as it is checking whether the "broken"
                     *option was checked*/
        }

        //check to see if the passed checkbox was the "broken" checkbox
        if (R.id.operable_broken == cb.getId()) {
            //set a bunch of other questions to gone or visible depending on the answer
            //to this specific question
            View v = findViewById(R.id.taste_row);
            v.setVisibility(checked ? View.GONE : View.VISIBLE);

            v = (TableRow) findViewById(R.id.flow_row);
            v.setVisibility(checked ? View.GONE : View.VISIBLE);

            //in addition, remove an option from the accessible question
            //no longer ask if it's refill accessible since that no longer applies
            CheckBox ncb = (CheckBox) findViewById(R.id.question_5_option_2);
            ncb.setVisibility(checked ? View.GONE : View.VISIBLE);

            //I don't know what this part is doing...
            if (false == ncb.isChecked()) {
                v = findViewById(R.id.question_6_row);
                v.setVisibility(checked ? View.GONE : View.VISIBLE);
            }
        }

        // dont do anything if this box was unchecked
        if (false == checked) {
            return;
        }

        //Recall: group_box_list is the arraylist of arraylists of checkboxes
        for (int i = 0; i < group_box_list.size(); i++) {
            //get an Arraylist of questions
            lcb = group_box_list.get(i);
            //cb is the passed checkbox to this function
            int index = lcb.indexOf(cb);

            // continue on if the check box wasn't found in this checkbox group
            if(-1 == index) {
                continue;
            }

            // switch all of the other checkboxes in this group off
            for (i = 0; i < lcb.size(); i++) {
                cb = (CheckBox) lcb.get(i);
                //now we overrite cb with the checkbox we
                //are looking at. this is ok because we only
                //need the index of cb
                if (i != index
                        && cb.isChecked())
                {
                    //uncheck the box
                    cb.setChecked(false);
                    checked = false;
                    //if the box we just unchecked was the "broken" checkbox then
                    //we will restore the other questions because now they may apply
                    //(reverses previous hiding code)
                    if (R.id.operable_broken == cb.getId()) {
                        View v = findViewById(R.id.taste_row);
                        v.setVisibility(checked ? View.GONE : View.VISIBLE);

                        v = findViewById(R.id.flow_row);
                        v.setVisibility(checked ? View.GONE : View.VISIBLE);

                        v = findViewById(R.id.question_5_row);
                        v.setVisibility(checked ? View.GONE : View.VISIBLE);

                        CheckBox ncb = (CheckBox) findViewById(R.id.question_5_option_2);
                        ncb.setVisibility(checked ? View.GONE : View.VISIBLE);

                        if (false == ncb.isChecked()) {
                            v = findViewById(R.id.question_6_row);
                            v.setVisibility(checked ? View.GONE : View.VISIBLE);
                        }
                    }
                }
            }
            return;
        }
    }

    //this is the check_box_listener. Every time the checkbox associated is clicked, we call
    //update checkboxes because we want to see what has changed and how we need to modify
    //the survey window
    OnClickListener check_box_listener = new OnClickListener() {
        public void onClick(View v) {
            update_checkbox_status ((CheckBox) v);
        }
    };

    //defines the listener for the submit button
    OnClickListener submit_button_listener = new OnClickListener() {
        //defines a helper function that isn't really needed anywhere else

        //for a given index, get the arraylist of checkboxes associated
        //iterate through the list and if one of the boxes is checked
        //return the index (+1) of that box, otherwise return zero
        //zero indicates no boxes in that list were checked
        private String get_group_result (int index) {
            List<CheckBox> lcb = group_box_list.get(index);
            for (int i = 0; i < lcb.size(); i++) {
                CheckBox cb = (CheckBox) lcb.get(i);
                if (cb.isChecked()) {
                    return Integer.toString(i+1);
                }
            }
            return "0";
        }

        //on the click (defined for this function)
        public void onClick(View v) {
            Date d = new Date(); //creates new Date object

            //here are a bunch of strings
            String q_location = "0";
            String q_visibility = "0";
            String q_operable = "0";
            String q_wheel = "0";
            String q_child = "0";
            String q_refill = "0";
            String q_refill_aux = "0";
            String q_taste = "0";
            String q_flow = "0";
            String q_type = "0";    //EDIT

            //these strings are initialized by the helper function above.
            //they are passed the index of the question (set as finals at the beginning
            //of the file. They will be a number if a box was checked and will be "0"
            //if no boxes for that question were checked
            q_location = get_group_result (GB_INDEX_LOC);
            q_visibility = get_group_result (GB_INDEX_VIS);
            q_operable = get_group_result (GB_INDEX_OPER);
            q_wheel = get_group_result (GB_INDEX_WHEEL);
            q_child = get_group_result (GB_INDEX_CHILD);
            q_refill = get_group_result (GB_INDEX_REFILL);
            q_refill_aux = get_group_result (GB_INDEX_REFILL_AUX);
            q_taste = get_group_result (GB_INDEX_TASTE);
            q_flow = get_group_result (GB_INDEX_FLOW);
            q_type = get_group_result (GB_INDEX_TYPE);   //EDIT

            /* make sure they dont submit an incomplete survey */
            if (q_location.equals("0")
                || q_visibility.equals("0")
                || q_operable.equals("0")
                || q_type.equals("0"))
            {
                /* we can only check these four questions in such a general way because
                 * there's no guarantee that the other questions will be there
                 * have to do the rest on a case by case basis*/
            //show a warning
                Toast
                .makeText (survey.this,
                           "You have not answered one or more questions. Please fill them all out.",
                           Toast.LENGTH_LONG)
                .show();
                return;
            }

            if (!q_operable.equals("2") //other cases
                && !q_refill.equals("1")
                && q_refill_aux.equals("0"))
            {
                Toast
                .makeText (survey.this,
                           "You have not marked why you couldn't refill from this fountain.",
                           Toast.LENGTH_LONG)
                .show();
                return;
            }

            if (!q_operable.equals("2")
                && (q_taste.equals("0")
                    || q_flow.equals("0")))
            {
                Toast
                .makeText (survey.this,
                           "You must fill out both the taste and flow questions.",
                           Toast.LENGTH_LONG)
                .show();
                return;
            }

            /* if the fountain was broken then throw out anything that couldn't
             * have been answered */
            if (q_operable.equals("2")) {
                q_refill =
                q_refill_aux =
                q_taste =
                q_flow = "0";
            }

            /* if they could refill a bottle at the fountain then throw out
             * refill aux questions */
            if (q_refill.equals("1")) {
                q_refill_aux = "0";
            }

            //initialize long and lat strings to empty
            String longitude = "";
            String latitude = "";
            //get the time (as String)
            String time = Long.toString(d.getTime());
            //get the photo file
            String photo_filename = filename;

            Log.d("SUBMIT SURVEY", "opening sdb object");

            //open the survey_db object
            sdb.open();
            //create an entry and assign it a row id
            long row_id = sdb.createEntry(q_location, q_visibility, q_type, q_operable,
                q_wheel, q_child, q_refill, q_refill_aux, q_taste, q_flow,
                longitude, latitude, time, getString(R.string.version),
                photo_filename);  //EDIT

            //close the object, we've uploaded the data
            sdb.close();

            //open the object again. is this necessary?
            sdb.open();
            //get the entry that we just submitted
            survey_db_row sr = sdb.fetchEntry(row_id);
            sdb.close();    //close the object again

            //log the results that we submitted
            Log.d("SUBMIT SURVEY", Long.toString(sr.row_id) + ", " +
                                   sr.q_taste + ", " +
                                   sr.q_visibility + ", " +
                                   sr.q_operable + ", " +
                                   sr.q_flow + ", " +
                                   sr.q_location + ", " +
                                   sr.q_type + "," +  //EDIT
                                   sr.longitude + ", " +
                                   sr.latitude + ", " +
                                   sr.time + ", " +
                                   sr.version + ", " +
                                   sr.photo_filename + ".");

            /* start location service */
            if (!preferences.getBoolean("light_loc", false)) {
                startService (new Intent(survey.this, light_loc.class));
                preferences.edit().putBoolean ("light_loc", true).commit ();
            }

            // popup success toast and return to home page
            Toast.makeText(survey.this, "Survey successfully submitted!", Toast.LENGTH_LONG).show();
            survey.this.startActivity (new Intent(survey.this, home.class));
            survey.this.finish();
        }
    };

    //this defines the listener for the take picture button
    OnClickListener take_picture_listener = new OnClickListener() {
        //start up the camera if clicked
        public void onClick(View v) {
            Intent photo_intent = new Intent(survey.this, photo.class);
            startActivityForResult(photo_intent, 0);
        }
    };

    //we do not implement this button
    OnClickListener clear_history_listener = new OnClickListener() {
        public void onClick(View v) {
            sdb.open();
            ArrayList<survey_db_row> sr_list = sdb.fetchAllEntries();
            sdb.close();

            for (int i = 0; i < sr_list.size(); i++) {
                survey_db_row sr = sr_list.get(i);
                File file = null;
                if ((sr.photo_filename != null) && (!sr.photo_filename.toString().equals(""))) {
                    file = new File(sr.photo_filename.toString());
                }
                if(file != null) {
                    file.delete();
                }
                sdb.open();
                sdb.deleteEntry(sr.row_id);
                sdb.close();
            }

/*
            sdb.open();
            sdb.refresh_db();
            sdb.close();
            */
        }
    };

    //defines what to do if the activity we call fails. (for camera, tell them to retake the picture
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_CANCELED != resultCode) {
            filename = data.getAction().toString();
            if ((null != filename) && (!filename.toString().equals(""))) {
                Bitmap bm = BitmapFactory.decodeFile(filename);
                if (bm != null) {
                    take_picture.setText("Retake Picture");
                    image_thumbnail.setVisibility(View.VISIBLE);
                    image_thumbnail.setImageBitmap(bm);
                }
            }
        }
    }
}