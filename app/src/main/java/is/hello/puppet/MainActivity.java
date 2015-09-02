package is.hello.puppet;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView buruberiVersion = (TextView) findViewById(R.id.activity_main_buruberi_version);
        buruberiVersion.setText(getString(R.string.info_buruberi_version, BuildConfig.BURUBERI_VERSION));

        final TextView appVersionName = (TextView) findViewById(R.id.activity_main_app_version_name);
        appVersionName.setText(getString(R.string.info_app_version_name, BuildConfig.VERSION_NAME));

        final TextView appVersionCode = (TextView) findViewById(R.id.activity_main_app_version_code);
        appVersionCode.setText(getString(R.string.info_app_version_code, BuildConfig.VERSION_CODE));
    }
}
