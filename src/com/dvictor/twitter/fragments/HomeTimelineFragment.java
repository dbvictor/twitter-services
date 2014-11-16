package com.dvictor.twitter.fragments;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.dvictor.twitter.activities.TimelineActivity;
import com.dvictor.twitter.models.Tweet;
import com.dvictor.twitter.services.TwitterService;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class HomeTimelineFragment extends TweetsListFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	/** Call the correct client API method for this fragment's tweets. */
	@Override
	protected void getTweets(long lastItemId, AsyncHttpResponseHandler handler){
		getClient().getHomeTimeline(lastItemId, handler);		
	}
	
	/** Query for the correct offline tweets for the particular fragment type. */
	@Override
	protected List<Tweet> getOfflineTweets(){
		return Tweet.retrieveAll();
	}
	
}
