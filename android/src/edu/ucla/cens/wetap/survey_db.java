package edu.ucla.cens.wetap;

import java.util.ArrayList;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//declaring new class survey_db
public class survey_db {
	//a lot of final string members
	public static final String KEY_Q_TASTE = "q_taste";
    public static final String KEY_Q_VISIBILITY = "q_visibility";
    public static final String KEY_Q_OPERABLE = "q_operable";
    public static final String KEY_Q_FLOW = "q_flow";
    public static final String KEY_Q_LOCATION = "q_location";
    public static final String KEY_Q_TYPE = "q_type";   //EDIT
    public static final String KEY_Q_WHEEL = "q_wheel";
    public static final String KEY_Q_CHILD = "q_child";
    public static final String KEY_Q_REFILL = "q_refill";
    public static final String KEY_Q_REFILL_AUX = "q_refill_aux";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_TIME = "time";
	public static final String KEY_PHOTO_FILENAME = "photo_filename";
    public static final String KEY_VERSION = "version";
	public static final String KEY_ROWID = "_id";
	//changeable members
	private static boolean databaseOpen = false;
	private static Object dbLock = new Object();
	public static final String TAG = "survey_db";
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	private Context mCtx = null;

	private static final String DATABASE_NAME = "survey_db";
	private static final String DATABASE_TABLE = "survey_table";
	private static final int DATABASE_VERSION = 3; //EDIT

	//this is one string that specifies the column names to create
	private static final String DATABASE_CREATE = "create table survey_table (_id integer primary key autoincrement, "
        + "q_taste text not null,"
        + "q_visibility text not null,"
        + "q_operable text not null,"
        + "q_flow text not null,"
        + "q_location text not null,"
        + "q_type text not null,"    //EDIT
        + "q_wheel text not null,"
        + "q_child text not null,"
        + "q_refill text not null,"
        + "q_refill_aux text not null,"
		+ "longitude text not null,"
		+ "latitude text not null,"
		+ "time text not null,"
        + "version text not null,"
		+ "photo_filename text not null"
		+ ");";

	//new object survey_db_row - basically just a ton of strings w/ one long
    public class survey_db_row extends Object {
    	public long row_id;
        public String q_taste;
        public String q_visibility;
        public String q_type;  //EDIT
        public String q_operable;
        public String q_flow;
        public String q_location;
        public String q_wheel;
        public String q_child;
        public String q_refill;
        public String q_refill_aux;
    	public String longitude;
    	public String latitude;
    	public String time;
        public String version;
    	public String photo_filename;
    }

    //new static class DatabaseHelper
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		//constructor
		DatabaseHelper(Context ctx)
		{
			super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		//db is an SQLiteDatabase
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
			//execSQL = execute a single SQL statement that is not a query
			//here we call DATABASE_CREATE
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
	}

	//survey_db constructor
	public survey_db(Context ctx)
	{
		mCtx = ctx;
	}

	public survey_db open() throws SQLException
    {
		synchronized(dbLock)	//getting lock
		{
			while (databaseOpen)
			{
				try
				{
					dbLock.wait();
				}
				catch (InterruptedException e){}
			}
			databaseOpen = true;						//change some of the member variables
			dbHelper = new DatabaseHelper(mCtx);
			db = dbHelper.getWritableDatabase();

			return this;
		}
	}

	public void close()
	{
		synchronized(dbLock)	//gets a lock on something
		{
			dbHelper.close();
			databaseOpen = false;	//reset the member variables to reflect this
			dbLock.notify();
		}
	}

	//takes a lot of strings as arguments, creates a new entry when called by survey
	//note: this is called when a survey is completed
	public long createEntry(String q_location, String q_visibility, String q_type, //EDIT
                            String q_operable, String q_wheel, String q_child,
                            String q_refill, String q_refill_aux,
                            String q_taste, String q_flow, String longitude,
                            String latitude, String time, String version,
                            String photo_filename)
	{
		//this an android class - created using default init size
		ContentValues vals = new ContentValues();
		//inputs a pair of strings - string name and string value (key, value)
        vals.put(KEY_Q_TASTE, q_taste);
        vals.put(KEY_Q_VISIBILITY, q_visibility);
        vals.put(KEY_Q_TYPE, q_type);       //EDIT
        vals.put(KEY_Q_OPERABLE, q_operable);
        vals.put(KEY_Q_FLOW, q_flow);
        vals.put(KEY_Q_WHEEL, q_wheel);
        vals.put(KEY_Q_CHILD, q_child);
        vals.put(KEY_Q_REFILL, q_refill);
        vals.put(KEY_Q_REFILL_AUX, q_refill_aux);
        vals.put(KEY_Q_LOCATION, q_location);
		vals.put(KEY_LONGITUDE, longitude);
		vals.put(KEY_LATITUDE, latitude);
		vals.put(KEY_TIME, time);
        vals.put(KEY_VERSION, version);
		vals.put(KEY_PHOTO_FILENAME, photo_filename);

		//insert(string, string, ContentValues)
		long rowid = db.insert(DATABASE_TABLE, null, vals);
		//insert returns the rowID of the inserted ContentValue
		//returns -1 if errors
		return rowid;
	}

