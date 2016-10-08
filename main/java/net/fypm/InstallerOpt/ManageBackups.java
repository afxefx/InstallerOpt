package net.fypm.InstallerOpt;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
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
    public ArrayList<PInfo> filesInFolderPackageInfo;
    public ArrayList<String> selectedItems;
    public String backupDir;
    public ArrayAdapter<PInfo> adapter;
    public boolean enableDebug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean enableDark = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_DARK_THEME, false);
        enableDebug = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_DEBUG, false);
        if (enableDark) {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.backup_list);

        backupDir = MultiprocessPreferences.getDefaultSharedPreferences(this).getString(Common.PREF_BACKUP_APK_LOCATION, null);
        new Task().execute();
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
                    //Toast.makeText(ManageBackups.this, selectedItems.toString(), Toast.LENGTH_SHORT).show();
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
            case R.id.stats:
                startActivity(new Intent(this, Stats.class));
                return true;
            case R.id.backup_delete_menu:
                if (selectedItems.size() > 0) {
                    new AsyncDelete(this, backupDir, selectedItems).execute("");
                    this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.backup_selection_empty, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.backup_restore_menu:
                if (selectedItems.size() > 0) {
                    new AsyncRestore(this, backupDir, selectedItems).execute("");
                    this.finish();
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
        //adapter.notifyDataSetChanged();
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

    class Task extends AsyncTask<String, String, Boolean> {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            if (pDialog == null) {
                pDialog = new ProgressDialog(ManageBackups.this);
                //pDialog.setMessage(ManageBackups.this.getString(R.string.move_file_prepare_message));
                pDialog.setMessage("Loading backups...");
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
            Collections.sort(filesInFolderPackageInfo, PInfo.COMPARE_BY_APKNAME);
            adapter = new CustomBackupListArrayAdapter(ManageBackups.this, filesInFolderPackageInfo);
            setListAdapter(adapter);
            //adapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pDialog.setMessage(String.format("%-12s %s %s %d", ManageBackups.this.getString(R.string.parse_backup_message).replace('*', ' '), String.valueOf(values[0]), ManageBackups.this.getString(R.string.parse_backup_of_message).replace('*', ' '), filesInFolder.size()));
            }

        @Override
        protected Boolean doInBackground(String... params) {
            filesInFolder = getFiles(backupDir);
            filesInFolderPackageInfo = new ArrayList<PInfo>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");

            for (int i = 0; i < filesInFolder.size(); i++) {
                PackageInfo p = getPackageManager().getPackageArchiveInfo(backupDir + File.separator + filesInFolder.get(i).toString(), 0);

                if (p != null) {
                    p.applicationInfo.sourceDir = backupDir + File.separator + filesInFolder.get(i).toString();
                    p.applicationInfo.publicSourceDir = backupDir + File.separator + filesInFolder.get(i).toString();

                    String appname = p.applicationInfo.loadLabel(getPackageManager()).toString();
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
                    String itemSize = Stats.humanReadableByteCount(Stats.getFileSize(item), true);
                    Date itemModified = new Date(item.lastModified());
                    String formattedDate = dateFormat.format(itemModified);
                    //String calculatedDigest = calculateMD5(item);
                    String apkName = filesInFolder.get(i).toString();

                    PInfo newInfo = new PInfo(appname, pname, 0, versionName, versionCode, appicon, itemSize, formattedDate, "", apkName, status);
                    filesInFolderPackageInfo.add(newInfo);
                    publishProgress(String.valueOf(i));
                }
            }
            return null;
        }
    }
}