package rocks.pocha.sampleapp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.pixplicity.easyprefs.library.Prefs;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {
    @Bind(R.id.app_id)
    TextView appId;
    @Bind(R.id.gcm_id)
    TextView gcmId;

    String _appId, _gcmId;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        progress = ProgressDialog.show(MainActivity.this, "", "Initializing ..", true, false);
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                _appId = Prefs.getString("app_id",null);
                if (_appId == null) {
                    //generate app_id
                    _appId = String.valueOf(new Random().nextInt(10000));
                    Prefs.putString("app_id", _appId);
                }

                _gcmId = Prefs.getString("gcm_id",null);
                if (_gcmId == null) {
                    //register with gcm & get id
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    try {
                        _gcmId = gcm.register("528993769112");
                        Prefs.putString("gcmId", _gcmId);

                        String url = "http://localhost:3000/endpoints/register/" + _appId + "/" + _gcmId;
                        OkHttpClient client = new OkHttpClient();
                        try {
                            Request request = new Request.Builder()
                                    .url(url)
                                    .build();

                            client.newCall(request).execute();
                        }
                        catch (Exception e){
                            Log.e("MainActivity",e.getMessage());
                        }

                    } catch (Exception e){
                        _gcmId = "Error registering device for GCM";
                        Log.e("MainActivity","Error while getting gcm id - "+ e.getMessage());
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(Boolean result) {
                progress.dismiss();
                appId.setText(_appId);
                gcmId.setText(_gcmId);
            }
        }.execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
