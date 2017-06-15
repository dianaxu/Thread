package com.example.admin.threaddownload;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.admin.threaddownload.entities.FileInfo;
import com.example.admin.threaddownload.services.DownloadService;

import java.util.List;

/**
 * @author Diana
 * @date 2017/6/13
 */

public class FileListAdapter extends BaseAdapter {
    private Context mContext;
    private List<FileInfo> mDate;

    public FileListAdapter(Context context, List<FileInfo> date) {
        this.mContext = context;
        this.mDate = date;
    }

    @Override
    public int getCount() {
        if (mDate != null) {
            return mDate.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mDate.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ViewHolder holder = null;
        final FileInfo fileInfo = mDate.get(position);
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.listitem_file, parent, false);
            holder = new ViewHolder(view);
            holder.tvFileName.setText(fileInfo.getFileName());
            holder.pbProgress.setMax(100);
            holder.btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_START);
                    intent.putExtra("fileInfo", fileInfo);
                    mContext.startService(intent);
                }
            });
            holder.btnPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_PAUSE);
                    intent.putExtra("fileInfo", fileInfo);
                    mContext.startService(intent);
                }
            });
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }
        holder.pbProgress.setProgress(fileInfo.getFinished());
        return view;
    }

    public void updateProgress(int id, int finished) {
        mDate.get(id).setFinished(finished);
        notifyDataSetChanged();
    }

    private class ViewHolder {
        private TextView tvFileName;
        private ProgressBar pbProgress;
        private Button btnStart;
        private Button btnPause;

        public ViewHolder(View view) {
            tvFileName = (TextView) view.findViewById(R.id.tv_fileName);
            pbProgress = (ProgressBar) view.findViewById(R.id.pb_progress);
            btnStart = (Button) view.findViewById(R.id.btn_start);
            btnPause = (Button) view.findViewById(R.id.btn_pause);
        }
    }
}
