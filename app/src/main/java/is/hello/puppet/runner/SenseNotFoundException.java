package is.hello.puppet.runner;

import is.hello.buruberi.bluetooth.errors.BluetoothError;

public class SenseNotFoundException extends BluetoothError {
    public SenseNotFoundException() {
        super("Could not find Sense");
    }
}
