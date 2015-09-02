package is.hello.puppet.runner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import is.hello.puppet.Intents;

public class TestCommandReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(getClass().getSimpleName(), "onReceive(" + intent + ")");

        if (Intents.ALL_ACTIONS.hasAction(intent.getAction())) {
            LocalBroadcastManager.getInstance(context)
                                 .sendBroadcast(intent);
        }
    }
}
