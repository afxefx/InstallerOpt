package net.fypm.InstallerOpt;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomBackupListArrayAdapter extends ArrayAdapter<PInfo> {

    private final List<PInfo> list;
    private final Activity context;

    static class ViewHolder {
        protected TextView appname;
        protected TextView backupdate;
        protected TextView filesize;
        protected ImageView appicon;
    }

    public CustomBackupListArrayAdapter(Activity context, List<PInfo> list) {
        super(context, R.layout.manage_backups_row, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.manage_backups_row, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.appname = (TextView) view.findViewById(R.id.appname);
            viewHolder.backupdate = (TextView) view.findViewById(R.id.backupdate);
            viewHolder.filesize = (TextView) view.findViewById(R.id.filesize);
            viewHolder.appicon = (ImageView) view.findViewById(R.id.appicon);
            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.appname.setText(list.get(position).getName() + " " + list.get(position).getVersionName() + "-" + list.get(position).getVersionCode());
        holder.backupdate.setText("Date: " + list.get(position).getItemModified());
        holder.filesize.setText("  Size: " + list.get(position).getItemSize());
        holder.appicon.setImageDrawable(list.get(position).getAppIcon());

        return view;
    }
}
