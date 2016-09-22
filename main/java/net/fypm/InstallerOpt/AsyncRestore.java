package net.fypm.InstallerOpt;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class AsyncRestore extends AsyncTask<String, String, String> {

    private static final String TAG = "InstallerOpt";
    String savePath;
    Activity ctx;
    private ProgressDialog pDialog;
    ArrayList<String> arr;

    AsyncRestore(Activity _ctx, String _savePath, ArrayList<String> Files) {
        this.ctx = _ctx;
        this.savePath = _savePath;
        this.arr = Files;
    }

    @Override
    protected void onPreExecute() {
        pDialog = new ProgressDialog(ctx);
        pDialog.setMessage(ctx.getString(R.string.backup_restore_file_prepare_message));
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
            if (f.mkdir()) {
                Log.i(TAG, "doInBackground: Backup directory created");
            } else {
                Log.e(TAG, "doInBackground: Unable to create backup directory");
            }
        }
        for (int i = 0; i < arr.size(); i++) {
            Restore(arr.get(i));
            publishProgress(String.valueOf(i));
        }
        return null;
    }

    @Override
    protected void onPostExecute(String unused) {
        Toast.makeText(ctx, R.string.backup_restore_complete_message, Toast.LENGTH_LONG).show();
        //pDialog.hide();
        pDialog.dismiss();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        pDialog.setMessage(String.format("%-12s %s %s %d", ctx.getString(R.string.restore_file_message).replace('*', ' '), String.valueOf(values[0]), ctx.getString(R.string.move_file_of_message).replace('*', ' '), arr.size()));
    }

    void Restore(String apkFile) {
        try {
            File apk = new File(savePath + File.separator + apkFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
            ctx.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Restore file error: ", e);
        }
    }
}