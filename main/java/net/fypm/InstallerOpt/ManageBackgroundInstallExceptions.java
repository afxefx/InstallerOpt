package net.fypm.InstallerOpt;

import android.app.ListActivity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ManageBackgroundInstallExceptions extends ListActivity {

    private static final String TAG = "InstallerOpt";

    public ArrayList<PInfo> installedApps;
    public ArrayList<String> selectedItems;
    public ArrayAdapter la;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean enableDark = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_DARK_THEME, false);
        if (enableDark) {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.backup_list);

        //installedApps = getPackages();
        installedApps = getInstalledApps(false);
        selectedItems = new ArrayList<String>();
        if (installedApps != null) {
            Collections.sort(installedApps, new Comparator<PInfo>() {
                public int compare(PInfo one, PInfo other) {
                    return one.getName().compareTo(other.getName());
                }
            });

            ArrayAdapter<PInfo> adapter = new CustomExceptionListArrayAdapter(this, installedApps);
            setListAdapter(adapter);

            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CheckableLinearLayout item = (CheckableLinearLayout) view;
                    if (item.isChecked()) {
                        String selected = installedApps.get(position).getName();
                        selectedItems.add(selected);
                    } else {
                        String selected = installedApps.get(position).getName();
                        selectedItems.remove(selected);
                    }
                    Toast.makeText(ManageBackgroundInstallExceptions.this, selectedItems.toString(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manage_exceptions_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                if (selectedItems.size() > 0) {
                    MultiprocessPreferences.getDefaultSharedPreferences(ManageBackgroundInstallExceptions.this).edit().putString(Common.PREF_INSTALL_BACKGROUND_EXCEPTIONS, selectedItems.toString()).apply();
                    this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.exception_selection_empty, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.discard_menu:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private ArrayList<PInfo> getInstalledApps(boolean getSysPackages) {
        ArrayList<PInfo> res = new ArrayList<PInfo>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            if ((!getSysPackages) && (p.versionName == null)) {
                continue;
            }
            String appname = p.applicationInfo.loadLabel(getPackageManager()).toString();
            String pname = p.packageName;
            int uid = p.applicationInfo.uid;
            //String versionName = p.versionName;
            //int versionCode = p.versionCode;
            Drawable appicon = p.applicationInfo.loadIcon(getPackageManager());
            PInfo newInfo = new PInfo(appname, pname, uid, "", 0, appicon, "", "", "", "");
            res.add(newInfo);
        }
        return res;
    }
}