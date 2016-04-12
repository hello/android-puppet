package is.hello.puppet.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;

import is.hello.puppet.BuildConfig;
import is.hello.puppet.R;

public class MainActivity extends Activity {

    private static final int PERMISSION_LOC_CODE = 0x10d;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView buruberiVersion = (TextView) findViewById(R.id.activity_main_buruberi_version);
        buruberiVersion.setText(getString(R.string.info_buruberi_version, BuildConfig.BURUBERI_VERSION));

        final TextView commonSenseVersion = (TextView) findViewById(R.id.activity_main_common_sense_version);
        commonSenseVersion.setText(getString(R.string.info_common_sense_version, BuildConfig.COMMON_SENSE_VERSION));

        final TextView appVersionName = (TextView) findViewById(R.id.activity_main_app_version_name);
        appVersionName.setText(getString(R.string.info_app_version_name, BuildConfig.VERSION_NAME));

        final TextView appVersionCode = (TextView) findViewById(R.id.activity_main_app_version_code);
        appVersionCode.setText(getString(R.string.info_app_version_code, BuildConfig.VERSION_CODE));

        // required for newer versions of Android
        final String[] permissions = {is.hello.puppet.Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_LOC_CODE);
    }
}
