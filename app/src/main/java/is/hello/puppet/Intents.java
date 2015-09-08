package is.hello.puppet;

import android.content.IntentFilter;

import is.hello.commonsense.bluetooth.model.protobuf.SenseCommandProtos.wifi_endpoint.sec_type;

/**
 * The actions and their extras exposed by the Puppet application.
 * Intended for use through the adb application manager.
 */
public final class Intents {
    //region Connectivity

    /**
     * Performs a Bluetooth Low Energy scan on the phone to select a Sense for testing.
     * <p>
     * <b>Input:</b>
     * May include an optional {@link #EXTRA_SENSE_ID} to narrow the scan to a specific
     * device. If omitted, the device with the strongest relative RSSI will be selected.
     * <p>
     * <b>Output:</b>
     * The advertised name of the selected Sense.
     * <p>
     * <b>Preconditions:</b>
     * None.
     * <p>
     * Literal value is <code>is.hello.puppet.ACTION_DISCOVER</code>
     */
    public static final String ACTION_DISCOVER = "is.hello.puppet.ACTION_DISCOVER";

    /**
     * A factory assigned Sense identifier.
     * <p>
     * Literal value is <code>sense_id</code>
     *
     * @see #ACTION_DISCOVER
     */
    public static final String EXTRA_SENSE_ID = "sense_id";


    /**
     * Attempts to connect to the selected Sense, placing the peripheral's
     * LEDs into trippy mode if successful.
     * <p>
     * <b>Input:</b>
     * Nothing.
     * <p>
     * <b>Output:</b>
     * The advertised name of the selected Sense.
     * <p>
     * <b>Preconditions:</b>
     * A Sense must be selected by {@link #ACTION_DISCOVER}.
     * <p>
     * Literal value is <code>is.hello.puppet.ACTION_CONNECT</code>
     */
    public static final String ACTION_CONNECT = "is.hello.puppet.ACTION_CONNECT";

    /**
     * Disconnects from the selected Sense. Does nothing if the selected Sense
     * is not connected, or there is no selected Sense.
     * <p>
     * <b>Input:</b>
     * Nothing.
     * <p>
     * <b>Output:</b>
     * The advertised name of the selected Sense, or an empty string if no Sense is selected.
     * <p>
     * <b>Preconditions:</b>
     * None
     * <p>
     * Literal value is <code>is.hello.puppet.ACTION_DISCONNECT</code>
     */
    public static final String ACTION_DISCONNECT = "is.hello.puppet.ACTION_DISCONNECT";

    /**
     * Clears the currently selected peripheral. Should not be issued until all
     * running commands have emitted an <code>end_command</code> event.
     * <p>
     * <b>Input:</b>
     * Nothing.
     * <p>
     * <b>Output:</b>
     * Nothing.
     * <p>
     * <b>Preconditions:</b>
     * No commands may be running.
     * <p>
     * Literal value is <code>is.hello.puppet.ACTION_RESET</code>
     */
    public static final String ACTION_RESET = "is.hello.puppet.ACTION_RESET";

    //endregion


    //region WiFi

    /**
     * Outputs the WiFi network currently connected to by the selected Sense.
     * <p>
     * <b>Input:</b>
     * Nothing.
     * <p>
     * <b>Output:</b>
     * The name of the network Sense is connected to, or <code>null</code> if there is none.
     * <p>
     * <b>Preconditions:</b>
     * A Sense must be selected by {@link #ACTION_DISCOVER}.
     * <p>
     * Literal value is <code>is.hello.puppet.ACTION_PRINT_WIFI_NETWORK</code>
     */
    public static final String ACTION_PRINT_WIFI_NETWORK = "is.hello.puppet.ACTION_PRINT_WIFI_NETWORK";

    /**
     * Performs a WiFi network scan on the selected Sense.
     * <p>
     * <b>Input:</b>
     * Nothing.
     * <p>
     * <b>Output:</b>
     * A comma-separated string containing the SSIDs of the networks scanned by Sense.
     * <p>
     * <b>Preconditions:</b>
     * A Sense must be selected by {@link #ACTION_DISCOVER}.
     * <p>
     * Literal value is <code>is.hello.puppet.ACTION_SCAN_WIFI</code>
     */
    public static final String ACTION_SCAN_WIFI = "is.hello.puppet.ACTION_SCAN_WIFI";

