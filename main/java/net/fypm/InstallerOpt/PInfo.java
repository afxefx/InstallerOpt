package net.fypm.InstallerOpt;

import android.graphics.drawable.Drawable;

import java.util.Comparator;

public class PInfo implements Comparable<PInfo> {
    private String appname;
    private String pname;
    private int uid;
    private String versionName;
    private int versionCode;
    private Drawable appicon;
    private String itemSize;
    private String formattedDate;
    private String calculatedDigest;
    private String apkName;
    private String state;

    public PInfo(String appname, String pname, int uid, String versionName, int versionCode, Drawable appicon, String itemSize, String formattedDate, String calculatedDigest, String apkName, String state) {
        this.appname = appname;
        this.pname = pname;
        this.uid = uid;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.appicon = appicon;
        this.itemSize = itemSize;
        this.formattedDate = formattedDate;
        this.calculatedDigest = calculatedDigest;
        this.apkName = apkName;
        this.state = state;
    }

    public int compareTo(PInfo other) {
        return appname.compareTo(other.appname);
    }

    public static Comparator<PInfo> COMPARE_BY_APKNAME = new Comparator<PInfo>() {
        public int compare(PInfo one, PInfo other) {
            return one.apkName.compareTo(other.apkName);
        }
    };

    public static Comparator<PInfo> COMPARE_BY_SIZE = new Comparator<PInfo>() {
        public int compare(PInfo one, PInfo other) {
            return one.itemSize.compareTo(other.itemSize);
        }
    };

    public static Comparator<PInfo> COMPARE_BY_DATE = new Comparator<PInfo>() {
        public int compare(PInfo one, PInfo other) {
            return one.formattedDate.compareTo(other.formattedDate);
        }
    };

    public String getName() {
        return appname;
    }

    public Drawable getAppIcon() {
        return appicon;
    }

    public int getUid() {
        return uid;
    }

    public String getPackageName() {
        return pname;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getItemSize() {
        return itemSize;
    }

    public String getItemModified() {
        return formattedDate;
    }

    public String getMD5() {
        return calculatedDigest;
    }

    public String getApkName() {
        return apkName;
    }

    public String getState() {
        return state;
    }
}
