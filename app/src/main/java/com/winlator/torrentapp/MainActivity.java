package com.winlator.torrentapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.TorrentOptions;
import com.github.se_bastiaan.torrentstream.TorrentStream;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements com.github.se_bastiaan.torrentstream.listeners.TorrentListener {

    private static final int REQUEST_PERMISSIONS = 1;
    private TorrentAdapter adapter;
    private ArrayList<Torrent> torrents = new ArrayList<>();
    private TorrentStream torrentStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TorrentAdapter(this, torrents);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showAddTorrentDialog());

        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
        } else {
            startTorrentSession();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTorrentSession();
            } else {
                // Handle permission denial
            }
        }
    }

    private void startTorrentSession() {
        TorrentOptions torrentOptions = new TorrentOptions.Builder()
                .saveLocation(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
                .removeFilesAfterStop(true)
                .build();

        torrentStream = TorrentStream.init(torrentOptions);
        torrentStream.addListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        torrentStream.stopStream();
    }

    private void showAddTorrentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Torrent");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String torrentLink = input.getText().toString();
            if (!torrentLink.isEmpty()) {
                Torrent torrent = new Torrent(torrentLink);
                torrents.add(torrent);
                adapter.notifyDataSetChanged();
                torrentStream.startStream(torrentLink);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onStreamStarted(com.github.se_bastiaan.torrentstream.Torrent torrent) {
        runOnUiThread(() -> {
            for (Torrent t : torrents) {
                if (t.getMagnetUri().equals(torrent.getTorrentHandle().uri())) {
                    t.setInfoHash(torrent.getTorrentHandle().infoHash().toString());
                    t.setName(torrent.getName());
                    t.setStatus("Downloading");
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        });
    }

    @Override
    public void onStreamError(com.github.se_bastiaan.torrentstream.Torrent torrent, Exception e) {
        // Handle error
    }

    @Override
    public void onStreamReady(com.github.se_bastiaan.torrentstream.Torrent torrent) {
        // Handle stream ready
    }

    @Override
    public void onStreamProgress(com.github.se_bastiaan.torrentstream.Torrent torrent, StreamStatus status) {
        runOnUiThread(() -> {
            for (Torrent t : torrents) {
                if (torrent.getTorrentHandle().infoHash().toString().equals(t.getInfoHash())) {
                    t.setProgress((int) status.progress);
                    if (status.progress == 100) {
                        t.setStatus("Finished");
                    }
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        });
    }

    @Override
    public void onStreamStopped() {
        // Handle stream stopped
    }
}
