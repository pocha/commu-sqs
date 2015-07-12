## Commu-SQS

This project is to demonstrate how to use Amazon SQS for a gauranteed & fastest server -> (Android) client communication. Usually, people use GCM for such cases but GCM message tend to get delayed. 

Here, one queue per Android client is created on Amazon SQS. Server inserts data to the queue. On Android, there is a persistent background service that keeps listening for data on the queue. On getting data, it generates a notification. 

In the event of user force closing the app, the background service will get killed. A daemon as part of the server (located in `libs/daemones/monitor_queue.rb`) keeps scanning all the queues. On finding pending data that has not been fetched, the daemon sends a **wake up GCM push** to the device. The push is received by the broadcast receiver, which in turn brings the killed background service to life. 

Note - no data is being passed in the GCM push as it would only increase the *data-duplication-check* overhead at the Android app. 

On top of the wake up push, in the Android client, a `BaseActivity` is created which checks if the service is running & starts it. Ideally, all your Activities should extend BaseActivity so that the service status is checked whenever any Activity is loaded (lets say from a different GCM push). 

### TO-DO 

I had plans to extend the service for upstream purpose (sending data from Android to server) but I am still in two-minds if this is the right approach. Ideally, `SyncAdapter` takes care of data transmission from client to server as it has inbuilt mechanism of checking when network connection is available etc. Comments on this are welcome. 

### Setup 

### Rails server

Copy `config/app_environment_variables.rb.example` to `config/app_environment_variables.rb` & fill in the appropriate details. 

Deploy the rails app. Start the monitor\_queue as `rake daemon:monitor_queue:start`. Do `tail -f logs/*` to see all the logs. If the app is deployed on heroku, do `heroku logs -t`. 

### Android App

1. Import `android-app` directory in Android Studio.
2. Copy `AwsCredentials.java.example` as `AwsCredentials.java` & fill in the required details like AWS & GCM credentials.
3. Modify line 31 in MainActivity.java

    private static String SERVER_URL= "http://192.168.1.5:3000/";

3. Deploy maadi (thats Kannada for *do it*)

### Seeing it in action

On opening the app, you would see the app generating a random app id & gcm id. The same is being sent to the server. Refresh the server to see a new queue being created. 

Now push a message to the queue, the message should immediately come to the app with a notification. 

Try force closing the app, create a new message & keep checking the daemon logs. There should be a GCM message that would be sent from the server to Android app, which will wake up the app & starts the background service. The app will then register the new message. 

Let me know how you like it by dropping me a word at [@pocha](http://twitter.com/pocha) :-)
