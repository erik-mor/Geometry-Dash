package com.example.geometry_dash;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LevelAdapter extends ArrayAdapter<String> {

    String[] levels;
    int[] progress;
    Context mContext;

    public LevelAdapter(@NonNull Context context, String[] levels, int[] progress) {
        super(context, R.layout.level_info);
        this.levels = levels;
        this.progress = progress;
        mContext = context;
    }

    @Override
    public int getCount() {
        return levels.length;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder mViewHolder = new ViewHolder();

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.level_info, parent, false);
            mViewHolder.levelName = (TextView) convertView.findViewById(R.id.levelName);
            mViewHolder.levelProgress = (ProgressBar) convertView.findViewById(R.id.levelProgress);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.levelName.setText(levels[position]);
        mViewHolder.levelProgress.setProgress(progress[position]);

        return convertView;
    }

    static class ViewHolder{
        TextView levelName;
        ProgressBar levelProgress;
    }
}
