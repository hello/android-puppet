package is.hello.puppet;

import android.content.IntentFilter;

public class Intents {
    public static final String ACTION_DISCOVER = "is.hello.puppet.ACTION_DISCOVER";
    public static final String EXTRA_SENSE_ID = "sense_id";

    public static final String ACTION_CONNECT = "is.hello.puppet.ACTION_CONNECT";
    public static final String ACTION_PRINT_WIFI_STATUS = "is.hello.puppet.ACTION_PRINT_WIFI_STATUS";
    public static final String ACTION_SCAN_WIFI = "is.hello.puppet.ACTION_SCAN_WIFI";
    public static final String ACTION_CONNECT_WIFI = "is.hello.puppet.ACTION_CONNECT_WIFI";
    public static final String EXTRA_WIFI_SSID = "wifi_ssid";
    public static final String EXTRA_WIFI_PASSWORD = "wifi_password";
    public static final String EXTRA_WIFI_SEC_TYPE = "wifi_sec_type";

    public static final String ACTION_LINK_ACCOUNT = "is.hello.puppet.ACTION_LINK_ACCOUNT";
    public static final String EXTRA_ACCOUNT_TOKEN = "account_token";

    public static final String ACTION_PAIR_PILL = "is.hello.puppet.ACTION_PAIR_PILL";
    public static final String ACTION_FACTORY_RESET = "is.hello.puppet.ACTION_FACTORY_RESET";

    public static final String ACTION_DISCONNECT = "is.hello.puppet.ACTION_DISCONNECT";

    public static final IntentFilter ALL_ACTIONS = new IntentFilter();
    static {
        ALL_ACTIONS.addAction(ACTION_DISCOVER);
        ALL_ACTIONS.addAction(ACTION_CONNECT);
        ALL_ACTIONS.addAction(ACTION_PRINT_WIFI_STATUS);
        ALL_ACTIONS.addAction(ACTION_SCAN_WIFI);
        ALL_ACTIONS.addAction(ACTION_CONNECT_WIFI);
        ALL_ACTIONS.addAction(ACTION_LINK_ACCOUNT);
        ALL_ACTIONS.addAction(ACTION_PAIR_PILL);
        ALL_ACTIONS.addAction(ACTION_FACTORY_RESET);
        ALL_ACTIONS.addAction(ACTION_DISCONNECT);
    }
}
