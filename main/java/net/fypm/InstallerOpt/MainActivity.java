package net.fypm.InstallerOpt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int REQUEST_DIRECTORY = 112;
    private static final int REQUEST_MOVE_BACKUPS = 113;
    private static final int REQUEST_REBOOT = 114;
    private static final int REQUEST_WRITE_STORAGE = 115;

    private static final String TAG = "InstallerOpt";

    @SuppressWarnings({"deprecation"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean enableDark = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_DARK_THEME, false);
        if (enableDark) {
            setTheme(R.style.AppThemeDark);
        }

        int oldVersionCode = MultiprocessPreferences.getDefaultSharedPreferences(MainActivity.this).getInt(Common.PREF_VERSION_CODE_KEY, Common.DOESNT_EXIST);
        if (oldVersionCode < 591) {
            resetPreferences();
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
            case R.id.stats:
                startActivity(new Intent(this, Stats.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("ValidFragment")
    public class PrefsFragment extends PreferenceFragment {

        boolean stateOfClose;
        boolean stateOfLaunch;
        boolean forceEnglish;


        @SuppressWarnings("deprecation")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Activity activity = getActivity();
            forceEnglish = MultiprocessPreferences.getDefaultSharedPreferences(activity).getBoolean(Common.PREF_ENABLE_FORCE_ENGLISH, false);
            stateOfClose = MultiprocessPreferences.getDefaultSharedPreferences(activity).getBoolean(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL, false);
            stateOfLaunch = MultiprocessPreferences.getDefaultSharedPreferences(activity).getBoolean(Common.PREF_ENABLE_AUTO_LAUNCH_INSTALL, false);
            if (stateOfClose && stateOfLaunch) {
                MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL, false).apply();
                MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean(Common.PREF_ENABLE_AUTO_LAUNCH_INSTALL, false).apply();
            }
            if (forceEnglish) {
                String languageToLoad  = "en";
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
            findPreference(Common.PREF_ENABLE_HIDE_APP_ICON).setOnPreferenceChangeListener(changeListenerLauncher);
            findPreference(Common.PREF_ENABLE_DARK_THEME).setOnPreferenceChangeListener(changeListenerLauncher2);
            findPreference(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL).setOnPreferenceChangeListener(changeListenerLauncher3);
            findPreference(Common.PREF_ENABLE_AUTO_LAUNCH_INSTALL).setOnPreferenceChangeListener(changeListenerLauncher4);
            findPreference(Common.PREF_ENABLE_BACKUP_APK_FILE).setOnPreferenceChangeListener(changeListenerLauncher5);
            findPreference(Common.PREF_ENABLE_EXTERNAL_SDCARD_FULL_ACCESS).setOnPreferenceChangeListener(changeListenerLauncher6);
            findPreference(Common.PREF_ENABLE_FORCE_ENGLISH).setOnPreferenceChangeListener(changeListenerLauncher7);
        }

        @Override
        public void onPause() {
            super.onPause();
            File sharedPrefsDir = new File(getActivity().getApplicationInfo().dataDir, "shared_prefs");
            File sharedPrefsFile = new File(sharedPrefsDir, getPreferenceManager().getSharedPreferencesName() + ".xml");
            if (sharedPrefsFile.exists()) {
                sharedPrefsFile.setReadable(true, false);
            }
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

            int savedVersionCode = MultiprocessPreferences.getDefaultSharedPreferences(this.getActivity()).getInt(Common.PREF_VERSION_CODE_KEY, Common.DOESNT_EXIST);

            if (currentVersionCode == savedVersionCode) {

                // This is just a normal run
                return;

            } else if (savedVersionCode == Common.DOESNT_EXIST) {

                // New install or shared preferences cleared
                Toast.makeText(getActivity(), getString(R.string.reset), Toast.LENGTH_LONG).show();
                Activity activity = getActivity();
                ComponentName alias = new ComponentName(
                        activity, "net.fypm.InstallerOpt.MainActivity-Alias");
                activity.getPackageManager().setComponentEnabledSetting(alias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean(Common.PREF_ENABLE_HIDE_APP_ICON, false).apply();

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
                Toast.makeText(getActivity(), getString(R.string.updated), Toast.LENGTH_LONG).show();

            }

            MultiprocessPreferences.getDefaultSharedPreferences(this.getActivity()).edit().putInt(Common.PREF_VERSION_CODE_KEY, currentVersionCode).apply();

        }

        private boolean isLauncherIconVisible(ComponentName componentName) {
            int enabledSetting = getPackageManager()
                    .getComponentEnabledSetting(componentName);
            return enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        }

        private final Preference.OnPreferenceChangeListener changeListenerLauncher = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Activity activity = getActivity();
                ComponentName alias = new ComponentName(
                        activity, "net.fypm.InstallerOpt.MainActivity-Alias");
                if (isLauncherIconVisible(alias)) {
                    activity.getPackageManager().setComponentEnabledSetting(alias, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                } else {
                    activity.getPackageManager().setComponentEnabledSetting(alias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                }
                return true;
            }
        };

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
                Activity activity = getActivity();
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
                Activity activity = getActivity();
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
                Activity activity = getActivity();
                if (newValue.equals(true)) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (isReadStorageAllowed()) {
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
                } else {
                    //MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putString(Common.PREF_BACKUP_APK_LOCATION, null).apply();
                }
                return true;
            }
        };

        private final Preference.OnPreferenceChangeListener changeListenerLauncher6 = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Activity activity = getActivity();
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

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
            super.onActivityResult(requestCode, resultCode, resultData);
            Activity activity = getActivity();
            if (requestCode == REQUEST_DIRECTORY) {
                if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                    String curBackupDir = MultiprocessPreferences.getDefaultSharedPreferences(activity).getString(Common.PREF_BACKUP_APK_LOCATION, null);
                    MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putString(Common.PREF_BACKUP_APK_LOCATION_OLD, curBackupDir).apply();
                    String newBackupDir = resultData.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
                    long curBackupDirSize = Stats.getFileSize(new File(curBackupDir));
                    long availableSpace = Stats.getAvailableSpaceInBytes(newBackupDir);
                    if (availableSpace < curBackupDirSize) {
                        Toast.makeText(activity, R.string.free_space_message, Toast.LENGTH_LONG).show();
                        chooseBackupDir();
                    }
                    if (curBackupDir != null && curBackupDir != newBackupDir) {
                        MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putString(Common.PREF_BACKUP_APK_LOCATION, newBackupDir).apply();
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
                    if(!new_file.exists())
                        new_file.mkdir();

                    for(int i=0;i<old_files.size();i++){
                        File check = new File(newBackupDir,old_files.get(i));
                        if(!check.exists())
                            TmpList.add(old_files.get(i));
                    }

                    if (old_files.size() > 0)
                        new AsyncCopy(activity, newBackupDir, TmpList).execute("");
                    else
                        Toast.makeText(getApplicationContext(), R.string.no_old_files_message, Toast.LENGTH_LONG).show();
                }
            }
            if (requestCode == REQUEST_REBOOT) {
                if (resultCode == Reboot.RESULT_CANCELED) {
                    chooseBackupDir();
                }
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            //Checking the request code of our request
            if (requestCode == REQUEST_WRITE_STORAGE) {
                Activity activity = getActivity();
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
            Activity activity = getActivity();
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
            Activity activity = getActivity();
            int result = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

            //If permission is granted returning true
            if (result == PackageManager.PERMISSION_GRANTED)
                return true;

            //If permission is not granted returning false
            return false;
        }

        //Requesting permission
        private void requestStoragePermission() {
            Activity activity = getActivity();
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //Explain here why you need this permission
                Toast.makeText(activity, R.string.write_permission_message, Toast.LENGTH_LONG).show();
            }
            //And finally ask for the permission
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
    }
}