    /**
     * Attempts to connect to a WiFi network on the selected Sense.
     * <p>
     * <b>Input:</b>
     * The extras {@link #EXTRA_WIFI_SSID} and {@link #EXTRA_WIFI_SEC_TYPE} must be specified.
     * {@link #EXTRA_WIFI_SEC_TYPE} must also be specified unless the network is open.
     * <p>
     * <b>Output:</b>
     * The SSID of the network Sense connected to.
     * <p>
     * <b>Preconditions:</b>
     * A Sense must be selected by {@link #ACTION_DISCOVER}.
     * <p>
     * Literal value is <code>is.hello.puppet.ACTION_CONNECT_WIFI</code>
     */
    public static final String ACTION_CONNECT_WIFI = "is.hello.puppet.ACTION_CONNECT_WIFI";

    /**
     * A WiFi network SSID. Required to issue a connection attempt.
     * <p>
     * Literal value is <code>wifi_ssid</code>
     */
    public static final String EXTRA_WIFI_SSID = "wifi_ssid";

    /**
     * A WiFi network security type. Value corresponds to the field names of {@link sec_type}.
     * <p>
     * <ol>
     *     <li>SL_SCAN_SEC_TYPE_OPEN</li>
     *     <li>SL_SCAN_SEC_TYPE_WEP</li>
     *     <li>SL_SCAN_SEC_TYPE_WPA</li>
     *     <li>SL_SCAN_SEC_TYPE_WPA2</li>
     * </ol>
     * <p>
     * Literal value is <code>wifi_sec_type</code>
     */
    public static final String EXTRA_WIFI_SEC_TYPE = "wifi_sec_type";

    /**
     * A WiFi network password. Required unless the {@link #EXTRA_WIFI_SEC_TYPE}
     * is {@link sec_type#SL_SCAN_SEC_TYPE_OPEN}.
     * <p>
     * Literal value is <code>wifi_password</code>
     */
    public static final String EXTRA_WIFI_PASSWORD = "wifi_password";

    //endregion


    //region Accounts

    /**
     * Attempts to link Sense to an account using a given access token.
     * <p>
     * <b>Input:</b>
     * An access token via {@link #EXTRA_ACCESS_TOKEN}.
     * <p>
     * <b>Output:</b>
     * The passed in access token.
     * <p>
     * <b>Preconditions:</b>
     * A Sense must be selected by {@link #ACTION_DISCOVER}.
     * <p>
     * Literal value is <code>is.hello.puppet.ACTION_LINK_ACCOUNT</code>
     */
    public static final String ACTION_LINK_ACCOUNT = "is.hello.puppet.ACTION_LINK_ACCOUNT";

    /**
     * Initiates the pill pairing process on the selected Sense.
     * <p>
     * <b>Input:</b>
     * An access token via {@link #EXTRA_ACCESS_TOKEN}.
     * <p>
     * <b>Output:</b>
     * The passed in access token.
     * <p>
     * <b>Preconditions:</b>
     * A Sense must be selected by {@link #ACTION_DISCOVER}.
     * <p>
     * Literal value is <code>is.hello.puppet.ACTION_PAIR_PILL</code>
     */
    public static final String ACTION_PAIR_PILL = "is.hello.puppet.ACTION_PAIR_PILL";

    /**
     * An access token from the Sense API.
     * <p>
     * Literal value is <code>access_token</code>
     */
    public static final String EXTRA_ACCESS_TOKEN = "access_token";

    /**
     * Issues a factory reset on the selected Sense.
     * <p>
     * <b>Input:</b>
     * None
     * <p>
     * <b>Output:</b>
     * The name of the selected Sense.
     * <p>
     * <b>Preconditions:</b>
     * A Sense must be selected by {@link #ACTION_DISCOVER}.
     * <p>
     * Literal value is <code>is.hello.puppet.ACTION_FACTORY_RESET</code>
     */
    public static final String ACTION_FACTORY_RESET = "is.hello.puppet.ACTION_FACTORY_RESET";

    //endregion


    /**
     * All of the actions exposed by the Puppet application.
     */
    public static final IntentFilter ALL_ACTIONS = new IntentFilter();
    static {
        ALL_ACTIONS.addAction(ACTION_DISCOVER);
        ALL_ACTIONS.addAction(ACTION_CONNECT);
        ALL_ACTIONS.addAction(ACTION_DISCONNECT);
        ALL_ACTIONS.addAction(ACTION_RESET);

        ALL_ACTIONS.addAction(ACTION_PRINT_WIFI_NETWORK);
        ALL_ACTIONS.addAction(ACTION_SCAN_WIFI);
        ALL_ACTIONS.addAction(ACTION_CONNECT_WIFI);

        ALL_ACTIONS.addAction(ACTION_LINK_ACCOUNT);
        ALL_ACTIONS.addAction(ACTION_PAIR_PILL);
        ALL_ACTIONS.addAction(ACTION_FACTORY_RESET);
    }
}
