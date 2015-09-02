package is.hello.puppet.runner;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import is.hello.buruberi.bluetooth.stacks.util.LoggerFacade;
import is.hello.buruberi.util.Either;

public class TestOutput {
    public static final String LOG_TAG = TestOutput.class.getSimpleName();

    public static final String EVENT_BEGIN_COMMAND = "begin_command";
    public static final String EVENT_END_COMMAND = "end_command";
    public static final String EVENT_ERROR = "error";
    public static final String EVENT_PRECONDITION_FAILED = "precondition_failed";

    public static final String KEY_EVENT = "event";
    public static final String KEY_ACTION = "action";
    public static final String KEY_RESULT = "result";
    public static final String KEY_MESSAGE = "message";

    private final LoggerFacade loggerFacade;

    public TestOutput(@NonNull LoggerFacade loggerFacade) {
        this.loggerFacade = loggerFacade;
    }

    private String constructMessage(@NonNull Object... values) {
        if ((values.length % 2) != 0) {
            throw new AssertionError("values must have even number of items");
        }

        try {
            JSONObject object = new JSONObject();
            for (int i = 0, length = values.length; i < length; i += 2) {
                final String key = values[i].toString();
                final Object value = values[i + 1];
                object.put(key, value);
            }
            return object.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void logBeginCommand(@NonNull String action) {
        loggerFacade.debug(LOG_TAG, constructMessage(KEY_EVENT, EVENT_BEGIN_COMMAND,
                                                     KEY_ACTION, action));
    }

    public void logEndCommand(@NonNull String action, @NonNull Either<String, Throwable> result) {
        result.match(value -> {
                         loggerFacade.info(LOG_TAG, constructMessage(KEY_EVENT, EVENT_END_COMMAND,
                                                                     KEY_ACTION, action,
                                                                     KEY_RESULT, value));
                     },
                     error -> {
                         loggerFacade.error(LOG_TAG,
                                            constructMessage(KEY_EVENT, EVENT_ERROR,
                                                             KEY_ACTION, action,
                                                             KEY_MESSAGE, error.getMessage()),
                                            error);
                     });
    }

    public void logValidationFailure(@NonNull String action, @NonNull String message) {
        loggerFacade.error(LOG_TAG,
                           constructMessage(KEY_EVENT, EVENT_PRECONDITION_FAILED,
                                            KEY_ACTION, action,
                                            KEY_MESSAGE, message),
                           null);
    }
}
