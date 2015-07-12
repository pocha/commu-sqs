package rocks.pocha.sampleapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class WakeUpReceiver extends WakefulBroadcastReceiver {
    public WakeUpReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(), MessageService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
    }
}
