package net.fypm.InstallerOpt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

public class Reboot extends Activity {

    public static final int RESULT_CANCELED = 116;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder rebootDialog = new AlertDialog.Builder(
                this, android.R.style.Theme_DeviceDefault_Dialog);
        rebootDialog.setTitle(R.string.enable_reboot_message);
        rebootDialog
                .setMessage(getText(R.string.sdcard_access_message));
        rebootDialog.setCancelable(false);
        rebootDialog.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        MultiprocessPreferences.getDefaultSharedPreferences(Reboot.this).edit().putBoolean(Common.PREF_ENABLE_EXTERNAL_SDCARD_FULL_ACCESS, true).apply();
                        MultiprocessPreferences.getDefaultSharedPreferences(Reboot.this).edit().putBoolean(Common.PREF_ENABLE_BACKUP_APK_FILE, false).apply();
                        MultiprocessPreferences.getDefaultSharedPreferences(Reboot.this).edit().putBoolean(Common.PREF_CONTINUE_BACKUP_ON_REBOOT, true).apply();
                        try {
                            Process proc = Runtime.getRuntime()
                                    .exec(new String[]{"su", "-c", "reboot"});
                            proc.waitFor();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Toast.makeText(Reboot.this, "Reboot operation failed, please do so manually!", Toast.LENGTH_LONG).show();

                        }
                        finish();
                    }
                });
        rebootDialog.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        setResult(Reboot.RESULT_CANCELED);
                        finish();
                    }
                });
        AlertDialog thisRebootDialog = rebootDialog.create();
        thisRebootDialog.show();
    }
}
