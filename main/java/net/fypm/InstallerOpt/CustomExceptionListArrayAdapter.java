package net.fypm.InstallerOpt;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomExceptionListArrayAdapter extends ArrayAdapter<PInfo> {

    private final List<PInfo> list;
    private final Activity context;

    static class ViewHolder {
        protected TextView appname;
        protected TextView pname;
        protected TextView uid;
        protected ImageView appicon;
    }

    public CustomExceptionListArrayAdapter(Activity context, List<PInfo> list) {
        super(context, R.layout.manage_background_install_exceptions_row, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.manage_background_install_exceptions_row, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.appname = (TextView) view.findViewById(R.id.appname);
            viewHolder.pname = (TextView) view.findViewById(R.id.pname);
            viewHolder.uid = (TextView) view.findViewById(R.id.uid);
            viewHolder.appicon = (ImageView) view.findViewById(R.id.appicon);
            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.appname.setText(list.get(position).getName());
        holder.pname.setText("Package: " + list.get(position).getPackageName());
        holder.uid.setText("UID: " + list.get(position).getUid());
        holder.appicon.setImageDrawable(list.get(position).getAppIcon());

        return view;
    }
}
