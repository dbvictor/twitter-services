package com.dvictor.twitter.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.dvictor.twitter.R;
import com.dvictor.twitter.fragments.HomeTimelineFragment;
import com.dvictor.twitter.fragments.MentionsTimelineFragment;
import com.dvictor.twitter.fragments.TweetsListFragment;
import com.dvictor.twitter.listeners.FragmentTabListener;
import com.dvictor.twitter.models.Tweet;
import com.dvictor.twitter.models.User;
import com.dvictor.twitter.receivers.TwitterUpdateAlarm;
import com.dvictor.twitter.services.TwitterService;
import com.dvictor.twitter.util.InternetStatus;

public class TimelineActivity extends FragmentActivity {
	// Constants
	private static final String FRAGMENTTAG_HOME     = "home";
	private static final String FRAGMENTTAG_MENTIONS = "mentions";
	private static final int    ACTIVITY_CREATE      = 1;
	private static final int    ACTIVITY_PROFILE     = 2;
	// Member Variables
	private InternetStatus     internetStatus;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		internetStatus = new InternetStatus(this);
		setupTabs();
		setupServices();
	}
	
	private void setupTabs() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);

        Tab tab1 = actionBar
            .newTab()
            .setText("Home")
            .setIcon(R.drawable.ic_home)
            .setTag("HomeTimelineFragment")
            .setTabListener(new FragmentTabListener<HomeTimelineFragment>(R.id.flContainer, this, FRAGMENTTAG_HOME, HomeTimelineFragment.class));

        actionBar.addTab(tab1);
        actionBar.selectTab(tab1);

        Tab tab2 = actionBar
            .newTab()
            .setText("Mentions")
            .setIcon(R.drawable.ic_mentions)
            .setTag("MentionsTimelineFragment")
            .setTabListener(new FragmentTabListener<MentionsTimelineFragment>(R.id.flContainer, this, FRAGMENTTAG_MENTIONS, MentionsTimelineFragment.class));

        actionBar.addTab(tab2);
    }
	
	/* START & SCHEDULE SERVICES */
	private void setupServices(){
		// Run Twitter Update service right away to start one update right away.
		Intent i = new Intent(this, TwitterService.class);
		i.putExtra("foo", "bar");
		startService(i);
		// Schedule an alarm to periodically check there-after
		scheduleAlarm();
	}
	
	// You can schedule this anywhere, but for simplicity we will do this in Activity for now.
	// Even a service can schedule an alarm.
	public void scheduleAlarm() {
		// What do you want to do when the alarm goes off.
	    // Construct an intent that will execute the AlarmReceiver
	    Intent intent = new Intent(getApplicationContext(), TwitterUpdateAlarm.class);
	    // Create a PendingIntent to be triggered when the alarm goes off
	    final PendingIntent pIntent = PendingIntent.getBroadcast(this, 1234, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    // Setup periodic alarm every 5 seconds
	    long firstMillis = System.currentTimeMillis(); // first run of alarm is immediate
	    int intervalMillis = 60*1000; // 1 minute  (actually responds within seconds of error)
	    AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
	    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, intervalMillis, pIntent);
	}

	/* BROADCAST RECEIVER -- not currently needed in this activity
	// 1. Defining the broadcast receiver
	private BroadcastReceiver myTestReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			String foo = intent.getStringExtra("foo");
			Toast.makeText(TimelineActivity.this, "Received message: "+foo, Toast.LENGTH_SHORT).show();
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		// Register for the particular broadcast based on ACTION string
		// Define a filter that will listen to this action.
		IntentFilter filter = new IntentFilter(TwitterService.ACTION);
		// Tell the broadcast receiver to register our receiver for what it listens for.
		// - Use local broadcast manager for faster actions specific to our app.
		// - Use normal broadcast manager for slower but global to whole phone.  Actions/filters for these must be documented somewhere, like Google for phone stuff.  You can also view all registered actions on a phone.
		LocalBroadcastManager.getInstance(this).registerReceiver(myTestReceiver, filter);
		// or `registerReceiver(testReceiver, filter)` for a normal broadcast
	}
	
	@Override
	protected void onPause() {
		// Unregister the listener when the application is paused
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(myTestReceiver);
		// or `unregisterReceiver(testReceiver)` for a normal broadcast
	} */
	
	// Inflate the menu; this adds items to the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_timeline, menu);
		MenuItem internetToggle = menu.findItem(R.id.actionInternetToggle);
		setupInternetToggle(internetToggle);
		return true;
	}

	/** Menu selection to turn on/off Internet to simulate offline. */
	public void internetToggle(MenuItem menuItem){
		internetStatus.setAppToggleEnabled(!internetStatus.isAppToggleEnabled());
		setupInternetToggle(menuItem);
		// If re-enabled, re-setup endless scroll & load data if if none loaded yet.
		if(internetStatus.isAppToggleEnabled()){
			TweetsListFragment fHome     = (TweetsListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENTTAG_HOME    );
			TweetsListFragment fMentions = (TweetsListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENTTAG_MENTIONS);
			if(fHome    !=null) fHome    .onInternetResume();
			if(fMentions!=null) fMentions.onInternetResume();
		}
	}

	/** Update the Internet status visual indicators. */
	public void setupInternetToggle(MenuItem menuItem){
		// Update the menu item to show the correct toggle state.
		// + toast just to make it clear what the current state is.
		if(internetStatus.isAppToggleEnabled()){
			Toast.makeText(this, "Internet ON", Toast.LENGTH_SHORT).show();
			menuItem.setIcon(R.drawable.ic_action_internet_off);
		}else{
			Toast.makeText(this, "Internet OFF", Toast.LENGTH_SHORT).show();
			menuItem.setIcon(R.drawable.ic_action_internet_on);
		}
	}
	
	/** Menu selection to create a new tweet. */
	public void create(MenuItem menuItem){
		if(!internetStatus.isAvailable()){ // If no network, don't allow create tweet.
			Toast.makeText(this, "Network Not Available!", Toast.LENGTH_SHORT).show();
		}else{
			//Toast.makeText(this, "Settings!", Toast.LENGTH_SHORT).show();
			Intent i = new Intent(this,CreateActivity.class);
			//no args: i.putExtra("settings", searchFilters);
			startActivityForResult(i, ACTIVITY_CREATE);
			overridePendingTransition(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom);
		}
	}

	/** Menu selection to view profile. */
	public void viewProfile(MenuItem menuItem){
		Intent i = new Intent(this,ProfileActivity.class);
		//no args: i.putExtra("settings", searchFilters);
		startActivityForResult(i, ACTIVITY_PROFILE);
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}
	
	public void onProfileClick(View v){
		// Get the user that they clicked on.
		User u = (User) v.getTag(); // We stored the user object in TweetArrayAdapter when the image is created.
		if(u==null){ // Error if not there.
			Toast.makeText(this, "Image Missing User Info!", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent i = new Intent(this,ProfileActivity.class);
		i.putExtra("user", u);
		startActivityForResult(i, ACTIVITY_PROFILE);
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	if(requestCode==ACTIVITY_CREATE){ // CreateActivity Result
    		if(resultCode == RESULT_OK){
    			Tweet tweet = (Tweet) data.getSerializableExtra("tweet");
    			if(tweet!=null){
    				TweetsListFragment fragmentHome = (TweetsListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENTTAG_HOME);
    				if(fragmentHome!=null){
    					fragmentHome.insert(tweet, 0);
    					Toast.makeText(this, "Timeline Updated", Toast.LENGTH_SHORT).show();
    				}
    			}else Toast.makeText(this, "MISSING RESULT", Toast.LENGTH_SHORT).show();    				
    		}
    	}
    }
	
}