	//to delete a row entry
	public boolean deleteEntry(long rowId)
	{
		int count = 0;
		//(where to delete from, whereClause-prolly what to delete, whereArgs - not used)
		count = db.delete(DATABASE_TABLE, KEY_ROWID+"="+rowId, null);

		//returns number of rows affected, if no rows affected - 0
        if(count > 0) {
            return true;
        }
        return false;
	}

	//refresh the database
    public void refresh_db()
    {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        db.execSQL(DATABASE_CREATE);
    }

    //returns all entries in the database
    //this function is never called - it is implemented in the clearHistory button which we
    //do not use (survey.java)
	public ArrayList <survey_db_row>  fetchAllEntries()
    {
		ArrayList<survey_db_row> ret = new ArrayList<survey_db_row>();

		try
		{
			/*returns a cursor object which is positioned before the first entry
			 *
			 * DATABASE_TABLE is the table to compile the query against
			 * String[] is the columns to return
			 * -returns all rows in the table
			 * -we won't be replacing anything so this argument is also null
			 * -rows are not grouped
			 * -we are not grouping rows so this argument must be null
			 * -use default sort order (could be unordered)*/
			Cursor c = db.query(DATABASE_TABLE, new String[] {KEY_ROWID,
                KEY_Q_TASTE, KEY_Q_VISIBILITY, KEY_Q_TYPE, KEY_Q_OPERABLE, KEY_Q_FLOW,  //EDIT
                KEY_Q_WHEEL, KEY_Q_CHILD, KEY_Q_REFILL, KEY_Q_REFILL_AUX,
                KEY_Q_LOCATION, KEY_LONGITUDE, KEY_LATITUDE, KEY_TIME,
                KEY_VERSION, KEY_PHOTO_FILENAME}, null, null, null,
                null, null);
			int numRows = c.getCount();

			c.moveToFirst(); //move cursor to the first row

			//iterate through all the rows
			for (int i =0; i < numRows; ++i)
			{
				survey_db_row sr = new survey_db_row();

				sr.row_id = c.getLong(0);
                sr.q_taste = c.getString(1);
                sr.q_visibility = c.getString(2);
                sr.q_type = c.getString(3); //EDIT
                sr.q_operable = c.getString(4);
                sr.q_flow = c.getString(5);
                sr.q_wheel = c.getString(6);
                sr.q_child = c.getString(7);
                sr.q_refill = c.getString(8);
                sr.q_refill_aux = c.getString(9);
                sr.q_location = c.getString(10);
                sr.longitude = c.getString(11);
                sr.latitude = c.getString(12);
                sr.time = c.getString(13);
                sr.version = c.getString(14);
                sr.photo_filename = c.getString(15);
				ret.add(sr);	//add each survey to the arraylist of survey_db objects

				c.moveToNext();
			}
			c.close();
		}
		catch (Exception e){
			Log.e(TAG, e.getMessage());
		}
		return ret;	//return the arraylist of surveys
	}

	/*this function is called by survey_upload in the run() function
	 * seems to do exactly the same thing as the previous function except that
	 * the columns argument is declared as a separate string before being called and
	 * the selection argument is the gps location. The selection argument doesn't seem
	 * to make a difference to the functionality of the method*/

