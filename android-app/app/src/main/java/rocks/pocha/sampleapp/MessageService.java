package rocks.pocha.sampleapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.pixplicity.easyprefs.library.Prefs;

public class MessageService extends Service {
    private static boolean isServiceRunning = false;

    private final static String TAG = "AwsSqsManager";

    private static BasicAWSCredentials credentials;
    private static ClientConfiguration config;
    private static AmazonSQSAsyncClient sqs;
    private static String queueUrl;

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public MessageService() {

    }

    public void onCreate(){
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Log.d("MessageService", "onStartCommand isServiceRunning = " + isServiceRunning);

            if (isServiceRunning == true)
                return START_STICKY;
            isServiceRunning = true;


            //start listening to the queue
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        credentials = new BasicAWSCredentials(AwsCredentials.AWS_ACCESS_KEY, AwsCredentials.AWS_SECRET_KEY);
                        config = new ClientConfiguration();
                        config.setConnectionTimeout(60000);
                        config.setSocketTimeout(60000);
                        AWSCredentialsProvider acp = new AWSCredentialsProvider() {
                            @Override
                            public AWSCredentials getCredentials() {
                                return credentials;
                            }

                            @Override
                            public void refresh() {
                            }
                        };
                        sqs = new AmazonSQSAsyncClient(acp, config);
                        sqs.setEndpoint("http://sqs." + AwsCredentials.AWS_REGION + ".amazonaws.com");
                        sqs.setRegion(Region.getRegion(Regions.fromName(AwsCredentials.AWS_REGION)));

                        String appId = Prefs.getString("app_id", null);
                        //This helps if AWS IP changes. JVM caches the IP address.
                        java.security.Security.setProperty("networkaddress.cache.ttl", "60");

                        CreateQueueRequest createQueueRequest = new CreateQueueRequest();
                        createQueueRequest.addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");
                        createQueueRequest.addAttributesEntry("VisibilityTimeout", "10");
                        createQueueRequest.setQueueName(appId);
                        CreateQueueResult result = sqs.createQueue(createQueueRequest);
                        queueUrl = result.getQueueUrl();
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(),"Probably wrong credentials in AwsCredentials.java", Toast.LENGTH_LONG);
                        Log.e(TAG,e.getMessage());
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest();
                    receiveMessageRequest.setQueueUrl(queueUrl);
                    receiveMessageRequest.setMaxNumberOfMessages(1); //get only one message

                    try {
                        while (true) {
                            ReceiveMessageResult message = sqs.receiveMessage(receiveMessageRequest);
                            for (Message m : message.getMessages()) {
                                showNotification(m.getBody());
                                DeleteMessageRequest request = new DeleteMessageRequest(queueUrl, m.getReceiptHandle());
                                sqs.deleteMessage(request);
                            }
                            Thread.sleep(1000);
                        }
                    }catch (Exception e){
                        //start this activity again
                        Log.e(TAG,"Error while fetching from SQS " + e.getMessage());
                        isServiceRunning = false;
                    }
                }
            }.execute();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } finally {
            return START_STICKY;
        }

    }

    private void showNotification(String message){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Hey You")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setContentText(message)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);

        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService
                (Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
