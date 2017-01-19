package net.fypm.InstallerOpt;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomBackupInstalledAppsListArrayAdapter extends ArrayAdapter<PInfo> {

    private final List<PInfo> list;
    private List<PInfo> objects;
    private final Activity context;
    private Filter filter;

    static class ViewHolder {
        protected TextView appname;
        //protected TextView pname;
        protected TextView status;
        protected TextView versioninfo;
        protected ImageView appicon;
    }

    public CustomBackupInstalledAppsListArrayAdapter(Activity context, List<PInfo> list) {
        super(context, R.layout.backup_installed_apps_row, list);
        this.context = context;
        this.list = list;
        this.objects = list;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public PInfo getItem(int position) {
        return objects.get(position);
    }

    /*@Override
    public long getItemId(int position) {
        return list.get(position).getId();
    }*/

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.backup_installed_apps_row, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.appname = (TextView) view.findViewById(R.id.appname);
            //viewHolder.pname = (TextView) view.findViewById(R.id.pname);
            viewHolder.status = (TextView) view.findViewById(R.id.status);
            viewHolder.versioninfo = (TextView) view.findViewById(R.id.versioninfo);
            viewHolder.appicon = (ImageView) view.findViewById(R.id.appicon);
            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.appname.setText(list.get(position).getName());
        //holder.pname.setText(list.get(position).getPackageName());
        holder.versioninfo.setText(context.getString(R.string.version_text) + list.get(position).getVersionName());
        holder.status.setText(context.getString(R.string.status_text) + list.get(position).getStatus());
        holder.appicon.setImageDrawable(list.get(position).getAppIcon());

        return view;
    }

    @Override
    public Filter getFilter() {
        if (filter == null)
            filter = new CustomBackupInstalledAppsListArrayAdapter.AppFilter<PInfo>(objects);
        return filter;
    }

    /**
     * Class for filtering in Arraylist listview. Objects need a valid
     * 'toString()' method.
     *
     * @author Tobias Schürg inspired by Alxandr
     *         (http://stackoverflow.com/a/2726348/570168)
     */
    private class AppFilter<T> extends Filter {

        private ArrayList<T> sourceObjects;

        public AppFilter(List<T> objects) {
            sourceObjects = new ArrayList<T>();
            synchronized (this) {
                sourceObjects.addAll(objects);
            }
        }

        @Override
        protected FilterResults performFiltering(CharSequence chars) {
            String filterSeq = chars.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (filterSeq != null && filterSeq.length() > 0) {
                ArrayList<T> filter = new ArrayList<T>();

                for (T object : sourceObjects) {
                    // the filtering itself:
                    if (filterSeq.length() > 1) {
                        if (object.toString().toLowerCase().contains(filterSeq)) {
                            filter.add(object);
                        }
                    } else {
                        if (object.toString().toLowerCase().startsWith(filterSeq)) {
                            filter.add(object);
                        }
                    }
                }
                result.count = filter.size();
                result.values = filter;
            } else {
                // add all objects
                synchronized (this) {
                    result.values = sourceObjects;
                    result.count = sourceObjects.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            // NOTE: this function is *always* called from the UI thread.
            ArrayList<T> filtered = (ArrayList<T>) results.values;
            notifyDataSetChanged();
            clear();
            for (int i = 0, l = filtered.size(); i < l; i++)
                add((PInfo) filtered.get(i));
            notifyDataSetInvalidated();
        }
    }

}
