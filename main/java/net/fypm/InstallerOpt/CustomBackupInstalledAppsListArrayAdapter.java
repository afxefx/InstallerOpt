package net.fypm.InstallerOpt;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomBackupInstalledAppsListArrayAdapter extends ArrayAdapter<PInfo> {

    private final List<PInfo> list;
    private final Activity context;

    static class ViewHolder {
        protected TextView appname;
        //protected TextView pname;
        protected TextView versioninfo;
        protected ImageView appicon;
    }

    public CustomBackupInstalledAppsListArrayAdapter(Activity context, List<PInfo> list) {
        super(context, R.layout.backup_installed_apps_row, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.backup_installed_apps_row, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.appname = (TextView) view.findViewById(R.id.appname);
            //viewHolder.pname = (TextView) view.findViewById(R.id.pname);
            viewHolder.versioninfo = (TextView) view.findViewById(R.id.versioninfo);
            viewHolder.appicon = (ImageView) view.findViewById(R.id.appicon);
            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.appname.setText(list.get(position).getName());
        //holder.pname.setText(list.get(position).getPackageName());
        holder.versioninfo.setText("Version: " + list.get(position).getVersionName());
        holder.appicon.setImageDrawable(list.get(position).getAppIcon());

        return view;
    }
}
