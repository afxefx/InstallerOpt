package net.fypm.InstallerOpt;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static net.fypm.InstallerOpt.Utils.maxBackupVersions;

public class BackupInstalledApps extends ListActivity {

    private static final String TAG = "InstallerOpt";

    public ArrayList<PInfo> installedApps;
    public ArrayList<String> selectedItems;
    public String appname;
    public ArrayAdapter<PInfo> adapter;
    public List<PackageInfo> packs;
    public String backupDir;
    public ListTask lt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean enableDark = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_DARK_THEME, false);
        if (enableDark) {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.backup_list);
        backupDir = MultiprocessPreferences.getDefaultSharedPreferences(this).getString(Common.PREF_BACKUP_APK_LOCATION, null);
        lt = new ListTask();
        lt.execute();
        selectedItems = new ArrayList<String>();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CheckableLinearLayout item = (CheckableLinearLayout) view;
                    if (item.isChecked()) {
                        String selected = installedApps.get(position).getSourceDir();
                        selectedItems.add(selected);
                    } else {
                        String selected = installedApps.get(position).getSourceDir();
                        selectedItems.remove(selected);
                    }
                    Toast.makeText(BackupInstalledApps.this, selectedItems.toString(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.backup_installed_apps_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.backup_menu:
                if (selectedItems.size() > 0) {
                    //MultiprocessPreferences.getDefaultSharedPreferences(ManageBackgroundInstallExceptions.this).edit().putString(Common.PREF_INSTALL_BACKGROUND_EXCEPTIONS, selectedItems.toString()).apply();
                    new AsyncBackup(this, backupDir, selectedItems).execute();
                    //this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.exception_selection_empty, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.cancel_menu:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class ListTask extends AsyncTask<String, String, Boolean> {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            if (pDialog == null) {
                pDialog = new ProgressDialog(BackupInstalledApps.this);
                //pDialog.setMessage(ManageBackups.this.getString(R.string.move_file_prepare_message));
                pDialog.setMessage(BackupInstalledApps.this.getString(R.string.loading_applications_message));
                //pDialog.setProgress(0);
                pDialog.setIndeterminate(true);
                pDialog.setCancelable(false);
                pDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
            pDialog = null;
            Collections.sort(installedApps, new Comparator<PInfo>() {
                public int compare(PInfo one, PInfo other) {
                    return one.getName().compareTo(other.getName());
                }
            });
            adapter = new CustomBackupInstalledAppsListArrayAdapter(BackupInstalledApps.this, installedApps);
            setListAdapter(adapter);
            //adapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pDialog.setMessage(String.format("%-12s %s %s %d", BackupInstalledApps.this.getString(R.string.parse_application_message).replace('*', ' '), String.valueOf(values[0]), BackupInstalledApps.this.getString(R.string.parse_application_of_message).replace('*', ' '), packs.size()) + "\n\n" + appname);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean getSysPackages = false;
            installedApps = new ArrayList<PInfo>();
            packs = getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
            for (int i = 0; i < packs.size(); i++) {
                PackageInfo p = packs.get(i);
                if ((!getSysPackages) && (p.versionName == null)) {
                    continue;
                }
                appname = p.applicationInfo.loadLabel(getPackageManager()).toString();
                //String pname = p.packageName;
                String sourceDir = p.applicationInfo.sourceDir;
                //int uid = p.applicationInfo.uid;
                String versionName = p.versionName;
                //int versionCode = p.versionCode;
                Drawable appicon = p.applicationInfo.loadIcon(getPackageManager());
                PInfo newInfo = new PInfo(appname, "", 0, versionName, 0, appicon, 0, "", "", "", "", "", sourceDir);
                installedApps.add(newInfo);
                publishProgress(String.valueOf(i));
            }
            return null;
        }
    }

    class AsyncBackup extends AsyncTask<String, String, String> {

        private static final String TAG = "InstallerOpt";
        String savePath;
        Activity ctx;
        private ProgressDialog pDialog;
        ArrayList<String> arr;
        public boolean enableDebug;

        AsyncBackup(Activity _ctx, String _savePath, ArrayList<String> Files) {
            this.ctx = _ctx;
            this.savePath = _savePath;
            this.arr = Files;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (pDialog == null) {
                pDialog = new ProgressDialog(ctx);
                pDialog.setMessage(ctx.getString(R.string.backup_file_prepare_message));
                //pDialog.setProgress(0);
                pDialog.setIndeterminate(true);
                pDialog.setCancelable(false);
                pDialog.show();
            }
        }

        @Override
        protected String doInBackground(String... urls) {
            File f = new File(savePath);
            if (!f.exists()) {
                if (f.mkdir()) {
                    Log.i(TAG, "doInBackground: Backup directory created");
                } else {
                    Log.e(TAG, "doInBackground: Unable to create backup directory");
                }
            }
            for (int i = 0; i < arr.size(); i++) {
                Backup(arr.get(i), savePath);
                publishProgress(String.valueOf(i));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            if(pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
            pDialog = null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pDialog.setMessage(String.format("%-12s %s %s %d", ctx.getString(R.string.backup_file_message).replace('*', ' '), String.valueOf(values[0]), ctx.getString(R.string.backup_file_of_message).replace('*', ' '), arr.size()));
        }

        void Backup(String apkFile, String dir) {
            enableDebug = MultiprocessPreferences.getDefaultSharedPreferences(ctx).getBoolean(Common.PREF_ENABLE_DEBUG, false);
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
                    deleteApkFile(oldest_file, true);
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
                            Toast.makeText(ctx, "APK file: " + apkFile + " successfully backed up",
                                    Toast.LENGTH_LONG).show();
                        }
                        Log.i(TAG, "APK file " + apkFile + " successfully backed up");
                    } else {
                        if (enableDebug) {
                            Toast.makeText(ctx, "APK file: " + apkFile + " was not successfully backed up",
                                    Toast.LENGTH_LONG).show();
                        }
                        Log.e(TAG, "APK file " + apkFile + " was not successfully backed up");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "backupApkFile Error Debug Start");
                Log.e(TAG, "Error caught: " + e);
                Log.e(TAG, "backupApkFile Error Debug End");
            }
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
    }

    public void deleteApkFile(String apkFile, boolean force) {
        String backupDir = MultiprocessPreferences.getDefaultSharedPreferences(this).getString(Common.PREF_BACKUP_APK_LOCATION, null);
        boolean enableDebug = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_DEBUG, false);
        File apk = new File(apkFile);
        String absolutePath = apk.getAbsolutePath();
        String filePath = apkFile.
                substring(0,absolutePath.lastIndexOf(File.separator));
        if (filePath.equals(backupDir) && !force) {
            Log.i(TAG, "deleteApkFile: Install started from backup directory, file not deleted");
            return;
        } else {
            try {
                if (!apk.delete()) {
                    if (enableDebug) {
                        Toast.makeText(this, "APK file: " + apkFile + " was not successfully deleted",
                                Toast.LENGTH_LONG).show();
                    }
                    Log.e(TAG, "APK file " + apkFile + " was not successfully deleted");
                    String message = apk.exists() ? "is in use by another app" : "does not exist";
                    throw new IOException("Cannot delete file, because file " + message + ".");
                } else {
                    if (enableDebug) {
                        Toast.makeText(this, "APK file: " + apkFile + " successfully deleted",
                                Toast.LENGTH_LONG).show();
                    }
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
}