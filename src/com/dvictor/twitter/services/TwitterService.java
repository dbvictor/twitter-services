package com.dvictor.twitter.services;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.dvictor.twitter.R;
import com.dvictor.twitter.TwitterApp;
import com.dvictor.twitter.activities.TimelineActivity;
import com.dvictor.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

// IMPORTANT: Just like an activity, it must be registered in the manifest.
public class TwitterService extends IntentService {
	public static final String ACTION = "com.dvictor.twitter.TwitterService"; // actual value doesn't matter, but common to use qualified name
	private static Long lastItemId = null;

	// IMPORTANT: services MUST have default constructor with no arguments.
	public TwitterService() {
		super("twitter service");
		// Initialize state variables
	}

	// Task to be performed.  If this were an AsyncTask, it is the doInBackground().
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.e("DVDEBUG","TwitterService.handleIntent");
		// 1. Read in intent arguments (incoming data, like what image to upload)
		// 2. Processing and output
		// 3. Die
		
		// Read
		// => Don't need any input: String foo = intent.getStringExtra("foo");

		// Check for new tweets
		// 1. Determine the last known item ID in SQLite if not already known
		if(lastItemId==null){
			// Just get all, and check the ID of the last one.  TODO - optimize this better to just get the latest.
			List<Tweet> tweets = Tweet.retrieveAll();
			if(tweets.size()<=0) lastItemId = 0l;
			else                 lastItemId = tweets.get(tweets.size()-1).getUid();
			Log.e("DVDEBUG","lastItemId determined to be = "+lastItemId);
		}else{ Log.e("DVDEBUG","lastItemId already known = "+lastItemId); }
		// 2. Get tweets since that ID
		TwitterApp.getRestClient().getHomeTimeline(lastItemId, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(JSONArray json) {
				Log.e("DVDEBUG","getHomeTimeline() onSuccess");
				ArrayList<Tweet> tweets = Tweet.fromJSON(json);
				Log.d("result", "found '"+tweets.size()+"' new tweets");
				Log.e("DVDEBUG","getHomeTimeline() onSuccess - results = "+tweets.size());
                // Persist any new results we find to SQLite
                try{
                	for(Tweet t : tweets){
                		t.getUser().save();
                		t.save();
                	}
					Log.d("persist", "Persisted '"+tweets.size()+"' results");
                }catch(Exception e){
					Log.e("error", e.toString());
                }
                // If found any
                if(tweets.size()<0){
                	lastItemId = tweets.get(tweets.size()-1).getUid();
                	// Notify UI
                	notifyUI(TwitterService.this, tweets);
                	// Notify User
	        		notifyUser(TwitterService.this, tweets);
                }
			}
			@Override
			public void onFailure(Throwable e, String s) {
				Log.e("DVDEBUG","getHomeTimeline() onFailure");
				Log.e("error", e.toString());
				Log.e("error", s.toString());
			}
		});
		
		// Output
		// A. Writing DB 
		// B. Creating Notification		
		// C. Sending a broadcast message
		// D. Launch intent directly (if critically urgent like amber alert)
	}
	
	private static void notifyUI(Context context, ArrayList<Tweet> tweets){
		Log.e("DVDEBUG","+notifyUI()");
		// BROADCAST
		// - Use local broadcast manager for faster actions specific to our app.
		// - Use normal broadcast manager for slower but global to whole phone.
		Intent broadcastInfo = new Intent(ACTION);
		broadcastInfo.putExtra("size", tweets.size());
		LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastInfo);
		Log.e("DVDEBUG","-notifyUI()");
	}
	
	private static void notifyUser(Context context, ArrayList<Tweet> tweets){
		Log.e("DVDEBUG","+notifyUser()");
		// NOTIFY USER OF NEW TWEETS
		// Notification action
		Intent i = new Intent(context, TimelineActivity.class);
		// If you want multiple to fire at the same time, generate by timeMS.  Else use a fixed one and it will fire only one.
		int requestID = (int) System.currentTimeMillis(); //unique requestID to differentiate between various notification with same NotifId
		int flags = PendingIntent.FLAG_CANCEL_CURRENT; // cancel old intent and create new one
		PendingIntent pIntent = PendingIntent.getActivity(context, requestID, i, flags);
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(context)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(""+tweets.size()+" New Tweets Received")
		        .setContentIntent(pIntent)		    
		        .setAutoCancel(true) // / Hide the notification after its selected
	            //No sub-actions: .addAction(R.drawable.ic_launcher, "Share", pIntent)
	            //No sub-actions: .addAction(R.drawable.ic_launcher, "Ignore", pIntent)
		        .setContentText("Last tweet was: "+tweets.get(tweets.size()-1).getBody());
		// mId allows you to update the notification later on.  It will show you only the last one with this ID.  You can go back and remove others also.
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(123, mBuilder.build()); // NOTE: Id is namespaced to your application package name.
		Log.e("DVDEBUG","-notifyUser()");
	}

}
