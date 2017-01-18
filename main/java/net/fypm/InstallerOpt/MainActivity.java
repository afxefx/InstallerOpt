package net.fypm.InstallerOpt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int REQUEST_DIRECTORY = 112;
    private static final int REQUEST_MOVE_BACKUPS = 113;
    private static final int REQUEST_REBOOT = 114;
    private static final int REQUEST_WRITE_STORAGE = 115;

    private static final String TAG = "InstallerOpt";
    public Activity activity;

    public MainActivity() {

    }

    @SuppressWarnings({"deprecation"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean enableDark = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_DARK_THEME, false);
        if (enableDark) {
            setTheme(R.style.AppThemeDark);
        }

        int oldVersionCode = MultiprocessPreferences.getDefaultSharedPreferences(this).getInt(Common.PREF_VERSION_CODE_KEY, Common.DOESNT_EXIST);
        if (oldVersionCode < 591) {
            resetPreferences();
        }
        if (oldVersionCode < 1801) {
            ComponentName alias = new ComponentName(
                    this, "net.fypm.InstallerOpt.MainActivity-Alias");
            this.getPackageManager().setComponentEnabledSetting(alias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            MultiprocessPreferences.getDefaultSharedPreferences(this).edit().putBoolean(Common.PREF_ENABLE_APP_ICON, true).apply();
        }
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment()).commit();
    }

    public void resetPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, About.class));
                return true;
            case R.id.backup_installed_menu:
                startActivity(new Intent(this, BackupInstalledApps.class));
                return true;
            case R.id.stats:
                startActivity(new Intent(this, ManageBackups.class));
                return true;
            case R.id.backupprefs:
                Intent backupPreferences = new Intent(
                        Common.ACTION_BACKUP_PREFERENCES);
                backupPreferences.setPackage(Common.PACKAGE_NAME);
                this.sendBroadcast(backupPreferences);
                return true;
            case R.id.restoreprefs:
                Intent restorePreferences = new Intent(
                        Common.ACTION_RESTORE_PREFERENCES);
                restorePreferences.setPackage(Common.PACKAGE_NAME);
                this.sendBroadcast(restorePreferences);
                this.finish();
                return true;
            case R.id.resetprefs:
                Intent resetPreferences = new Intent(
                        Common.ACTION_RESET_PREFERENCES);
                resetPreferences.setPackage(Common.PACKAGE_NAME);
                this.sendBroadcast(resetPreferences);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("ValidFragment")
    public class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

        boolean stateOfClose;
        boolean stateOfLaunch;
        boolean forceEnglish;


        @SuppressWarnings("deprecation")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            activity = getActivity();
            forceEnglish = MultiprocessPreferences.getDefaultSharedPreferences(activity).getBoolean(Common.PREF_ENABLE_FORCE_ENGLISH, false);
            stateOfClose = MultiprocessPreferences.getDefaultSharedPreferences(activity).getBoolean(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL, false);
            stateOfLaunch = MultiprocessPreferences.getDefaultSharedPreferences(activity).getBoolean(Common.PREF_ENABLE_AUTO_LAUNCH_INSTALL, false);
            if (stateOfClose && stateOfLaunch) {
                MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL, false).apply();
                MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean(Common.PREF_ENABLE_AUTO_LAUNCH_INSTALL, false).apply();
            }
            if (forceEnglish) {
                String languageToLoad = "en";
                Locale locale = new Locale(languageToLoad);
                //Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
            } else {
                Locale locale = Locale.getDefault();
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());

            }
            checkFirstRun();
            getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.prefs);
            findPreference(Common.PREF_ENABLE_APP_ICON).setOnPreferenceChangeListener(changeListenerLauncher);
            //findPreference(Common.PREF_ENABLE_MODULE).setOnPreferenceChangeListener(changeListenerLauncher1);
            findPreference(Common.PREF_ENABLE_DARK_THEME).setOnPreferenceChangeListener(changeListenerLauncher2);
            findPreference(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL).setOnPreferenceChangeListener(changeListenerLauncher3);
            findPreference(Common.PREF_ENABLE_AUTO_LAUNCH_INSTALL).setOnPreferenceChangeListener(changeListenerLauncher4);
            findPreference(Common.PREF_ENABLE_BACKUP_APK_FILE).setOnPreferenceChangeListener(changeListenerLauncher5);
            findPreference(Common.PREF_ENABLE_EXTERNAL_SDCARD_FULL_ACCESS).setOnPreferenceChangeListener(changeListenerLauncher6);
            findPreference(Common.PREF_ENABLE_FORCE_ENGLISH).setOnPreferenceChangeListener(changeListenerLauncher7);
            findPreference(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP).setOnPreferenceChangeListener(changeListenerLauncher8);
            findPreference(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP_PROMPT).setOnPreferenceChangeListener(changeListenerLauncher9);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            activity = getActivity();
            long time = SystemClock.elapsedRealtime();
            MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putLong(Common.PREF_MODIFIED_TIME, time).apply();
            MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean(Common.PREF_MODIFIED_PREFERENCES, true).apply();
            Log.i(TAG, "onSharedPreferenceChanged: Change detected");
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            File sharedPrefsDir = new File(getActivity().getApplicationInfo().dataDir, "shared_prefs");
            File sharedPrefsFile = new File(sharedPrefsDir, getPreferenceManager().getSharedPreferencesName() + ".xml");
            if (sharedPrefsFile.exists()) {
                sharedPrefsFile.setReadable(true, false);
            }
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        private void checkFirstRun() {

            int currentVersionCode = 0;
            try {
                currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            } catch (android.content.pm.PackageManager.NameNotFoundException e) {
                // handle exception
                Log.e(TAG, "checkFirstRun - Current version code not found");
                Log.e(TAG, "checkFirstRun: ", e);
                Log.e(TAG, "checkFirstRun - Current version code not found");
                return;
            }

            int savedVersionCode = MultiprocessPreferences.getDefaultSharedPreferences(activity).getInt(Common.PREF_VERSION_CODE_KEY, Common.DOESNT_EXIST);

            if (currentVersionCode == savedVersionCode) {

                // This is just a normal run
                return;

            } else if (savedVersionCode == Common.DOESNT_EXIST) {

                // New install or shared preferences cleared
                Toast.makeText(getActivity(), getString(R.string.reset), Toast.LENGTH_LONG).show();
                activity = getActivity();
                ComponentName alias = new ComponentName(
                        activity, "net.fypm.InstallerOpt.MainActivity-Alias");
                activity.getPackageManager().setComponentEnabledSetting(alias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean(Common.PREF_ENABLE_APP_ICON, true).apply();

                String dirPath = activity.getFilesDir().getParentFile().getPath() + "/shared_prefs/";
                File sharedPrefsFileOld = new File(dirPath, "prefs.xml");
                if (sharedPrefsFileOld.exists()) {
                    boolean deleted = sharedPrefsFileOld.delete();
                    if (deleted) {
                        Log.e(TAG, "checkFirstRun - Old preference file found and deleted");
                        Log.e(TAG, "checkFirstRun: ", null);
                        Log.e(TAG, "checkFirstRun - Old preference file found and deleted");
                    }
                }

            } else if (currentVersionCode > savedVersionCode) {

                // This is an upgrade
                Toast.makeText(activity, getString(R.string.updated), Toast.LENGTH_SHORT).show();

            }

            MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putInt(Common.PREF_VERSION_CODE_KEY, currentVersionCode).apply();

        }

        private boolean isLauncherIconVisible(ComponentName componentName) {
            int enabledSetting = getPackageManager()
                    .getComponentEnabledSetting(componentName);
            return enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        }

        private final Preference.OnPreferenceChangeListener changeListenerLauncher = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                activity = getActivity();
                ComponentName alias = new ComponentName(
                        activity, "net.fypm.InstallerOpt.MainActivity-Alias");
                if (newValue.equals(false)) {
                    activity.getPackageManager().setComponentEnabledSetting(alias, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                } else {
                    activity.getPackageManager().setComponentEnabledSetting(alias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                }
                /*if (isLauncherIconVisible(alias)) {
                    activity.getPackageManager().setComponentEnabledSetting(alias, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                } else {
                    activity.getPackageManager().setComponentEnabledSetting(alias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                }*/
                return true;
            }
        };

        /*private final Preference.OnPreferenceChangeListener changeListenerLauncher1 = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent refresh = new Intent(getActivity(), getActivity()
                        .getClass());
                startActivity(refresh);
                getActivity().finish();
                return true;
            }
        };*/

        private final Preference.OnPreferenceChangeListener changeListenerLauncher2 = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent refresh = new Intent(getActivity(), getActivity()
                        .getClass());
                startActivity(refresh);
                getActivity().finish();
                return true;
            }
        };

        private final Preference.OnPreferenceChangeListener changeListenerLauncher3 = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                activity = getActivity();
                if (newValue.equals(true)) {
                    findPreference(Common.PREF_ENABLE_AUTO_LAUNCH_INSTALL).setEnabled(false);
                    MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean(Common.PREF_ENABLE_AUTO_LAUNCH_INSTALL, false).apply();
                } else {
                    findPreference(Common.PREF_ENABLE_AUTO_LAUNCH_INSTALL).setEnabled(true);
                }
                return true;
            }
        };

        private final Preference.OnPreferenceChangeListener changeListenerLauncher4 = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                activity = getActivity();
                if (newValue.equals(true)) {
                    findPreference(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL).setEnabled(false);
                    MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL, false).apply();
                } else {
                    findPreference(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL).setEnabled(true);
                }
                return true;
            }
        };

        private final Preference.OnPreferenceChangeListener changeListenerLauncher5 = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                activity = getActivity();
                if (newValue.equals(true)) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (isReadStorageAllowed()) {
                            Log.i(TAG, "isReadStorageAllowed(): True");
                            //Toast.makeText(MainActivity.this,"You already granted the permission, thanks!",Toast.LENGTH_LONG).show();
                            //return;
                        } else {
                            requestStoragePermission();
                        }
                    }
                    boolean externalSD = MultiprocessPreferences.getDefaultSharedPreferences(activity).getBoolean(Common.PREF_ENABLE_EXTERNAL_SDCARD_FULL_ACCESS, false);
                    if (!externalSD) {
                        startActivityForResult(new Intent(activity, Reboot.class), REQUEST_REBOOT);
                    } else {
                        chooseBackupDir();
                    }
                }
                return true;
            }
        };

        private final Preference.OnPreferenceChangeListener changeListenerLauncher6 = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                activity = getActivity();
                if (newValue.equals(true)) {
                    startActivity(new Intent(activity, Reboot.class));
                }
                return true;
            }
        };

        private final Preference.OnPreferenceChangeListener changeListenerLauncher7 = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent refresh = new Intent(getActivity(), getActivity()
                        .getClass());
                startActivity(refresh);
                getActivity().finish();
                return true;
            }
        };

        private final Preference.OnPreferenceChangeListener changeListenerLauncher8 = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                activity = getActivity();
                if (newValue.equals(true)) {
                    findPreference(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP_PROMPT).setEnabled(false);
                    MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP_PROMPT, false).apply();
                } else {
                    findPreference(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP_PROMPT).setEnabled(true);
                }
                return true;
            }
        };

        private final Preference.OnPreferenceChangeListener changeListenerLauncher9 = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                activity = getActivity();
                if (newValue.equals(true)) {
                    findPreference(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP).setEnabled(false);
                    MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP, false).apply();
                } else {
                    findPreference(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP).setEnabled(true);
                }
                return true;
            }
        };

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
            super.onActivityResult(requestCode, resultCode, resultData);
            activity = getActivity();
            if (requestCode == REQUEST_DIRECTORY) {
                if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                    String curBackupDir = MultiprocessPreferences.getDefaultSharedPreferences(activity).getString(Common.PREF_BACKUP_APK_LOCATION, null);
                    String newBackupDir = resultData.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
                    boolean writeable;
                    boolean enoughSpace;
                    if (newBackupDir != null) {
                        File newBackupDirWriteable = new File(newBackupDir);
                        if (newBackupDirWriteable.canWrite()) {
                            writeable = true;
                        } else {
                            writeable = false;
                            Log.e(TAG, "Unable to write to chosen folder");
                            Toast.makeText(activity, R.string.non_writeable_message, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        writeable = false;
                    }
                    if (curBackupDir != null && writeable) {
                        long curBackupDirSize = Stats.getFileSize(new File(curBackupDir));
                        long availableSpace = Stats.getAvailableSpaceInBytes(newBackupDir);
                        if (availableSpace > curBackupDirSize) {
                            enoughSpace = true;
                        } else {
                            enoughSpace = false;
                            Log.e(TAG, "Chosen folder does not have enough free space");
                            Toast.makeText(activity, R.string.free_space_message, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        enoughSpace = true;
                    }
                    if (enoughSpace && writeable) {
                        MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putString(Common.PREF_BACKUP_APK_LOCATION_OLD, curBackupDir).apply();
                        MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putString(Common.PREF_BACKUP_APK_LOCATION, newBackupDir).apply();
                    } else {
                        chooseBackupDir();
                    }
                    if (curBackupDir != null && enoughSpace && writeable && !curBackupDir.equals(newBackupDir)) {
                        Log.i(TAG, "curBackupDir: " + curBackupDir + " Length is: " + curBackupDir.length());
                        Log.i(TAG, "newBackupDir: " + newBackupDir + " Length is: " + newBackupDir.length());
                        startActivityForResult(new Intent(activity, MoveBackups.class), REQUEST_MOVE_BACKUPS);
                    }
                } else {
                    MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean(Common.PREF_ENABLE_BACKUP_APK_FILE, false).apply();
                    Preference pref = findPreference(Common.PREF_ENABLE_BACKUP_APK_FILE);
                    CheckBoxPreference check = (CheckBoxPreference) pref;
                    check.setChecked(false);
                }
            }
            if (requestCode == REQUEST_MOVE_BACKUPS) {
                if (resultCode == MoveBackups.RESULT_MOVE_PERFORM) {
                    String oldBackupDir = MultiprocessPreferences.getDefaultSharedPreferences(activity).getString(Common.PREF_BACKUP_APK_LOCATION_OLD, null);
                    String newBackupDir = MultiprocessPreferences.getDefaultSharedPreferences(activity).getString(Common.PREF_BACKUP_APK_LOCATION, null);

                    ArrayList<String> TmpList = new ArrayList<String>();
                    File old_file = new File(oldBackupDir);
                    //Log.i(TAG, "old_file " + old_file);
                    ArrayList<String> old_files = new ArrayList<String>(Arrays.asList(old_file.list()));
                    //Log.i(TAG, "old_files array " + old_files.size());

                    File new_file = new File(newBackupDir);
                    if (!new_file.exists()) {
                        if (new_file.mkdir()) {
                            Log.i(TAG, "onActivityResult: Backup directory created");
                        } else {
                            Log.e(TAG, "onActivityResult: Unable to create backup directory");
                        }
                    }

                    for (int i = 0; i < old_files.size(); i++) {
                        File check = new File(newBackupDir, old_files.get(i));
                        if (!check.exists())
                            TmpList.add(old_files.get(i));
                    }

                    if (old_files.size() > 0) {
                        new AsyncCopy(activity, newBackupDir, TmpList).execute("");
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.no_old_files_message, Toast.LENGTH_LONG).show();
                    }
                }
            }
            if (requestCode == REQUEST_REBOOT) {
                if (resultCode == Reboot.RESULT_CANCELED) {
                    chooseBackupDir();
                }
            }
        }

        @NonNull
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            //Checking the request code of our request
            if (requestCode == REQUEST_WRITE_STORAGE) {
                activity = getActivity();
                //If permission is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Displaying a toast
                    Toast.makeText(activity, getString(R.string.perm_granted), Toast.LENGTH_LONG).show();

                } else {
                    //Displaying another toast if permission is not granted
                    Toast.makeText(activity, getString(R.string.perm_denied), Toast.LENGTH_LONG).show();
                }
            }
        }

        private void chooseBackupDir() {
            activity = getActivity();
            final Intent chooserIntent = new Intent(activity, DirectoryChooserActivity.class);

            final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                    .allowReadOnlyDirectory(false)
                    .allowNewDirectoryNameModification(true)
                    .initialDirectory("/")
                    .newDirectoryName("InstallerOpt")
                    .build();

            chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);

            // REQUEST_DIRECTORY is a constant integer to identify the request, e.g. 0
            startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
        }

        private boolean isReadStorageAllowed() {
            //Getting the permission status
            activity = getActivity();
            int result = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

            //If permission is granted returning true
            if (result == PackageManager.PERMISSION_GRANTED)
                return true;

            //If permission is not granted returning false
            return false;
        }

        //Requesting permission
        private void requestStoragePermission() {
            activity = getActivity();
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //Explain here why you need this permission
                Toast.makeText(activity, R.string.write_permission_message, Toast.LENGTH_LONG).show();
            }
            //And finally ask for the permission
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
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

    class AsyncCopy extends AsyncTask<String, String, String> {

        private static final String TAG = "InstallerOpt";
        String oldPath;
        String savePath;
        Activity ctx;
        private ProgressDialog pDialog;
        ArrayList<String> arr;

        AsyncCopy(Activity _ctx, String _savePath, ArrayList<String> Files) {
            this.ctx = _ctx;
            this.oldPath = MultiprocessPreferences.getDefaultSharedPreferences(ctx).getString(Common.PREF_BACKUP_APK_LOCATION_OLD, null);
            this.savePath = _savePath;
            this.arr = Files;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (pDialog == null) {
                lockScreenOrientation();
                pDialog = new ProgressDialog(ctx);
                pDialog.setMessage(ctx.getString(R.string.move_file_prepare_message));
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
                Copy(arr.get(i));
                publishProgress(String.valueOf(i));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            long curBackupDirSize = Stats.getFileSize(new File(oldPath));
            long newBackupDirSize = Stats.getFileSize(new File(savePath));
            if (newBackupDirSize == curBackupDirSize && !oldPath.equals(savePath)) {
                Stats.deleteRecursive(new File(oldPath + File.separator));
                Toast.makeText(ctx, R.string.move_complete_message, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ctx, R.string.folder_size_error_message, Toast.LENGTH_LONG).show();
            }
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
            unlockScreenOrientation();
            pDialog = null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pDialog.setMessage(String.format("%-12s %s %s %d", ctx.getString(R.string.move_file_message).replace('*', ' '), String.valueOf(values[0]), ctx.getString(R.string.move_file_of_message).replace('*', ' '), arr.size()));
        }

        void Copy(String fname) {
            try {
                int count;
                InputStream input = new FileInputStream(oldPath + File.separator + fname);
                OutputStream output = new FileOutputStream(savePath + File.separator + fname);
                byte data[] = new byte[1024];
                while ((count = input.read(data)) > 0) {
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                Log.e(TAG, "Copy file error: ", e);
            }
        }
    }
}
