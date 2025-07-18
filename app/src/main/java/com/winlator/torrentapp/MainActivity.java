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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.BlockFinishedAlert;
import com.frostwire.jlibtorrent.alerts.TorrentAddedAlert;
import com.frostwire.jlibtorrent.alerts.TorrentFinishedAlert;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 1;
    private TorrentAdapter adapter;
    private ArrayList<Torrent> torrents = new ArrayList<>();
    private SessionManager sessionManager;

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
        sessionManager = new SessionManager() {
            @Override
            public void onAlert(Alert<?> alert) {
                super.onAlert(alert);
                AlertType type = alert.type();
                switch (type) {
                    case TORRENT_ADDED:
                        TorrentAddedAlert ta = (TorrentAddedAlert) alert;
                        ta.handle().resume();
                        runOnUiThread(() -> {
                            for (Torrent t : torrents) {
                                if (ta.handle().infoHash().toString().equals(t.getMagnetUri())) {
                                    t.setName(ta.handle().torrentFile().name());
                                    adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        });
                        break;
                    case BLOCK_FINISHED:
                        BlockFinishedAlert bf = (BlockFinishedAlert) alert;
                        float progress = bf.handle().status().progress() * 100;
                        runOnUiThread(() -> {
                            for (Torrent t : torrents) {
                                if (bf.handle().infoHash().toString().equals(t.getMagnetUri())) {
                                    t.setProgress((int) progress);
                                    t.setStatus("Downloading");
                                    adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        });
                        break;
                    case TORRENT_FINISHED:
                        TorrentFinishedAlert tf = (TorrentFinishedAlert) alert;
                        runOnUiThread(() -> {
                            for (Torrent t : torrents) {
                                if (tf.handle().infoHash().toString().equals(t.getMagnetUri())) {
                                    t.setStatus("Finished");
                                    t.setProgress(100);
                                    adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        });
                        break;
                }
            }
        };
        sessionManager.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sessionManager.stop();
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
                startDownload(torrentLink);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void startDownload(String torrentLink) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        sessionManager.download(torrentLink, downloadsDir);
    }
}
