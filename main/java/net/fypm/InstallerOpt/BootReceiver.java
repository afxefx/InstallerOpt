package net.fypm.InstallerOpt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
//import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "InstallerOpt";

    public BootReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            try {
                /*Toast.makeText(context, "Boot completed",
                        Toast.LENGTH_LONG).show();*/
                Log.i(TAG, "Boot Complete");
            } catch (Exception e) {
                Log.e(TAG, "BootReceiver.onReceive: ", e);
            }
        }
    }
}
