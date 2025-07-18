package com.winlator.torrentapp;

public class Torrent {
    private String name;
    private String status;
    private int progress;
    private String magnetUri;

    public Torrent(String magnetUri) {
        this.magnetUri = magnetUri;
        this.name = magnetUri; // Placeholder, will be updated later
        this.status = "Waiting...";
        this.progress = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getMagnetUri() {
        return magnetUri;
    }
}
