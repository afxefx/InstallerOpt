package net.fypm.InstallerOpt;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class ManageBackups extends ListActivity {

    private static final String TAG = "InstallerOpt";

    public ArrayList<String> filesInFolder;
    public String appname;
    public ArrayList<PInfo> filesInFolderPackageInfo;
    public ArrayList<String> selectedItems;
    public String backupDir;
    public static ArrayAdapter<PInfo> adapter;
    public boolean enableDebug;
    public boolean unknownApps;
    public boolean unknownAppsPrompt;
    public ListTask lt;
    public boolean reload;
    private EditText searchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean enableDark = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_DARK_THEME, false);
        enableDebug = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_DEBUG, false);
        unknownApps = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP, false);
        unknownAppsPrompt = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP_PROMPT, false);
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
                        String selected = filesInFolderPackageInfo.get(position).getApkName();
                        selectedItems.add(selected);
                    } else {
                        String selected = filesInFolderPackageInfo.get(position).getApkName();
                        selectedItems.remove(selected);
                    }
                    Toast.makeText(ManageBackups.this, selectedItems.size() + " backups selected", Toast.LENGTH_SHORT).show();
                }
            });

        searchBox = (EditText) findViewById(R.id.searchbox);

        // Add Text Change Listener to EditText
        searchBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call back the Adapter with current character to Filter
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manage_backup_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Sent via InstallerOpt");
                intent.setType("*/*");

                ArrayList<Uri> files = new ArrayList<Uri>();

                for(String path : selectedItems) {
                    File file = new File(backupDir + File.separator + path);
                    Uri uri = Uri.fromFile(file);
                    files.add(uri);
                }

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    reload = false;
                    startActivity(Intent.createChooser(intent, "Share"));
                } else {
                    Toast.makeText(getApplicationContext(), "Not able to share this file type", Toast.LENGTH_LONG).show();
                }
                //startActivity(intent);
                return true;
            case R.id.stats:
                startActivity(new Intent(this, Stats.class));
                return true;
            case R.id.backup_delete_menu:
                if (selectedItems.size() > 0) {
                    reload = true;
                    new AsyncDelete(this, backupDir, selectedItems).execute();
                    //this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.backup_selection_empty, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.backup_restore_menu:
                if (selectedItems.size() > 0) {
                    reload = true;
                    new AsyncRestore(this, backupDir, selectedItems).execute();
                    //this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.backup_selection_empty, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.sort_name_asc:
                Collections.sort(filesInFolderPackageInfo, PInfo.COMPARE_BY_APKNAME);
                adapter.notifyDataSetChanged();
                return true;
            case R.id.sort_name_dec:
                Collections.sort(filesInFolderPackageInfo, Collections.reverseOrder(PInfo.COMPARE_BY_APKNAME));
                //Collections.sort(filesInFolder, Collections.reverseOrder(new NaturalOrderComparator()));
                adapter.notifyDataSetChanged();
                return true;
            case R.id.sort_size_asc:
                Collections.sort(filesInFolderPackageInfo, PInfo.COMPARE_BY_SIZE);
                //Collections.sort(filesInFolder, new NaturalOrderComparator());
                adapter.notifyDataSetChanged();
                return true;
            case R.id.sort_size_dec:
                Collections.sort(filesInFolderPackageInfo, Collections.reverseOrder(PInfo.COMPARE_BY_SIZE));
                //Collections.sort(filesInFolder, Collections.reverseOrder(new NaturalOrderComparator()));
                adapter.notifyDataSetChanged();
                return true;
            case R.id.sort_status_asc:
                Collections.sort(filesInFolderPackageInfo, PInfo.COMPARE_BY_STATUS);
                //Collections.sort(filesInFolder, new NaturalOrderComparator());
                adapter.notifyDataSetChanged();
                return true;
            case R.id.sort_status_dec:
                Collections.sort(filesInFolderPackageInfo, Collections.reverseOrder(PInfo.COMPARE_BY_STATUS));
                //Collections.sort(filesInFolder, Collections.reverseOrder(new NaturalOrderComparator()));
                adapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*public void onListItemClick(ListView parent, View v, int position, long id) {
        CheckedTextView item = (CheckedTextView) v;
        if (item.isChecked()) {
            selectedItems.add(filesInFolder.get(position));
        } else {
            selectedItems.remove(filesInFolder.get(position));
        }
        *//*Toast.makeText(this, FilesInFolder.get(position) + " checked : " +
                item.isChecked(), Toast.LENGTH_SHORT).show();*//*
        Toast.makeText(this, selectedItems.toString(), Toast.LENGTH_SHORT).show();
    }*/

    @Override
    public void onResume() {
        super.onResume();
        if (lt.getStatus() != AsyncTask.Status.PENDING && lt.getStatus() != AsyncTask.Status.RUNNING && reload){
            lt = new ListTask();
            lt.execute();
        }
        /*if (lt.getStatus() == AsyncTask.Status.FINISHED && adapter != null) {
            adapter.notifyDataSetChanged();
            selectedItems.clear();
        }*/
    }

    public static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }

    public ArrayList<String> getFiles(String DirectoryPath) {
        ArrayList<String> arrayFiles = new ArrayList<String>();
        File f = new File(DirectoryPath);

        File[] files = f.listFiles();
        if (files.length == 0) {
            return null;
        } else {
            for (int i = 0; i < files.length; i++) {
                arrayFiles.add(files[i].getName());
            }
        }

        return arrayFiles;
    }

    private void lockScreenOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void unlockScreenOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    class ListTask extends AsyncTask<String, String, Boolean> {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            if (pDialog == null) {
                lockScreenOrientation();
                pDialog = new ProgressDialog(ManageBackups.this);
                //pDialog.setMessage(ManageBackups.this.getString(R.string.move_file_prepare_message));
                pDialog.setMessage(ManageBackups.this.getString(R.string.loading_backup_message));
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
            unlockScreenOrientation();
            pDialog = null;
            Collections.sort(filesInFolderPackageInfo, PInfo.COMPARE_BY_APKNAME);
            adapter = new CustomBackupListArrayAdapter(ManageBackups.this, filesInFolderPackageInfo);
            setListAdapter(adapter);
            selectedItems.clear();
            //adapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pDialog.setMessage(String.format("%-12s %s %s %d", ManageBackups.this.getString(R.string.parse_backup_message).replace('*', ' '), String.valueOf(values[0]), ManageBackups.this.getString(R.string.parse_backup_of_message).replace('*', ' '), filesInFolder.size()) + "\n\n" + appname);
            }

        @Override
        protected Boolean doInBackground(String... params) {
            filesInFolder = getFiles(backupDir);
            filesInFolderPackageInfo = new ArrayList<PInfo>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

            for (int i = 0; i < filesInFolder.size(); i++) {
                PackageInfo p = getPackageManager().getPackageArchiveInfo(backupDir + File.separator + filesInFolder.get(i).toString(), 0);

                if (p != null) {
                    p.applicationInfo.sourceDir = backupDir + File.separator + filesInFolder.get(i).toString();
                    p.applicationInfo.publicSourceDir = backupDir + File.separator + filesInFolder.get(i).toString();

                    appname = p.applicationInfo.loadLabel(getPackageManager()).toString();
                    String pname = p.packageName;
                    //int uid = p.applicationInfo.uid;
                    String versionName = p.versionName;
                    int versionCode = p.versionCode;

                    String currentVersion = "";
                    int currentCode = 0;
                    String status = "";
                    try {
                        PackageInfo pi = getPackageManager().getPackageInfo(pname, 0);
                        currentVersion = pi.versionName;
                        currentCode = pi.versionCode;
                        if (versionName.equals(currentVersion) && versionCode == currentCode) {
                            status = "Installed";
                        } else if (versionName.compareTo(currentVersion) < 0 || versionCode < currentCode) {
                            status = "Older";
                        } else if (versionName.compareTo(currentVersion) > 0 || versionCode > currentCode) {
                            status = "Newer";
                        }

                    } catch (PackageManager.NameNotFoundException e) {
                        if (enableDebug) {
                            Log.e(TAG, appname + " is not installed.");
                        }
                        status = "Not Installed";
                    }

                    Drawable appicon = p.applicationInfo.loadIcon(getPackageManager());
                    File item = new File(backupDir + File.separator + filesInFolder.get(i).toString());
                    long itemSize = Stats.getFileSize(item);
                    String itemSizeHuman = Stats.humanReadableByteCount(itemSize, true);
                    Date itemModified = new Date(item.lastModified());
                    String formattedDate = dateFormat.format(itemModified);
                    //String calculatedDigest = calculateMD5(item);
                    String apkName = filesInFolder.get(i).toString();

                    PInfo newInfo = new PInfo(appname, pname, 0, versionName, versionCode, appicon, itemSize, itemSizeHuman, formattedDate, "", apkName, status, "");
                    filesInFolderPackageInfo.add(newInfo);
                    publishProgress(String.valueOf(i));
                }
            }
            return null;
        }
    }

    class AsyncDelete extends AsyncTask<String, String, String> {

        private static final String TAG = "InstallerOpt";
        String savePath;
        Activity ctx;
        private ProgressDialog pDialog;
        ArrayList<String> arr;

        AsyncDelete(Activity _ctx, String _savePath, ArrayList<String> Files) {
            this.ctx = _ctx;
            this.savePath = _savePath;
            this.arr = Files;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (pDialog == null) {
                lockScreenOrientation();
                pDialog = new ProgressDialog(ctx);
                pDialog.setMessage(ctx.getString(R.string.backup_delete_file_prepare_message));
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
                Delete(arr.get(i));
                publishProgress(String.valueOf(i));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            Toast.makeText(ctx, R.string.backup_delete_complete_message, Toast.LENGTH_LONG).show();
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
            unlockScreenOrientation();
            pDialog = null;
            if(lt.getStatus() != AsyncTask.Status.PENDING && lt.getStatus() != AsyncTask.Status.RUNNING){
                lt = new ListTask();
                lt.execute();
            }
            /*if (lt.getStatus() == AsyncTask.Status.FINISHED && adapter != null) {
                adapter.notifyDataSetChanged();
                selectedItems.clear();
            }*/
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pDialog.setMessage(String.format("%-12s %s %s %d", ctx.getString(R.string.delete_file_message).replace('*', ' '), String.valueOf(values[0]), ctx.getString(R.string.move_file_of_message).replace('*', ' '), arr.size()));
        }

        void Delete(String apkFile) {
            File apk = new File(savePath + File.separator + apkFile);
            try {
                if (!apk.delete()) {
                    Toast.makeText(ctx, "APK file: " + apkFile + " was not successfully deleted",
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "APK file " + apkFile + " was not successfully deleted");
                    String message = apk.exists() ? "is in use by another app" : "does not exist";
                    throw new IOException("Cannot delete file, because file " + message + ".");
                } else {
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

    class AsyncRestore extends AsyncTask<String, String, String> {

        private static final String TAG = "InstallerOpt";
        String savePath;
        Activity ctx;
        private ProgressDialog pDialog;
        ArrayList<String> arr;

        AsyncRestore(Activity _ctx, String _savePath, ArrayList<String> Files) {
            this.ctx = _ctx;
            this.savePath = _savePath;
            this.arr = Files;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (pDialog == null) {
                lockScreenOrientation();
                MultiprocessPreferences.getDefaultSharedPreferences(ctx).edit().putBoolean(Common.PREF_ENABLE_AUTO_INSTALL_CANCEL_OVERRIDE, true).apply();
                if (!unknownApps) {
                    MultiprocessPreferences.getDefaultSharedPreferences(ctx).edit().putBoolean(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP, true).apply();
                    MultiprocessPreferences.getDefaultSharedPreferences(ctx).edit().putBoolean(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP_PROMPT, false).apply();
                }
                pDialog = new ProgressDialog(ctx);
                pDialog.setMessage(ctx.getString(R.string.backup_restore_file_prepare_message));
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
                Restore(arr.get(i));
                publishProgress(String.valueOf(i));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            Toast.makeText(ctx, R.string.backup_restore_complete_message, Toast.LENGTH_LONG).show();
            MultiprocessPreferences.getDefaultSharedPreferences(ctx).edit().putBoolean(Common.PREF_ENABLE_AUTO_INSTALL_CANCEL_OVERRIDE, false).apply();
            MultiprocessPreferences.getDefaultSharedPreferences(ctx).edit().putBoolean(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP, unknownApps).apply();
            MultiprocessPreferences.getDefaultSharedPreferences(ctx).edit().putBoolean(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP_PROMPT, unknownAppsPrompt).apply();
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
            unlockScreenOrientation();
            pDialog = null;
            if (lt.getStatus() != AsyncTask.Status.PENDING && lt.getStatus() != AsyncTask.Status.RUNNING){
                lt = new ListTask();
                lt.execute();
            }
            /*if (lt.getStatus() == AsyncTask.Status.FINISHED && adapter != null) {
                adapter.notifyDataSetChanged();
                selectedItems.clear();
            }*/
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pDialog.setMessage(String.format("%-12s %s %s %d", ctx.getString(R.string.restore_file_message).replace('*', ' '), String.valueOf(values[0]), ctx.getString(R.string.move_file_of_message).replace('*', ' '), arr.size()));
        }

        void Restore(String apkFile) {
            try {
                File apk = new File(savePath + File.separator + apkFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
                ctx.startActivity(intent);
            } catch (Exception e) {
                MultiprocessPreferences.getDefaultSharedPreferences(ctx).edit().putBoolean(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP, unknownApps).apply();
                MultiprocessPreferences.getDefaultSharedPreferences(ctx).edit().putBoolean(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP_PROMPT, unknownAppsPrompt).apply();
                Log.e(TAG, "Restore file error: ", e);
            }
        }
    }
}