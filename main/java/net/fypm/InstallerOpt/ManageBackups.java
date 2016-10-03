package net.fypm.InstallerOpt;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean enableDark = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_DARK_THEME, false);
        if (enableDark) {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.backup_list);

        backupDir = MultiprocessPreferences.getDefaultSharedPreferences(this).getString(Common.PREF_BACKUP_APK_LOCATION, null);
        filesInFolder = getFiles(backupDir);
        filesInFolderPackageInfo = getFilesPackageInfo(false, backupDir, filesInFolder);
        selectedItems = new ArrayList<String>();
        if (filesInFolderPackageInfo != null) {
            Collections.sort(filesInFolderPackageInfo, PInfo.COMPARE_BY_APKNAME);

            adapter = new CustomBackupListArrayAdapter(this, filesInFolderPackageInfo);
            setListAdapter(adapter);

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
        adapter.notifyDataSetChanged();
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

    private ArrayList<PInfo> getFilesPackageInfo(boolean getSysPackages, String DirectoryPath, ArrayList files) {
        boolean enableDebug = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_DEBUG, false);

        ArrayList<PInfo> res = new ArrayList<PInfo>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");

        for (int i = 0; i < files.size(); i++) {
            PackageInfo p = getPackageManager().getPackageArchiveInfo(DirectoryPath + File.separator + files.get(i).toString(), 0);

            if (p != null) {
                //packs.add(p);
                if ((!getSysPackages) && (p.versionName == null)) {
                    continue;
                }

                p.applicationInfo.sourceDir = DirectoryPath + File.separator + files.get(i).toString();
                p.applicationInfo.publicSourceDir = DirectoryPath + File.separator + files.get(i).toString();

                String appname = p.applicationInfo.loadLabel(getPackageManager()).toString();
                String pname = p.packageName;
                //int uid = p.applicationInfo.uid;
                String versionName = p.versionName;
                int versionCode = p.versionCode;

                String currentVersion = "";
                int currentCode = 0;
                String state = "";
                try {
                    PackageInfo pi   = getPackageManager().getPackageInfo(pname, 0);
                    currentVersion = pi.versionName;
                    currentCode = pi.versionCode;
                    if (versionName.equals(currentVersion) && versionCode == currentCode) {
                        state = "Installed";
                    } else if (versionName.compareTo(currentVersion) < 0 || versionCode < currentCode) {
                        state = "Older";
                    } else if (versionName.compareTo(currentVersion) > 0 || versionCode > currentCode) {
                        state = "Newer";
                    }

                } catch (PackageManager.NameNotFoundException e) {
                    if (enableDebug) {
                        Log.e(TAG, appname + " is not installed.");
                    }
                    state = "Not Installed";
                }


                Drawable appicon = p.applicationInfo.loadIcon(getPackageManager());
                File item = new File(DirectoryPath + File.separator + files.get(i).toString());
                String itemSize = Stats.humanReadableByteCount(Stats.getFileSize(item), true);
                Date itemModified = new Date(item.lastModified());
                String formattedDate = dateFormat.format(itemModified);
                //String calculatedDigest = calculateMD5(item);
                String apkName = files.get(i).toString();

                PInfo newInfo = new PInfo(appname, pname, 0, versionName, versionCode, appicon, itemSize, formattedDate, "", apkName, state);
                res.add(newInfo);

            }
        }
        return res;
    }

}