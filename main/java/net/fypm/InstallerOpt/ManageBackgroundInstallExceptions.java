package net.fypm.InstallerOpt;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
    public ArrayAdapter<PInfo> adapter;
    public List<PackageInfo> packs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean enableDark = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_DARK_THEME, false);
        if (enableDark) {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.backup_list);
        new Task().execute();
        selectedItems = new ArrayList<String>();

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

    class Task extends AsyncTask<String, String, Boolean> {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            if (pDialog == null) {
                pDialog = new ProgressDialog(ManageBackgroundInstallExceptions.this);
                //pDialog.setMessage(ManageBackups.this.getString(R.string.move_file_prepare_message));
                pDialog.setMessage(ManageBackgroundInstallExceptions.this.getString(R.string.loading_applications_message));
                //pDialog.setProgress(0);
                pDialog.setIndeterminate(true);
                pDialog.setCancelable(false);
                pDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
            pDialog = null;
            Collections.sort(installedApps, new Comparator<PInfo>() {
                public int compare(PInfo one, PInfo other) {
                    return one.getName().compareTo(other.getName());
                }
            });
            adapter = new CustomExceptionListArrayAdapter(ManageBackgroundInstallExceptions.this, installedApps);
            setListAdapter(adapter);
            //adapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pDialog.setMessage(String.format("%-12s %s %s %d", ManageBackgroundInstallExceptions.this.getString(R.string.parse_application_message).replace('*', ' '), String.valueOf(values[0]), ManageBackgroundInstallExceptions.this.getString(R.string.parse_application_of_message).replace('*', ' '), packs.size()));
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean getSysPackages = false;
            installedApps = new ArrayList<PInfo>();
            packs = getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
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
                PInfo newInfo = new PInfo(appname, pname, uid, "", 0, appicon, "", "", "", "", "");
                installedApps.add(newInfo);
                publishProgress(String.valueOf(i));
            }
            return null;
        }
    }
}