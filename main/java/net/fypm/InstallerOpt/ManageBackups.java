package net.fypm.InstallerOpt;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class ManageBackups extends ListActivity {

    public ArrayList<String> filesInFolder;
    public ArrayList<String> selectedItems;
    public String backupDir;
    public ArrayAdapter la;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean enableDark = MultiprocessPreferences.getDefaultSharedPreferences(this).getBoolean(Common.PREF_ENABLE_DARK_THEME, false);
        if (enableDark) {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.activity_main);

        backupDir = MultiprocessPreferences.getDefaultSharedPreferences(this).getString(Common.PREF_BACKUP_APK_LOCATION, null);
        filesInFolder = GetFiles(backupDir);
        selectedItems = new ArrayList<String>();
        Collections.sort(filesInFolder, new NaturalOrderComparator());

        ListView listview = getListView();
        listview.setChoiceMode(listview.CHOICE_MODE_MULTIPLE);

        la = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, filesInFolder);
        setListAdapter(la);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manage_backup_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stats:
                startActivity(new Intent(this, Stats.class));
                return true;
            case R.id.backup_delete_menu:
                if (selectedItems.size() > 0) {
                    new AsyncDelete(this, backupDir, selectedItems).execute("");
                    this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.backup_selection_empty, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.backup_restore_menu:
                if (selectedItems.size() > 0) {
                    new AsyncRestore(this, backupDir, selectedItems).execute("");
                    this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.backup_selection_empty, Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        CheckedTextView item = (CheckedTextView) v;
        if (item.isChecked()) {
            selectedItems.add(filesInFolder.get(position));
        } else {
            selectedItems.remove(filesInFolder.get(position));
        }
        /*Toast.makeText(this, FilesInFolder.get(position) + " checked : " +
                item.isChecked(), Toast.LENGTH_SHORT).show();*/
        //Toast.makeText(this, selectedItems.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume(){
        super.onResume();
        la.notifyDataSetChanged();
    }

    public ArrayList<String> GetFiles(String DirectoryPath) {
        ArrayList<String> arrayFiles = new ArrayList<String>();
        File f = new File(DirectoryPath);

        f.mkdirs();
        File[] files = f.listFiles();
        if (files.length == 0)
            return null;
        else {
            for (int i = 0; i < files.length; i++)
                arrayFiles.add(files[i].getName());
        }

        return arrayFiles;
    }


}
