package is.hello.puppet.runner;

import is.hello.buruberi.bluetooth.errors.BuruberiException;

public class SenseNotFoundException extends BuruberiException {
    public SenseNotFoundException() {
        super("Could not find Sense");
    }
}
