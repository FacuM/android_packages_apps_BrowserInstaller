package com.xenonhd.browserinstaller;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int DONE = 1;
    private int[] icons = {R.mipmap.ic_chrome, R.mipmap.ic_firefox, R.mipmap.ic_opera, R.mipmap.ic_via};
    private String url = null;
    private String browser;
    private String appPackageName = null;
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            long downloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (intent.getAction() != null && intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (downloadManager != null) {
                    install.setData(downloadManager.getUriForDownloadedFile(downloadID));
                }
                startActivityForResult(install, DONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(View.inflate(this, R.layout.activity_main, null));

        AlertDialog dialog = builder.create();
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });

        dialog.show();
        Button button = dialog.findViewById(R.id.install);
        final Spinner spinner = dialog.findViewById(R.id.spinner);
        final String[] browsers = getResources().getStringArray(R.array.browsers_array);
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this, icons, browsers);
        if (spinner != null) {
            spinner.setAdapter(spinnerAdapter);
        }

        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkCapabilities networkCapabilities;
                    if (connectivityManager != null) {
                        networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                        if (networkCapabilities != null) {
                            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)  ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)  ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)      ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH))
                            {
                                if (spinner != null) {
                                    performInstall(icons[(int) spinner.getSelectedItemId()]);
                                }
                            } else {
                                Toast.makeText(MainActivity.this, R.string.not_connected, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
        if (intent.resolveActivity(packageManager) != null) {
            ComponentName componentName = new ComponentName(getApplicationContext(), MainActivity.class);
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    private boolean isPlayStoreThere() {
        try {
            getApplicationContext().getPackageManager().getPackageInfo("com.android.vending", 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    private void performInstall(int i) {

        switch (i) {

            case R.mipmap.ic_chrome:
                browser = "Chrome";
                url = getString(R.string.chromeXenonServer);
                appPackageName = getString(R.string.chromePackage);
                break;

            case R.mipmap.ic_firefox:
                browser = "Chromium";
                url = getString(R.string.firefoxXenonServer);
                appPackageName = getString(R.string.firefoxPackage);
                break;

            case R.mipmap.ic_opera:
                browser = "Dolphin";
                url = getString(R.string.operaXenonServer);
                appPackageName = getString(R.string.operaPackage);
                break;

            case R.mipmap.ic_via:
                browser = "Firefox";
                url = getString(R.string.viaXenonServer);
                appPackageName = getString(R.string.viaPackage);
                break;
        }

        if (isPlayStoreThere() && appPackageName != null) {
            startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)), DONE);
        } else {
            Log.d(TAG, "performInstall: starting download...");
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
            req.setTitle(getString(R.string.app_name));
            req.setDescription(getString(R.string.notification).replace("$", browser));
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, appPackageName + ".apk");
            DownloadManager mgr = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
            if (mgr != null) {
                mgr.enqueue(req);
            }

            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            registerReceiver(downloadReceiver, filter);
            Toast.makeText(MainActivity.this, R.string.downloading, Toast.LENGTH_LONG).show();
        }

    }
}
