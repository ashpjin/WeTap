package edu.ucla.cens.wetap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import edu.ucla.cens.wetap.survey_db;
import edu.ucla.cens.wetap.survey_db.survey_db_row;

//defining the survey_upload class; import from survey_db.java and survey_db.survey_db_row (?)
public class survey_upload extends Service{
    private survey_db sdb;
    private SharedPreferences preferences;

	PostThread post;
	private static final String TAG = "SurveyUploadThread";

    
    @Override
    public void onCreate() {
        preferences = getSharedPreferences(getString(R.string.preferences), Activity.MODE_PRIVATE);
        //try to authenticate user. True if they are , False if they aren't
        if (!preferences.getBoolean("authenticated", false)) {
            Log.d(TAG, "user is not authenticated... stopping this service");
            return;
        }
        //create new Survey_db object
		sdb = new survey_db(this);

		//Toast class is a predefined Android class: http://developer.android.com/reference/android/widget/Toast.html
		//context:this, will print R.string.surveyuploadstarted, for a short length (then show the toast)
        Toast.makeText(this, R.string.surveyuploadstarted, Toast.LENGTH_SHORT).show();
        
        //creat new PostThread object, and start it
    	post = new PostThread();
    	post.start();
    }
    
    
    //???
	@Override
	public IBinder onBind(Intent arg0) {
		//* TODO Auto-generated method stub
		return null;
	}
	
    @Override
    public void onDestroy() {
    	//context this, prints R.string.surveyuploadstopped for a short length, then show the toast
        Toast.makeText(this, R.string.surveyuploadstopped, Toast.LENGTH_SHORT).show();
        //log the fact that we're stopping the surveyupload thread
        Log.d(TAG, "Stopping the thread");
        //exit the thread
        post.exit();
    }
	    
    //defining the PostThread class which is a Thread
	public class PostThread extends Thread{
		
		public Boolean runThread = true;
		
		//definte a PicFiles class that has only function that will append .jpg to a file name
		private class PicFiles implements FilenameFilter{
			public boolean accept(File file, String name) {
				return (name.endsWith(".jpg"));
			}	
		}
		
		public void run(){

			try {
				while(runThread)
				{
					this.sleep(10000);	//let the program sleep for a second before logging that we are still
										//running the thread
					Log.d(TAG, "Running the thread");

					//*list all trace files
			        sdb.open();
			        //create a new arraylist of survey_db_row items by getting all the completed entries from 
			        //the survey_db object (look in survey_db.java)
					ArrayList<survey_db_row> sr_list = sdb.fetch_all_completed_entries();
					sdb.close();
					
					//some logs: arraylist size, uploading url, version(?)
	                Log.d(TAG, "Points to submit: " + Integer.toString(sr_list.size()));
                    Log.d(TAG, "uploading to: " + getString(R.string.surveyuploadurl));
                    Log.d(TAG, "version: " + getString(R.string.version));

                    //iterate through the entire arraylist of survey_upload_row items
					for (int i=0; i < sr_list.size(); i++)
					{
						survey_db_row sr = sr_list.get(i); 
						File file = null;
						
						//check if there is a photo attached to this survey. If so, make the photoname a string
						//and open it: set to file. Otherwise just log that there is no photo
						if ((sr.photo_filename != null) && (!sr.photo_filename.toString().equals(""))) {
                            Log.d(TAG, "FILENAME: is not null/empty");
							file = new File(sr.photo_filename.toString());
                        } else {
                            Log.d(TAG, "FILENAME: IS NULL");
                        }
						//log photofilename (whether it's null or not)
                        Log.d(TAG, "FILENAME: " + sr.photo_filename);
						try
						{
							//doPost is written function - defined below
							//takes all the strings as arguments and returns a boolean
							if(doPost(getString(R.string.surveyuploadurl),
                                      sr.q_taste, sr.q_visibility,
                                      sr.q_operable, sr.q_flow, sr.q_wheel,
                                      sr.q_child, sr.q_refill, sr.q_refill_aux,
                                      sr.q_location, sr.longitude, sr.latitude,
                                      sr.time, sr.version, sr.photo_filename))
							{
								//the photo wasn't null, delete it (why?)
								if(file != null) {
									file.delete();
								}
								//if the first if succeeds, delete the entry from the the
								//survey_db because we've already seen it
						        sdb.open();
								sdb.deleteEntry(sr.row_id);
						        sdb.close();
							}
						}
						catch (IOException e) 
						{
							//* TODO Auto-generated catch block
							Log.d(TAG, "threw an IOException for sending file.");
							e.printStackTrace();
						}
						//sleep for a short while before attempting to get the next survey row item
						this.sleep(1000);
					}
				} 
			}
			catch (InterruptedException e) 
			{
				//* TODO Auto-generated catch block
				e.printStackTrace();
			}
		}//ends the run() function
	
		public void exit() //changes variable to false, will Thread will take care of exitting the thread
		{
			runThread = false;
		}
		
