package com.dvictor.twitter.fragments;

import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dvictor.twitter.TwitterApp;
import com.dvictor.twitter.clients.TwitterClient;
import com.dvictor.twitter.models.Tweet;
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
