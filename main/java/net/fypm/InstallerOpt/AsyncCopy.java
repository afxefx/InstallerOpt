package net.fypm.InstallerOpt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class AsyncCopy extends AsyncTask<String, String, String> {

    private static final String TAG = "InstallerOpt";
    String oldPath;
    String savePath;
    Activity ctx;
    private ProgressDialog pDialog;
    ArrayList<String> arr;

    AsyncCopy(Activity _ctx, String _savePath, ArrayList<String> Files) {
        this.ctx = _ctx;
        this.oldPath = MultiprocessPreferences.getDefaultSharedPreferences(ctx).getString(Common.PREF_BACKUP_APK_LOCATION_OLD, null);
        this.savePath = _savePath;
        this.arr = Files;
    }

    @Override
    protected void onPreExecute() {
        pDialog = new ProgressDialog(ctx);
        pDialog.setMessage(ctx.getString(R.string.move_file_prepare_message));
        //pDialog.setProgress(0);
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);
        pDialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... urls) {
        File f = new File(savePath);
        if (!f.exists()) {
            f.mkdir();
        }
        for (int i = 0; i < arr.size(); i++) {
            Copy(arr.get(i));
            publishProgress(String.valueOf(i));
        }
        return null;
    }

    @Override
    protected void onPostExecute(String unused) {
        long curBackupDirSize = Stats.getFileSize(new File(oldPath));
        long newBackupDirSize = Stats.getFileSize(new File(savePath));
        if (newBackupDirSize == curBackupDirSize) {
            Stats.deleteRecursive(new File(oldPath + File.separator));
            Toast.makeText(ctx, R.string.move_complete_message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(ctx, R.string.folder_size_error_message, Toast.LENGTH_LONG).show();
        }
        pDialog.hide();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        pDialog.setMessage(String.format("%-12s %s %s %d", ctx.getString(R.string.move_file_message).replace('*', ' '), String.valueOf(values[0]), ctx.getString(R.string.move_file_of_message).replace('*', ' '), arr.size()));
    }

    void Copy(String fname) {
        try {
            int count;
            InputStream input = new FileInputStream(oldPath + File.separator + fname);
            OutputStream output = new FileOutputStream(savePath + File.separator + fname);
            byte data[] = new byte[1024];
            while ((count = input.read(data)) > 0) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            Log.e(TAG, "Copy file error: ", e);
        }
    }
}