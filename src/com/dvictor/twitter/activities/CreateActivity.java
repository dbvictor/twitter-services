package com.dvictor.twitter.activities;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dvictor.twitter.R;
import com.dvictor.twitter.TwitterApp;
import com.dvictor.twitter.R.id;
import com.dvictor.twitter.R.layout;
import com.dvictor.twitter.clients.TwitterClient;
import com.dvictor.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

public class CreateActivity extends Activity {
	private TwitterClient client;
	private Tweet tweet;
	// Remembered Views
	private EditText etBody;
	private TextView tvCharsRemaining;
	// Constants
	private static final int MAX_LENGTH = 140;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create);
		client = TwitterApp.getRestClient();
		// Remember views for easy access later.
		etBody           = (EditText) findViewById(R.id.etNewTweet      );
		tvCharsRemaining = (TextView) findViewById(R.id.tvCharsRemaining);
		tvCharsRemaining.setText(""+MAX_LENGTH+" remaining" );
		// Setup events
		setupTextChangeListener();
	}
	
	private void setupTextChangeListener(){
		etBody.addTextChangedListener(new TextWatcher(){
			@Override public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				// do nothing
			}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				// do nothing
			}
			@Override public void afterTextChanged(Editable s) {
				int count = etBody.getText().toString().length();
				tvCharsRemaining.setText(""+(MAX_LENGTH-count)+" remaining" );
			}
		});
	}
	
	public void create(View v){
		String etBodyText = etBody.getText().toString();
		// If empty, don't allow send.
		if((etBodyText==null)||(etBodyText.trim().length()<=0)){
			Toast.makeText(this, "Nothing to Post!", Toast.LENGTH_SHORT).show();
		// Else send tweet!
		}else{
			// 1. Send text to Twitter
			final CreateActivity parentThis = this;
			client.createTweet(etBody.getText().toString(), new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(JSONObject json) {
					Log.d("json", "Created JSON: "+json.toString());
					tweet = Tweet.fromJSON(json);
					Toast.makeText(parentThis, "Posted", Toast.LENGTH_SHORT).show();
					// 2. Return result to timeline activity
					Intent i = new Intent();
					i.putExtra("tweet", tweet);
					setResult(RESULT_OK, i);
					finish();
					overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top);
				}
				@Override
				public void onFailure(Throwable e, String s) {
					Log.d("debug", e.toString());
					Log.d("debug", s.toString());
					Toast.makeText(parentThis, "FAILED!", Toast.LENGTH_SHORT).show();
					// Don't return to timeline.  Allow them a chance to retry.  They can always hit the back button.
				}
			});
		}
	}
	
	/** Override the back button behavior to override default animation. */
	@Override
	public void onBackPressed(){
		finish();
		overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top);
	}	
	
}
