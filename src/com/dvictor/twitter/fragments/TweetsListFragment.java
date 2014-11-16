package com.dvictor.twitter.fragments;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.dvictor.twitter.R;
import com.dvictor.twitter.TwitterApp;
import com.dvictor.twitter.activities.TimelineActivity;
import com.dvictor.twitter.adapters.TweetArrayAdapter;
import com.dvictor.twitter.clients.TwitterClient;
import com.dvictor.twitter.listeners.EndlessScrollListener;
import com.dvictor.twitter.models.Tweet;
import com.dvictor.twitter.services.TwitterService;
import com.dvictor.twitter.util.InternetStatus;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

abstract public class TweetsListFragment extends Fragment {
	private TwitterClient      client;
	private ArrayList<Tweet>   tweets;
	private TweetArrayAdapter  aTweets;
	private ListView		   lvTweets;
	private long               lastItemId;
	private InternetStatus     internetStatus;
	private SwipeRefreshLayout swipeContainer;	

	/** Non-view / non-UI related initialization. (fires before onCreateView()) */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Non-view initialization
		client         = TwitterApp.getRestClient();
		internetStatus = new InternetStatus(getActivity());
		lastItemId     = 0; // Always start from 0.
		tweets         = new ArrayList<Tweet>();
		aTweets        = new TweetArrayAdapter(getActivity(), tweets); // WARNING: RARELY USE getActivity().  Other usage is likely improper.
		populateTimeline(true);
	}
	
	/** View/UI-related initialization. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate Layout
		View v = inflater.inflate(R.layout.fragment_tweets_list, container, false); // false = don't attach to container yet.
		// Assign view preferences
		setupSwipeContainer(v);
		lvTweets = (ListView) v.findViewById(R.id.lvTweets);
		lvTweets.setAdapter(aTweets);
		setupEndlessScroll();
		// Return layout view
		return v;
	}
	
	/** Setup swipe down to refresh. */
	private void setupSwipeContainer(View v){
        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                populateTimeline(true); // true = start from beginning again
                setupEndlessScroll(); // Resetup endless scroll in case it previously hit the bottom and stopped scrolling further again. 
            } 
        });
        // Configure the refreshing colors
        swipeContainer.setColorScheme(android.R.color.holo_blue_bright, 
                android.R.color.holo_green_light, 
                android.R.color.holo_orange_light, 
                android.R.color.holo_red_light);		
	}
	
	private void setupEndlessScroll(){
		lvTweets.setOnScrollListener(new EndlessScrollListener() {
			/** The endless scroll listener will call us whenever its count says that we need more.  We don't care what page it is on, we just get more. */
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				populateTimeline(false); 
			}
		});
	}
	
	/**
	 * After Internet/network toggled back on, re-setup anything necessary.  Such as:
	 * 1. Start endless scroll again, which might have thought it reached the end and stopped trying for more.
	 * 2. Load data for the first time if we never had retrieved (or found) any yet, because endless scroll will not load more if already 0. */
	public void onInternetResume(){
		// Re-setup endless scroll if re-enabled.
		setupEndlessScroll(); // Re-enable endless scrolling because if it hit end before, it would not try again.
		if(tweets.size()==0) populateTimeline(true); // If no tweets so far, then the app just started and we have to re-run populate because it could have ran already and found no network.
	}

	
	/** Delegate the adding to the internal adapter. */
	//NOT NEEDED: public void addAll(List<Tweet> tweets){
	//NOT NEEDED:	aTweets.addAll(tweets);
	//NOT NEEDED:}
	
	/** Insert a new tweet at any position. */
	public void insert(Tweet tweet, int position){
		aTweets.insert(tweet, position);		
	}
	
	/** Subclasses can get the TwitterClient instance. */
	protected TwitterClient getClient(){
		return client;
	}
	
    /** Populate the list with tweets. */
	public void populateTimeline(final boolean refresh){
		Log.d("DVDEBUG", "+ TimelineActivity.populateTimeline()");
		if(!internetStatus.isAvailable()){ // If no network, don't allow create tweet.
			Toast.makeText(getActivity(), "Network Not Available!", Toast.LENGTH_SHORT).show();
			if(refresh) populateTimelineOffline(refresh);
		}else{
			final TweetsListFragment parentThis = this;
			if(refresh) lastItemId = 0; // If told to refresh from beginning, start again from 0.
			getTweets(lastItemId, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(JSONArray json) {
					Log.d("json", parentThis.getClass().getSimpleName()+" JSON: "+json.toString());
					if(refresh) aTweets.clear(); // If told to refresh from beginning, clear existing results
					ArrayList<Tweet> retrievedTweets = Tweet.fromJSON(json);
					aTweets.addAll(retrievedTweets);
					lastItemId = tweets.get(tweets.size()-1).getUid(); // record the last item ID we've seen now, so we know where to continue off from next time.
	                // Now we call setRefreshing(false) to signal refresh has finished
	                swipeContainer.setRefreshing(false);
	                // Persist results we found so far.  Save all tweets we ever find.  The specific fragment type only matters later when we query for specific kinds to load offline.
	                try{
	                	for(Tweet t : retrievedTweets){
	                		t.getUser().save();
	                		t.save();
	                	}
						Log.d("persist", "Persisted Timeline Results");
	                }catch(Exception e){
						Log.e("error", e.toString());
						Toast.makeText(parentThis.getActivity(), "PERSIST FAILED!", Toast.LENGTH_SHORT).show();
	                }
				}
				@Override
				public void onFailure(Throwable e, String s) {
					Log.e("error", e.toString());
					Log.e("error", s.toString());
				}
			});
		}
	}
	
	/** Populate the timeline based on offline content. */
	private void populateTimelineOffline(final boolean refresh){
		List<Tweet> retrievedTweets = getOfflineTweets();
		aTweets.addAll(retrievedTweets);
		lastItemId = tweets.get(tweets.size()-1).getUid(); // record the last item ID we've seen now, so we know where to continue off from next time.
		Toast.makeText(getActivity(), "Offline Content: "+retrievedTweets.size(), Toast.LENGTH_SHORT).show();
        swipeContainer.setRefreshing(false);
	}

	/** Call the correct client API method for this fragment's tweets. */
	abstract protected void getTweets(long lastItemId, AsyncHttpResponseHandler handler);
	
	/** Query for the correct offline tweets for the particular fragment type. */
	abstract protected List<Tweet> getOfflineTweets();
	
	/* BROADCAST RECEIVER */
	// 1. Defining the broadcast receiver
	private BroadcastReceiver myTestReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			int newTweetsSize = intent.getIntExtra("size",-1);
			Toast.makeText(getActivity(), ""+newTweetsSize+" New Tweets Ready", Toast.LENGTH_SHORT).show();
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		// Register for the particular broadcast based on ACTION string
		// Define a filter that will listen to this action.
		IntentFilter filter = new IntentFilter(TwitterService.ACTION);
		// Tell the broadcast receiver to register our receiver for what it listens for.
		// - Use local broadcast manager for faster actions specific to our app.
		// - Use normal broadcast manager for slower but global to whole phone.  Actions/filters for these must be documented somewhere, like Google for phone stuff.  You can also view all registered actions on a phone.
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(myTestReceiver, filter);
		// or `registerReceiver(testReceiver, filter)` for a normal broadcast
	}
	
	@Override
	public void onPause() {
		// Unregister the listener when the application is paused
		super.onPause();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(myTestReceiver);
		// or `unregisterReceiver(testReceiver)` for a normal broadcast
	}
	
}
