package net.fypm.InstallerOpt;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

    public static int bootCompleted;
    public Context ctx;
    public Context mContext;

    public BootReceiver() {
        try {
            //mContext = getApplicationUsingReflection();
        } catch (Throwable e) {

        }
        try {
            ctx = mContext.createPackageContext("net.fypm.InstallerOpt", Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            //xlog_start("getInstallerOptContext Error");
            //xlog("", e);
            //xlog_end("getInstallerOptContext Error");
        }
        setPref(ctx, "boot_completed", false);
    }
    //private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            try {
                //XposedBridge.log("Boot Completed");
                Toast.makeText(context, "Boot completed",
                        Toast.LENGTH_LONG).show();
                setPref(ctx, "boot_completed", true);
                //bootCompleted = 1;
                //context.startService(new Intent(context,ConnectivityListener.class));
                //Log.i(TAG,"Starting Service ConnectivityListener");
            } catch (Exception e) {
                //Log.e(TAG,e.toString());
            }
        }
    }

    public static void setPref(Context context, String pref, Boolean value) {
        MultiprocessPreferences.getDefaultSharedPreferences(context).edit().putBoolean(pref, value).apply();
    }

    /*public static Application getApplicationUsingReflection() throws Exception {
        return (Application) Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication").invoke(null, (Object[]) null);
    }*/
}
