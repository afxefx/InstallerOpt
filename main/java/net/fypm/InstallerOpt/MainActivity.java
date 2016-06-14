package net.fypm.InstallerOpt;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;
import java.io.File;


public class MainActivity extends Activity {

    @SuppressWarnings({"deprecation"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean enableDark = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean("enable_darkui", false);
        if (enableDark) {
            setTheme(R.style.AppThemeDark);
        }
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;
        int oldVersionCode = MultiprocessPreferences.getDefaultSharedPreferences(MainActivity.this).getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);
        if (oldVersionCode < 538) {
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

    @SuppressLint("ValidFragment")
    public class PrefsFragment extends PreferenceFragment {

        boolean stateOfClose;
        boolean stateOfLaunch;

        @SuppressWarnings("deprecation")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Activity activity = getActivity();
            stateOfClose = MultiprocessPreferences.getDefaultSharedPreferences(activity).getBoolean("enable_auto_install_close", false);
            stateOfLaunch = MultiprocessPreferences.getDefaultSharedPreferences(activity).getBoolean("enable_auto_install_launch", false);
            if (stateOfClose && stateOfLaunch) {
                MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean("enable_auto_install_close", false).apply();
                MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean("enable_auto_install_launch", false).apply();
            }
            checkFirstRun();
            getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.prefs);
            findPreference("enable_hide_icon").setOnPreferenceChangeListener(changeListenerLauncher);
            findPreference("enable_darkui").setOnPreferenceChangeListener(changeListenerLauncher2);
            findPreference("enable_auto_install_close").setOnPreferenceChangeListener(changeListenerLauncher3);
            findPreference("enable_auto_install_launch").setOnPreferenceChangeListener(changeListenerLauncher4);
            //findPreference("enabled_version").setOnPreferenceChangeListener(changeListenerLauncher5);
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

            final String PREF_VERSION_CODE_KEY = "version_code";
            final int DOESNT_EXIST = -1;

            int currentVersionCode = 0;
            try {
                currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            } catch (android.content.pm.PackageManager.NameNotFoundException e) {
                // handle exception
                Main.xlog_start("checkFirstRun - Current version code not found");
                Main.xlog("", e);
                Main.xlog_end("checkFirstRun - Current version code not found");
                return;
            }

            int savedVersionCode = MultiprocessPreferences.getDefaultSharedPreferences(this.getActivity()).getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

            if (currentVersionCode == savedVersionCode) {

                // This is just a normal run
                return;

            } else if (savedVersionCode == DOESNT_EXIST) {

                // New install or shared preferences cleared
                Toast.makeText(getActivity(), "First run detected, resetting preferences to avoid conflicts", Toast.LENGTH_LONG).show();
                Activity activity = getActivity();
                ComponentName alias = new ComponentName(
                        activity, "net.fypm.InstallerOpt.MainActivity-Alias");
                activity.getPackageManager().setComponentEnabledSetting(alias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean("enabled_hide_icon", false).apply();
                //Fix below absolute path to use relative
                String dirPath = activity.getFilesDir().getParentFile().getPath() + "/shared_prefs/";
                File sharedPrefsFileOld = new File(dirPath, "prefs.xml");
                if (sharedPrefsFileOld.exists()) {
                    boolean deleted = sharedPrefsFileOld.delete();
                    if (deleted) {
                        //Main.xlog_start("checkFirstRun - First run or data has been cleared");
                        //Main.xlog("Old preference file found and deleted", null);
                        //Main.xlog_end("checkFirstRun - First run or data has been cleared");
                        //XposedBridge.log("Old preference file found and deleted");
                    }
                }

            } else if (currentVersionCode > savedVersionCode) {

                // This is an upgrade
                Toast.makeText(getActivity(), "Module has been updated", Toast.LENGTH_LONG).show();

            }

            MultiprocessPreferences.getDefaultSharedPreferences(this.getActivity()).edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();

        }

        private boolean isLauncherIconVisible(ComponentName componentName) {
            int enabledSetting = getPackageManager()
                    .getComponentEnabledSetting(componentName);
            return enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        }

        /*// Send an Intent with an action named "my-event".
        private void sendMessage() {
            Intent intent = new Intent("my-event");
            // add data
            intent.putExtra("message", "data");
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
        }*/

        private final Preference.OnPreferenceChangeListener changeListenerLauncher = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Activity activity = getActivity();
               /* PackageManager packageManager = activity.getPackageManager();
                int state = (Boolean) newValue ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;*/
                ComponentName alias = new ComponentName(
                        activity, "net.fypm.InstallerOpt.MainActivity-Alias");
                /*activity.getPackageManager().setComponentEnabledSetting(alias, state,
                        PackageManager.DONT_KILL_APP);*/
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
                if (newValue.equals(true) ) {
                    findPreference("enable_auto_install_launch").setEnabled(false);
                    MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean("enable_auto_install_launch", false).apply();
                } else {
                    findPreference("enable_auto_install_launch").setEnabled(true);
                }
                return true;
            }
        };

        private final Preference.OnPreferenceChangeListener changeListenerLauncher4 = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Activity activity = getActivity();
                if (newValue.equals(true) ) {
                    findPreference("enable_auto_install_close").setEnabled(false);
                    MultiprocessPreferences.getDefaultSharedPreferences(activity).edit().putBoolean("enable_auto_install_close", false).apply();
                } else {
                    findPreference("enable_auto_install_close").setEnabled(true);
                }
                return true;
            }
        };

        /*private final Preference.OnPreferenceChangeListener changeListenerLauncher5 = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                initPrefs();
                return true;
            }
        };*/

    }
}