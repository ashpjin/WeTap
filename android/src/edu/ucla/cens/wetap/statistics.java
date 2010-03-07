package edu.ucla.cens.wetap;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import android.widget.TextView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.view.Menu;
import android.view.MenuItem;

import edu.ucla.cens.wetap.survey_db;

public class statistics extends Activity{

    private int without_gps;
    private int with_gps;
    private SharedPreferences preferences;

    private survey_db sdb;
    private StatsThread thread;

    // Need handler for callbacks to the UI thread
    final Handler mHandler = new Handler();

    // Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            setValues();
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //uses layout in the statistics.xml file
        Log.d("STATS", "starting contentview");
        setContentView(R.layout.statistics);

        //sets the member "preferences" to the return. preferences is a SharedPreferences object
        preferences = getSharedPreferences(getString(R.string.preferences), Activity.MODE_PRIVATE);

        //new survey_db object
        sdb = new survey_db(this);

        //create new StatsThread object, and start it
        Log.d("STATS", "starting thread");
        thread = new StatsThread();
        thread.start();
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

    public void onDestroy() {
         //log the fact that we're stopping the surveyupload thread
        Log.d("STATS", "Stopping the thread");
        //exit the thread
        thread.exit();

        //put here to resolve supernotcalledexception
        super.onDestroy();
    }

    //change the Textview to display the number of waiting uploads
    public void setValues()
    {
        Log.d("STATS", "about to set values for statistics");

        //change the view to show these values
        TextView tv = (TextView) findViewById (R.id.num_without);
        tv.setText (Integer.toString(without_gps));

        tv = (TextView) findViewById (R.id.num_with);
        tv.setText (Integer.toString(with_gps));
    }

    public class StatsThread extends Thread
    {
        private boolean runThread = true;
        public void run(){
            while(runThread){

                /* start location service */
                if (!preferences.getBoolean("light_loc", false)) {
                    startService (new Intent(statistics.this, light_loc.class));
                    preferences.edit().putBoolean ("light_loc", true).commit ();
                }

                //open the db and get the two returns that we want, then close the object again
                sdb.open();

                without_gps = sdb.count_gpsless_entries();
                with_gps = sdb.count_gpssed_entries();

                sdb.close();

                //use this to update the view outside of the thread (otherwise there will be a
                //CalledFromWrongThreadException)
                mHandler.post(mUpdateResults);

                try {
                    StatsThread.sleep(8000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        //stops the run function
        public void exit()
        {
            Log.d("STATS", "exitting thread");
            runThread = false;
        }
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