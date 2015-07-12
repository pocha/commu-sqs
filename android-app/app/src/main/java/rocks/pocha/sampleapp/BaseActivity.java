package rocks.pocha.sampleapp;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;

import com.pixplicity.easyprefs.library.Prefs;


public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }

    @Override
    protected void onStart(){
        super.onStart();
        Context context = getApplicationContext();
        context.startService(new Intent(context.getApplicationContext(), MessageService.class));
        //check if service is alive, else start it
    }
}
