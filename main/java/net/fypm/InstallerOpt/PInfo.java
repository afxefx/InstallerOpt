package net.fypm.InstallerOpt;

import android.graphics.drawable.Drawable;

import java.util.Comparator;
import java.util.StringTokenizer;

public class PInfo implements Comparable<PInfo> {
    private String appname;
    private String pname;
    private int uid;
    private String versionName;
    private int versionCode;
    private Drawable appicon;
    private long itemSize;
    private String itemSizeHuman;
    private String formattedDate;
    private String calculatedDigest;
    private String apkName;
    private String status;
    private String sourceDir;

    public PInfo(String appname, String pname, int uid, String versionName, int versionCode, Drawable appicon, long itemSize, String itemSizeHuman, String formattedDate, String calculatedDigest, String apkName, String status, String sourceDir) {
        this.appname = appname;
        this.pname = pname;
        this.uid = uid;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.appicon = appicon;
        this.itemSize = itemSize;
        this.itemSizeHuman = itemSizeHuman;
        this.formattedDate = formattedDate;
        this.calculatedDigest = calculatedDigest;
        this.apkName = apkName;
        this.status = status;
        this.sourceDir = sourceDir;
    }

    public int compareTo(PInfo other) {
        return appname.compareTo(other.appname);
    }

    public static Comparator<PInfo> COMPARE_BY_APKNAME = new Comparator<PInfo>() {
        public int compare(PInfo one, PInfo other) {
            return one.apkName.compareTo(other.apkName);
        }
    };

    public static Comparator<PInfo> COMPARE_BY_DATE = new Comparator<PInfo>() {
        public int compare(PInfo one, PInfo other) {
            return one.formattedDate.compareTo(other.formattedDate);
        }
    };

    public static Comparator<PInfo> COMPARE_BY_SIZE = new Comparator<PInfo>() {
        public int compare(PInfo one, PInfo other) {
            return Long.valueOf(one.itemSize).compareTo(Long.valueOf(other.itemSize));
        }
    };

    public static Comparator<PInfo> COMPARE_BY_STATUS = new Comparator<PInfo>() {
        public int compare(PInfo one, PInfo other) {
            return one.status.compareTo(other.status);
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

    public long getItemSize() {
        return itemSize;
    }

    public String getItemSizeHuman() {
        return itemSizeHuman;
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

    public String getStatus() {
        return status;
    }

    public String getSourceDir() {
        return sourceDir;
    }
}
