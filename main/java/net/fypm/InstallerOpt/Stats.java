package net.fypm.InstallerOpt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Stats extends Activity {

    private static final String TAG = "InstallerOpt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String backupDir = MultiprocessPreferences.getDefaultSharedPreferences(this).getString(Common.PREF_BACKUP_APK_LOCATION, null);
        File f = new File(backupDir);
        if (!f.exists()) {
            if (f.mkdir()) {
                Log.e(TAG, "Backup directory did not exist and was created, possibly deleted outside of InstallerOpt???");
                Toast.makeText(this, R.string.backup_location_missing_message, Toast.LENGTH_LONG).show();
            }
        }
        AlertDialog.Builder statsDialog = new AlertDialog.Builder(
                this, android.R.style.Theme_DeviceDefault_Dialog);
        statsDialog.setTitle(R.string.stats_menu);
        if (!backupDir.equals(null)) {
            final File PACKAGE_DIR = new File(backupDir + File.separator);
            long mSize = getFileSize(PACKAGE_DIR);
            statsDialog.setMessage(String.format("%s %s %s %s %-22s %10s %-22s %10s %-20s %s",
                    getString(R.string.backup_location), PACKAGE_DIR.toString(),
                    getString(R.string.backup_last_backed_up), getLatestFilefromDir(backupDir),
                    getString(R.string.backup_used), humanReadableByteCount(mSize, true),
                    getString(R.string.backup_free), humanReadableByteCount(getAvailableSpaceInBytes(backupDir), true),
                    getString(R.string.backup_total_items), getFileCount(PACKAGE_DIR)
            ));
            statsDialog.setCancelable(false);
            statsDialog.setPositiveButton(R.string.delete_all_button_text,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            deleteRecursive(PACKAGE_DIR);
                            finish();
                        }
                    });
            statsDialog.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });
        } else {
            statsDialog
                    .setMessage(R.string.enable_backup_message);
            statsDialog.setCancelable(true);
            statsDialog.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });
        }
        AlertDialog thisStatsDialog = statsDialog.create();
        thisStatsDialog.show();
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        if (fileOrDirectory.delete()) {
            Log.i(TAG, "deleteRecursive: Successfully deleted");
        }
    }

    public static long getAvailableSpaceInBytes(String path) {
        long availableSpace = -1L;
        StatFs stat = new StatFs(path);
        if (Build.VERSION.SDK_INT >= 18) {
            availableSpace = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        } else {
            availableSpace = stat.getAvailableBlocks() * stat.getBlockSize();
        }
        return availableSpace;
    }

    public static String getFileCount(final File file) {
        ArrayList<String> backup_dir = new ArrayList<String>(Arrays.asList(file.list()));
        int result = backup_dir.size();
        String total = result + " apps";
        return total;
    }

    public static long getFileSize(final File file) {
        if (file == null || !file.exists())
            return 0;
        if (!file.isDirectory())
            return file.length();
        final List<File> dirs = new LinkedList<File>();
        dirs.add(file);
        long result = 0;
        while (!dirs.isEmpty()) {
            final File dir = dirs.remove(0);
            if (!dir.exists())
                continue;
            final File[] listFiles = dir.listFiles();
            if (listFiles == null || listFiles.length == 0)
                continue;
            for (final File child : listFiles) {
                result += child.length();
                if (child.isDirectory())
                    dirs.add(child);
            }
        }
        return result;
    }

    private String getLatestFilefromDir(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return "N/A";
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i];
            }
        }
        String filename = lastModifiedFile.getName();
        return filename;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