    public ArrayList <survey_db_row>  fetch_all_completed_entries()
    {
        ArrayList<survey_db_row> ret = new ArrayList<survey_db_row>();

        try
        {
            String[] columns = new String[] {KEY_ROWID, KEY_Q_TASTE,
                KEY_Q_VISIBILITY, KEY_Q_TYPE, KEY_Q_OPERABLE, KEY_Q_FLOW,  //EDIT
                KEY_Q_WHEEL, KEY_Q_CHILD, KEY_Q_REFILL, KEY_Q_REFILL_AUX,
                KEY_Q_LOCATION, KEY_LONGITUDE, KEY_LATITUDE, KEY_TIME,
                KEY_VERSION, KEY_PHOTO_FILENAME};
            String selection = KEY_LONGITUDE + "<>\"\"" + " AND " +
                               KEY_LATITUDE + "<>\"\"";

            //would making selection null even make a difference here?
            Cursor c = db.query(DATABASE_TABLE, columns, selection, null, null,
                                null, null);
            int numRows = c.getCount();

            c.moveToFirst();

            for (int i =0; i < numRows; ++i)
            {
                survey_db_row sr = new survey_db_row();

                sr.row_id = c.getLong(0);
                sr.q_taste = c.getString(1);
                sr.q_visibility = c.getString(2);
                sr.q_type = c.getString(3); //EDIT
                sr.q_operable = c.getString(4);
                sr.q_flow = c.getString(5);
                sr.q_wheel = c.getString(6);
                sr.q_child = c.getString(7);
                sr.q_refill = c.getString(8);
                sr.q_refill_aux = c.getString(9);
                sr.q_location = c.getString(10);
                sr.longitude = c.getString(11);
                sr.latitude = c.getString(12);
                sr.time = c.getString(13);
                sr.version = c.getString(14);
                sr.photo_filename = c.getString(15);
                ret.add(sr);

                c.moveToNext();
            }
            c.close();
        }
        catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
        return ret;
    }

    //passed a rowID of a specific entry
	public survey_db_row fetchEntry(long rowId) throws SQLException
	{
		//same call as before only we've specified that we only want one row
        Cursor c = db.query(DATABASE_TABLE, new String[] {KEY_ROWID,
            KEY_Q_TASTE, KEY_Q_VISIBILITY, KEY_Q_TYPE, KEY_Q_OPERABLE, KEY_Q_FLOW, //EDIT
            KEY_Q_WHEEL, KEY_Q_CHILD, KEY_Q_REFILL, KEY_Q_REFILL_AUX,
            KEY_Q_LOCATION, KEY_LONGITUDE, KEY_LATITUDE, KEY_TIME, KEY_VERSION,
            KEY_PHOTO_FILENAME}, KEY_ROWID+"="+rowId, null, null,
            null, null);
		survey_db_row sr = new survey_db_row();

		if (c != null) {
			c.moveToFirst();

            sr.row_id = c.getLong(0);
            sr.q_taste = c.getString(1);
            sr.q_visibility = c.getString(2);
            sr.q_type = c.getString(3); //EDIT
            sr.q_operable = c.getString(4);
            sr.q_flow = c.getString(5);
            sr.q_wheel = c.getString(6);
            sr.q_child = c.getString(7);
            sr.q_refill = c.getString(8);
            sr.q_refill_aux = c.getString(9);
            sr.q_location = c.getString(10);
            sr.longitude = c.getString(11);
            sr.latitude = c.getString(12);
            sr.time = c.getString(13);
            sr.version = c.getString(14);
            sr.photo_filename = c.getString(15);
		}
		else
		{
			/*if that row does not exist in our database, set all strings
			 * in our survey_row object to null so that we don't get an
			 * error later. We can check for null later on*/
            sr.row_id = -1;
            sr.q_taste = sr.q_visibility = sr.q_operable = sr.q_flow = sr.q_type =  //EDIT
            sr.q_wheel = sr.q_child = sr.q_refill = sr.q_refill_aux =
            sr.q_location = sr.longitude = sr.latitude = sr.time =
            sr.photo_filename = null;
		}
		c.close();
		return sr;	//return the survey_row entry specified
	}

	//takes the gps long and lat
    public int update_gpsless_entries (String lon, String lat) {
        ContentValues values = new ContentValues();	//creates new contentValues and adds coords
        values.put (KEY_LONGITUDE, lon);
        values.put (KEY_LATITUDE, lat);

        String where_clause = KEY_LONGITUDE + "=\"\"" + " AND " + KEY_LATITUDE + "=\"\"";

        //calls the database to update the long and lat columns of all rows
        int ret = db.update (DATABASE_TABLE, values, where_clause, null);
        return ret; //returns number of rows affected
    }
}
