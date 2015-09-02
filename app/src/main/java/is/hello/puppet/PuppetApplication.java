package is.hello.puppet;

import android.app.Application;
import android.content.Intent;

import is.hello.puppet.runner.SensePeripheralService;

public class PuppetApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        final Intent intent = new Intent(this, SensePeripheralService.class);
        startService(intent);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        final Intent intent = new Intent(this, SensePeripheralService.class);
        stopService(intent);
    }
}