		/*
		 * this uses java.net.HttpURLConnection another way to do it is to use apache HttpPost
		 * but the API seems a bit complicated. If you figure out how to use it and its more
		 * efficient then let me know (vids@ucla.edu) Thanks.
		 */
	    private boolean doPost(String url, String q_taste, String q_visibility,
                               String q_operable, String q_flow,
                               String q_wheel, String q_child, String q_refill,
                               String q_refill_aux, String q_location,
                               String longitude, String latitude, String time,
                               String version,
                               String photo_filename) throws IOException //Idk what throws IOException means here
	    {
	    	Log.d(TAG, "Attempting to send file:" + photo_filename);
	    	Log.d(TAG, "Trying to post: "+url.toString()+" "+photo_filename.toString() + " "+ longitude.toString() + " ...");
	    	
	    	HttpClient httpClient = authenticate.httpClient; //??
	    	HttpPost request = new HttpPost(url.toString()); //?? - giving the url as a string as argument
	    	
	    	Log.d(TAG, "After Request");
	    	
	    	//where is this class defined (not in SDK)
	    	//add all the arguments as strings with descriptors as new parts to the entity
	    	MultipartEntity entity = new MultipartEntity();
	    	entity.addPart("q_taste", new StringBody(q_taste.toString()));
	    	entity.addPart("q_visibility", new StringBody(q_visibility.toString()));
            entity.addPart("q_operable", new StringBody(q_operable.toString()));
            entity.addPart("q_flow", new StringBody(q_flow.toString()));
            entity.addPart("q_wheel", new StringBody(q_wheel.toString()));
            entity.addPart("q_child", new StringBody(q_child.toString()));
            entity.addPart("q_refill", new StringBody(q_refill.toString()));
            entity.addPart("q_refill_aux", new StringBody(q_refill_aux.toString()));
            entity.addPart("q_location", new StringBody(q_location.toString()));
            entity.addPart("longitude", new StringBody(longitude.toString()));
            entity.addPart("latitude", new StringBody(latitude.toString()));
            entity.addPart("time", new StringBody(time.toString()));
            entity.addPart("version", new StringBody(version.toString()));

	    	
	    	Log.d(TAG, "After adding string");

	    	//add the photo file name to the entity as: string if no name exists, file if exists
	    	//log accordingly
            if (photo_filename == null || photo_filename.equals("")) {
                Log.d(TAG, "ADDING empty string as file contents");
                entity.addPart("file", new StringBody(""));
            } else {
                Log.d(TAG, "ADDING the actual file body of: >>" + photo_filename + "<<");
    	    	File file = new File(photo_filename.toString());
	        	entity.addPart("file", new FileBody(file));
            }
	    	
	    	Log.d(TAG, "After adding file");
	    	
	    	//what is "request"? - this part will require reading up about java.net.HttpURLConnection
	    	request.setEntity(entity);
	    	
	    	Log.d(TAG, "After setting entity");
	    	
	    	HttpResponse response = httpClient.execute(request);
	    	
	    	Log.d(TAG, "Doing HTTP Reqest");

	    	int status = response.getStatusLine().getStatusCode();
	    	//*Log.d(TAG, generateString(response.getEntity().getContent()));
	    	Log.d(TAG, "Status Message: "+Integer.toString(status));
	    	
	    	if(status == HttpStatus.SC_OK)
	    	{
		    	Log.d(TAG, "Sent file.");
	    		return true;
	    	}
	    	else
	    	{
		    	Log.d(TAG, "File not sent.");
	    		return false;
	    	}
	    	
	    }
	    
	    public String generateString(InputStream stream) {
	    	//create some new objects (defined in SDK)
  	      InputStreamReader reader = new InputStreamReader(stream);
  	       BufferedReader buffer = new BufferedReader(reader);
  	       StringBuilder sb = new StringBuilder(); //new Stringbuilder with inital size of 16 char (?)
  	    
  	       try {
  	           String cur;
  	           //read lines until there are no more (a.k.a. the null case)
  	           while ((cur = buffer.readLine()) != null) {
  	               sb.append(cur + "\n");	//append each read line but put a newline
  	               						  	//in after each line (returns sb)
  	           }
  	       } catch (IOException e) {
  	           //* TODO Auto-generated catch block
  	           e.printStackTrace();
  	       }
  	    
  	       try {
  	           stream.close();	//try to close the InputStreamReader
  	       } catch (IOException e) {
  	           //* TODO Auto-generated catch block
  	           e.printStackTrace();
  	       }
  	       return sb.toString(); //returns contents of sb - all the lines and newlines 
  	       						 //appended together
	    }
	    
	    
         /*
          * Read file into String. 
          */
         private String readFileAsString(File file) throws java.io.IOException{
             StringBuilder fileData = new StringBuilder(1024); //constructed with 1024 char capacity
             BufferedReader reader = new BufferedReader(new FileReader(file));
             char[] buf = new char[1024]; //create a character array of size 1024
             int numRead=0;			//not "false" initialization
             while((numRead=reader.read(buf)) != -1){	//reads from the BufferedReader. -1 is returned for eof
            	 										//needs more arguements? length, offset?
            	 fileData.append(buf, 0, numRead); //append to stringBuilder everything in buf
             }
             reader.close();
             return fileData.toString();
         }	
	     //^don't really understand how this is working because it doesn't look like it should
         //find where it is called.
	    
	}

}
