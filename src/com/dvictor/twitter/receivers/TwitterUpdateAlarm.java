package com.dvictor.twitter.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dvictor.twitter.services.TwitterService;

public class TwitterUpdateAlarm extends BroadcastReceiver {

	public TwitterUpdateAlarm() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// Alarm manager will fire a message.
		// This receiver receives the message.
		// Start the intent service.
		context.startService(new Intent(context,TwitterService.class));
	}

}
