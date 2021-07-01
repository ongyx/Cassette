package com.ongyx.cassette.models;

import android.content.Context;
import android.widget.Toast;

import com.ongyx.cassette.Callback;
import com.ongyx.cassette.Utils;

import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;

public class Song implements Serializable {

    private String title;
    private String artist;
    private String id;  // youtube id
    private String cover;

    public Song(String title, String artist, String id, String cover) {
        this.title = title;
        this.artist = artist;
        this.id = id;
        this.cover = cover;
    }

    public Song(String youtubeUrl, Context context, Callback<Song> callback) {

        try {
            this.id = Utils.extractYoutubeId(youtubeUrl);
        } catch (URISyntaxException e) {
            this.id = null;
        }

        if (this.id == null) {
            Toast.makeText(context, String.format("%s is not a valid Youtube URL.", youtubeUrl), Toast.LENGTH_SHORT).show();
            return;
        }

        Utils.extractYoutubeInfo(
                youtubeUrl,
                (info) -> {
                    this.title = (String) info.get("title");
                    this.artist = (String) info.get("author_name");
                    this.cover = (String) info.get("thumbnail_url");

                    callback.invoke(this);
                },
                context
        );
    }

    public String getYoutubeLink() {
        return String.format("https://youtube.com/watch?v=%s", this.id);
    }

    public File getPath() {
        return Utils.joinPaths(Utils.filesDir, this.id + ".m4a");
    }

    public File getCoverPath() {
        return new File(this.getPath().toString().replace(".m4a", ".jpg"));
    }

    public void download(Context context, Callback<Song> callback) {
        Utils.downloadFromYoutube(this.getYoutubeLink(), this.getPath().toString(), context);
        Utils.download(this.getCover(), this.getCoverPath().toString(), context);

        callback.invoke(this);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

}