# Twitter with Services & Notifications
<i> Android Intermediate Class - Week 3 - Services and Notifications </i>

## Author
- David Victor

## User Stories
<ul>
  <li> Create an IntentService that retrieves new home timeline data from Twitter
    <ul>
      <li> Store the home timeline data fetch from the service into a SQLite database (using ActiveAndroid or equivalent)
    </ul>
  <li> Use an AlarmManager to periodically wake the service and trigger downloads of new tweets
  <li> Create a local notification whenever new tweets have been found and downloaded
    <ul>
      <li> Only create notification when new data is found
      <li> Don't create duplicate notifications, overwrite the same notification each time updating the count
      <li> i.e "30 new tweets since you last opened Twitter"
    </ul>
  <li> Twitter app should then load current home timeline tweets from the database
  <li> <b>(Optional)</b> Download new mentions timeline data as well and store to database.
    <ul>
      <li> Twitter should fetch mentions timeline from database as well
    </ul>
  <li> <b>(Optional)</b> If twitter app is open, have the service notify the twitter app when new tweets are available and app should refresh to show them.
  <li> <b>(Optional)</b> Change notifications style to custom styling with expandable view of latest tweet body
</ul>

## Demo
![Demo](demo.gif "Demo") 

## Reference
- [Assignment](https://yahoo.jiveon.com/docs/DOC-6532)
- [Using ActiveAndroid for SQLite](http://guides.codepath.com/android/ActiveAndroid-Guide)
- [Starting Background Services](http://guides.codepath.com/android/Starting-Background-Services)
- [Creating Notifications](http://guides.codepath.com/android/Notifications)
- [Periodic AlarmManager](http://guides.codepath.com/android/Starting-Background-Services#using-with-alarmmanager-for-periodic-tasks) 
