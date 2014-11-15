package com.dvictor.twitter.fragments;

import java.util.List;

import android.os.Bundle;

import android.util.Log;
import com.dvictor.twitter.models.Tweet;
import com.dvictor.twitter.models.User;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class UserTimelineFragment extends TweetsListFragment {
	/** If show a custom user timeline instead of the current user. (-1 for default current user) */
	private long optCustomUid = -1;

	/** [For static loading] The activity can tell the timeline to show a custom user instead
	 *  of defaulting to the current user.  Set to NULL (default) for current user. */
	public void setCustomUser(User user){ // Needed if we do the static loading alternative.
		if(user==null) optCustomUid  = -1;
		else           optCustomUid = user.getUid();
	}
	
	/** [For dynamic loading] The activity can pass a custom user instead of defaulting to the current user.
	 *  Set to NULL (default) for current user. */
    public static UserTimelineFragment newInstance(User user) {
    	UserTimelineFragment f = new UserTimelineFragment();
        Bundle args = new Bundle();
        if(user!=null) args.putLong("uid", user.getUid());
        f.setArguments(args);
        Log.e("DVDEBUG","new fragment: "+user);
        if(user!=null) Log.e("DVDEBUG","new fragment: "+user.getUid());
        return f;
    }	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		optCustomUid = getArguments().getLong("uid", -1);
		super.onCreate(savedInstanceState);
	}
	
	/** Call the correct client API method for this fragment's tweets. */
	@Override
	protected void getTweets(long lastItemId, AsyncHttpResponseHandler handler){
		Log.e("DVDEBUG","Get Tweets for: "+optCustomUid);
		getClient().getUserTimeline(optCustomUid, lastItemId, handler);		
	}
	
	/** Query for the correct offline tweets for the particular fragment type. */
	@Override
	protected List<Tweet> getOfflineTweets(){
		return Tweet.retrieveAll();
	}

}
