package net.fypm.InstallerOpt;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Hashtable;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XSharedPreferences;

public class Main implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {

    public XC_MethodHook appInfoHook;
    public XC_MethodHook autoCloseInstallHook;
    public XC_MethodHook autoHideInstallHook;
    public XC_MethodHook autoInstallHook;
    public XC_LayoutInflated autoInstallHook2;
    public XC_MethodHook autoCloseUninstallHook;
    public XC_MethodHook autoUninstallHook;
    public XC_MethodHook bootCompletedHook;
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
    public XC_MethodHook getPackageInfoHook;
    public XC_MethodHook hideAppCrashesHook;
    public XC_MethodHook initAppStorageSettingsButtonsHook;
    public XC_MethodHook initUninstallButtonsHook;
    public XC_MethodHook installPackageHook;
    public XC_MethodHook scanPackageHook;
    public XC_MethodHook showButtonsHook;
    public XC_MethodHook systemAppsHook;
    public XC_MethodHook updatePrefsHook;
    public XC_MethodHook unknownAppsHook;
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
    public static boolean backupApkFiles;
    public static boolean bootCompleted;
    public static boolean checkDuplicatedPermissions;
    public static boolean checkLuckyPatcher;
    public static boolean checkPermissions;
    public static boolean checkSdkVersion;
    public static boolean checkSignatures;
    public static boolean confirmCheckSignatures;
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
    public static boolean installUnknownApps;
    public static boolean keepAppsData;
    public static boolean prefsChanged;
    public static boolean showButtons;
    public static boolean uninstallBackground;
    public static boolean uninstallSystemApps;
    public static boolean verifyApps;
    public static boolean verifyJar;
    public static boolean verifySignature;
    public static long prefsModifiedTime;
    private static final String TAG = "InstallerOpt";

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {

        disableCheckSignatures = true;
        prefsChanged = false;

        try {
            xlog_start("XSharedPreferences - Init");
            prefs = new XSharedPreferences(Main.class.getPackage().getName());
            prefs.makeWorldReadable();
            prefs.reload();
            enableDebug = prefs.getBoolean(Common.PREF_ENABLE_DEBUG, false);
            //updatePrefs();
            xlog("Success", null);
            xlog_end("XSharedPreferences - Init");
        } catch (Throwable e) {
            xlog("", e);
            xlog_end("XSharedPreferences - Init");
        }
        if (enableDebug) {
            //xlog("bootCompleted value at initZygote", bootCompleted);
            xlog_start("Signature Checking and Verification Overview");
            xlog("disableCheckSignatures status", disableCheckSignatures);
            xlog("checkSignatures status ", checkSignatures);
            xlog("verifySignature status ", verifySignature);
            xlog_end("Signature Checking and Verification Overview");
        }

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
                if (enableDebug) {
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

                if (enablePlay) {
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

                if (enableLaunchApp) {
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

                if (enablePackageName) {
                    appLabel.setTag(0);
                    appLabel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final int status = (Integer) v.getTag();
                            if (status == 0) {
                                appLabel.setText(packageName);
                                v.setTag(1);
                            } else {
                                appLabel.setText(appName);
                                v.setTag(0);
                            }
                        }
                    });
                }

                if (uninstallSystemApps) {
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
                deleteApkFiles = getPref(Common.PREF_ENABLE_DELETE_APK_FILE_INSTALL, getInstallerOptContext());
                enableVibrateDevice = getPref(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL_VIBRATE, getInstallerOptContext());
                Button mLaunch = (Button) XposedHelpers.getObjectField(
                        XposedHelpers.getSurroundingThis(param.thisObject),
                        "mLaunchButton");

                Message msg = (Message) param.args[0];
                boolean installedApp = false;
                if (msg != null) {
                    installedApp = (msg.arg1 == Common.INSTALL_SUCCEEDED);
                }
                if (enableAutoLaunchInstall) {
                    if (installedApp && mLaunch != null) {
                        mLaunch.performClick();
                    }
                }

                if (enableAutoCloseInstall) {
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
                        if (enableDebug) {
                            xlog_start("autoCloseInstallHook");
                            xlog("msg", msg);
                            xlog("mDone", mDone);
                            xlog("appInstalledText", appInstalledText);
                            xlog_end("autoCloseInstallHook");
                        }
                        if (!appInstalledText.isEmpty()) {
                            Toast.makeText(mContext, appInstalledText,
                                    Toast.LENGTH_LONG).show();
                            if (enableVibrateDevice) {
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
                        Toast.makeText(mContext, "App not installed\n\nError code: " + msg.arg1,
                                Toast.LENGTH_LONG).show();
                        if (enableDebug) {
                            xlog_start("autoCloseInstallHook");
                            xlog("Install failed", msg);
                            xlog("msg", msg);
                            xlog_end("autoCloseInstallHook");
                        }
                    }
                }

                if (deleteApkFiles && installedApp) {
                    Uri packageUri = (Uri) XposedHelpers.getObjectField(
                            XposedHelpers.getSurroundingThis(param.thisObject),
                            "mPackageURI");
                    String apkFile = packageUri.getPath();
                    deleteApkFile(apkFile);
                    if (enableDebug) {
                        xlog_start("deleteApkFiles");
                        xlog("APK file: ", apkFile);
                        xlog_end("deleteApkFiles");
                    }
                }

                if (enableDebug) {
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
                if (!autoInstallCancelled && enableAutoHideInstall) {
                    packageInstaller.onBackPressed();
                }
            }
        };

        autoInstallHook = new XC_MethodHook() {
            @Override
            public void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                enableVersion = getPref(Common.PREF_ENABLE_SHOW_VERSION, getInstallerOptContext());
                enableVersionCode = getPref(Common.PREF_ENABLE_SHOW_VERSION_CODE, getInstallerOptContext());
                enableVersionInline = getPref(Common.PREF_ENABLE_SHOW_VERSION_INLINE, getInstallerOptContext());
                enableVersionToast = getPref(Common.PREF_ENABLE_SHOW_VERSION_TOAST, getInstallerOptContext());
                enableAutoInstall = getPref(Common.PREF_ENABLE_AUTO_INSTALL, getInstallerOptContext());
                //Add below two in prefs
                //checkSignatures = getPref("enabled_disable_sig_check", getInstallerOptContext());
                confirmCheckSignatures = getPref("enabled_confirm_check_signatures", getInstallerOptContext());
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
                String packageName = mPkgInfo.packageName;
                newVersion = mPkgInfo.versionName;
                versionInfo = String.format("%s %20s", res.getString(R.string.new_version), newVersion);
                try {
                    pi = mPm.getPackageInfo(packageName, 0);
                    currentVersion = pi.versionName;
                    versionInfo += String.format("%s %20s", res.getString(R.string.current_version), currentVersion);
                } catch (PackageManager.NameNotFoundException e) {
                    if (enableDebug) {
                        xlog_start("autoInstallHook - Current version not found");
                        xlog("", e);
                        xlog_end("autoInstallHook - Current version not found");
                    }
                }
                if (enableVersion && !enableVersionCode) {
                    if (enableVersionInline) {
                        if (view != null) {
                            CharSequence temp = view.getText();
                            temp = temp + "\n\n" + versionInfo + "\n";
                            view.setText(temp);
                        }
                    }

                    if (enableVersionToast) {
                        Toast.makeText(mContext, versionInfo, Toast.LENGTH_LONG)
                                .show();
                    }
                }
                newCode = mPkgInfo.versionCode;
                versionCode = String.format("%s %10d", res.getString(R.string.new_version_code), newCode);
                try {
                    pi2 = mPm.getPackageInfo(packageName, 0);
                    currentCode = pi2.versionCode;
                    versionCode += String.format("%s %10d", res.getString(R.string.current_version_code), currentCode);
                } catch (PackageManager.NameNotFoundException e) {
                    if (enableDebug) {
                        xlog_start("autoInstallHook - Current version code not found");
                        xlog("", e);
                        xlog_end("autoInstallHook - Current version code not found");
                    }
                }
                if (enableVersionCode && !enableVersion) {
                    if (enableVersionInline) {
                        if (view != null) {
                            CharSequence temp = view.getText();
                            temp = temp
                                    + "\n\n"
                                    + versionCode
                                    + "\n";
                            view.setText(temp);
                        }
                    }

                    if (enableVersionToast) {
                        Toast.makeText(mContext, versionCode, Toast.LENGTH_LONG)
                                .show();
                    }
                }

                if (enableDebug) {
                    xlog_start("autoInstallHook");
                    if (currentVersion != null) {
                        xlog("Current version: ", currentVersion);
                    }
                    xlog("New version: ", newVersion);
                    if (currentCode != 0) {
                        xlog("Current version code: ", currentCode);
                    }
                    xlog("New version code", newCode);
                    xlog("Current application", mContext.toString());
                    xlog("Current button", mOk.toString());
                    xlog("Current package info", mPkgInfo.toString());
                    xlog("Current package name", packageName);
                    xlog("checkSignatures", checkSignatures);
                    xlog_end("autoInstallHook");
                }

                if (enableVersion && enableVersionCode) {
                    String versionAll = versionInfo + "\n\n" + versionCode;
                    if (enableVersionInline) {
                        if (view != null) {
                            CharSequence temp = view.getText();
                            temp = temp + "\n\n" + versionAll + "\n";
                            view.setText(temp);
                        }
                    }

                    if (enableVersionToast) {
                        Toast.makeText(mContext, versionAll, Toast.LENGTH_LONG)
                                .show();
                    }
                }

                if (enableAutoInstall) {
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

                if (confirmCheckSignatures && !checkSignatures) {
                    Intent confirmSignatureCheck = new Intent(
                            Common.ACTION_CONFIRM_CHECK_SIGNATURE);
                    confirmSignatureCheck.setPackage(Common.PACKAGE_NAME);
                    getInstallerOptContext().sendBroadcast(confirmSignatureCheck);

                }
                /*ScrollView mScrollView = (ScrollView) XposedHelpers.getObjectField(
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
                mContext = AndroidAppHelper.currentApplication();
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
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
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                enableAutoCloseUninstall = getPref(Common.PREF_ENABLE_AUTO_CLOSE_UNINSTALL, getInstallerOptContext());
                if (enableAutoCloseUninstall) {
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
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                enableAutoUninstall = getPref(Common.PREF_ENABLE_AUTO_UNINSTALL, getInstallerOptContext());
                if (enableAutoUninstall) {
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
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                enableAutoUninstall = getPref(Common.PREF_ENABLE_AUTO_UNINSTALL, getInstallerOptContext());
                if (enableAutoUninstall) {
                    try {
                        Button mOk = (Button) XposedHelpers.getObjectField(
                                param.thisObject, "mOk");
                        if (mOk != null) {
                            mOk.performClick();
                        }
                    } catch (NoSuchFieldError nsfe) {
                        xlog_start("autoUninstallHook");
                        xlog("Error caught: ", nsfe);
                        xlog_end("autoUninstallHook");
                    }
                }
            }
        };

        bootCompletedHook = new XC_MethodHook() {
            @Override
            public void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                Log.i(TAG, "bootCompletedHook: bootCompleted value before changing " + bootCompleted);
                bootCompleted = true;
                Log.i(TAG, "bootCompletedHook: bootCompleted after changing " + bootCompleted);
                mContext = AndroidAppHelper.currentApplication();
                setPref(mContext, Common.PREF_MODIFIED_PREFERENCES, false, 0, 0, null);
            }
        };

        checkDuplicatedPermissionsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                prefs.reload();
                checkDuplicatedPermissions = prefs.getBoolean(Common.PREF_DISABLE_CHECK_DUPLICATED_PERMISSION, false);
                if (checkDuplicatedPermissions) {
                    xlog("Disable check duplicate permissions set to", checkDuplicatedPermissions);
                    param.setResult(true);
                    return;
                }
            }
        };

        checkPermissionsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                prefs.reload();
                checkPermissions = prefs.getBoolean(Common.PREF_DISABLE_CHECK_PERMISSION, false);
                if (checkPermissions) {
                    xlog("Disable check permissions set to", checkPermissions);
                    param.setResult(PackageManager.PERMISSION_GRANTED);
                    return;
                }
            }
        };

        checkSdkVersionHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                prefs.reload();
                checkSdkVersion = prefs.getBoolean(Common.PREF_DISABLE_CHECK_SDK_VERSION, false);
                if (checkSdkVersion) {
                    xlog("checkSdkVersion set to", checkSdkVersion);
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
                if (/*disableCheckSignatures && */checkSignatures) {
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
                mContext = AndroidAppHelper.currentApplication();
                debugApps = getPref(Common.PREF_ENABLE_DEBUG_APP, getInstallerOptContext());
                int id = 5;
                int flags = (Integer) param.args[id];
                if (debugApps) {
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
                if (keepAppsData && (flags & Common.DELETE_KEEP_DATA) == 0) {
                    flags |= Common.DELETE_KEEP_DATA;
                }

                if (uninstallBackground && Binder.getCallingUid() == Common.ROOT_UID) {
                    param.setResult(null);
                    if (enableDebug) {
                        Toast.makeText(mContext, "Background uninstall attempt blocked", Toast.LENGTH_LONG)
                                .show();
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
                if (deviceAdmins) {
                    if (enableDebug) {
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
                if (disableUserApps) {
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
                if (disableUserApps) {
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

        getPackageInfoHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                /*try {
                    checkLuckyPatcher = getPref(Common.PREF_DISABLE_CHECK_LUCKY_PATCHER, getInstallerOptContext());
                } catch (Throwable e) {
                    checkLuckyPatcher = prefs.getBoolean(Common.PREF_DISABLE_CHECK_LUCKY_PATCHER, false);
                }*/
                if (checkLuckyPatcher) {
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
                }
            }
        };

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
                    hideAppCrashes = prefs.getBoolean(Common.PREF_ENABLE_HIDE_APP_CRASHES, false);
                    enableDebug = prefs.getBoolean(Common.PREF_ENABLE_DEBUG, false);
                    if (enableDebug) {
                        Log.i(TAG, "hideAppCrashes set via shared prefs");
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "hideAppCrashes error via shared prefs: ", e);
                }
                if (hideAppCrashes) {
                    try {
                        if (enableDebug) {
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
                        xlog("hideAppCrashes: ", e);
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
                if (enableAppStorageSettingsButtons) {
                    if (enableDebug) {
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

        initUninstallButtonsHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                disableUserApps = getPref(Common.PREF_ENABLE_DISABLE_USER_APPS, getInstallerOptContext());
                if (disableUserApps) {
                    if (enableDebug) {
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
                //add below to prefs
                downgradeApps = getPref(Common.PREF_ENABLE_DOWNGRADE_APP, getInstallerOptContext());
                forwardLock = getPref(Common.PREF_DISABLE_FORWARD_LOCK, getInstallerOptContext());
                installAppsOnExternal = getPref(Common.PREF_ENABLE_INSTALL_EXTERNAL_STORAGE, getInstallerOptContext());
                installBackground = getPref(Common.PREF_DISABLE_INSTALL_BACKGROUND, getInstallerOptContext());
                mContext = (Context) XposedHelpers.getObjectField(
                        param.thisObject, "mContext");
                boolean isInstallStage = "installStage".equals(param.method
                        .getName());
                int flags = 0;
                int id = 0;

                if (isInstallStage) {
                    try {
                        id = 4;
                        flags = (Integer) XposedHelpers.getObjectField(
                                param.args[id], "installFlags");
                        if (enableDebug) {
                            xlog_start("isInstallStage");
                            xlog("isInstallStage equals", isInstallStage);
                            xlog("flags", flags);
                            xlog_end("isInstallStage");
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
                        if (enableDebug) {
                            xlog_start("isInstallStage");
                            xlog("isInstallStage equals", isInstallStage);
                            xlog("id", id);
                            xlog("flags", flags);
                            xlog_end("isInstallStage");
                        }
                    } catch (Exception e) {
                        XposedBridge.log(e);
                        XposedBridge.log("Stacktrace follows:");
                        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                            XposedBridge.log("HookDetection: " + stackTraceElement.getClassName() + "->" + stackTraceElement.getMethodName());
                        }
                    }
                }
                if (downgradeApps) {
                    if ((flags & Common.INSTALL_ALLOW_DOWNGRADE) == 0) {
                        flags |= Common.INSTALL_ALLOW_DOWNGRADE;
                    }
                }
                if (forwardLock) {
                    if ((flags & Common.INSTALL_FORWARD_LOCK) != 0) {
                        flags &= ~Common.INSTALL_FORWARD_LOCK;
                    }
                }
                if (installAppsOnExternal) {
                    if ((flags & Common.INSTALL_EXTERNAL) == 0) {
                        flags |= Common.INSTALL_EXTERNAL;
                    }
                }

                if (isInstallStage) {
                    Object sessions = param.args[id];
                    XposedHelpers.setIntField(sessions, "installFlags",
                            flags);
                    param.args[id] = sessions;
                    if (enableDebug) {
                        xlog("sessions", sessions);
                    }
                } else {
                    param.args[id] = flags;
                }

                if (installBackground && Binder.getCallingUid() == Common.ROOT_UID) {
                    param.setResult(null);
                    if (enableDebug) {
                        Toast.makeText(mContext, "Background install attempt blocked", Toast.LENGTH_LONG)
                                .show();
                    }
                }

                if (backupApkFiles && backupDir != null) {
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
                            if (enableDebug) {
                                xlog_start("backupApkFilesHook");
                                xlog("APK file: ", apkFile);
                                xlog_end("backupApkFilesHook");
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
                disableCheckSignatures = false;
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                disableCheckSignatures = true;
            }
        };

        showButtonsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                showButtons = getPref(Common.PREF_ENABLE_SHOW_BUTTON, getInstallerOptContext());
                if (showButtons) {
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
                if (disableSystemApps) {
                    param.setResult(false);
                    return;
                }

            }
        };

        updatePrefsHook = new XC_MethodHook() {
            @Override
            public void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                //mContext = AndroidAppHelper.currentApplication();
                //updatePrefs(null);
                prefsChanged = getPref(Common.PREF_MODIFIED_PREFERENCES, getInstallerOptContext());
            }
        };

        unknownAppsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                installUnknownApps = getPref(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP, getInstallerOptContext());
                if (installUnknownApps) {
                    param.setResult(true);
                    return;
                }

            }
        };

        verifyAppsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                //prefs.reload();
                mContext = AndroidAppHelper.currentApplication();
                verifyApps = getPref(Common.PREF_DISABLE_VERIFY_APP, getInstallerOptContext());
                if (verifyApps) {
                    xlog_start("verifyAppsHook");
                    xlog("Disable app verification set to", verifyApps);
                    xlog_end("verifyAppsHook");
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
                prefs.reload();
                verifyJar = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_JAR, false);
                if (verifyJar) {
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
                prefs.reload();
                verifySignature = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_SIGNATURE, false);
                if (verifySignature) {
                    /*xlog_start("verifySignatureHook");
                    xlog("Disable signature verification set to: ", verifySignature);
                    xlog_end("verifySignatureHook");*/
                    param.setResult(true);
                    return;
                }
                //}
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
                if (checkSignatures) {
                    /*xlog_start("verifySignaturesHook");
                    xlog("Disable signature checking set to", checkSignatures);
                    xlog_end("verifySignaturesHook");*/
                    param.setResult(null);
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
        /*} else if (resparam.packageName.equals(Common.GOOGLE_PACKAGEINSTALLER_PKG)) {
            resparam.res.hookLayout(Common.GOOGLE_PACKAGEINSTALLER_PKG, "layout", "install_confirm", autoInstallHook2);*/
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
            }*/

            if (Common.ANDROID_PKG.equals(lpparam.packageName)
                    && Common.ANDROID_PKG.equals(lpparam.processName)) {
                Class<?> packageManagerClass = XposedHelpers.findClass(
                        Common.PACKAGEMANAGERSERVICE, lpparam.classLoader);
                Class<?> devicePolicyManagerClass = XposedHelpers.findClass(
                        Common.DEVICEPOLICYMANAGERSERVICE, lpparam.classLoader);
                Class<?> packageParserClass = XposedHelpers.findClass(
                        Common.PACKAGEPARSER, lpparam.classLoader);
                Class<?> jarVerifierClass = XposedHelpers.findClass(
                        Common.JARVERIFIER, lpparam.classLoader);
                Class<?> signatureClass = XposedHelpers.findClass(Common.SIGNATURE,
                        lpparam.classLoader);
                Class<?> appErrorDialogClass = XposedHelpers.findClass(
                        Common.APPERRORDIALOG, lpparam.classLoader);

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
                XposedBridge.hookAllMethods(Process.class, "start",
                        debugAppsHook);

                // 4.0 and newer
                XposedBridge.hookAllConstructors(appErrorDialogClass,
                        hideAppCrashesHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(View.class,
                        "onFilterTouchEventForSecurity", showButtonsHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(devicePolicyManagerClass,
                        "packageHasActiveAdmins", deviceAdminsHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(jarVerifierClass, "verify",
                        verifyJarHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(MessageDigest.class, "isEqual",
                        verifySignatureHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(signatureClass, "verify",
                        verifySignatureHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass,
                        "verifySignaturesLP", verifySignaturesHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass,
                        "isVerificationEnabled", verifyAppsHook);

            /*// 4.0 and newer
            XposedBridge.hookAllMethods(packageManagerClass, "getPackageInfo",
                    getPackageInfoHook);*/

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
                XposedBridge.hookAllMethods(packageManagerClass, "scanPackageLI",
                        scanPackageHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass, "checkPermission",
                        checkPermissionsHook);

                // 4.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass,
                        "checkUidPermission", checkPermissionsHook);

                if (Common.LOLLIPOP_NEWER) {
                    // 5.0 and newer
                    XposedBridge.hookAllMethods(packageManagerClass,
                            "checkUpgradeKeySetLP", checkDuplicatedPermissionsHook);
                }

                if (Common.LOLLIPOP_NEWER) {
                    // 5.0 and newer
                    XposedBridge.hookAllMethods(packageManagerClass,
                            "installPackageAsUser", installPackageHook);
                /*XposedBridge.hookAllMethods(packageManagerClass,
                        "installPackageLI", installPackageHook);*/
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

                // 5.0 and newer
                XposedBridge.hookAllMethods(packageManagerClass,
                        "deletePackage", deletePackageHook);

            }

            if (lpparam.packageName.equals(Common.PACKAGEINSTALLER_PKG) || lpparam.packageName.equals(Common.GOOGLE_PACKAGEINSTALLER_PKG) || lpparam.packageName.equals(Common.MOKEE_PACKAGEINSTALLER_PKG) || resparam.packageName.equals(Common.SAMSUNG_PACKAGEINSTALLER_PKG)) {
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
                        }
                    }
                } else {
                    // 4.0 - 5.0
                    XposedHelpers.findAndHookMethod(
                            Common.PACKAGEINSTALLERACTIVITY, lpparam.classLoader,
                            "isInstallingUnknownAppsAllowed", unknownAppsHook);
                }

                try {
                    XposedHelpers.findAndHookMethod(Common.PACKAGEINSTALLERGRANTPERMISSIONSACTIVITY,
                            lpparam.classLoader, "onBackPressed", grantPermissionsBackButtonHook);
                } catch (NoSuchMethodError nsme) {
                    xlog_start("grantPermissionsBackButtonHook");
                    xlog("Method not found", nsme);
                    xlog_end("grantPermissionsBackButtonHook");
                }

                XposedHelpers.findAndHookMethod(Common.PACKAGEINSTALLERACTIVITY,
                        lpparam.classLoader, "startInstallConfirm",
                        autoInstallHook);

                XposedHelpers.findAndHookMethod(Common.INSTALLAPPPROGRESS + "$1",
                        lpparam.classLoader, "handleMessage", Message.class,
                        autoCloseInstallHook);

                // 4.0 and newer
                XposedHelpers.findAndHookMethod(Common.INSTALLAPPPROGRESS,
                        lpparam.classLoader, "initView", autoHideInstallHook);

                // 4.0 - 4.4
                XposedHelpers.findAndHookMethod(Common.UNINSTALLAPPPROGRESS,
                        lpparam.classLoader, "initView", autoCloseUninstallHook);

                if (Common.KITKAT_NEWER) {
                    // 4.4 and newer
                    XposedHelpers.findAndHookMethod(
                            Common.PACKAGEINSTALLERACTIVITY, lpparam.classLoader,
                            "isVerifyAppsEnabled", verifyAppsHook);
                }

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
            }

            if (lpparam.packageName.equals(Common.SETTINGS_PKG)) {
                disableChangerClass = XposedHelpers.findClass(
                        Common.INSTALLEDAPPDETAILS + ".DisableChanger",
                        lpparam.classLoader);

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

                // 4.2 and newer
                if (Common.JB_MR1_NEWER) {
                    XposedHelpers.findAndHookMethod(Common.INSTALLEDAPPDETAILS,
                            lpparam.classLoader, "initUninstallButtons",
                            initUninstallButtonsHook);
                }

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

                XposedHelpers.findAndHookMethod(Common.INSTALLEDAPPDETAILS,
                        lpparam.classLoader, "setAppLabelAndIcon",
                        PackageInfo.class, appInfoHook);

                // 4.2 and newer
                if (Common.JB_MR1_NEWER) {
                    XposedHelpers.findAndHookMethod(Common.APPSTORAGEDETAILS,
                            lpparam.classLoader, "initDataButtons",
                            initAppStorageSettingsButtonsHook);
                }
            }
        } catch (Throwable t) {
            xlog_start("handleLoadPackage");
            xlog("handleLoadPackage error caught: ", t);
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
        return MultiprocessPreferences.getDefaultSharedPreferences(context).getBoolean(pref, false);
    }

    public static String getPrefString(String pref, Context context) {
        return MultiprocessPreferences.getDefaultSharedPreferences(context).getString(pref, null);
    }

    public static long getPrefLong(String pref, Context context) {
        return MultiprocessPreferences.getDefaultSharedPreferences(context).getLong(pref, 0);
    }

    public boolean isExpertModeEnabled() {
        mContext = AndroidAppHelper.currentApplication();
        //Add below to prefs
        try {
            boolean enabled = getPref(Common.PREF_ENABLE_EXPERT_MODE, getInstallerOptContext());
            return enabled;
        } catch (Throwable e) {
            boolean enabled = prefs.getBoolean(Common.PREF_ENABLE_EXPERT_MODE, false);
            return enabled;
        }
        //return enabled;
    }

    public void updatePrefs() {
        //if (context == null) {
        //prefs.reload();
        xlog_start("updatePrefs");
        xlog("Preferences reloaded via sharedpreferences", null);
        xlog_end("updatePrefs");
        backupApkFiles = prefs.getBoolean(Common.PREF_ENABLE_BACKUP_APK_FILE, false);
        checkDuplicatedPermissions = prefs.getBoolean(Common.PREF_DISABLE_CHECK_DUPLICATED_PERMISSION, false);
        checkLuckyPatcher = prefs.getBoolean(Common.PREF_DISABLE_CHECK_LUCKY_PATCHER, false);
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
        installUnknownApps = prefs.getBoolean(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP, false);
        keepAppsData = prefs.getBoolean(Common.PREF_ENABLE_KEEP_APP_DATA, false);
        showButtons = prefs.getBoolean(Common.PREF_ENABLE_SHOW_BUTTON, false);
        uninstallBackground = prefs.getBoolean(Common.PREF_DISABLE_UNINSTALL_BACKGROUND, false);
        uninstallSystemApps = prefs.getBoolean(Common.PREF_ENABLE_UNINSTALL_SYSTEM_APP, false);
        verifyApps = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_APP, false);
        verifyJar = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_JAR, false);
        verifySignature = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_SIGNATURE, false);
        /*} else {
            xlog_start("updatePrefs");
            xlog("Preferences reloaded via context", null);
            xlog_end("updatePrefs");
            backupApkFiles = getPref(Common.PREF_ENABLE_BACKUP_APK_FILE, context);
            checkDuplicatedPermissions = getPref(Common.PREF_DISABLE_CHECK_DUPLICATED_PERMISSION, context);
            checkLuckyPatcher = getPref(Common.PREF_DISABLE_CHECK_LUCKY_PATCHER, context);
            checkPermissions = getPref(Common.PREF_DISABLE_CHECK_PERMISSION, context);
            checkSdkVersion = getPref(Common.PREF_DISABLE_CHECK_SDK_VERSION, context);
            checkSignatures = getPref(Common.PREF_DISABLE_CHECK_SIGNATURE, context);
            debugApps = getPref(Common.PREF_ENABLE_DEBUG_APP, context);
            deleteApkFiles = getPref(Common.PREF_ENABLE_DELETE_APK_FILE_INSTALL, context);
            deviceAdmins = getPref(Common.PREF_ENABLE_UNINSTALL_DEVICE_ADMIN, context);
            disableSystemApps = getPref(Common.PREF_ENABLE_DISABLE_SYSTEM_APP, context);
            disableUserApps = getPref(Common.PREF_ENABLE_DISABLE_USER_APPS, context);
            downgradeApps = getPref(Common.PREF_ENABLE_DOWNGRADE_APP, context);
            enableAppStorageSettingsButtons = getPref(Common.PREF_ENABLE_APP_STORAGE_BUTTONS, context);
            enableAutoHideInstall = getPref(Common.PREF_ENABLE_AUTO_HIDE_INSTALL, context);
            enableAutoInstall = getPref(Common.PREF_ENABLE_AUTO_INSTALL, context);
            enableAutoCloseInstall = getPref(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL, context);
            enableAutoLaunchInstall = getPref(Common.PREF_ENABLE_AUTO_LAUNCH_INSTALL, context);
            enableAutoCloseUninstall = getPref(Common.PREF_ENABLE_AUTO_CLOSE_UNINSTALL, context);
            enableAutoUninstall = getPref(Common.PREF_ENABLE_AUTO_UNINSTALL, context);
            enableDebug = getPref(Common.PREF_ENABLE_DEBUG, context);
            enableLaunchApp = getPref(Common.PREF_ENABLE_LAUNCH_APP, context);
            enablePackageName = getPref(Common.PREF_ENABLE_SHOW_PACKAGE_NAME, context);
            enablePlay = getPref(Common.PREF_ENABLE_OPEN_APP_GOOGLE_PLAY, context);
            enableVersion = getPref(Common.PREF_ENABLE_SHOW_VERSION, context);
            enableVersionCode = getPref(Common.PREF_ENABLE_SHOW_VERSION_CODE, context);
            enableVersionInline = getPref(Common.PREF_ENABLE_SHOW_VERSION_INLINE, context);
            enableVersionToast = getPref(Common.PREF_ENABLE_SHOW_VERSION_TOAST, context);
            enableVibrateDevice = getPref(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL_VIBRATE, context);
            forwardLock = getPref(Common.PREF_DISABLE_FORWARD_LOCK, context);
            hideAppCrashes = getPref(Common.PREF_ENABLE_HIDE_APP_CRASHES, context);
            installAppsOnExternal = getPref(Common.PREF_ENABLE_INSTALL_EXTERNAL_STORAGE, context);
            installBackground = getPref(Common.PREF_DISABLE_INSTALL_BACKGROUND, context);
            installUnknownApps = getPref(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP, context);
            keepAppsData = getPref(Common.PREF_ENABLE_KEEP_APP_DATA, context);
            showButtons = getPref(Common.PREF_ENABLE_SHOW_BUTTON, context);
            uninstallBackground = getPref(Common.PREF_DISABLE_UNINSTALL_BACKGROUND, context);
            uninstallSystemApps = getPref(Common.PREF_ENABLE_UNINSTALL_SYSTEM_APP, context);
            verifyApps = getPref(Common.PREF_DISABLE_VERIFY_APP, context);
            verifyJar = getPref(Common.PREF_DISABLE_VERIFY_JAR, context);
            verifySignature = getPref(Common.PREF_DISABLE_VERIFY_SIGNATURE, context);
        }*/

        /*public static boolean autoInstallCancelled;
        public static boolean bootCompleted;
        public static boolean confirmCheckSignatures;
        public static boolean disableCheckSignatures;*/
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

    public void vibrateDevice(int duration) {
        Intent vibrateDevice = new Intent(
                Common.ACTION_VIBRATE_DEVICE);
        //uninstallSystemApp.setPackage(Common.PACKAGE_NAME);
        vibrateDevice.putExtra(Common.DURATION, duration);
        getInstallerOptContext().sendBroadcast(vibrateDevice);
    }

    public void uninstallSystemApp(String packageName) {
        Intent uninstallSystemApp = new Intent(
                Common.ACTION_UNINSTALL_SYSTEM_APP);
        uninstallSystemApp.setPackage(Common.PACKAGE_NAME);
        uninstallSystemApp.putExtra(Common.PACKAGE, packageName);
        getInstallerOptContext().sendBroadcast(uninstallSystemApp);
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
