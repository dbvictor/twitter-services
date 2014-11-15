package com.dvictor.twitter.adapters;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dvictor.twitter.R;
import com.dvictor.twitter.fragments.ModelDialogFragment;
import com.dvictor.twitter.listeners.OnSwipeTouchListener;
import com.dvictor.twitter.models.Tweet;
import com.nostra13.universalimageloader.core.ImageLoader;

public class TweetArrayAdapter extends ArrayAdapter<Tweet> {
	final FragmentActivity activity;
	public TweetArrayAdapter(FragmentActivity activity, List<Tweet> objects){
		super(activity, 0, objects);
		this.activity = activity;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Tweet tweet = getItem(position);
		View v;
		if (convertView == null) {
			LayoutInflater inflator = LayoutInflater.from(getContext());
			v = inflator.inflate(R.layout.tweet_item, parent, false);
		} else {
			v = convertView;
		}
		// Find views within template
		ImageView ivProfileImage = (ImageView) v.findViewById(R.id.ivProfileImage);
		TextView  tvRealName     = (TextView ) v.findViewById(R.id.tvRealName);
		TextView  tvUserName     = (TextView ) v.findViewById(R.id.tvUserName);
		TextView  tvTime         = (TextView ) v.findViewById(R.id.tvTime);
		TextView  tvBody         = (TextView ) v.findViewById(R.id.tvBody);
		// Clear existing image (needed if it was reused)
		ivProfileImage.setImageResource(android.R.color.transparent);
		// Populate views with tweet data.
		ImageLoader imageLoader = ImageLoader.getInstance();  // Universal loader we will use to get the image for us (asynchronously)
		imageLoader.displayImage(tweet.getUser().getImageUrl(), ivProfileImage); // Asynchronously load image using universal loader.
		tvRealName.setText(tweet.getUser().getRealName());
		tvUserName.setText("@"+tweet.getUser().getScreenName());
		tvTime.setText("("+getRelativeTimeAgo(tweet.getCreatedAt())+")");
		tvBody.setText(tweet.getBody());
		// Store the user into the image so that when they click on it, we can know which user to show profile.
		ivProfileImage.setTag(tweet.getUser());
		
		// Setup Swipe Actions
		setupSwipeActions(tvRealName,tvUserName,tvBody);
		
		return v;
	}
	
	// getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
	public String getRelativeTimeAgo(String rawJsonDate) {
		String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
		SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
		sf.setLenient(true);

		String relativeDate = "";
		try {
			long dateMillis = sf.parse(rawJsonDate).getTime();
			relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
					System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}

		return relativeDate;
	}
	
	/** Setup swipe actions */
	private void setupSwipeActions(View... swipableItems){
		for(View v : swipableItems){
			// - Right
			v.setOnTouchListener(new OnSwipeTouchListener(v.getContext()) {
				  @Override
				  public void onSwipeRight() {
				    Toast.makeText(activity, "Right", Toast.LENGTH_SHORT).show();
				    // If created by user, modal dialog to delete
				    // TODO: Detect if created by user.
				  	FragmentManager fm = activity.getSupportFragmentManager();
				  	ModelDialogFragment alertDialog = ModelDialogFragment.newInstance("Delete?","Do you want to delete?","Yes","No", new Runnable(){
						@Override
						public void run() {
							// TODO: actually implement this.
						    Toast.makeText(activity, "Deleted", Toast.LENGTH_SHORT).show();
						}
				  	});
				  	alertDialog.show(fm, "fragment_confirm");
				  	// TODO: If not created by user, modal dialog to re-tweet.
				  }
			});
		}
	}
}
