package net.fypm.InstallerOpt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Stats extends Activity {

    public static final String APP_DIR = Environment
            .getExternalStorageDirectory()
            + File.separator
            + Common.PACKAGE_TAG
            + File.separator;
    public static final File PACKAGE_DIR = new File(APP_DIR);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder statsDialog = new AlertDialog.Builder(
                this, android.R.style.Theme_DeviceDefault_Dialog);
        statsDialog.setTitle("Statistics");
        long mSize = getFileSize(PACKAGE_DIR);
        statsDialog
                .setMessage("Size of APK backup folder is: " + humanReadableByteCount(mSize, true));
        statsDialog.setCancelable(true);
        statsDialog.setPositiveButton("Delete",
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

        AlertDialog thisStatsDialog = statsDialog.create();
        thisStatsDialog.show();

    }

    public void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    public static long getFileSize(final File file)
    {
        if(file==null||!file.exists())
            return 0;
        if(!file.isDirectory())
            return file.length();
        final List<File> dirs=new LinkedList<File>();
        dirs.add(file);
        long result=0;
        while(!dirs.isEmpty())
        {
            final File dir=dirs.remove(0);
            if(!dir.exists())
                continue;
            final File[] listFiles=dir.listFiles();
            if(listFiles==null||listFiles.length==0)
                continue;
            for(final File child : listFiles)
            {
                result+=child.length();
                if(child.isDirectory())
                    dirs.add(child);
            }
        }
        return result;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}
