package edu.ucla.cens.wetap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.widget.TextView;

import android.content.Context;
import android.content.Intent;

import android.view.Menu;
import android.view.MenuItem;

import edu.ucla.cens.wetap.survey_db;
import edu.ucla.cens.wetap.survey_db.survey_db_row;

public class statistics extends Activity{

    private int without_gps;
    private int with_gps;

    private survey_db sdb;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //uses layout in the statistics.xml file
        Log.d("STATS", "starting contentview");
        setContentView(R.layout.statistics);

        sdb = new survey_db(this);
        sdb.open();

        without_gps = sdb.count_gpsless_entries();
        with_gps = sdb.count_gpssed_entries();

        sdb.close();

        Log.d("STATS", "about to set values for statistics");

        TextView tv = (TextView) findViewById (R.id.num_without);
        tv.setText (Integer.toString(without_gps));

        tv = (TextView) findViewById (R.id.num_with);
        tv.setText (Integer.toString(with_gps));

    }

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
        m.add (Menu.NONE, 4, Menu.NONE, "Survey").setIcon (android.R.drawable.ic_menu_agenda);
        return true;
    }

    @Override
    //this function is called when a menu item has been selected
    public boolean onOptionsItemSelected (MenuItem index) {
        //will tell us which button was pressed
        Context ctx = statistics.this;
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
                i = new Intent (ctx, survey.class);
                break;
            default:
                return false;
        }
        //start the Activity and finish(?) the survey
        ctx.startActivity (i);
        this.finish();
        return true;
    }
}