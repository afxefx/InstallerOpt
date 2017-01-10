package net.fypm.InstallerOpt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class About extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder aboutDialog = new AlertDialog.Builder(
                this, android.R.style.Theme_DeviceDefault_Dialog);
        final TextView message = new TextView(this);
        final SpannableString s =
                new SpannableString(this.getText(R.string.dialog_message));
        Linkify.addLinks(s, Linkify.WEB_URLS);
        message.setText(s);
        aboutDialog.setTitle(R.string.about_menu);
        aboutDialog.setMessage(s);
        aboutDialog.setCancelable(false);
        aboutDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });
        AlertDialog thisAboutDialog = aboutDialog.create();
        thisAboutDialog.show();
        ((TextView)thisAboutDialog.findViewById(android.R.id.message)).setClickable(true);
        ((TextView)thisAboutDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }
}
