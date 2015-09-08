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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import is.hello.buruberi.bluetooth.Buruberi;
import is.hello.buruberi.bluetooth.stacks.BluetoothStack;
import is.hello.buruberi.bluetooth.stacks.util.Operation;
import is.hello.buruberi.bluetooth.stacks.util.PeripheralCriteria;
import is.hello.buruberi.util.Either;
import is.hello.commonsense.bluetooth.SensePeripheral;
import is.hello.commonsense.bluetooth.model.SenseLedAnimation;
import is.hello.commonsense.bluetooth.model.protobuf.SenseCommandProtos.wifi_endpoint;
import is.hello.commonsense.bluetooth.model.protobuf.SenseCommandProtos.wifi_endpoint.sec_type;
import is.hello.puppet.Intents;
import rx.Observable;

public class SensePeripheralService extends Service {
    public static final String OUTPUT_LOG_TAG = SensePeripheralService.class.getSimpleName();

    private static boolean running = false;

    public static boolean isRunning() {
        return running;
    }

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
                case Intents.ACTION_RESET:
                    reset();
                    break;
                case Intents.ACTION_DISCONNECT:
                    disconnect();
                    break;

                case Intents.ACTION_PRINT_WIFI_NETWORK:
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

        SensePeripheralService.running = true;

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

        SensePeripheralService.running = false;

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

    private void endCommand(@NonNull Either<String, Throwable> result) {
        if (currentAction != null) {
            testOutput.logEndCommand(currentAction, result);
            this.currentAction = null;
        }
    }

    private void noSense(@NonNull String action) {
        testOutput.logValidationFailure(action, "No Sense currently discovered.");
    }

    private void runSenseCommand(@NonNull String action,
                                 @NonNull Observable<String> value) {
        if (sense == null) {
            noSense(action);
            return;
        }

        if (beginCommand(action)) {
            final Observable<String> withAnimation = sense.runLedAnimation(SenseLedAnimation.BUSY)
                                                          .flatMap(ignored -> value)
                                                          .flatMap(v -> sense.runLedAnimation(SenseLedAnimation.TRIPPY)
                                                                             .map(ignored -> v));

            withAnimation.subscribe(v -> endCommand(Either.left(v)),
                                    e -> endCommand(Either.right(e)));
        }
    }

    private void discover(@NonNull Intent intent) {
        final String senseId = intent.getStringExtra(Intents.EXTRA_SENSE_ID);

        if (!beginCommand(intent.getAction())) {
            return;
        }

        final Observable<SensePeripheral> discover;
        if (TextUtils.isEmpty(senseId)) {
            discover = SensePeripheral.discover(bluetoothStack, new PeripheralCriteria())
                                      .flatMap(peripherals -> {
                                          if (peripherals.isEmpty()) {
                                              return Observable.error(new SenseNotFoundException());
                                          } else {
                                              final SensePeripheral closest = Collections.max(peripherals, (l, r) -> {
                                                  final int a = l.getScannedRssi(),
                                                          b = r.getScannedRssi();
                                                  return (a < b) ? -1 : ((a > b) ? 1 : 0);
                                              });
                                              return Observable.just(closest);
                                          }
                                      });
        } else {
            discover = SensePeripheral.rediscover(bluetoothStack, senseId, false)
                                      .flatMap(sensePeripheral -> {
                                          if (sensePeripheral == null) {
                                              return Observable.error(new SenseNotFoundException());
                                          } else {
                                              return Observable.just(sensePeripheral);
                                          }
                                      });
        }
        discover.subscribe(sense -> {
                               this.sense = sense;
                               endCommand(Either.left(sense.getName()));
                           },
                           e -> endCommand(Either.right(e)));
    }

    private void connect() {
        if (sense == null) {
            noSense(Intents.ACTION_CONNECT);
            return;
        }

        if (!beginCommand(Intents.ACTION_CONNECT)) {
            return;
        }

        final Observable<Operation> connect = sense.connect().last();
        final Observable<SensePeripheral> withAnimation = connect.flatMap(ignored -> sense.runLedAnimation(SenseLedAnimation.TRIPPY))
                                                                 .map(ignored -> sense);
        withAnimation.subscribe(v -> endCommand(Either.left(v.getName())),
                                e -> endCommand(Either.right(e)));
    }

    private void reset() {
        if (this.currentAction != null) {
            testOutput.logValidationFailure(Intents.ACTION_RESET, "Cannot reset when a command is running.");
            return;
        }

        if (beginCommand(Intents.ACTION_RESET)) {
            this.sense = null;

            endCommand(Either.left(""));
        }
    }

    private void printConnectionStatus() {
        if (sense == null) {
            noSense(Intents.ACTION_PRINT_WIFI_NETWORK);
            return;
        }

        runSenseCommand(Intents.ACTION_PRINT_WIFI_NETWORK, sense.getWifiNetwork()
                                                               .map(s -> s.ssid));
    }

    private void scanWiFiNetworks() {
        if (sense == null) {
            noSense(Intents.ACTION_SCAN_WIFI);
            return;
        }

        final Observable<List<wifi_endpoint>> scan = sense.scanForWifiNetworks();
        final Observable<String> prettyScan = scan.map(networks -> {
            final List<String> ssids = new ArrayList<>();
            for (wifi_endpoint network : networks) {
                ssids.add(network.getSsid());
            }
            return TextUtils.join(", ", ssids);
        });
        runSenseCommand(Intents.ACTION_SCAN_WIFI, prettyScan);
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

        runSenseCommand(Intents.ACTION_CONNECT_WIFI, sense.connectToWiFiNetwork(ssid, securityType, password)
                                                          .last()
                                                          .map(ignored -> ssid));
    }

    private void linkAccount(@NonNull Intent intent) {
        final String accessToken = getRequiredStringExtra(intent, Intents.EXTRA_ACCESS_TOKEN);
        if (accessToken == null) {
            return;
        }

        if (sense == null) {
            noSense(Intents.ACTION_LINK_ACCOUNT);
            return;
        }

        runSenseCommand(Intents.ACTION_LINK_ACCOUNT, sense.linkAccount(accessToken)
                                                          .map(ignored -> accessToken));
    }

    private void pairPill(@NonNull Intent intent) {
        final String accessToken = getRequiredStringExtra(intent, Intents.EXTRA_ACCESS_TOKEN);
        if (accessToken == null) {
            return;
        }

        if (sense == null) {
            noSense(Intents.ACTION_PAIR_PILL);
            return;
        }

        runSenseCommand(Intents.ACTION_PAIR_PILL, sense.pairPill(accessToken));
    }

    private void factoryReset() {
        if (sense == null) {
            noSense(Intents.ACTION_FACTORY_RESET);
            return;
        }

        runSenseCommand(Intents.ACTION_FACTORY_RESET, sense.factoryReset()
                                                           .map(ignored -> ""));
    }

    private void disconnect() {
        if (!beginCommand(Intents.ACTION_DISCONNECT)) {
            return;
        }

        if (sense != null) {
            final Observable<SensePeripheral> disconnect = sense.disconnect();
            disconnect.subscribe(v -> endCommand(Either.left(v.getName())),
                                 e -> endCommand(Either.right(e)));
        } else {
            endCommand(Either.left(""));
        }
    }

    //endregion
}
