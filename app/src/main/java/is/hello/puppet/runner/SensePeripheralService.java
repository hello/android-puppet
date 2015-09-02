package is.hello.puppet.runner;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import is.hello.buruberi.bluetooth.Buruberi;
import is.hello.buruberi.bluetooth.stacks.BluetoothStack;
import is.hello.buruberi.util.Either;
import is.hello.puppet.Intents;
import is.hello.puppet.bluetooth.sense.SensePeripheral;
import is.hello.puppet.bluetooth.sense.model.protobuf.SenseCommandProtos.wifi_endpoint.sec_type;
import rx.Observable;

public class SensePeripheralService extends Service {
    public static final String OUTPUT_LOG_TAG = SensePeripheralService.class.getSimpleName();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(OUTPUT_LOG_TAG, "Received " + intent);
            switch (intent.getAction()) {
                case Intents.ACTION_DISCOVER:
                    discover(intent);
                    break;
                case Intents.ACTION_CONNECT:
                    connect();
                    break;
                case Intents.ACTION_GET_CONNECTION_STATUS:
                    printConnectionStatus();
                    break;
                case Intents.ACTION_SCAN_WIFI:
                    scanWiFiNetworks();
                    break;
                case Intents.ACTION_CONNECT_WIFI:
                    connectToWiFiNetwork(intent);
                    break;
                case Intents.ACTION_LINK_ACCOUNT:
                    linkAccount(intent);
                    break;
                case Intents.ACTION_PAIR_PILL:
                    pairPill(intent);
                    break;
                case Intents.ACTION_FACTORY_RESET:
                    factoryReset();
                    break;
                default:
                    testOutput.logValidationFailure(intent.getAction(), "Action is unknown, ignoring.");
                    break;
            }
        }
    };

    private TestOutput testOutput;
    private BluetoothStack bluetoothStack;
    private @Nullable String currentAction;
    private @Nullable SensePeripheral sense;


    //region Lifecycle

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v(OUTPUT_LOG_TAG, "onCreate()");

        this.bluetoothStack = new Buruberi()
                .setApplicationContext(getApplicationContext())
                .build();

        this.testOutput = new TestOutput(bluetoothStack.getLogger());

        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(receiver, Intents.ALL_ACTIONS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.v(OUTPUT_LOG_TAG, "onDestroy()");

        LocalBroadcastManager.getInstance(this)
                             .unregisterReceiver(receiver);
    }

    //endregion


    //region Utilities

    private @Nullable String getRequiredStringExtra(@NonNull Intent intent, @NonNull String key) {
        final String extra = intent.getStringExtra(key);
        if (TextUtils.isEmpty(extra)) {
            testOutput.logValidationFailure(intent.getAction(), "Missing value for '" + key + "'");
            return null;
        } else {
            return extra;
        }
    }

    //endregion


    //region Commands

    private boolean beginCommand(@NonNull String action) {
        if (this.currentAction != null) {
            testOutput.logValidationFailure(action, "'" + currentAction + "' is already running");
            return false;
        } else {
            testOutput.logBeginCommand(action);
            this.currentAction = action;
            return true;
        }
    }

    private <T> void endCommand(@NonNull Either<T, Throwable> result) {
        if (currentAction != null) {
            testOutput.logEndCommand(currentAction, result);
            this.currentAction = null;
        }
    }

    private void noSense(@NonNull String action) {
        testOutput.logValidationFailure(action, "No Sense currently discovered.");
    }

    private <T> void runSenseCommand(@NonNull String action,
                                     @NonNull Observable<T> value) {
        if (beginCommand(action)) {
            value.subscribe(v -> endCommand(Either.left(v)),
                            e -> endCommand(Either.right(e)));
        }
    }

    private void discover(@NonNull Intent intent) {
        final String senseId = getRequiredStringExtra(intent, Intents.EXTRA_SENSE_ID);
        if (senseId == null) {
            return;
        }

        if (!beginCommand(intent.getAction())) {
            return;
        }

        final Observable<SensePeripheral> discover = SensePeripheral.rediscover(bluetoothStack,
                                                                                senseId,
                                                                                false);
        discover.subscribe(sense -> {
                               this.sense = sense;
                               endCommand(Either.left(sense));
                           },
                           e -> endCommand(Either.right(e)));
    }

    private void connect() {
        if (sense == null) {
            noSense(Intents.ACTION_CONNECT);
            return;
        }

        runSenseCommand(Intents.ACTION_CONNECT, sense.connect());
    }

    private void printConnectionStatus() {
        if (sense == null) {
            noSense(Intents.ACTION_GET_CONNECTION_STATUS);
            return;
        }

        runSenseCommand(Intents.ACTION_GET_CONNECTION_STATUS, sense.getWifiNetwork());
    }

    private void scanWiFiNetworks() {
        if (sense == null) {
            noSense(Intents.ACTION_SCAN_WIFI);
            return;
        }

        runSenseCommand(Intents.ACTION_SCAN_WIFI, sense.scanForWifiNetworks());
    }

    private void connectToWiFiNetwork(@NonNull Intent intent) {
        final String secTypeString = getRequiredStringExtra(intent, Intents.EXTRA_WIFI_SEC_TYPE);
        final String ssid = getRequiredStringExtra(intent, Intents.EXTRA_WIFI_SSID);
        if (secTypeString == null || ssid == null) {
            return;
        }

        final String password = intent.getStringExtra(Intents.EXTRA_WIFI_PASSWORD);
        final sec_type securityType;

        try {
            securityType = sec_type.valueOf(secTypeString);
        } catch (Exception e) {
            Log.e(OUTPUT_LOG_TAG, "[ASSERT] Invalid security type '" + secTypeString + "'");
            return;
        }

        if (sense == null) {
            noSense(Intents.ACTION_CONNECT_WIFI);
            return;
        }

        runSenseCommand(Intents.ACTION_CONNECT_WIFI, sense.connectToWiFiNetwork(ssid, securityType, password));
    }

    private void linkAccount(@NonNull Intent intent) {
        final String accountToken = getRequiredStringExtra(intent, Intents.EXTRA_ACCOUNT_TOKEN);
        if (accountToken == null) {
            return;
        }

        if (sense == null) {
            noSense(Intents.ACTION_LINK_ACCOUNT);
            return;
        }

        runSenseCommand(Intents.ACTION_LINK_ACCOUNT, sense.linkAccount(accountToken));
    }

    private void pairPill(@NonNull Intent intent) {
        final String accountToken = getRequiredStringExtra(intent, Intents.EXTRA_ACCOUNT_TOKEN);
        if (accountToken == null) {
            return;
        }

        if (sense == null) {
            noSense(Intents.ACTION_PAIR_PILL);
            return;
        }

        runSenseCommand(Intents.ACTION_PAIR_PILL, sense.pairPill(accountToken));
    }

    private void factoryReset() {
        if (sense == null) {
            noSense(Intents.ACTION_FACTORY_RESET);
            return;
        }

        runSenseCommand(Intents.ACTION_FACTORY_RESET, sense.factoryReset());
    }

    //endregion
}
