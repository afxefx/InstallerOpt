package net.fypm.InstallerOpt;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;

@SuppressWarnings("deprecation")
public class Utils extends BroadcastReceiver {

    private static final String TAG = "InstallerOpt";
    public static final String APP_DIR = Environment
            .getExternalStorageDirectory()
            + File.separator
            + Common.PACKAGE_TAG
            + File.separator;
    public static final File PACKAGE_DIR = new File(APP_DIR);
    public static final File PREFERENCES_BACKUP_FILE = new File(APP_DIR
            + File.separator + Common.PACKAGE_TAG + ".backup");
    public Context ctx;
    public Resources resources;
    public boolean enableDebug;
    public boolean enableNotifications;
    public static int maxBackupVersions = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!PACKAGE_DIR.exists()) {
            PACKAGE_DIR.mkdir();
        }
        enableDebug = MultiprocessPreferences.getDefaultSharedPreferences(context).getBoolean(Common.PREF_ENABLE_DEBUG, false);
        enableNotifications = MultiprocessPreferences.getDefaultSharedPreferences(context).getBoolean(Common.PREF_ENABLE_NOTIFICATIONS, false);
        //maxBackupVersions = MultiprocessPreferences.getDefaultSharedPreferences(context).getInt(Common.PREF_MAX_BACKUP_VERSIONS, 3);
        ctx = context;
        resources = ctx.getResources();
        String action = intent.getAction();

        Bundle extras = intent.getExtras();
        boolean hasExtras = extras != null;
        if (Common.ACTION_BACKUP_APK_FILE.equals(action)) {
            if (hasExtras) {
                String apkFile = extras.getString(Common.FILE);
                String backupDir = extras.getString(Common.BACKUP_DIR);
                backupApkFile(apkFile, backupDir);
            }
        } else if (Common.ACTION_DELETE_APK_FILE.equals(action)) {
            if (hasExtras) {
                String apkFile = extras.getString(Common.FILE);
                deleteApkFile(apkFile, false, true);
            }
        } else if (Common.ACTION_UNINSTALL_SYSTEM_APP.equals(action)) {
            if (hasExtras) {
                String packageName = extras.getString(Common.PACKAGE);
                uninstallSystemApp(packageName);
            }
        } else if (Common.ACTION_VIBRATE_DEVICE.equals(action)) {
            if (hasExtras) {
                int duration = extras.getInt(Common.DURATION);
                vibrateDevice(duration);
            }
        } else if (Common.ACTION_POST_NOTIFICATION.equals(action)) {
            if (hasExtras) {
                String description = extras.getString(Common.DESCRIPTION);
                String title = extras.getString(Common.TITLE);
                String thirdline = extras.getString(Common.THIRDLINE);
                postNotification(title, description, thirdline, ctx);
            }
        } else if (Common.ACTION_BACKUP_PREFERENCES.equals(action)) {
            backupPreferences();
        } else if (Common.ACTION_RESTORE_PREFERENCES.equals(action)) {
            restorePreferences();
        } else if (Common.ACTION_RESET_PREFERENCES.equals(action)) {
            resetPreferences();
        } else if (Common.ACTION_CONFIRM_CHECK_SIGNATURE.equals(action)) {
            confirmCheckSignatures();
        }
    }

    public void backupApkFile(String apkFile, String dir) {
        try {
            File f = new File(dir);
            if (!f.exists()) {
                if (f.mkdir()) {
                    Log.e(TAG, "Backup directory did not exist and was created, possibly deleted outside of InstallerOpt???");
                    Toast.makeText(ctx, R.string.backup_location_missing_message, Toast.LENGTH_LONG).show();
                }
            }
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageArchiveInfo(apkFile, 0);
            pi.applicationInfo.sourceDir = apkFile;
            pi.applicationInfo.publicSourceDir = apkFile;
            ApplicationInfo ai = pi.applicationInfo;
            String appName = (String) pm.getApplicationLabel(ai);
            int versionCode = pi.versionCode;
            String versionName = pi.versionName;
            String fileName = appName + " " + versionName + "-" + versionCode + ".apk";
            String backupApkFile = dir + File.separator + fileName;
            File src = new File(apkFile);
            File dst = new File(backupApkFile);

            ArrayList<String> TmpList = new ArrayList<String>();
            File old_file = new File(dir);
            ArrayList<String> old_files = new ArrayList<String>(Arrays.asList(old_file.list()));

            for (int i = 0; i < old_files.size(); i++) {
                if (old_files.get(i).contains(appName))
                    TmpList.add(old_files.get(i));
            }
            if (TmpList.size() > maxBackupVersions) {
                Collections.sort(TmpList, new NaturalOrderComparator());
                //Collections.sort(TmpList);
                do {
                    String oldest_file = dir + File.separator + TmpList.get(0);
                    deleteApkFile(oldest_file, true, false);
                    TmpList.remove(0);
                    Log.i(TAG, "Max backup limit reached, oldest backup file has been deleted" + oldest_file);
                } while (TmpList.size() > maxBackupVersions);
                if (enableDebug) {
                    Toast.makeText(ctx, "Max backup limit reached, oldest backup file has been deleted",
                            Toast.LENGTH_LONG).show();
                }
                //return;
            }

            if (enableDebug) {
                Log.i(TAG, "backupApkFile Debug Start");
                Log.i(TAG, "Backup directory: " + dir);
                Log.i(TAG, "APK file: " + apkFile);
                Log.i(TAG, "pm: " + pm);
                Log.i(TAG, "pi: " + pi);
                Log.i(TAG, "applicationInfo sourceDir: " + pi.applicationInfo.sourceDir);
                Log.i(TAG, "applicationInfo.publicSourceDir: " + pi.applicationInfo.publicSourceDir);
                Log.i(TAG, "ai: " + ai);
                Log.i(TAG, "appName: " + appName);
                Log.i(TAG, "versionName: " + versionName);
                Log.i(TAG, "fileName: " + fileName);
                Log.i(TAG, "backupApkFile: " + backupApkFile);
                Log.i(TAG, "Source file " + src);
                Log.i(TAG, "Destination file " + dst);
                Log.i(TAG, "backupApkFile Debug End");
            }
            if (!dst.equals(src)) {
                if (copyFile(src, dst)) {
                    if (enableDebug) {
                        Toast.makeText(ctx, apkFile + " successfully backed up",
                                Toast.LENGTH_LONG).show();
                    }
                    /*if (enableNotifications) {
                        postNotification("Backup Success", apkFile + "\nsuccessfully backed up", "", ctx);
                    }*/
                    Log.i(TAG, "APK file " + apkFile + " successfully backed up");
                } else {
                    if (enableDebug) {
                        Toast.makeText(ctx, apkFile + " was not successfully backed up",
                                Toast.LENGTH_LONG).show();
                    }
                    /*if (enableNotifications) {
                        postNotification("Backup Failure", apkFile + "\nwas not successfully backed up", "", ctx);
                    }*/
                    Log.e(TAG, "APK file " + apkFile + " was not successfully backed up");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "backupApkFile Error Debug Start");
            Log.e(TAG, "Error caught: " + e);
            Log.e(TAG, "backupApkFile Error Debug End");
        }
    }

    public void confirmCheckSignatures() {
        Intent openConfirmCheckSignatures = new Intent(ctx,
                ConfirmCheckSignatures.class);
        openConfirmCheckSignatures.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(openConfirmCheckSignatures);
    }

    public boolean copyFile(File src, File dst) throws IOException {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "Error caught in copyFile: " + t);
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                Log.e(TAG, "HookDetection: " + stackTraceElement.getClassName() + "->" + stackTraceElement.getMethodName());
            }
            return false;
        }
    }

    public void deleteApkFile(String apkFile, boolean force, boolean notif) {
        String backupDir = MultiprocessPreferences.getDefaultSharedPreferences(ctx).getString(Common.PREF_BACKUP_APK_LOCATION, null);
        File apk = new File(apkFile);
        String absolutePath = apk.getAbsolutePath();
        String filePath = apkFile.
                substring(0, absolutePath.lastIndexOf(File.separator));
        if (filePath.equals(backupDir) && !force) {
            Log.i(TAG, "deleteApkFile: Install started from backup directory, file not deleted");
            return;
        } else {
            try {
                if (!apk.delete()) {
                    if (enableDebug) {
                        Toast.makeText(ctx, apkFile + " was not successfully deleted",
                                Toast.LENGTH_LONG).show();
                    }
                    /*if (enableNotifications && notif) {
                        postNotification("Delete Failure", apkFile + "\nwas not successfully deleted", "", ctx);
                    }*/
                    Log.e(TAG, "APK file " + apkFile + " was not successfully deleted");
                    String message = apk.exists() ? "is in use by another app" : "does not exist";
                    throw new IOException("Cannot delete file, because file " + message + ".");
                } else {
                    if (enableDebug) {
                        Toast.makeText(ctx, apkFile + " successfully deleted",
                                Toast.LENGTH_LONG).show();
                    }
                    /*if (enableNotifications && notif) {
                        postNotification("Delete Success", apkFile + "\nsuccessfully deleted", "", ctx);
                    }*/
                    Log.i(TAG, "APK file " + apkFile + " successfully deleted");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error caught in deleteApkFile: " + e);
                for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                    Log.e(TAG, "HookDetection: " + stackTraceElement.getClassName() + "->" + stackTraceElement.getMethodName());
                }
            }
        }
    }

    private void postNotification(String title, String description, String thirdline, Context ctx) {

        Random random = new Random();
        int mId = random.nextInt();
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, new Intent(ctx, MainActivity.class), 0);
        //Resources r = ctx.getResources();
        NotificationCompat.Builder notification = new NotificationCompat.Builder(ctx)
                .setTicker(description)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(description)
                .setSubText(thirdline)
                .setContentIntent(pi)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(mId, notification.build());
    }

    public void uninstallSystemApp(String packageName) {
        PackageManager pm = ctx.getPackageManager();
        PackageInfo pkgInfo;
        try {
            pkgInfo = pm.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }
        final String apkFile = pkgInfo.applicationInfo.sourceDir;
        boolean installedInSystem = apkFile.startsWith("/system");
        String removeAPK = "rm " + apkFile;
        String removeData = "pm clear " + packageName;
        String remountRW = "mount -o remount,rw /system";
        String remountRO = "mount -o remount,ro /system";

        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(
                    process.getOutputStream());
            if (installedInSystem) {
                os.writeBytes(remountRW + "\n");
                os.writeBytes(removeAPK + "\n");
                os.writeBytes(remountRO + "\n");
            } else {
                os.writeBytes(removeAPK + "\n");
            }
            os.writeBytes(removeData + "\n");
            os.writeBytes("exit\n");
            os.flush();
            os.close();
            Toast.makeText(ctx, resources.getString(R.string.app_uninstalled),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error in uninstallSystemApp", e);
        }
    }

    public void vibrateDevice(int duration) {
        Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(duration);
    }

    public void backupPreferences() {
        if (!PREFERENCES_BACKUP_FILE.exists()) {
            try {
                PREFERENCES_BACKUP_FILE.createNewFile();
            } catch (Exception e) {
                Log.e(TAG, "Error in backupPreferences", e);
            }
        }

        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(
                    PREFERENCES_BACKUP_FILE));
            SharedPreferences prefs = ctx.getSharedPreferences(
                    Common.PACKAGE_PREFERENCES, Context.MODE_WORLD_READABLE);
            output.writeObject(prefs.getAll());
        } catch (Exception e) {
            Log.e(TAG, "Error in backupPreferences", e);
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in backupPreferences", e);
            }
        }

        Toast.makeText(ctx,
                resources.getString(R.string.preferences_backed_up),
                Toast.LENGTH_LONG).show();
    }

    public void restorePreferences() {
        if (!PREFERENCES_BACKUP_FILE.exists()) {
            Toast.makeText(ctx, resources.getString(R.string.no_backup_file),
                    Toast.LENGTH_LONG).show();
            return;
        }

        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(
                    PREFERENCES_BACKUP_FILE));
            SharedPreferences prefs = ctx.getSharedPreferences(
                    Common.PACKAGE_PREFERENCES, Context.MODE_WORLD_READABLE);
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.clear();
            @SuppressWarnings("unchecked")
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object value = entry.getValue();
                String key = entry.getKey();
                if (value instanceof Boolean) {
                    prefsEditor.putBoolean(key, ((Boolean) value).booleanValue());
                } else if (value instanceof String) {
                    prefsEditor.putString(key, (String) value);
                } else if (value instanceof Long) {
                    prefsEditor.putLong(key, (Long) value);
                } else if (value instanceof Integer) {
                    prefsEditor.putInt(key, (Integer) value);
                }
            }
            prefsEditor.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error in restorePreferences", e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in restorePreferences", e);
            }
        }

        Toast.makeText(ctx, resources.getString(R.string.preferences_restored),
                Toast.LENGTH_LONG).show();
    }

    public void resetPreferences() {
        SharedPreferences prefs = ctx.getSharedPreferences(
                Common.PACKAGE_PREFERENCES, Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.clear();
        prefsEditor.commit();

        Toast.makeText(ctx, resources.getString(R.string.preferences_reset),
                Toast.LENGTH_LONG).show();
    }

    /*final PendingResult result = goAsync();
Thread thread = new Thread() {
   public void run() {
      int i;
      // Do processing
      result.setResultCode(i);
      result.finish();
   }
};
thread.start();

    */

}
