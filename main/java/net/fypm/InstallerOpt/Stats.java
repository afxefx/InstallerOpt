package net.fypm.InstallerOpt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StatFs;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Stats extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String backupDir = MultiprocessPreferences.getDefaultSharedPreferences(this).getString(Common.PREF_BACKUP_APK_LOCATION, null);
        AlertDialog.Builder statsDialog = new AlertDialog.Builder(
                this, android.R.style.Theme_DeviceDefault_Dialog);
        statsDialog.setTitle(R.string.stats_menu);
        if (backupDir != null) {
            final File PACKAGE_DIR = new File(backupDir + File.separator);
            long mSize = getFileSize(PACKAGE_DIR);
            statsDialog
                    /*.setMessage("Backup folder location:\n" + PACKAGE_DIR + "\n\nBackup folder used:\t\t\t\t"
                            + humanReadableByteCount(mSize, true) + "\nBackup folder free:\t\t\t\t\t\t\t\t"
                            + humanReadableByteCount(getAvailableSpaceInBytes(backupDir), true));*/
                    .setMessage(String.format("%s %s %-22s %10s %-22s %10s", getString(R.string.backup_location), PACKAGE_DIR.toString(), getString(R.string.backup_used), humanReadableByteCount(mSize, true), getString(R.string.backup_free), humanReadableByteCount(getAvailableSpaceInBytes(backupDir), true)));
            statsDialog.setCancelable(false);
            statsDialog.setPositiveButton(R.string.delete_button_text,
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
        fileOrDirectory.delete();
    }

    public static long getAvailableSpaceInBytes(String path) {
        long availableSpace = -1L;
        StatFs stat = new StatFs(path);
        availableSpace = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();

        return availableSpace;
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

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
