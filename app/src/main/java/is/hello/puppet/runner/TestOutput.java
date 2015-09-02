package is.hello.puppet.runner;

import android.support.annotation.NonNull;

import is.hello.buruberi.bluetooth.stacks.util.LoggerFacade;
import is.hello.buruberi.util.Either;

public class TestOutput {
    public static final String LOG_TAG = TestOutput.class.getSimpleName();

    private final LoggerFacade loggerFacade;

    public TestOutput(@NonNull LoggerFacade loggerFacade) {
        this.loggerFacade = loggerFacade;
    }

    public void logBeginCommand(@NonNull String action) {
        loggerFacade.debug(LOG_TAG, "[BEGIN " + action + "]");
    }

    public <T> void logEndCommand(@NonNull String action, @NonNull Either<T, Throwable> result) {
        result.match(value -> loggerFacade.info(LOG_TAG, "[END " + action + "] " + result),
                     error -> loggerFacade.error(LOG_TAG, "[END " + action + "]", error));
    }

    public void logValidationFailure(@NonNull String action, @NonNull String message) {
        loggerFacade.error(LOG_TAG, "[ASSERT " + action + "] " + message, null);
    }
}
