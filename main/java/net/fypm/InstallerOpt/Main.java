package net.fypm.InstallerOpt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AndroidAppHelper;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Hashtable;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {

    public XC_MethodHook appInfoHook;
    public XC_MethodHook autoCloseInstallHook;
    public XC_MethodHook autoHideInstallHook;
    public XC_MethodHook autoInstallHook;
    public XC_LayoutInflated autoInstallHook2;
    public XC_MethodHook autoCloseUninstallHook;
    public XC_MethodHook autoUninstallHook;
    //public XC_MethodHook bootCompletedHook;
    public XC_MethodHook checkDuplicatedPermissionsHook;
    public XC_MethodHook checkPermissionsHook;
    public XC_MethodHook checkSdkVersionHook;
    public XC_MethodHook checkSignaturesHook;
    public XC_MethodHook debugAppsHook;
    public XC_MethodHook deletePackageHook;
    public XC_MethodHook deviceAdminsHook;
    public XC_MethodHook disableChangerHook;
    public XC_MethodHook disableUserAppsHook;
    public XC_MethodHook externalSdCardAccessHook; // 4.4 - 5.0
    public XC_MethodHook externalSdCardAccessHook2; // 6.0 and up
    //public XC_MethodHook getPackageInfoHook;
    public XC_MethodHook hideAppCrashesHook;
    public XC_MethodHook initAppStorageSettingsButtonsHook;
    public XC_MethodHook initAppOpsDetailsHook;
    public XC_MethodHook initUninstallButtonsHook;
    public XC_MethodHook installPackageHook;
    public XC_MethodHook scanPackageHook;
    public XC_MethodHook showButtonsHook;
    public XC_MethodHook systemAppsHook;
    //public XC_MethodHook updatePrefsHook;
    public XC_MethodHook unknownAppsHook;
    public XC_MethodReplacement unknownAppsPrompt;
    public XC_MethodHook unknownAppsHookPrompt;
    public XC_MethodHook verifyAppsHook;
    public XC_MethodHook verifyJarHook;
    public XC_MethodHook verifySignatureHook;
    public XC_MethodHook verifySignaturesHook;
    public XC_MethodHook grantPermissionsBackButtonHook;


    public Class<?> disableChangerClass;
    public Context mContext;
    public TextView view;
    public static String backupDir;
    public static XSharedPreferences prefs;
    public static boolean autoInstallCancelled;
    public static boolean autoInstallCancelOverride;
    public static boolean backupApkFiles;
    public static boolean bootCompleted;
    public static boolean checkDuplicatedPermissions;
    //public static boolean checkLuckyPatcher;
    public static boolean checkPermissions;
    public static boolean checkSdkVersion;
    public static boolean checkSignatures;
    //public static boolean confirmCheckSignatures;
    public static boolean debugApps;
    public static boolean deleteApkFiles;
    public static boolean deviceAdmins;
    public static boolean disableCheckSignatures;
    public static boolean disableSystemApps;
    public static boolean disableUserApps;
    public static boolean downgradeApps;
    public static boolean enableAppStorageSettingsButtons;
    public static boolean enableAutoHideInstall;
    public static boolean enableAutoInstall;
    public static boolean enableAutoCloseInstall;
    public static boolean enableAutoLaunchInstall;
    public static boolean enableAutoCloseUninstall;
    public static boolean enableAutoUninstall;
    public static boolean enableDebug;
    public static boolean enableLaunchApp;
    public static boolean enableNotifications;
    public static boolean enableOpenAppOps;
    public static boolean enablePackageName;
    public static boolean enablePlay;
    public static boolean enableVersion;
    public static boolean enableVersionInline;
    public static boolean enableVersionCode;
    public static boolean enableVersionToast;
    public static boolean enableVibrateDevice;
    public static boolean externalSdCardFullAccess;
    public static boolean forwardLock;
    public static boolean hideAppCrashes;
    public static boolean installAppsOnExternal;
    public static boolean installBackground;
    public static boolean installShell;
    public static boolean installUnknownApps;
    public static boolean installUnknownAppsPrompt;
    public static boolean keepAppsData;
    //public static boolean prefsChanged;
    public static boolean showButtons;
    public static boolean uninstallBackground;
    public static boolean uninstallSystemApps;
    public static boolean verifyApps;
    public static boolean verifyJar;
    public static boolean verifySignature;
    public static boolean rom_CM;
    public static boolean rom_TW;
    //public static long prefsModifiedTime;
    private static final String TAG = "InstallerOpt";

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {

        /*File bootfile = new File(Environment.getExternalStorageDirectory() + File.separator + "bootfile");
        if(bootfile.exists())
        {
            xlog("Bootfile exist, deleting", null);
            bootfile.delete();
        }*/


        xlog_start("ROM Detection");
        File frameworkTW = new File("/system/framework/twframework-res.apk");
        if (frameworkTW.exists()) {
            rom_TW = true;
            xlog("TW rom", rom_TW);
        } else {
            rom_CM = isCyanogenMod();
            if (rom_CM) {
                xlog("CM rom", rom_CM);
            } else {
                xlog("Neither TW nor CM rom detected", null);
            }
        }
        xlog_end("ROM Detection");

        disableCheckSignatures = true;
        //prefsChanged = false;

        xlog_start("XSharedPreferences - Init");
        try {
            prefs = new XSharedPreferences(Main.class.getPackage().getName());
            prefs.makeWorldReadable();
            prefs.reload();
            updatePrefs();
            xlog("Success", null);
        } catch (Throwable e) {
            xlog("Error initializing shared preferences", e);
        }
        xlog_end("XSharedPreferences - Init");

        xlog_start("Signature Checking and Verification Overview");
        xlog("Disable signature check status", checkSignatures);
        xlog("Disable application verification status", verifySignature);
        xlog_end("Signature Checking and Verification Overview");


        appInfoHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                enablePlay = getPref(Common.PREF_ENABLE_OPEN_APP_GOOGLE_PLAY, getInstallerOptContext());
                enablePackageName = getPref(Common.PREF_ENABLE_SHOW_PACKAGE_NAME, getInstallerOptContext());
                enableLaunchApp = getPref(Common.PREF_ENABLE_LAUNCH_APP, getInstallerOptContext());
                uninstallSystemApps = getPref(Common.PREF_ENABLE_UNINSTALL_SYSTEM_APP, getInstallerOptContext());
                /*prefsModifiedTime = getPrefLong("prefsModifiedTime", getInstallerOptContext());
                if (prefsModifiedTime + 900000 > SystemClock.elapsedRealtime()) {
                    setPref(mContext, Common.PREF_MODIFIED_PREFERENCES, true, 0, 0, null);
                }*/
                PackageInfo pkgInfo = (PackageInfo) param.args[0];
                Object view;
                Resources mResources;

                if (Build.VERSION.SDK_INT >= 23) {
                    view = XposedHelpers.getObjectField(param.thisObject, "mHeader");
                    mResources = (Resources) XposedHelpers.callMethod(param.thisObject, "getResources");
                } else {
                    view = XposedHelpers.getObjectField(param.thisObject, "mRootView");
                    mResources = ((View) view).getResources();
                }
                View appSnippet;
                TextView appVersion;
                int iconId = 0;
                int labelId = 0;
                int appSnippetId = mResources.getIdentifier("app_snippet", "id", Common.SETTINGS_PKG);

                if (Build.VERSION.SDK_INT >= 23) {
                    int appVersionId = mResources.getIdentifier("summary", "id", "android");
                    appVersion = (TextView) XposedHelpers.callMethod(view, "findViewById", appVersionId);
                    appSnippet = (View) XposedHelpers.callMethod(view, "findViewById", appSnippetId);
                    iconId = mResources.getIdentifier("icon", "id", "android");
                    labelId = mResources.getIdentifier("title", "id", "android");
                } else {
                    appVersion = (TextView) XposedHelpers.getObjectField(param.thisObject, "mAppVersion");
                    appSnippet = ((View) view).findViewById(appSnippetId);
                    iconId = mResources.getIdentifier("app_icon", "id", Common.SETTINGS_PKG);
                    labelId = mResources.getIdentifier("app_name", "id", Common.SETTINGS_PKG);
                }
                ImageView appIcon = (ImageView) appSnippet.findViewById(iconId);
                final TextView appLabel = (TextView) appSnippet.findViewById(labelId);
                String version = appVersion.getText().toString();
                //final Resources res = getInstallerOptContext().getResources();
                final String apkFile = pkgInfo.applicationInfo.sourceDir;
                final String packageName = pkgInfo.packageName;
                final String appName = appLabel.getText().toString();
                if (enableDebug && isModuleEnabled()) {
                    xlog_start("appInfoHook");
                    xlog("Hooked setAppLabelAndIcon", null);
                    xlog("Current application", mContext.toString());
                    xlog("Current package info", pkgInfo.toString());
                    xlog("Current package APK", apkFile);
                    xlog("Current package name", packageName);
                    xlog("Current version name", version);
                    xlog("Application label", appName);
                    xlog_end("appInfoHook");
                }

                if (enablePlay && isModuleEnabled()) {
                    appIcon.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            String uri = "market://details?id=" + packageName;
                            Intent openGooglePlay = new Intent(
                                    Intent.ACTION_VIEW, Uri.parse(uri));
                            openGooglePlay
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(openGooglePlay);
                            return true;
                        }
                    });
                }

                if (enableLaunchApp && isModuleEnabled()) {
                    appIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent launchIntent = mContext.getPackageManager()
                                    .getLaunchIntentForPackage(packageName);
                            if (launchIntent != null) {
                                launchIntent
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(launchIntent);
                            }
                        }
                    });
                }

                if (enablePackageName && isModuleEnabled()) {
                    appLabel.setTag(0);
                    appLabel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final int status = (Integer) v.getTag();
                            if (status == 0) {
                                appLabel.setText(packageName);
                                appLabel.setLines(1);
                                appLabel.setHorizontallyScrolling(true);
                                appLabel.setMarqueeRepeatLimit(-1);
                                appLabel.setSelected(true);
                                v.setTag(1);
                            } else {
                                appLabel.setText(appName);
                                appLabel.setLines(1);
                                appLabel.setHorizontallyScrolling(true);
                                appLabel.setMarqueeRepeatLimit(-1);
                                appLabel.setSelected(true);
                                v.setTag(0);
                            }
                        }
                    });
                }

                if (uninstallSystemApps && isModuleEnabled()) {
                    View.OnLongClickListener uninstallSystemApp = new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            //Toast.makeText(mContext, "Long click detected",
                            //        Toast.LENGTH_LONG).show();
                            try {
                                uninstallSystemApp(packageName);
                            } catch (Throwable e) {
                                xlog_start("appInfoHook - uninstallSystemApps");
                                xlog("", e);
                                xlog_end("appInfoHook - uninstallSystemApps");
                            }
                            return true;
                        }
                    };
                    Button mUninstallButton = (Button) XposedHelpers
                            .getObjectField(param.thisObject,
                                    "mUninstallButton");
                    /*Button mSpecialDisableButton = (Button) XposedHelpers
                            .getObjectField(param.thisObject,
                                    "mSpecialDisableButton");*/
                    mUninstallButton.setOnLongClickListener(uninstallSystemApp);
                    /*mSpecialDisableButton
                            .setOnLongClickListener(uninstallSystemApp);*/
                }
            }
        };

        autoCloseInstallHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                enableAutoCloseInstall = getPref(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL, getInstallerOptContext());
                enableAutoLaunchInstall = getPref(Common.PREF_ENABLE_AUTO_LAUNCH_INSTALL, getInstallerOptContext());
                enableNotifications = getPref(Common.PREF_ENABLE_NOTIFICATIONS, getInstallerOptContext());
                enableOpenAppOps = getPref(Common.PREF_ENABLE_OPEN_APP_OPS, getInstallerOptContext());
                deleteApkFiles = getPref(Common.PREF_ENABLE_DELETE_APK_FILE_INSTALL, getInstallerOptContext());
                enableVibrateDevice = getPref(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL_VIBRATE, getInstallerOptContext());
                Resources res = getInstallerOptContext().getResources();
                Button mLaunch = (Button) XposedHelpers.getObjectField(
                        XposedHelpers.getSurroundingThis(param.thisObject),
                        "mLaunchButton");
                Uri packageUri = (Uri) XposedHelpers.getObjectField(
                        XposedHelpers.getSurroundingThis(param.thisObject),
                        "mPackageURI");

                Message msg = (Message) param.args[0];
                boolean installedApp = false;
                if (msg != null) {
                    installedApp = (msg.arg1 == Common.INSTALL_SUCCEEDED);
                }
                if (enableAutoLaunchInstall && isModuleEnabled()) {
                    if (installedApp && mLaunch != null) {
                        mLaunch.performClick();
                    }
                }

                if (enableAutoCloseInstall && isModuleEnabled()) {
                    Button mDone = (Button) XposedHelpers.getObjectField(
                            XposedHelpers.getSurroundingThis(param.thisObject),
                            "mDoneButton");

                    if (installedApp && mDone != null) {
                        mDone.performClick();
                        String appInstalledText = "";
                        Resources resources = mContext.getResources();
                        appInstalledText = (String) resources.getText(resources
                                .getIdentifier("install_done", "string",
                                        Common.PACKAGEINSTALLER_PKG));
                        if (enableDebug && isModuleEnabled()) {
                            xlog_start("autoCloseInstallHook");
                            xlog("msg", msg);
                            xlog("mDone", mDone);
                            xlog("appInstalledText", appInstalledText);
                            xlog_end("autoCloseInstallHook");
                        }
                        if (!appInstalledText.isEmpty()) {
                            /*Toast.makeText(mContext, appInstalledText,
                                    Toast.LENGTH_LONG).show();*/
                            if (enableNotifications && isModuleEnabled()) {
                                postNotification(res.getString(R.string.install_status_success), packageUri.getLastPathSegment() + res.getString(R.string.install_status_success_cont), "");
                            }
                            if (enableVibrateDevice && isModuleEnabled()) {
                                try {
                                    vibrateDevice(500);
                                    xlog("Vibrate on install successful", null);
                                } catch (Exception e) {
                                    xlog("Unable to vibrate on install", e);
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        /*Toast.makeText(mContext, "App not installed\n\nError code: " + msg.arg1,
                                Toast.LENGTH_LONG).show();*/
                        if (enableNotifications && isModuleEnabled()) {
                            postNotification(res.getString(R.string.install_status_failure), packageUri.getLastPathSegment() + res.getString(R.string.install_status_failure_cont), res.getString(R.string.install_status_failure_error_code) + msg.arg1);
                        }
                        if (enableDebug && isModuleEnabled()) {
                            xlog_start("autoCloseInstallHook");
                            xlog("Install failed", msg);
                            xlog("msg", msg);
                            xlog_end("autoCloseInstallHook");
                        }
                    }
                }

                if (deleteApkFiles && installedApp && isModuleEnabled()) {
                    String apkFile = packageUri.getPath();
                    deleteApkFile(apkFile);
                    if (enableDebug && isModuleEnabled()) {
                        xlog_start("autoCloseInstallHook - deleteApkFiles");
                        xlog("APK file: ", apkFile);
                        xlog_end("autoCloseInstallHook - deleteApkFiles");
                    }
                }

                if (enableOpenAppOps && Common.JB_MR2_NEWER && isModuleEnabled()) {
                    ApplicationInfo appInfo = (ApplicationInfo) XposedHelpers
                            .getObjectField(XposedHelpers
                                            .getSurroundingThis(param.thisObject),
                                    "mAppInfo");
                    if (appInfo == null) {
                        return;
                    }
                    String packageName = appInfo.packageName;
                    Intent openAppInAppOps = new Intent();
                    boolean useSettingsApp = true;

                    try {
                        PackageInfo pkgInfo = mContext.getPackageManager()
                                .getPackageInfo(Common.APPOPSXPOSED_PKG, 0);
                        if (pkgInfo.versionCode >= 12100) {
                            openAppInAppOps.setClassName(
                                    Common.APPOPSXPOSED_PKG,
                                    Common.APPOPSXPOSED_APPOPSACTIVITY);
                            useSettingsApp = false;
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        if (enableDebug && isModuleEnabled()) {
                            xlog_start("enableOpenAppOps - NameNotFoundException");
                            xlog("", e);
                            xlog_end("enableOpenAppOps - NameNotFoundException");
                        }
                    }

                    if (useSettingsApp && isModuleEnabled()) {
                        openAppInAppOps
                                .setAction(Common.ACTION_APP_OPS_SETTINGS);
                    }

                    Bundle args = new Bundle();
                    args.putString(Common.PACKAGE, packageName);

                    openAppInAppOps.putExtra(
                            PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                            Common.APPOPSDETAILS);
                    openAppInAppOps
                            .putExtra(
                                    PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,
                                    args);
                    openAppInAppOps.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        mContext.startActivity(openAppInAppOps);
                    } catch (ActivityNotFoundException e) {
                        if (enableDebug && isModuleEnabled()) {
                            xlog_start("enableOpenAppOps - ActivityNotFoundException");
                            xlog("", e);
                            xlog_end("enableOpenAppOps - ActivityNotFoundException");
                        }
                    }

                }

                if (enableDebug && isModuleEnabled()) {
                    xlog_start("autoCloseInstallHook");
                    xlog("Auto close install status", enableAutoCloseInstall);
                    xlog("Auto launch install status", enableAutoLaunchInstall);
                    xlog("mLaunch", mLaunch);
                    xlog("msg", msg);
                    xlog("installedApp status", installedApp);
                    xlog_end("autoCloseInstallHook");
                }
            }
        };

        autoHideInstallHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                enableAutoHideInstall = getPref(Common.PREF_ENABLE_AUTO_HIDE_INSTALL, getInstallerOptContext());
                Activity packageInstaller = (Activity) param.thisObject;
                if (!autoInstallCancelled && enableAutoHideInstall && isModuleEnabled()) {
                    packageInstaller.onBackPressed();
                }
            }
        };

        autoInstallHook = new XC_MethodHook() {
            @Override
            public void afterHookedMethod(MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                enableVersion = getPref(Common.PREF_ENABLE_SHOW_VERSION, getInstallerOptContext());
                enableVersionCode = getPref(Common.PREF_ENABLE_SHOW_VERSION_CODE, getInstallerOptContext());
                enableVersionInline = getPref(Common.PREF_ENABLE_SHOW_VERSION_INLINE, getInstallerOptContext());
                enableVersionToast = getPref(Common.PREF_ENABLE_SHOW_VERSION_TOAST, getInstallerOptContext());
                enableAutoInstall = getPref(Common.PREF_ENABLE_AUTO_INSTALL, getInstallerOptContext());
                autoInstallCancelOverride = getPref(Common.PREF_ENABLE_AUTO_INSTALL_CANCEL_OVERRIDE, getInstallerOptContext());
                //Add below two in prefs
                //checkSignatures = getPref("enabled_disable_sig_check", getInstallerOptContext());
                //confirmCheckSignatures = getPref("enabled_confirm_check_signatures", getInstallerOptContext());
                //int msg = (int) XposedHelpers.getObjectField(param.thisObject, "msg");
                int newCode = 0;
                int currentCode = 0;
                PackageInfo pi = null;
                PackageInfo pi2 = null;
                String versionInfo = null;
                String newVersion = null;
                String currentVersion = null;
                String versionCode = null;
                Button mOk = (Button) XposedHelpers.getObjectField(
                        param.thisObject, "mOk");
                PackageManager mPm = mContext.getPackageManager();
                PackageInfo mPkgInfo = (PackageInfo) XposedHelpers
                        .getObjectField(param.thisObject, "mPkgInfo");
                Resources res = getInstallerOptContext().getResources();
                LayoutInflater inflater = LayoutInflater.from(getInstallerOptContext());
                GridLayout layoutVersion = (GridLayout) inflater.inflate(res.getLayout(R.layout.version_layout), null);
                Toast toast = new Toast(mContext);
                toast.setDuration(Toast.LENGTH_LONG);
                String packageName = mPkgInfo.packageName;
                newVersion = mPkgInfo.versionName;
                versionInfo = String.format("%s %25s", res.getString(R.string.new_version), newVersion);
                try {
                    pi = mPm.getPackageInfo(packageName, 0);
                    currentVersion = pi.versionName;
                    versionInfo += String.format("%s %20s", res.getString(R.string.current_version_inline), currentVersion);
                } catch (PackageManager.NameNotFoundException e) {
                    if (enableDebug && isModuleEnabled()) {
                        xlog_start("autoInstallHook - Current version not found");
                        xlog("", e);
                        xlog_end("autoInstallHook - Current version not found");
                    }
                }
                if (enableVersion && !enableVersionCode && isModuleEnabled()) {
                    if (enableVersionInline && isModuleEnabled()) {
                        if (view != null) {
                            CharSequence temp = view.getText();
                            temp = temp + "\n\n" + versionInfo + "\n";
                            view.setText(temp);
                        }
                    }

                    if (enableVersionToast && isModuleEnabled()) {
                        //Toast.makeText(mContext, versionInfo, Toast.LENGTH_LONG).show();
                        if (currentVersion != null) {
                            ((TextView) layoutVersion.findViewById(R.id.current_version)).setText(currentVersion);
                        } else {
                            ((TextView) layoutVersion.findViewById(R.id.current_version)).setText(res.getString(R.string.not_available_text));
                        }
                        ((TextView) layoutVersion.findViewById(R.id.new_version)).setText(newVersion);
                        toast.setView(layoutVersion);
                        toast.show();
                    }
                }
                newCode = mPkgInfo.versionCode;
                versionCode = String.format("%10s %19d", res.getString(R.string.new_version_code), newCode);
                try {
                    pi2 = mPm.getPackageInfo(packageName, 0);
                    currentCode = pi2.versionCode;
                    versionCode += String.format("%10s %14d", res.getString(R.string.current_version_code_inline), currentCode);
                } catch (PackageManager.NameNotFoundException e) {
                    if (enableDebug && isModuleEnabled()) {
                        xlog_start("autoInstallHook - Current version code not found");
                        xlog("", e);
                        xlog_end("autoInstallHook - Current version code not found");
                    }
                }
                if (enableVersionCode && !enableVersion && isModuleEnabled()) {
                    if (enableVersionInline && isModuleEnabled()) {
                        if (view != null) {
                            CharSequence temp = view.getText();
                            temp = temp
                                    + "\n\n"
                                    + versionCode
                                    + "\n";
                            view.setText(temp);
                        }
                    }

                    if (enableVersionToast && isModuleEnabled()) {
                        //Toast.makeText(mContext, versionCode, Toast.LENGTH_LONG).show();
                        if (currentCode != 0) {
                            ((TextView) layoutVersion.findViewById(R.id.current_version_code)).setText(String.valueOf(currentCode));
                        } else {
                            ((TextView) layoutVersion.findViewById(R.id.current_version_code)).setText(res.getString(R.string.not_available_text));
                        }
                        ((TextView) layoutVersion.findViewById(R.id.new_version_code)).setText(String.valueOf(newCode));
                        toast.setView(layoutVersion);
                        toast.show();
                    }
                }

                if (enableDebug && isModuleEnabled()) {
                    xlog_start("autoInstallHook");
                    if (currentVersion != null) {
                        xlog("Current version", currentVersion);
                    }
                    xlog("New version", newVersion);
                    if (currentCode != 0) {
                        xlog("Current version code", currentCode);
                    }
                    xlog("New version code", newCode);
                    xlog("Current application", mContext.toString());
                    xlog("Current button", mOk.toString());
                    xlog("Current package info", mPkgInfo.toString());
                    xlog("Current package name", packageName);
                    xlog("Disable signature check status", checkSignatures);
                    xlog_end("autoInstallHook");
                }

                if (enableVersion && enableVersionCode && isModuleEnabled()) {
                    String versionAll = versionInfo + "\n\n" + versionCode;
                    if (enableVersionInline && isModuleEnabled()) {
                        if (view != null) {
                            CharSequence temp = view.getText();
                            temp = temp + "\n\n" + versionAll + "\n";
                            view.setText(temp);
                        }
                    }

                    if (enableVersionToast && isModuleEnabled()) {
                        //Toast.makeText(mContext, versionAll, Toast.LENGTH_LONG).show();
                        if (currentVersion != null) {
                            ((TextView) layoutVersion.findViewById(R.id.current_version)).setText(currentVersion);
                        } else {
                            ((TextView) layoutVersion.findViewById(R.id.current_version)).setText(res.getString(R.string.not_available_text));
                            //((TextView) layoutVersion.findViewById(R.id.current_version)).setVisibility(View.VISIBLE);
                        }
                        ((TextView) layoutVersion.findViewById(R.id.new_version)).setText(newVersion);
                        if (currentCode != 0) {
                            ((TextView) layoutVersion.findViewById(R.id.current_version_code)).setText(String.valueOf(currentCode));
                        } else {
                            ((TextView) layoutVersion.findViewById(R.id.current_version_code)).setText(res.getString(R.string.not_available_text));
                        }
                        ((TextView) layoutVersion.findViewById(R.id.new_version_code)).setText(String.valueOf(newCode));
                        toast.setView(layoutVersion);
                        toast.show();

                    }
                }

                if (enableAutoInstall && !autoInstallCancelOverride && isModuleEnabled()) {
                    if ((newVersion.equals(currentVersion) && newCode == currentCode) || newCode < currentCode) {
                        Toast.makeText(mContext, "Auto install cancelled due to matching version info and/or current version is newer than one being installed", Toast.LENGTH_LONG)
                                .show();
                        autoInstallCancelled = true;
                    } else {
                        autoInstallCancelled = false;
                        XposedHelpers.setObjectField(param.thisObject,
                                "mScrollView", null);
                        XposedHelpers.setBooleanField(param.thisObject,
                                "mOkCanInstall", true);
                        mOk.performClick();
                    }
                }

                /*if (confirmCheckSignatures && !checkSignatures) {
                    Intent confirmSignatureCheck = new Intent(
                            Common.ACTION_CONFIRM_CHECK_SIGNATURE);
                    confirmSignatureCheck.setPackage(Common.PACKAGE_NAME);
                    getInstallerOptContext().sendBroadcast(confirmSignatureCheck);

                }
                ScrollView mScrollView = (ScrollView) XposedHelpers.getObjectField(
                        param.thisObject, "mScrollView");
                TextView label = (TextView) XposedHelpers.getObjectField(
                        param.thisObject, "label");
                TextView label = new TextView(mContext);
                label.setText("blahblah");*/
                //mScrollView.addView(label);
                //setPref(getInstallerOptContext(), "app_version", null, 0, versionInfo);
                //setPref(getInstallerOptContext(), "app_version_code", null, 0, versionCode);
            }
        };


        autoInstallHook2 = new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                //mContext = AndroidAppHelper.currentApplication();
                //enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                //versionCode = getPrefString("app_version", getInstallerOptContext());
                //versionInfo = getPrefString("app_version_code", getInstallerOptContext());

                view = (TextView) liparam.view.findViewById(
                        liparam.res.getIdentifier("install_confirm_question", "id", Common.PACKAGEINSTALLER_PKG));
                /*CharSequence temp = view.getText();
                temp = "\n\n" + versionInfo + "\n\n" + versionCode;
                view.setText(temp);

                String versionInfo = null;
                String versionCode = null;*/

                //MultiprocessPreferences.edit(mContext).remove("app_version");
                //MultiprocessPreferences.edit(mContext).remove("app_version_code");
            }
        };

        autoCloseUninstallHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                //enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                enableAutoCloseUninstall = getPref(Common.PREF_ENABLE_AUTO_CLOSE_UNINSTALL, getInstallerOptContext());
                if (enableAutoCloseUninstall && isModuleEnabled()) {
                    Button mOk = (Button) XposedHelpers.getObjectField(
                            param.thisObject, "mOkButton");
                    if (mOk != null) {
                        mOk.performClick();
                    }
                }
            }
        };

        autoUninstallHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                //enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                enableAutoUninstall = getPref(Common.PREF_ENABLE_AUTO_UNINSTALL, getInstallerOptContext());
                if (enableAutoUninstall && isModuleEnabled()) {
                    if (Common.LOLLIPOP_NEWER) {
                        Activity packageInstaller = (Activity) param.thisObject;
                        packageInstaller.onBackPressed();
                        XposedHelpers.callMethod(param.thisObject,
                                "startUninstallProgress");
                    }
                }
            }

            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                //enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                enableAutoUninstall = getPref(Common.PREF_ENABLE_AUTO_UNINSTALL, getInstallerOptContext());
                if (enableAutoUninstall && isModuleEnabled()) {
                    try {
                        Button mOk = (Button) XposedHelpers.getObjectField(
                                param.thisObject, "mOk");
                        if (mOk != null) {
                            mOk.performClick();
                        }
                    } catch (NoSuchFieldError nsfe) {
                        xlog_start("autoUninstallHook");
                        xlog("Error caught", nsfe);
                        xlog_end("autoUninstallHook");
                    }
                }
            }
        };

        /*bootCompletedHook = new XC_MethodHook() {
            @Override
            public void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (isModuleEnabled()) {
                    Log.i(TAG, "bootCompletedHook: bootCompleted value before changing " + bootCompleted);
                    bootCompleted = true;
                    Log.i(TAG, "bootCompletedHook: bootCompleted after changing " + bootCompleted);
                *//*File bootfile = new File(Environment.getExternalStorageDirectory() + File.separator + "bootfile");
                bootfile.createNewFile();
                byte data=1;
                if(bootfile.exists())
                {
                    OutputStream fo = new FileOutputStream(bootfile);
                    fo.write(data);
                    fo.close();
                }*//*
                    //mContext = AndroidAppHelper.currentApplication();
                    //setPref(mContext, Common.PREF_MODIFIED_PREFERENCES, false, 0, 0, null);
                }
            }
        };*/

        checkDuplicatedPermissionsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    prefs.reload();
                    checkDuplicatedPermissions = prefs.getBoolean(Common.PREF_DISABLE_CHECK_DUPLICATED_PERMISSION, false);
                    enableDebug = prefs.getBoolean(Common.PREF_ENABLE_DEBUG, false);
                } catch (Throwable e) {
                    mContext = AndroidAppHelper.currentApplication();
                    checkDuplicatedPermissions = getPref(Common.PREF_DISABLE_CHECK_DUPLICATED_PERMISSION, getInstallerOptContext());
                    enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                }
                if (checkDuplicatedPermissions && isModuleEnabled()) {
                    if (enableDebug && isModuleEnabled()) {
                        xlog("Disable duplicate permissions check set to", checkDuplicatedPermissions);
                    }
                    param.setResult(true);
                    return;
                }
            }
        };

        checkPermissionsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    prefs.reload();
                    checkPermissions = prefs.getBoolean(Common.PREF_DISABLE_CHECK_PERMISSION, false);
                    enableDebug = prefs.getBoolean(Common.PREF_ENABLE_DEBUG, false);
                } catch (Throwable e) {
                    mContext = AndroidAppHelper.currentApplication();
                    checkPermissions = getPref(Common.PREF_DISABLE_CHECK_PERMISSION, getInstallerOptContext());
                    enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                }
                if (checkPermissions && isModuleEnabled()) {
                    /*if (enableDebug) {
                        xlog("Disable check permissions set to", checkPermissions);
                    }*/
                    param.setResult(PackageManager.PERMISSION_GRANTED);
                    return;
                }
            }
        };

        checkSdkVersionHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    prefs.reload();
                    checkSdkVersion = prefs.getBoolean(Common.PREF_DISABLE_CHECK_SDK_VERSION, false);
                    enableDebug = prefs.getBoolean(Common.PREF_ENABLE_DEBUG, false);
                } catch (Throwable e) {
                    mContext = AndroidAppHelper.currentApplication();
                    checkSdkVersion = getPref(Common.PREF_DISABLE_CHECK_SDK_VERSION, getInstallerOptContext());
                    enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                }
                if (checkSdkVersion && isModuleEnabled()) {
                    if (enableDebug && isModuleEnabled()) {
                        xlog("checkSdkVersion set to", checkSdkVersion);
                    }
                    XposedHelpers.setObjectField(param.thisObject,
                            "SDK_VERSION", Common.LATEST_ANDROID_RELEASE);
                }
            }
        };

        checkSignaturesHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    prefs.reload();
                    checkSignatures = prefs.getBoolean(Common.PREF_DISABLE_CHECK_SIGNATURE, false);
                } catch (Throwable e) {
                    mContext = AndroidAppHelper.currentApplication();
                    checkSignatures = getPref(Common.PREF_DISABLE_CHECK_SIGNATURE, getInstallerOptContext());
                }
                //xlog("disableCheckSignatures value in checkSignaturesHook", disableCheckSignatures);
                //xlog("checkSignatures value in checkSignaturesHook", checkSignatures);
                if (disableCheckSignatures && checkSignatures && isModuleEnabled()) {
                    /*xlog_start("checkSignaturesHook");
                    xlog("Disable signature checks set to", checkSignatures);
                    xlog_end("checkSignaturesHook");*/
                    param.setResult(PackageManager.SIGNATURE_MATCH);
                    return;
                }
            }
        };

        debugAppsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    prefs.reload();
                    debugApps = prefs.getBoolean(Common.PREF_ENABLE_DEBUG_APP, false);
                } catch (Throwable e) {
                    mContext = AndroidAppHelper.currentApplication();
                    debugApps = getPref(Common.PREF_ENABLE_DEBUG_APP, getInstallerOptContext());
                }
                int id = 5;
                int flags = (Integer) param.args[id];
                if (debugApps && isModuleEnabled()) {
                    if ((flags & Common.DEBUG_ENABLE_DEBUGGER) == 0) {
                        flags |= Common.DEBUG_ENABLE_DEBUGGER;
                    }
                }
                param.args[id] = flags;
            }
        };

        deletePackageHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                keepAppsData = getPref(Common.PREF_ENABLE_KEEP_APP_DATA, getInstallerOptContext());
                uninstallBackground = getPref(Common.PREF_DISABLE_UNINSTALL_BACKGROUND, getInstallerOptContext());
                int id = 3;
                int flags = (Integer) param.args[id];

                if (keepAppsData && isModuleEnabled() && (flags & Common.DELETE_KEEP_DATA) == 0) {
                    flags |= Common.DELETE_KEEP_DATA;
                }

                if (uninstallBackground && isModuleEnabled() && Binder.getCallingUid() == Common.ROOT_UID) {
                    param.setResult(null);
                    if (enableDebug && isModuleEnabled()) {
                        //Toast.makeText(mContext, "Background uninstall attempt blocked", Toast.LENGTH_LONG).show();
                        xlog_start("deletePackageHook - uninstallBackground");
                        xlog("Background uninstall attempt blocked", null);
                        xlog_end("deletePackageHook - uninstallBackground");
                    }
                }
                //if (isModuleEnabled()) {
                param.args[id] = flags;
                //}
            }
        };

        deviceAdminsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                prefs.reload();
                deviceAdmins = prefs.getBoolean(Common.PREF_ENABLE_UNINSTALL_DEVICE_ADMIN, false);
                if (deviceAdmins && isModuleEnabled()) {
                    if (enableDebug && isModuleEnabled()) {
                        xlog_start("deviceAdminsHook");
                        xlog("deviceAdmins set to", deviceAdmins);
                        xlog_end("deviceAdminsHook");
                    }
                    param.setResult(false);
                    return;
                }

            }
        };

        disableChangerHook = new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                disableUserApps = getPref(Common.PREF_ENABLE_DISABLE_USER_APPS, getInstallerOptContext());
                if (disableUserApps && isModuleEnabled()) {
                    Object v = param.args[0];
                    Object mUninstallButton = XposedHelpers
                            .getObjectField(param.thisObject,
                                    "mUninstallButton");

                    if (v == mUninstallButton) {
                        Object appEntry = XposedHelpers.getObjectField(
                                param.thisObject, "mAppEntry");
                        Object appEntryInfo = XposedHelpers.getObjectField(
                                appEntry, "info");
                        boolean appEntryInfoEnabled = (Boolean) XposedHelpers
                                .getObjectField(appEntryInfo, "enabled");

                        if (!appEntryInfoEnabled) {
                            Object disableChanger = XposedHelpers
                                    .newInstance(
                                            disableChangerClass,
                                            param.thisObject,
                                            appEntryInfo,
                                            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
                            XposedHelpers.callMethod(disableChanger, "execute",
                                    (Object) null);
                            XposedHelpers.callMethod(param.thisObject, "refreshUi");
                            return null;
                        } else {
                            Object disableChanger = XposedHelpers
                                    .newInstance(
                                            disableChangerClass,
                                            param.thisObject,
                                            appEntryInfo,
                                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER);
                            XposedHelpers.callMethod(disableChanger, "execute",
                                    (Object) null);
                            XposedHelpers.callMethod(param.thisObject, "refreshUi");
                            return null;
                        }
                    }
                }
                return XposedBridge.invokeOriginalMethod(param.method,
                        param.thisObject, param.args);
            }
        };

        disableUserAppsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                disableUserApps = getPref(Common.PREF_ENABLE_DISABLE_USER_APPS, getInstallerOptContext());
                if (disableUserApps && isModuleEnabled()) {
                    if ((Integer) param.args[0] == 9)
                        param.args[0] = 7;
                }
            }
        };

        externalSdCardAccessHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                prefs.reload();
                externalSdCardFullAccess = prefs.getBoolean(
                        Common.PREF_ENABLE_EXTERNAL_SDCARD_FULL_ACCESS, true);
                String permission = (String) param.args[1];
                if (!externalSdCardFullAccess) {
                    return;
                }
                if (Common.PERM_WRITE_EXTERNAL_STORAGE
                        .equals(permission)
                        || Common.PERM_ACCESS_ALL_EXTERNAL_STORAGE
                        .equals(permission)) {
                    Class<?> process = XposedHelpers.findClass(
                            "android.os.Process", null);
                    int gid = (Integer) XposedHelpers.callStaticMethod(process,
                            "getGidForName", "media_rw");
                    Object permissions = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        permissions = XposedHelpers.getObjectField(
                                param.thisObject, "mPermissions");
                    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                        Object settings = XposedHelpers.getObjectField(
                                param.thisObject, "mSettings");
                        permissions = XposedHelpers.getObjectField(settings,
                                "mPermissions");
                    }
                    Object bp = XposedHelpers.callMethod(permissions, "get",
                            permission);
                    int[] bpGids = (int[]) XposedHelpers.getObjectField(bp,
                            "gids");
                    XposedHelpers.setObjectField(bp, "gids",
                            appendInt(bpGids, gid));
                }
            }
        };


        externalSdCardAccessHook2 = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                prefs.reload();
                externalSdCardFullAccess = prefs.getBoolean(
                        Common.PREF_ENABLE_EXTERNAL_SDCARD_FULL_ACCESS, true);
                if (!externalSdCardFullAccess) {
                    return;
                }
                Object extras = XposedHelpers.getObjectField(param.args[0], "mExtras");
                Object ps = XposedHelpers.callMethod(extras, "getPermissionsState");
                Object settings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
                Object permissions = XposedHelpers.getObjectField(settings, "mPermissions");
                boolean hasPermission = (boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", Common.PERM_WRITE_MEDIA_STORAGE);
                if (!hasPermission) {
                    Object permWriteMediaStorage = XposedHelpers.callMethod(permissions, "get",
                            Common.PERM_WRITE_MEDIA_STORAGE);
                    XposedHelpers.callMethod(ps, "grantInstallPermission", permWriteMediaStorage);
                }

            }
        };

        /*getPackageInfoHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                *//*mContext = AndroidAppHelper.currentApplication();
                try {
                    checkLuckyPatcher = getPref(Common.PREF_DISABLE_CHECK_LUCKY_PATCHER, getInstallerOptContext());
                } catch (Throwable e) {
                    checkLuckyPatcher = prefs.getBoolean(Common.PREF_DISABLE_CHECK_LUCKY_PATCHER, false);
                }
                if (checkLuckyPatcher && isModuleEnabled()) {
                    String packageName = (String) param.args[0];
                    int uid = Binder.getCallingUid();
                    String caller = (String) XposedHelpers.callMethod(
                            param.thisObject, "getNameForUid", uid);
                    if (uid != Common.SYSTEM_UID) {
                        if (Common.LUCKYPATCHER_PKG.equals(packageName)
                                && !Common.LUCKYPATCHER_PKG.equals(caller)) {
                            param.args[0] = Common.EMPTY_STRING;
                        }
                    }
                }*//*
            }
        };*/

        grantPermissionsBackButtonHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //setResultAndFinish()
                XposedHelpers.callMethod(param.thisObject, "setResultAndFinish");

            }
        };

        hideAppCrashesHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    prefs.reload();
                    enableDebug = prefs.getBoolean(Common.PREF_ENABLE_DEBUG, false);
                    hideAppCrashes = prefs.getBoolean(Common.PREF_ENABLE_HIDE_APP_CRASHES, false);
                    if (enableDebug && isModuleEnabled()) {
                        Log.i(TAG, "hideAppCrashes set via shared prefs");
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "hideAppCrashes error via shared prefs: ", e);
                }
                if (hideAppCrashes && isModuleEnabled()) {
                    try {
                        if (enableDebug && isModuleEnabled()) {
                            xlog_start("hideAppCrashesHook");
                            xlog("hideAppCrashes set to", hideAppCrashes);
                            xlog("App crashed", param.args[3]);
                            xlog_end("hideAppCrashesHook");
                        }
                        XposedHelpers.setObjectField(param.thisObject,
                                "DISMISS_TIMEOUT", 0);
                        //XposedHelpers.callMethod(param.thisObject, "dismiss");
                    } catch (Throwable e) {
                        xlog_start("hideAppCrashesHook");
                        xlog("hideAppCrashes", e);
                        xlog_end("hideAppCrashesHook");
                    }
                }
            }
        };

        initAppStorageSettingsButtonsHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                enableAppStorageSettingsButtons = getPref(Common.PREF_ENABLE_APP_STORAGE_BUTTONS, getInstallerOptContext());
                if (enableAppStorageSettingsButtons && isModuleEnabled()) {
                    if (enableDebug && isModuleEnabled()) {
                        xlog_start("initAppStorageSettingsButtonsHook");
                        xlog("Hooked initAppStorageSettingsButtonsHook", null);
                        xlog_end("initAppStorageSettingsButtonsHook");
                    }
                    Button mClearDataButton = (Button) XposedHelpers
                            .getObjectField(param.thisObject,
                                    "mClearDataButton");
                    XposedHelpers.callMethod(mClearDataButton,
                            "setOnClickListener", param.thisObject);
                    Button mClearCacheButton = (Button) XposedHelpers
                            .getObjectField(param.thisObject,
                                    "mClearCacheButton");
                    XposedHelpers.callMethod(mClearCacheButton,
                            "setOnClickListener", param.thisObject);
                    mClearDataButton.setEnabled(true);
                    mClearCacheButton.setEnabled(true);
                }
            }
        };

        initAppOpsDetailsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(false);
                return;
            }
        };

        initUninstallButtonsHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                disableUserApps = getPref(Common.PREF_ENABLE_DISABLE_USER_APPS, getInstallerOptContext());
                if (disableUserApps && isModuleEnabled()) {
                    if (enableDebug && isModuleEnabled()) {
                        xlog_start("initUninstallButtonsHook");
                        xlog("Hooked initUninstallButtons", null);
                        xlog_end("initUninstallButtonsHook");
                    }
                    Object appEntry = XposedHelpers.getObjectField(
                            param.thisObject, "mAppEntry");
                    final Object appEntryInfo = XposedHelpers.getObjectField(
                            appEntry, "info");
                    Object appPackageInfo = XposedHelpers.getObjectField(
                            param.thisObject, "mPackageInfo");
                    final String appPackageName = (String) XposedHelpers.getObjectField(
                            appPackageInfo, "packageName");
                    boolean appEntryInfoEnabled = (Boolean) XposedHelpers
                            .getObjectField(appEntryInfo, "enabled");
                    int appEntryInfoFlags = (Integer) XposedHelpers
                            .getObjectField(appEntryInfo, "flags");
                    boolean isSystem = false;
                    if ((appEntryInfoFlags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        isSystem = true;
                    }
                    if (!isSystem) {
                        Button mUninstallButton = (Button) XposedHelpers
                                .getObjectField(param.thisObject,
                                        "mUninstallButton");
                        XposedHelpers.callMethod(mUninstallButton,
                                "setOnClickListener", param.thisObject);
                        mUninstallButton.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                /*Toast.makeText(mContext, "Long click detected, now figure out the uninstall Jerry",
                                        Toast.LENGTH_LONG).show();*/
                                try {
                                    XposedHelpers.callMethod(param.thisObject, "uninstallPkg", appPackageName, true, false);
                                } catch (Throwable e) {
                                    xlog_start("initUninstallButtonsHook - mUninstallButton onLongClick");
                                    xlog("", e);
                                    xlog_end("initUninstallButtonsHook - mUninstallButton onLongClick");
                                }
                                return true;
                            }
                        });
                        Resources resources = (Resources) XposedHelpers
                                .callMethod(param.thisObject, "getResources");
                        String textDisable = (String) resources
                                .getText(resources.getIdentifier(
                                        "disable_text", "string",
                                        Common.SETTINGS_PKG));
                        if (textDisable == null) {
                            textDisable = getInstallerOptContext().getResources()
                                    .getString(R.string.disable);
                        }
                        String textEnable = (String) resources
                                .getText(resources.getIdentifier("enable_text",
                                        "string", Common.SETTINGS_PKG));
                        if (textEnable == null) {
                            textEnable = getInstallerOptContext().getResources()
                                    .getString(R.string.enable);
                        }

                        if (appEntryInfoEnabled) {
                            mUninstallButton.setText(textDisable);
                            /*Toast.makeText(mContext, "App is enabled",
                                    Toast.LENGTH_LONG).show();*/
                        } else {
                            mUninstallButton.setText(textEnable);
                            /*Toast.makeText(mContext, "App is disabled",
                                    Toast.LENGTH_LONG).show();*/
                        }
                        /*try {
                            View mMoreControlButtons = (View) XposedHelpers
                                    .getObjectField(param.thisObject,
                                            "mMoreControlButtons");
                            mMoreControlButtons.setVisibility(View.VISIBLE);
                        } catch (NoSuchFieldError e) {
                            mSpecialDisableButton.setVisibility(View.VISIBLE);
                        }*/
                    }
                }
            }
        };

        installPackageHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                backupApkFiles = getPref(Common.PREF_ENABLE_BACKUP_APK_FILE, getInstallerOptContext());
                backupDir = getPrefString(Common.PREF_BACKUP_APK_LOCATION, getInstallerOptContext());
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                enableNotifications = getPref(Common.PREF_ENABLE_NOTIFICATIONS, getInstallerOptContext());
                downgradeApps = getPref(Common.PREF_ENABLE_DOWNGRADE_APP, getInstallerOptContext());
                forwardLock = getPref(Common.PREF_DISABLE_FORWARD_LOCK, getInstallerOptContext());
                installAppsOnExternal = getPref(Common.PREF_ENABLE_INSTALL_EXTERNAL_STORAGE, getInstallerOptContext());
                installBackground = getPref(Common.PREF_DISABLE_INSTALL_BACKGROUND, getInstallerOptContext());
                installShell = getPref(Common.PREF_DISABLE_INSTALL_SHELL, getInstallerOptContext());
                mContext = (Context) XposedHelpers.getObjectField(
                        param.thisObject, "mContext");
                boolean isInstallStage = "installStage".equals(param.method
                        .getName());
                boolean isInstallPackageAsUser = "installPackageAsUser".equals(param.method
                        .getName());
                int flags = 0;
                int id = 0;
                if (isInstallStage && isModuleEnabled()) {
                    try {
                        id = 4;
                        flags = (Integer) XposedHelpers.getObjectField(
                                param.args[id], "installFlags");
                        if (enableDebug && isModuleEnabled()) {
                            xlog_start("installPackageHook - isInstallStage");
                            xlog("isInstallStage equals", isInstallStage);
                            xlog("flags", flags);
                            xlog_end("installPackageHook - isInstallStage");
                        }
                    } catch (Exception e) {
                        XposedBridge.log(e);
                        XposedBridge.log("Stacktrace follows:");
                        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                            XposedBridge.log("HookDetection: " + stackTraceElement.getClassName() + "->" + stackTraceElement.getMethodName());
                        }
                    }
                } else {
                    try {
                        id = Common.JB_MR1_NEWER ? 2 : 1;
                        flags = (Integer) param.args[id];
                        if (enableDebug && isModuleEnabled()) {
                            xlog_start("installPackageHook - isNotInstallStage");
                            xlog("isInstallStage equals", isInstallStage);
                            xlog("id", id);
                            xlog("flags", flags);
                            xlog_end("installPackageHook - isNotInstallStage");
                        }
                    } catch (Exception e) {
                        XposedBridge.log(e);
                        XposedBridge.log("Stacktrace follows:");
                        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                            XposedBridge.log("HookDetection: " + stackTraceElement.getClassName() + "->" + stackTraceElement.getMethodName());
                        }
                    }
                }
                if (downgradeApps && isModuleEnabled()) {
                    if ((flags & Common.INSTALL_ALLOW_DOWNGRADE) == 0) {
                        flags |= Common.INSTALL_ALLOW_DOWNGRADE;
                    }
                }
                if (forwardLock && isModuleEnabled()) {
                    if ((flags & Common.INSTALL_FORWARD_LOCK) != 0) {
                        flags &= ~Common.INSTALL_FORWARD_LOCK;
                    }
                }
                if (installAppsOnExternal && isModuleEnabled()) {
                    if ((flags & Common.INSTALL_EXTERNAL) == 0) {
                        flags |= Common.INSTALL_EXTERNAL;
                    }
                }

                if (isInstallStage && isModuleEnabled()) {
                    Object sessions = param.args[id];
                    XposedHelpers.setIntField(sessions, "installFlags",
                            flags);
                    param.args[id] = sessions;
                    if (enableDebug && isModuleEnabled()) {
                        xlog("sessions", sessions);
                    }
                } else {
                    param.args[id] = flags;
                }

                /*if (isInstallPackageAsUser) {
                    int uid = Binder.getCallingUid();
                    xlog("Calling UID: ", uid);
                    int total = param.args.length;
                    xlog("Total arguments: ", total);
                    for (int i = 0; i < total; i++) {
                        if (param.args[i] != null) {
                            try {
                                xlog("Argument " + i, param.args[i]);
                            } catch (Throwable t) {
                                xlog("Error caught", t);
                            }
                        }
                    }
                }

                if (isInstallStage) {
                    int installerUid = (int) XposedHelpers.getObjectField(param.thisObject, "installUid");
                    xlog("installerUid: ", installerUid);
                }*/

                if (installBackground && isModuleEnabled() && Binder.getCallingUid() == Common.ROOT_UID) {
                    param.setResult(null);
                    if (enableNotifications && isModuleEnabled()) {
                        Looper.prepare();
                        //Toast.makeText(mContext, "Background install attempt blocked", Toast.LENGTH_LONG).show();
                        postNotification("Install Blocked", "Background install attempt blocked", "");
                        Looper.loop();
                    }
                    if (enableDebug && isModuleEnabled()) {
                        xlog_start("installPackageHook - installBackground");
                        xlog("Background install attempt blocked", null);
                        xlog_end("installPackageHook - installBackground");
                    }
                    return;
                }

                if (installShell && isModuleEnabled() && Binder.getCallingUid() == Common.SHELL_UID) {
                    param.setResult(null);
                    if (enableNotifications && isModuleEnabled()) {
                        Looper.prepare();
                        //Toast.makeText(mContext, "ADB install attempt blocked", Toast.LENGTH_LONG).show();
                        postNotification("Install Blocked", "ADB install attempt blocked", "");
                        Looper.loop();
                    }
                    if (enableDebug && isModuleEnabled()) {
                        xlog_start("installPackageHook - installShell");
                        xlog("ADB install attempt blocked", null);
                        xlog_end("installPackageHook - installShell");
                    }
                    return;
                }

                if (backupApkFiles && isModuleEnabled() && backupDir != null) {
                    if (!isInstallStage) {
                        String apkFile = null;
                        if (Common.LOLLIPOP_NEWER) {
                            apkFile = (String) param.args[0];
                        } else {
                            Uri packageUri = (Uri) param.args[0];
                            apkFile = packageUri.getPath();
                        }
                        if (apkFile != null) {
                            backupApkFile(apkFile, backupDir);
                            if (enableDebug && isModuleEnabled()) {
                                xlog_start("installPackageHook - backupApkFiles");
                                xlog("APK file", apkFile);
                                xlog_end("installPackageHook - backupApkFiles");
                                /*XposedBridge.log("Stacktrace follows:");
                                for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                                    XposedBridge.log("HookDetection: " + stackTraceElement.getClassName() + "->" + stackTraceElement.getMethodName());
                                }*/
                            }
                        }
                    }
                }
            }
        };

        scanPackageHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                //xlog("disableCheckSignatures value before scanPackageLI", disableCheckSignatures);
                disableCheckSignatures = false;
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //xlog("disableCheckSignatures value after scanPackageLI", disableCheckSignatures);
                disableCheckSignatures = true;
            }
        };

        showButtonsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                showButtons = getPref(Common.PREF_ENABLE_SHOW_BUTTON, getInstallerOptContext());
                if (showButtons && isModuleEnabled()) {
                    param.setResult(true);
                    return;
                }
            }
        };

        systemAppsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                disableSystemApps = getPref(Common.PREF_ENABLE_DISABLE_SYSTEM_APP, getInstallerOptContext());
                if (disableSystemApps && isModuleEnabled()) {
                    param.setResult(false);
                    return;
                }

            }
        };

        /*updatePrefsHook = new XC_MethodHook() {
            @Override
            public void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                //mContext = AndroidAppHelper.currentApplication();
                //updatePrefs(null);
                prefsChanged = getPref(Common.PREF_MODIFIED_PREFERENCES, getInstallerOptContext());
            }
        };*/

        unknownAppsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                installUnknownApps = getPref(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP, getInstallerOptContext());
                if (installUnknownApps && isModuleEnabled()) {
                    param.setResult(true);
                    return;
                }
            }
        };

        unknownAppsPrompt = new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                installUnknownAppsPrompt = getPref(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP_PROMPT, getInstallerOptContext());
                if (installUnknownAppsPrompt && isModuleEnabled()) {
                    int DLG_UNKNOWN_APPS = Build.VERSION.SDK_INT >= 17 ? 1 : 2;
                    if ((Integer) param.args[0] != DLG_UNKNOWN_APPS) {
                        XposedHelpers.callMethod(param.thisObject, "removeDialog", (Integer) param.args[0]);
                        XposedHelpers.callMethod(param.thisObject, "showDialog", (Integer) param.args[0]);
                    }
                    return null;
                }
                XposedHelpers.callMethod(param.thisObject, "removeDialog", (Integer) param.args[0]);
                XposedHelpers.callMethod(param.thisObject, "showDialog", (Integer) param.args[0]);
                return null;
            }
        };

        unknownAppsHookPrompt = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                installUnknownAppsPrompt = getPref(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP_PROMPT, getInstallerOptContext());
                if (installUnknownAppsPrompt && isModuleEnabled()) {
                    xlog_start("unknownAppsHookPrompt");
                    String checkUnknownSourceMethod = "";

                    String instAlertTitle = "Package installation";
                    String instAlertBody = "You are trying to install an apk from an unknown source.\nYour phone/tablet and personal data are more vulnerable to attack by apps from unknown sources. You agree that you are solely responsible for any damage to your phone or loss of data that may result from using these apps.\n\nDo you want to continue the installation?";

                    if (Build.VERSION.SDK_INT >= 22) {
                        checkUnknownSourceMethod = "isUnknownSourcesEnabled";
                    } else {
                        checkUnknownSourceMethod = "isInstallingUnknownAppsAllowed";
                    }
                    if (Common.MARSHMALLOW_NEWER) {
                        if (!(Boolean) XposedHelpers.callMethod(param.thisObject, "isUnknownSourcesAllowedByAdmin")) {
                            return;
                        }
                    }
                    if (!(Boolean) XposedHelpers.callMethod(param.thisObject, checkUnknownSourceMethod)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder((Context) param.thisObject);
                        builder.setTitle(instAlertTitle);
                        //Alert taken from Android settings
                        builder.setMessage(instAlertBody);
                        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                XposedHelpers.callMethod(param.thisObject, "initiateInstall");
                            }
                        });
                        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                XposedHelpers.callMethod(param.thisObject, "finish");
                            }
                        });
                        builder.show();
                    }
                }
            }
        };

        verifyAppsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                //prefs.reload();
                mContext = AndroidAppHelper.currentApplication();
                verifyApps = getPref(Common.PREF_DISABLE_VERIFY_APP, getInstallerOptContext());
                if (verifyApps && isModuleEnabled()) {
                    if (enableDebug && isModuleEnabled()) {
                        xlog_start("verifyAppsHook");
                        xlog("Disable app verification set to", verifyApps);
                        xlog_end("verifyAppsHook");
                    }
                    param.setResult(false);
                    return;
                }
                //}
            }
        };

        verifyJarHook = new XC_MethodHook() {
            @SuppressWarnings("unchecked")
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    prefs.reload();
                    verifyJar = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_JAR, false);
                } catch (Throwable e) {
                    mContext = AndroidAppHelper.currentApplication();
                    verifyJar = getPref(Common.PREF_DISABLE_VERIFY_JAR, getInstallerOptContext());
                }
                if (verifyJar && isModuleEnabled()) {
                    //xlog("Disable JAR verification set to", verifyJar);
                    String name = (String) XposedHelpers.getObjectField(
                            param.thisObject, "name");
                    if (Common.LOLLIPOP_NEWER) {
                        Certificate[][] certChains = (Certificate[][]) XposedHelpers
                                .getObjectField(param.thisObject, "certChains");
                        Hashtable<String, Certificate[][]> verifiedEntries = null;

                        try {
                            verifiedEntries = (Hashtable<String, Certificate[][]>) XposedHelpers
                                    .findField(param.thisObject.getClass(),
                                            "verifiedEntries").get(
                                            param.thisObject);
                        } catch (NoSuchFieldError e) {
                            verifiedEntries = (Hashtable<String, Certificate[][]>) XposedHelpers
                                    .getObjectField(
                                            XposedHelpers
                                                    .getSurroundingThis(param.thisObject),
                                            "verifiedEntries");
                        }
                        verifiedEntries.put(name, certChains);
                    } else {
                        Certificate[] certificates = (Certificate[]) XposedHelpers
                                .getObjectField(param.thisObject,
                                        "certificates");
                        Hashtable<String, Certificate[]> verifiedEntries = null;
                        try {
                            verifiedEntries = (Hashtable<String, Certificate[]>) XposedHelpers
                                    .findField(param.thisObject.getClass(),
                                            "verifiedEntries").get(
                                            param.thisObject);
                        } catch (NoSuchFieldError e) {
                            verifiedEntries = (Hashtable<String, Certificate[]>) XposedHelpers
                                    .getObjectField(
                                            XposedHelpers
                                                    .getSurroundingThis(param.thisObject),
                                            "verifiedEntries");
                        }
                        verifiedEntries.put(name, certificates);
                    }
                    param.setResult(null);
                }
            }
        };

        verifySignatureHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    prefs.reload();
                    verifySignature = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_SIGNATURE, false);
                } catch (Throwable e) {
                    mContext = AndroidAppHelper.currentApplication();
                    verifySignature = getPref(Common.PREF_DISABLE_VERIFY_SIGNATURE, getInstallerOptContext());
                }
                if (verifySignature && isModuleEnabled()) {
                    /*xlog("verifySignatureHook: Boot complete", bootCompleted);
                    xlog_start("verifySignatureHook");
                    xlog("Disable signature verification set to", verifySignature);
                    xlog_end("verifySignatureHook");*/
                    param.setResult(true);
                    return;
                }
            }
        };

        verifySignaturesHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    prefs.reload();
                    checkSignatures = prefs.getBoolean(Common.PREF_DISABLE_CHECK_SIGNATURE, false);
                } catch (Throwable e) {
                    mContext = AndroidAppHelper.currentApplication();
                    checkSignatures = getPref(Common.PREF_DISABLE_CHECK_SIGNATURE, getInstallerOptContext());
                }
                if (checkSignatures && isModuleEnabled()) {
                    //xlog("verifySignaturesHook: Boot complete", bootCompleted);
                    /*xlog_start("verifySignaturesHook");
                    xlog("Disable signature checking set to", checkSignatures);
                    xlog_end("verifySignaturesHook");*/
                    param.setResult(true);
                    return;
                }
            }
        };

    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        try {
            if (resparam.packageName.equals(Common.PACKAGEINSTALLER_PKG) || resparam.packageName.equals(Common.GOOGLE_PACKAGEINSTALLER_PKG) || resparam.packageName.equals(Common.MOKEE_PACKAGEINSTALLER_PKG) || resparam.packageName.equals(Common.SAMSUNG_PACKAGEINSTALLER_PKG)) {
                resparam.res.hookLayout(Common.PACKAGEINSTALLER_PKG, "layout", "install_confirm", autoInstallHook2);
            }
        } catch (Throwable t) {
            xlog_start("handleInitPackageResources");
            xlog("handleInitPackageResources error caught: ", t);
            xlog_end("handleInitPackageResources");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        try {
            /*if (lpparam.packageName.equals(Common.INSTALLEROPT)) {
                XposedHelpers.findAndHookMethod(
                        Common.INSTALLEROPTBOOTRECEIVER, lpparam.classLoader,
                        "onReceive", Context.class, Intent.class, bootCompletedHook);
                XposedHelpers.findAndHookMethod(
                        Common.INSTALLEROPTMAINACTIVITY, lpparam.classLoader,
                        "onSharedPreferenceChanged", SharedPreferences.class, String.class, updatePrefsHook);
            }

            if (Common.INSTALLEROPT.equals(lpparam.packageName)) {
                XposedHelpers.findAndHookMethod(Common.INSTALLEROPT
                                + ".Preferences", lpparam.classLoader, "isModuleEnabled",
                        XC_MethodReplacement.returnConstant(true));
            }*/

            if (Common.ANDROID_PKG.equals(lpparam.packageName)
                    && Common.ANDROID_PKG.equals(lpparam.processName) && !lpparam.packageName.equals(Common.SYSTEM_UI)) {
                Class<?> appErrorDialogClass = XposedHelpers.findClass(
                        Common.APPERRORDIALOG, lpparam.classLoader);
                Class<?> devicePolicyManagerClass = XposedHelpers.findClass(
                        Common.DEVICEPOLICYMANAGERSERVICE, lpparam.classLoader);
                Class<?> jarVerifierClass = XposedHelpers.findClass(
                        Common.JARVERIFIER, lpparam.classLoader);
                Class<?> openSSLSignatureClass = XposedHelpers.findClass(
                        Common.OPENSSLSIGNATURE, lpparam.classLoader);
                Class<?> packageManagerClass = XposedHelpers.findClass(
                        Common.PACKAGEMANAGERSERVICE, lpparam.classLoader);
                Class<?> packageParserClass = XposedHelpers.findClass(
                        Common.PACKAGEPARSER, lpparam.classLoader);
                Class<?> signatureClass = XposedHelpers.findClass(
                        Common.SIGNATURE, lpparam.classLoader);

                if (Common.LOLLIPOP_NEWER) {
                    // 5.0 and newer
                    XposedBridge.hookAllMethods(packageManagerClass,
                            "checkUpgradeKeySetLP", checkDuplicatedPermissionsHook);
                }

                // 4.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass, "checkPermission",
                        checkPermissionsHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass,
                        "checkUidPermission", checkPermissionsHook);

                if (Common.LOLLIPOP_NEWER) {
                    // 5.0 and newer
                    XposedBridge.hookAllMethods(packageParserClass, "parseBaseApk",
                            checkSdkVersionHook);
                } else {
                    // 4.0 - 4.4
                    XposedBridge.hookAllMethods(packageParserClass, "parsePackage",
                            checkSdkVersionHook);
                }

                // 4.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass,
                        "compareSignatures", checkSignaturesHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass,
                        "checkUidSignatures", checkSignaturesHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass,
                        "checkSignatures", checkSignaturesHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(android.os.Process.class, "start",
                        debugAppsHook);

                // 5.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass,
                        "deletePackage", deletePackageHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(devicePolicyManagerClass,
                        "packageHasActiveAdmins", deviceAdminsHook);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    XposedHelpers.findAndHookMethod(XposedHelpers.findClass(
                            "com.android.server.pm.PackageManagerService",
                            lpparam.classLoader), "grantPermissionsLPw",
                            Common.CLASS_PACKAGE_PARSER_PACKAGE, boolean.class, String.class, externalSdCardAccessHook2);
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
                    XposedHelpers.findAndHookMethod(
                            XposedHelpers.findClass(
                                    "com.android.server.SystemConfig",
                                    lpparam.classLoader), "readPermission",
                            XmlPullParser.class, String.class,
                            externalSdCardAccessHook);
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                    XposedHelpers.findAndHookMethod(XposedHelpers.findClass(
                            "com.android.server.pm.PackageManagerService",
                            lpparam.classLoader), "readPermission",
                            XmlPullParser.class, String.class,
                            externalSdCardAccessHook);
                }

                /*// 4.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass, "getPackageInfo",
                    getPackageInfoHook);*/

                // 4.0 and newer
                XposedBridge.hookAllConstructors(appErrorDialogClass,
                        hideAppCrashesHook);

                if (Common.LOLLIPOP_NEWER) {
                    // 5.0 and newer
                    XposedBridge.hookAllMethods(packageManagerClass,
                            "installPackageAsUser", installPackageHook);
                    XposedBridge.hookAllMethods(packageManagerClass,
                            "installStage", installPackageHook);
                } else {
                    if (Common.JB_MR1_NEWER) {
                        // 4.2 - 4.4
                        XposedBridge.hookAllMethods(packageManagerClass,
                                "installPackageWithVerificationAndEncryption",
                                installPackageHook);
                    } else {
                        // 4.0 - 4.1
                        XposedBridge.hookAllMethods(packageManagerClass,
                                "installPackageWithVerification",
                                installPackageHook);
                    }
                }

                // 4.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass, "scanPackageLI",
                        scanPackageHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(View.class,
                        "onFilterTouchEventForSecurity", showButtonsHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass,
                        "isVerificationEnabled", verifyAppsHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(jarVerifierClass, "verify",
                        verifyJarHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(MessageDigest.class, "isEqual",
                        verifySignatureHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(signatureClass, "verify",
                        verifySignatureHook);

                // 4.4 and newer
                if (Common.KITKAT_NEWER) {
                    XposedBridge.hookAllMethods(openSSLSignatureClass, "engineVerify",
                            verifySignatureHook);
                }

                // 4.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass,
                        "verifySignaturesLP", verifySignaturesHook);

            }

            if (lpparam.packageName.equals(Common.PACKAGEINSTALLER_PKG) || lpparam.packageName.equals(Common.GOOGLE_PACKAGEINSTALLER_PKG) || lpparam.packageName.equals(Common.MOKEE_PACKAGEINSTALLER_PKG) || lpparam.packageName.equals(Common.SAMSUNG_PACKAGEINSTALLER_PKG)) {
                XposedHelpers.findAndHookMethod(Common.INSTALLAPPPROGRESS + "$1",
                        lpparam.classLoader, "handleMessage", Message.class,
                        autoCloseInstallHook);

                // 4.0 - 4.4
                XposedHelpers.findAndHookMethod(Common.UNINSTALLAPPPROGRESS,
                        lpparam.classLoader, "initView", autoCloseUninstallHook);

                // 4.0 and newer
                XposedHelpers.findAndHookMethod(Common.INSTALLAPPPROGRESS,
                        lpparam.classLoader, "initView", autoHideInstallHook);

                XposedHelpers.findAndHookMethod(Common.PACKAGEINSTALLERACTIVITY,
                        lpparam.classLoader, "startInstallConfirm",
                        autoInstallHook);

                if (Common.LOLLIPOP_NEWER) {
                    try {
                        // 5.0 and newer
                        XposedHelpers.findAndHookMethod(Common.UNINSTALLERACTIVITY,
                                lpparam.classLoader, "showConfirmationDialog",
                                autoUninstallHook);
                    } catch (NoSuchMethodError nsme) {
                        // Samsung 5.0
                        XposedBridge.hookAllMethods(XposedHelpers.findClass(
                                Common.UNINSTALLERACTIVITY, lpparam.classLoader),
                                "onCreate", autoUninstallHook);
                    }
                } else {
                    // 4.0 and newer
                    XposedHelpers.findAndHookMethod(Common.UNINSTALLERACTIVITY,
                            lpparam.classLoader, "onCreate", Bundle.class,
                            autoUninstallHook);
                }

                if (Common.MARSHMALLOW_NEWER && !rom_TW && !rom_CM) {
                    try {
                        XposedHelpers.findAndHookMethod(Common.PACKAGEINSTALLERGRANTPERMISSIONSACTIVITY,
                                lpparam.classLoader, "onBackPressed", grantPermissionsBackButtonHook);
                    } catch (NoSuchMethodError nsme) {
                        xlog_start("grantPermissionsBackButtonHook");
                        xlog("Method not found", nsme);
                        xlog_end("grantPermissionsBackButtonHook");
                    }
                }

                try {
                    XposedHelpers.findAndHookMethod(Common.PACKAGEINSTALLERACTIVITY,
                            lpparam.classLoader, "onCreate", "android.os.Bundle", unknownAppsHookPrompt);
                } catch (NoSuchMethodError nsme) {
                    xlog_start("unknownAppsHookPrompt");
                    xlog("Method not found", nsme);
                    xlog_end("unknownAppsHookPrompt");
                }

                try {
                    XposedHelpers.findAndHookMethod(Common.PACKAGEINSTALLERACTIVITY,
                            lpparam.classLoader, "showDialogInner", Integer.TYPE, unknownAppsPrompt);
                } catch (NoSuchMethodError nsme) {
                    xlog_start("unknownAppsPrompt");
                    xlog("Method not found", nsme);
                    xlog_end("unknownAppsPrompt");
                }

                if (Common.LOLLIPOP_MR1_NEWER) {
                    // 5.1 and newer
                    try {
                        XposedHelpers.findAndHookMethod(
                                Common.PACKAGEINSTALLERACTIVITY, lpparam.classLoader,
                                "isUnknownSourcesEnabled", unknownAppsHook);
                    } catch (NoSuchMethodError nsme) {
                        // Samsung 5.1
                        try {
                            XposedHelpers.findAndHookMethod(
                                    Common.PACKAGEINSTALLERACTIVITY, lpparam.classLoader,
                                    "isUnknownSourcesEnabled", unknownAppsHook);
                        } catch (NoSuchMethodError nsme2) {
                            xlog_start("isUnknownSourcesEnabled");
                            xlog("Method not found", nsme);
                            xlog_end("isUnknownSourcesEnabled");
                        }
                    }
                } else {
                    // 4.0 - 5.0
                    XposedHelpers.findAndHookMethod(
                            Common.PACKAGEINSTALLERACTIVITY, lpparam.classLoader,
                            "isInstallingUnknownAppsAllowed", unknownAppsHook);
                }

                if (Common.KITKAT_NEWER) {
                    // 4.4 and newer
                    XposedHelpers.findAndHookMethod(
                            Common.PACKAGEINSTALLERACTIVITY, lpparam.classLoader,
                            "isVerifyAppsEnabled", verifyAppsHook);
                }
            }

            if (lpparam.packageName.equals(Common.SETTINGS_PKG)) {
                disableChangerClass = XposedHelpers.findClass(
                        Common.INSTALLEDAPPDETAILS + ".DisableChanger",
                        lpparam.classLoader);

                XposedHelpers.findAndHookMethod(Common.INSTALLEDAPPDETAILS,
                        lpparam.classLoader, "setAppLabelAndIcon",
                        PackageInfo.class, appInfoHook);

                // 4.0 and newer
                XposedHelpers.findAndHookMethod(Common.INSTALLEDAPPDETAILS,
                        lpparam.classLoader, "onClick", View.class,
                        disableChangerHook);

                // 4.0 - 5.1
                if (!Common.MARSHMALLOW_NEWER) {
                    XposedHelpers.findAndHookMethod(Common.INSTALLEDAPPDETAILS,
                            lpparam.classLoader, "showDialogInner", int.class,
                            int.class, disableUserAppsHook);
                }

                // 4.2 and newer
                if (Common.JB_MR1_NEWER) {
                    if (rom_CM || rom_TW) {
                        XposedHelpers.findAndHookMethod(Common.APPSTORAGEDETAILS,
                                lpparam.classLoader, "initDataButtons",
                                initAppStorageSettingsButtonsHook);
                    }
                    if (rom_CM) {
                        try {
                            XposedHelpers.findAndHookMethod(Common.APPOPSDETAILS,
                                    lpparam.classLoader, "isPlatformSigned",
                                    initAppOpsDetailsHook);
                        } catch (NoSuchMethodError nsme) {
                            xlog_start("initAppOpsDetailsHook");
                            xlog("Method not found", nsme);
                            xlog_end("initAppOpsDetailsHook");
                        }
                    }
                }

                // 4.2 and newer
                if (Common.JB_MR1_NEWER) {
                    XposedHelpers.findAndHookMethod(Common.INSTALLEDAPPDETAILS,
                            lpparam.classLoader, "initUninstallButtons",
                            initUninstallButtonsHook);
                }

                if (Common.JB_NEWER) {
                    if (Common.LOLLIPOP_NEWER) {
                        // 5.0 and newer
                        XposedHelpers.findAndHookMethod(Common.UTILS,
                                lpparam.classLoader, "isSystemPackage",
                                PackageManager.class, PackageInfo.class,
                                systemAppsHook);
                    } else {
                        // 4.1 - 4.4
                        XposedHelpers.findAndHookMethod(Common.INSTALLEDAPPDETAILS,
                                lpparam.classLoader, "isThisASystemPackage",
                                systemAppsHook);
                    }
                }
            }
        } catch (Throwable t) {
            xlog_start("handleLoadPackage");
            xlog("handleLoadPackage error caught: ", t);
            XposedBridge.log("Stacktrace follows:");
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                XposedBridge.log("HookDetection: " + stackTraceElement.getClassName() + "->" + stackTraceElement.getMethodName());
            }
            xlog_end("handleLoadPackage");
        }
    }

    public int[] appendInt(int[] cur, int val) {
        if (cur == null) {
            return new int[]{val};
        }
        final int N = cur.length;
        for (int i = 0; i < N; i++) {
            if (cur[i] == val) {
                return cur;
            }
        }
        int[] ret = new int[N + 1];
        System.arraycopy(cur, 0, ret, 0, N);
        ret[N] = val;
        return ret;
    }

    public void backupApkFile(String apkFile, String dir) {
        Intent backupApkFile = new Intent(Common.ACTION_BACKUP_APK_FILE);
        backupApkFile.setPackage(Common.PACKAGE_NAME);
        backupApkFile.putExtra(Common.FILE, apkFile);
        backupApkFile.putExtra(Common.BACKUP_DIR, dir);
        getInstallerOptContext().sendBroadcast(backupApkFile);
    }

    public void deleteApkFile(String apkFile) {
        Intent deleteApkFile = new Intent(Common.ACTION_DELETE_APK_FILE);
        deleteApkFile.setPackage(Common.PACKAGE_NAME);
        deleteApkFile.putExtra(Common.FILE, apkFile);
        getInstallerOptContext().sendBroadcast(deleteApkFile);
    }

    public Context getInstallerOptContext() {
        Context context = null;
        try {
            context = mContext.createPackageContext(Common.PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            xlog_start("getInstallerOptContext Error");
            xlog("", e);
            xlog_end("getInstallerOptContext Error");
        }
        return context;
    }

    public static Boolean getPref(String pref, Context context) {
        try {
            return MultiprocessPreferences.getDefaultSharedPreferences(context).getBoolean(pref, false);
        } catch (Throwable t) {
            xlog_start("getPref Error");
            xlog("", t);
            xlog_end("getPref Error");
        }
        return false;
    }

    public static String getPrefString(String pref, Context context) {
        try {
            return MultiprocessPreferences.getDefaultSharedPreferences(context).getString(pref, null);
        } catch (Throwable t) {
            xlog_start("getPrefString Error");
            xlog("", t);
            xlog_end("getPrefString Error");
        }
        return null;
    }

    public static long getPrefLong(String pref, Context context) {
        try {
            return MultiprocessPreferences.getDefaultSharedPreferences(context).getLong(pref, 0);
        } catch (Throwable t) {
            xlog_start("getPrefLong Error");
            xlog("", t);
            xlog_end("getPrefLong Error");
        }
        return 0;
    }

    public boolean isCyanogenMod() {
        boolean isCyanogenMod = false;
        String host = android.os.Build.HOST;
        String version = System.getProperty("os.version");
        BufferedReader reader = null;

        try {
            if (host.contains("cyanogenmod") || host.contains("mokee") || host.contains("Emotion")) {
                isCyanogenMod = true;
            } else if (version.contains("cyanogenmod") || version.contains("mokee") || version.contains("Emotion")) {
                isCyanogenMod = true;
            } else {
                // This does not require root
                reader = new BufferedReader(new FileReader("/proc/version"), 256);
                version = reader.readLine();

                if (version.contains("cyanogenmod") || version.contains("mokee") || version.contains("Emotion")) {
                    isCyanogenMod = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }

        return isCyanogenMod;
    }

    /*public boolean isExpertModeEnabled() {
        try {
            return prefs.getBoolean(Common.PREF_ENABLE_EXPERT_MODE, false);
        } catch (Throwable e) {
            mContext = AndroidAppHelper.currentApplication();
            return getPref(Common.PREF_ENABLE_EXPERT_MODE, getInstallerOptContext());
        }
    }*/

    public boolean isModuleEnabled() {
        try {
            prefs.reload();
            return prefs.getBoolean(Common.PREF_ENABLE_MODULE, false);
        } catch (Throwable e) {
            mContext = AndroidAppHelper.currentApplication();
            return getPref(Common.PREF_ENABLE_MODULE, getInstallerOptContext());
        }
    }

    public void postNotification(String title, String description, String thirdline) {
        Intent postNotification = new Intent(
                Common.ACTION_POST_NOTIFICATION);
        postNotification.putExtra(Common.DESCRIPTION, description);
        postNotification.putExtra(Common.THIRDLINE, thirdline);
        postNotification.putExtra(Common.TITLE, title);
        getInstallerOptContext().sendBroadcast(postNotification);
    }

    public static void setPref(Context context, String pref, Boolean value, int value2, long value3, String value4) {
        if (value != null) {
            MultiprocessPreferences.getDefaultSharedPreferences(context).edit().putBoolean(pref, value).apply();
        }
        if (value2 != 0) {
            MultiprocessPreferences.getDefaultSharedPreferences(context).edit().putInt(pref, value2).apply();
        }
        if (value3 != 0) {
            MultiprocessPreferences.getDefaultSharedPreferences(context).edit().putLong(pref, value3).apply();
        }
        if (value4 != null) {
            MultiprocessPreferences.getDefaultSharedPreferences(context).edit().putString(pref, value4).apply();
        }
    }

    public void updatePrefs() {
        backupApkFiles = prefs.getBoolean(Common.PREF_ENABLE_BACKUP_APK_FILE, false);
        checkDuplicatedPermissions = prefs.getBoolean(Common.PREF_DISABLE_CHECK_DUPLICATED_PERMISSION, false);
        //checkLuckyPatcher = prefs.getBoolean(Common.PREF_DISABLE_CHECK_LUCKY_PATCHER, false);
        checkPermissions = prefs.getBoolean(Common.PREF_DISABLE_CHECK_PERMISSION, false);
        checkSdkVersion = prefs.getBoolean(Common.PREF_DISABLE_CHECK_SDK_VERSION, false);
        checkSignatures = prefs.getBoolean(Common.PREF_DISABLE_CHECK_SIGNATURE, false);
        debugApps = prefs.getBoolean(Common.PREF_ENABLE_DEBUG_APP, false);
        deleteApkFiles = prefs.getBoolean(Common.PREF_ENABLE_DELETE_APK_FILE_INSTALL, false);
        deviceAdmins = prefs.getBoolean(Common.PREF_ENABLE_UNINSTALL_DEVICE_ADMIN, false);
        disableSystemApps = prefs.getBoolean(Common.PREF_ENABLE_DISABLE_SYSTEM_APP, false);
        disableUserApps = prefs.getBoolean(Common.PREF_ENABLE_DISABLE_USER_APPS, false);
        downgradeApps = prefs.getBoolean(Common.PREF_ENABLE_DOWNGRADE_APP, false);
        enableAppStorageSettingsButtons = prefs.getBoolean(Common.PREF_ENABLE_APP_STORAGE_BUTTONS, false);
        enableAutoHideInstall = prefs.getBoolean(Common.PREF_ENABLE_AUTO_HIDE_INSTALL, false);
        enableAutoInstall = prefs.getBoolean(Common.PREF_ENABLE_AUTO_INSTALL, false);
        enableAutoCloseInstall = prefs.getBoolean(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL, false);
        enableAutoLaunchInstall = prefs.getBoolean(Common.PREF_ENABLE_AUTO_LAUNCH_INSTALL, false);
        enableAutoCloseUninstall = prefs.getBoolean(Common.PREF_ENABLE_AUTO_CLOSE_UNINSTALL, false);
        enableAutoUninstall = prefs.getBoolean(Common.PREF_ENABLE_AUTO_UNINSTALL, false);
        enableDebug = prefs.getBoolean(Common.PREF_ENABLE_DEBUG, false);
        enableLaunchApp = prefs.getBoolean(Common.PREF_ENABLE_LAUNCH_APP, false);
        enableOpenAppOps = prefs.getBoolean(Common.PREF_ENABLE_OPEN_APP_OPS, false);
        enableNotifications = prefs.getBoolean(Common.PREF_ENABLE_NOTIFICATIONS, false);
        enablePackageName = prefs.getBoolean(Common.PREF_ENABLE_SHOW_PACKAGE_NAME, false);
        enablePlay = prefs.getBoolean(Common.PREF_ENABLE_OPEN_APP_GOOGLE_PLAY, false);
        enableVersion = prefs.getBoolean(Common.PREF_ENABLE_SHOW_VERSION, false);
        enableVersionCode = prefs.getBoolean(Common.PREF_ENABLE_SHOW_VERSION_CODE, false);
        enableVersionInline = prefs.getBoolean(Common.PREF_ENABLE_SHOW_VERSION_INLINE, false);
        enableVersionToast = prefs.getBoolean(Common.PREF_ENABLE_SHOW_VERSION_TOAST, false);
        enableVibrateDevice = prefs.getBoolean(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL_VIBRATE, false);
        forwardLock = prefs.getBoolean(Common.PREF_DISABLE_FORWARD_LOCK, false);
        hideAppCrashes = prefs.getBoolean(Common.PREF_ENABLE_HIDE_APP_CRASHES, false);
        installAppsOnExternal = prefs.getBoolean(Common.PREF_ENABLE_INSTALL_EXTERNAL_STORAGE, false);
        installBackground = prefs.getBoolean(Common.PREF_DISABLE_INSTALL_BACKGROUND, false);
        installShell = prefs.getBoolean(Common.PREF_DISABLE_INSTALL_SHELL, false);
        installUnknownApps = prefs.getBoolean(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP, false);
        keepAppsData = prefs.getBoolean(Common.PREF_ENABLE_KEEP_APP_DATA, false);
        showButtons = prefs.getBoolean(Common.PREF_ENABLE_SHOW_BUTTON, false);
        uninstallBackground = prefs.getBoolean(Common.PREF_DISABLE_UNINSTALL_BACKGROUND, false);
        uninstallSystemApps = prefs.getBoolean(Common.PREF_ENABLE_UNINSTALL_SYSTEM_APP, false);
        verifyApps = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_APP, false);
        verifyJar = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_JAR, false);
        verifySignature = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_SIGNATURE, false);
    }

    public void uninstallSystemApp(String packageName) {
        Intent uninstallSystemApp = new Intent(
                Common.ACTION_UNINSTALL_SYSTEM_APP);
        uninstallSystemApp.setPackage(Common.PACKAGE_NAME);
        uninstallSystemApp.putExtra(Common.PACKAGE, packageName);
        getInstallerOptContext().sendBroadcast(uninstallSystemApp);
    }

    public void vibrateDevice(int duration) {
        Intent vibrateDevice = new Intent(
                Common.ACTION_VIBRATE_DEVICE);
        vibrateDevice.putExtra(Common.DURATION, duration);
        getInstallerOptContext().sendBroadcast(vibrateDevice);
    }

    public static void xlog(String description, Object object) {
        if (object == null) {
            XposedBridge.log("     " + description);
        } else {
            XposedBridge.log("     " + description + ": " + object);
        }
    }

    public static void xlog_start(String header) {
        XposedBridge.log("[ InstallerOpt Debug Start - " + header + " ]");
    }

    public static void xlog_end(String footer) {
        XposedBridge.log("[ InstallerOpt Debug End - " + footer + " ]");
    }
}
