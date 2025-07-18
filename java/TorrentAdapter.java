package com.winlator.torrentapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class TorrentAdapter extends RecyclerView.Adapter<TorrentAdapter.ViewHolder> {

    private ArrayList<Torrent> torrents;
    private Context context;

    public TorrentAdapter(Context context, ArrayList<Torrent> torrents) {
        this.context = context;
        this.torrents = torrents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_torrent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Torrent torrent = torrents.get(position);
        holder.torrentName.setText(torrent.getName());
        holder.torrentProgress.setProgress(torrent.getProgress());
        holder.torrentStatus.setText(torrent.getStatus());

        holder.itemView.setOnClickListener(v -> {
            if (torrent.getProgress() == 100) {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(downloadsDir), "*/*");
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "No app to open this file type", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return torrents.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView torrentName;
        public ProgressBar torrentProgress;
        public TextView torrentStatus;

        public ViewHolder(View view) {
            super(view);
            torrentName = view.findViewById(R.id.torrent_name);
            torrentProgress = view.findViewById(R.id.torrent_progress);
            torrentStatus = view.findViewById(R.id.torrent_status);
        }
    }
}
