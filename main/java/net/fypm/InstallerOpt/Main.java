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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;

import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Hashtable;

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XSharedPreferences;

public class Main implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    public XC_MethodHook appInfoHook;
    public XC_MethodHook autoCloseInstallHook;
    public XC_MethodHook autoHideInstallHook;
    public XC_MethodHook autoInstallHook;
    public XC_MethodHook autoCloseUninstallHook;
    public XC_MethodHook autoUninstallHook;
    public XC_MethodHook bootCompletedHook;
    public XC_MethodHook checkDuplicatedPermissionsHook;
    public XC_MethodHook checkPermissionsHook;
    public XC_MethodHook checkSdkVersionHook;
    public XC_MethodHook checkSignaturesHook;
    public XC_MethodHook debugAppsHook;
    public XC_MethodHook deviceAdminsHook;
    public XC_MethodHook disableChangerHook;
    public XC_MethodHook disableUserAppsHook;
    public XC_MethodHook getPackageInfoHook;
    public XC_MethodHook hideAppCrashesHook;
    public XC_MethodHook initUninstallButtonsHook;
    public XC_MethodHook installPackageHook;
    public XC_MethodHook scanPackageHook;
    public XC_MethodHook showButtonsHook;
    public XC_MethodHook systemAppsHook;
    public XC_MethodHook unknownAppsHook;
    public XC_MethodHook verifyAppsHook;
    public XC_MethodHook verifyJarHook;
    public XC_MethodHook verifySignatureHook;
    public XC_MethodHook verifySignaturesHook;

    public Class<?> disableChangerClass;
    public Context mContext;
    public static int appCrash;
    public static int errorCount;
    public static boolean bootCompleted;
    public static XSharedPreferences prefs;
    public static boolean autoInstallCanceled;
    public static boolean backupApkFiles;
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
    public static boolean enableAutoHideInstall;
    public static boolean enableAutoInstall;
    public static boolean enableAutoInstallClose;
    public static boolean enableAutoInstallLaunch;
    public static boolean enableAutoCloseUninstall;
    public static boolean enableAutoUninstall;
    public static boolean enableDebug;
    public static boolean enableLaunchApp;
    public static boolean enablePackageName;
    public static boolean enablePlay;
    public static boolean enableVersion;
    public static boolean enableVersionCode;
    public static boolean forwardLock;
    public static boolean hideAppCrashes;
    public static boolean installAppsOnExternal;
    public static boolean installBackground;
    public static boolean installUnknownApps;
    public static boolean showButtons;
    public static boolean uninstallSystemApps;
    public static boolean verifyApps;
    public static boolean verifyJar;
    public static boolean verifySignature;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {

        appCrash = 0;
        errorCount = 0;

        //xlog("bootCompleted value at initZygote", bootCompleted);

        xlog_start("XSharedPreferences - Init");
        try {
            //prefs = initPrefs();
            prefs = new XSharedPreferences(Main.class.getPackage().getName());
            prefs.makeWorldReadable();
            prefs.reload();
            //initPrefs();
            xlog("Success", null);
        } catch (Throwable e) {
            xlog("", e);
        }
        xlog_end("XSharedPreferences - Init");

        disableCheckSignatures = true;
        //checkPermissions = prefs.getBoolean(Common.PREF_DISABLE_CHECK_PERMISSION, false);
        //checkSignatures = true;//prefs.getBoolean(Common.PREF_DISABLE_CHECK_SIGNATURE, false);
        //verifySignature = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_SIGNATURE, false);

        xlog_start("Signature Checking and Verification Overview");
        xlog("disableCheckSignatures status", disableCheckSignatures);
        xlog("checkSignatures status ", checkSignatures);
        xlog("verifySignature status", verifySignature);
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
                enableAutoInstallClose = getPref(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL, getInstallerOptContext());
                enableAutoInstallLaunch = getPref(Common.PREF_ENABLE_LAUNCH_INSTALL, getInstallerOptContext());
                deleteApkFiles = getPref(Common.PREF_ENABLE_DELETE_APK_FILE_INSTALL, getInstallerOptContext());

                Button mLaunch = (Button) XposedHelpers.getObjectField(
                        XposedHelpers.getSurroundingThis(param.thisObject),
                        "mLaunchButton");

                Message msg = (Message) param.args[0];
                boolean installedApp = false;
                if (msg != null) {
                    installedApp = (msg.arg1 == Common.INSTALL_SUCCEEDED);
                }

                if (enableAutoInstallLaunch) {
                    if (installedApp && mLaunch != null) {
                        mLaunch.performClick();
                    }
                }

                if (enableAutoInstallClose) {
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
                        if (!appInstalledText.isEmpty()) {
                            Toast.makeText(mContext, appInstalledText,
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(mContext, "App not installed",
                                Toast.LENGTH_LONG).show();
                    }
                }

                if (deleteApkFiles) {
                    Uri packageUri = (Uri) XposedHelpers.getObjectField(
                            XposedHelpers.getSurroundingThis(param.thisObject),
                            "mPackageURI");
                    String apkFile = packageUri.getPath();
                    deleteApkFile(apkFile);
                }
            }
        };

        autoHideInstallHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                enableAutoHideInstall = getPref(Common.PREF_ENABLE_AUTO_HIDE_INSTALL, getInstallerOptContext());
                Activity packageInstaller = (Activity) param.thisObject;
                if (!autoInstallCanceled && enableAutoHideInstall) {
                    packageInstaller.onBackPressed();
                }
            }
        };

        autoInstallHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                enableDebug = getPref(Common.PREF_ENABLE_DEBUG, getInstallerOptContext());
                enableVersion = getPref(Common.PREF_ENABLE_SHOW_VERSION, getInstallerOptContext());
                enableVersionCode = getPref(Common.PREF_ENABLE_SHOW_VERSION_CODE, getInstallerOptContext());
                enableAutoInstall = getPref(Common.PREF_ENABLE_AUTO_INSTALL, getInstallerOptContext());
                //Add below two in prefs
                //checkSignatures = getPref("enabled_disable_sig_check", getInstallerOptContext());
                confirmCheckSignatures = getPref("enabled_confirm_check_signatures", getInstallerOptContext());
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
                versionInfo = res.getString(R.string.new_version)
                        + ": " + mPkgInfo.versionName;
                try {
                    pi = mPm.getPackageInfo(packageName, 0);
                    currentVersion = pi.versionName;
                    versionInfo += "\n"
                            + res.getString(R.string.current_version)
                            + ": " + currentVersion;
                } catch (PackageManager.NameNotFoundException e) {
                    if (enableDebug) {
                        xlog_start("autoInstallHook - Current version not found");
                        xlog("", e);
                        xlog_end("autoInstallHook - Current version not found");
                    }
                }
                if (enableVersion && !enableVersionCode) {
                    Toast.makeText(mContext, versionInfo, Toast.LENGTH_LONG)
                            .show();
                }

                   /*Button mOk2 = (Button) XposedHelpers.getObjectField(
                            param.thisObject, "mOk");
                    PackageManager mPm2 = mContext.getPackageManager();
                    PackageInfo mPkgInfo2 = (PackageInfo) XposedHelpers
                            .getObjectField(param.thisObject, "mPkgInfo");
                    Resources res2 = getInstallerOptContext().getResources();
                    String packageName2 = mPkgInfo.packageName;*/
                newCode = mPkgInfo.versionCode;
                versionCode = res.getString(R.string.new_version_code)
                        + ": " + mPkgInfo.versionCode;
                try {
                    pi2 = mPm.getPackageInfo(packageName, 0);
                    currentCode = pi2.versionCode;
                    versionCode += "\n"
                            + res.getString(R.string.current_version_code)
                            + ": " + currentCode;
                } catch (PackageManager.NameNotFoundException e) {
                    if (enableDebug) {
                        xlog_start("autoInstallHook - Current version code not found");
                        xlog("", e);
                        xlog_end("autoInstallHook - Current version code not found");
                    }
                }
                if (enableVersionCode && !enableVersion) {
                    Toast.makeText(mContext, versionCode, Toast.LENGTH_LONG)
                            .show();
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

                    Toast.makeText(mContext, versionAll, Toast.LENGTH_LONG)
                            .show();

                    /*String msg = (String) XposedHelpers.getObjectField(
                            param.thisObject, "msg");

                    msg += versionAll;*/

                }

                if (enableAutoInstall) {
                    if ((newVersion.equals(currentVersion) && newCode == currentCode) || newCode < currentCode) {
                        Toast.makeText(mContext, "Auto install cancelled due to matching version info and/or current version is newer than one being installed", Toast.LENGTH_LONG)
                                .show();
                        autoInstallCanceled = true;
                    } else {
                        autoInstallCanceled = false;
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
            }
        };

        autoCloseUninstallHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                //prefs.reload();
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

            /*@Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                enableAutoUninstall = getPref(Common.PREF_ENABLE_AUTO_UNINSTALL, getInstallerOptContext());

                if (enableAutoUninstall) {
                    Button mOk = (Button) XposedHelpers.getObjectField(
                            param.thisObject, "mOk");
                    if (mOk != null) {
                        mOk.performClick();
                    }

                }
            }*/

        };

        bootCompletedHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                //xlog_start("bootCompletedHook");
                //xlog("Boot completed status before calling bootCompletedHook method", bootCompleted);
                //ctx = getInstallerOptContext();
                //bootCompletedHook(ctx);
                //watcher();
                //initPrefs();
                //xlog("Reloading preferences after onPause method in InstallerOpt Main Activity", bootCompleted);
                //xlog_end("bootCompletedHook");
            }
        };

        checkDuplicatedPermissionsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
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
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                prefs.reload();
                checkPermissions = prefs.getBoolean(Common.PREF_DISABLE_CHECK_PERMISSION, false);
                if (/*isExpertModeEnabled() && */checkPermissions) {
                    xlog("Disable check permissions set to", checkPermissions);
                    param.setResult(PackageManager.PERMISSION_GRANTED);
                    return;
                }
            }
        };

        checkSdkVersionHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
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
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                try {
                    prefs.reload();
                    checkSignatures = prefs.getBoolean(Common.PREF_DISABLE_CHECK_SIGNATURE, false);
                } catch (Throwable e) {
                    checkSignatures = getPref(Common.PREF_DISABLE_CHECK_SIGNATURE, getInstallerOptContext());
                }
                if (disableCheckSignatures && checkSignatures) {
                    //xlog("Disable signature checks set to", checkSignatures);
                    param.setResult(PackageManager.SIGNATURE_MATCH);
                    return;
                }
            }
        };

        debugAppsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                debugApps = getPref(Common.PREF_ENABLE_DEBUG_APP, getInstallerOptContext());
                int id = 5;
                int flags = (Integer) param.args[id];
                if (debugApps) {
                    if ((flags & Common.DEBUG_ENABLE_DEBUGGER) == 0) {
                        flags |= Common.DEBUG_ENABLE_DEBUGGER;
                    }
                }
                //if (isModuleEnabled()) {
                param.args[id] = flags;
                //}
            }
        };

        deviceAdminsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                prefs.reload();
                deviceAdmins = prefs.getBoolean(Common.PREF_ENABLE_UNINSTALL_DEVICE_ADMIN, false);
                if (deviceAdmins) {
                    xlog("deviceAdmins set to", deviceAdmins);
                    param.setResult(false);
                    return;
                }

            }
        };

        disableChangerHook = new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param)
                    throws Throwable {
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
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                disableUserApps = getPref(Common.PREF_ENABLE_DISABLE_USER_APPS, getInstallerOptContext());
                if (disableUserApps) {
                    if ((Integer) param.args[0] == 9)
                        param.args[0] = 7;
                }
            }
        };

        getPackageInfoHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
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

        hideAppCrashesHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                //mContext = AndroidAppHelper.currentApplication();
                /*hideAppCrashes = getPref(Common.PREF_ENABLE_HIDE_APP_CRASHES, getInstallerOptContext());*/
                prefs.reload();
                hideAppCrashes = prefs.getBoolean(Common.PREF_ENABLE_HIDE_APP_CRASHES, false);
                if (hideAppCrashes) {
                    xlog("hideAppCrashes set to", hideAppCrashes);
                            XposedHelpers.setObjectField(param.thisObject,
                                    "DISMISS_TIMEOUT", 0);
                }
            }
        };

        initUninstallButtonsHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param)
                    throws Throwable {
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
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                backupApkFiles = getPref(Common.PREF_ENABLE_BACKUP_APK_FILE, getInstallerOptContext());
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
                    id = 4;
                    flags = (Integer) XposedHelpers.getObjectField(
                            param.args[id], "installFlags");
                    if (enableDebug) {
                        xlog_start("isInstallStage");
                        xlog("Install flags: ", flags);
                        xlog_end("isInstallStage");
                    }
                } else {
                    try {
                        id = Common.JB_MR1_NEWER ? 2 : 1;
                        xlog("id equals", id);
                        flags = (Integer) param.args[id];
                        xlog("flags equal", flags);
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
                } else {
                    param.args[id] = flags;
                }

                if (installBackground) {
                    if (Binder.getCallingUid() == Common.ROOT_UID) {
                        param.setResult(null);
                    }
                }

                if (backupApkFiles) {
                    if (!isInstallStage) {
                        String apkFile = null;
                        if (Common.LOLLIPOP_NEWER) {
                            apkFile = (String) param.args[0];
                        } else {
                            Uri packageUri = (Uri) param.args[0];
                            apkFile = packageUri.getPath();
                        }
                        if (apkFile != null) {
                            backupApkFile(apkFile);
                        }
                        if (enableDebug) {
                            xlog_start("backupApkFilesHook");
                            xlog("APK file: ", apkFile);
                            xlog_end("backupApkFilesHook");
                            XposedBridge.log("Stacktrace follows:");
                            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                                XposedBridge.log("HookDetection: " + stackTraceElement.getClassName() + "->" + stackTraceElement.getMethodName());
                            }
                        }
                    }
                }
            }
        };

        scanPackageHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                disableCheckSignatures = false;
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                disableCheckSignatures = true;
            }
        };

        showButtonsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
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
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                disableSystemApps = getPref(Common.PREF_ENABLE_DISABLE_SYSTEM_APP, getInstallerOptContext());
                if (disableSystemApps) {
                    param.setResult(false);
                    return;
                }

            }
        };

        unknownAppsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                mContext = AndroidAppHelper.currentApplication();
                //add below to prefs
                installUnknownApps = getPref(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP, getInstallerOptContext());
                if (installUnknownApps) {
                    param.setResult(true);
                    return;
                }

            }
        };

        verifyAppsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                //prefs.reload();
                verifyApps = getPref(Common.PREF_DISABLE_VERIFY_APP, getInstallerOptContext());
                if (disableCheckSignatures && verifyApps) {
                    xlog("Disable app verification set to", verifyApps);
                    param.setResult(false);
                    return;
                }
                //}
            }
        };

        verifyJarHook = new XC_MethodHook() {
            @SuppressWarnings("unchecked")
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
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
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                prefs.reload();
                verifySignature = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_SIGNATURE, false);
                if (disableCheckSignatures && verifySignature) {
                    //xlog("Disable signature verification set", verifySignature);
                    param.setResult(true);
                    return;
                }
                //}
            }
        };

        verifySignaturesHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                try {
                    prefs.reload();
                    checkSignatures = prefs.getBoolean(Common.PREF_DISABLE_CHECK_SIGNATURE, false);
                } catch (Throwable e) {
                    checkSignatures = getPref(Common.PREF_DISABLE_CHECK_SIGNATURE, getInstallerOptContext());
                }
                if (disableCheckSignatures && checkSignatures) {
                    //xlog("Disable signature checking set to", checkSignatures);
                    param.setResult(true);
                    return;
                }
            }
        };

    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        /*if (lpparam.packageName.equals(Common.SYSTEM_UI)) {
                XposedHelpers.findAndHookMethod(
                        Common.SYSTEMUIACTIVITY, lpparam.classLoader,
                        "onCreate", bootCompletedHook);
            }*/

        /*if (lpparam.packageName.equals(Common.INSTALLEROPT)) {
            XposedHelpers.findAndHookMethod(
                    Common.INSTALLEROPTACTIVITY, lpparam.classLoader,
                    "onPause", bootCompletedHook);
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

        }


        if (lpparam.packageName.equals(Common.PACKAGEINSTALLER_PKG) || lpparam.packageName.equals(Common.GOOGLE_PACKAGEINSTALLER_PKG)) {
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

        }
    }


    public void backupApkFile(String apkFile) {
        Intent backupApkFile = new Intent(Common.ACTION_BACKUP_APK_FILE);
        backupApkFile.setPackage(Common.PACKAGE_NAME);
        backupApkFile.putExtra(Common.FILE, apkFile);
        getInstallerOptContext().sendBroadcast(backupApkFile);
    }

    /*public void bootCompletedHook(Context context) {
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver,
                new IntentFilter("my-event"));


        // Unregister since the activity is not visible
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
            initPrefs();
        }
    };*/

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

    public static boolean getPref(String pref, Context context) {
        return MultiprocessPreferences.getDefaultSharedPreferences(context).getBoolean(pref, false);
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

    public void initPrefs() {
        /*prefs = new XSharedPreferences(Main.class.getPackage().getName());
        prefs.makeWorldReadable();
        prefs.reload();*/
        //autoInstallCanceled;
        backupApkFiles = prefs.getBoolean(Common.PREF_ENABLE_BACKUP_APK_FILE, false);;
        checkDuplicatedPermissions = prefs.getBoolean(Common.PREF_DISABLE_CHECK_DUPLICATED_PERMISSION, false);
        checkLuckyPatcher = prefs.getBoolean(Common.PREF_DISABLE_CHECK_LUCKY_PATCHER, false);
        checkPermissions = prefs.getBoolean(Common.PREF_DISABLE_CHECK_PERMISSION, false);
        checkSdkVersion = prefs.getBoolean(Common.PREF_DISABLE_CHECK_SDK_VERSION, false);
        checkSignatures = prefs.getBoolean(Common.PREF_DISABLE_CHECK_SIGNATURE, false);
        //confirmCheckSignatures;
        debugApps = prefs.getBoolean(Common.PREF_ENABLE_DEBUG_APP, false);
        deleteApkFiles = prefs.getBoolean(Common.PREF_ENABLE_DELETE_APK_FILE_INSTALL, false);
        deviceAdmins = prefs.getBoolean(Common.PREF_ENABLE_UNINSTALL_DEVICE_ADMIN, false);
        //disableCheckSignatures;
        disableSystemApps = prefs.getBoolean(Common.PREF_ENABLE_DISABLE_SYSTEM_APP, false);
        disableUserApps = prefs.getBoolean(Common.PREF_ENABLE_DISABLE_USER_APPS, false);
        downgradeApps = prefs.getBoolean(Common.PREF_ENABLE_DOWNGRADE_APP, false);
        enableAutoHideInstall = prefs.getBoolean(Common.PREF_ENABLE_AUTO_HIDE_INSTALL, false);
        enableAutoInstall = prefs.getBoolean(Common.PREF_ENABLE_AUTO_INSTALL, false);
        enableAutoInstallClose = prefs.getBoolean(Common.PREF_ENABLE_AUTO_CLOSE_INSTALL, false);
        enableAutoInstallLaunch = prefs.getBoolean(Common.PREF_ENABLE_LAUNCH_INSTALL, false);
        enableAutoCloseUninstall = prefs.getBoolean(Common.PREF_ENABLE_AUTO_CLOSE_UNINSTALL, false);
        enableAutoUninstall = prefs.getBoolean(Common.PREF_ENABLE_AUTO_UNINSTALL, false);
        enableDebug = prefs.getBoolean(Common.PREF_ENABLE_DEBUG, false);
        enableLaunchApp = prefs.getBoolean(Common.PREF_ENABLE_LAUNCH_APP, false);
        enablePackageName = prefs.getBoolean(Common.PREF_ENABLE_SHOW_PACKAGE_NAME, false);
        enablePlay = prefs.getBoolean(Common.PREF_ENABLE_OPEN_APP_GOOGLE_PLAY, false);
        enableVersion = prefs.getBoolean(Common.PREF_ENABLE_SHOW_VERSION, false);
        enableVersionCode = prefs.getBoolean(Common.PREF_ENABLE_SHOW_VERSION_CODE, false);
        forwardLock = prefs.getBoolean(Common.PREF_DISABLE_FORWARD_LOCK, false);
        hideAppCrashes = prefs.getBoolean(Common.PREF_ENABLE_HIDE_APP_CRASHES, false);
        installAppsOnExternal = prefs.getBoolean(Common.PREF_ENABLE_INSTALL_EXTERNAL_STORAGE, false);
        installBackground = prefs.getBoolean(Common.PREF_DISABLE_INSTALL_BACKGROUND, false);
        installUnknownApps = prefs.getBoolean(Common.PREF_ENABLE_INSTALL_UNKNOWN_APP, false);
        showButtons = prefs.getBoolean(Common.PREF_ENABLE_SHOW_BUTTON, false);
        uninstallSystemApps = prefs.getBoolean(Common.PREF_ENABLE_UNINSTALL_SYSTEM_APP, false);
        verifyApps = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_APP, false);
        verifyJar = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_JAR, false);
        verifySignature = prefs.getBoolean(Common.PREF_DISABLE_VERIFY_SIGNATURE, false);
    }

    public static void setPref(Context context, String pref, Boolean value) {
        MultiprocessPreferences.getDefaultSharedPreferences(context).edit().putBoolean(pref, value).apply();
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