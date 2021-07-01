package com.ongyx.cassette.models;

import com.ongyx.cassette.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import io.paperdb.Book;
import io.paperdb.Paper;

public class Playlist implements Serializable {

    private final transient String TAG = "Playlist";

    private String name;
    private String description;
    private String checksum;

    public ArrayList<Song> songs = new ArrayList<>();

    public Playlist(String name, String description, String checksum) {
        this.name = name;
        this.description = description;
        this.checksum = checksum;
    }

    public static String create(String name, String description) {
        // This is used as a filename-safe name to store playlists as folders.
        String checksum = Utils.checksum(name);
        Book book = Paper.book();

        if (book.read(checksum) != null) {
            // playlist exists or collision detected.
            return null;
        }

        Playlist playlist = new Playlist(name, description, checksum);

        book.write(checksum, playlist);

        return checksum;
    }

    public void sync() {
        Paper.book().write(this.checksum, this);
    }

    // Get the playlist cover (first song's cover).
    // Returns null if there are no songs.
    public File getCover() {
        if (this.songs.size() != 0) {
            return this.songs.get(0).getCoverPath();
        }

        return null;
    }

    public boolean isDownloaded(int index) {
        Song song = this.songs.get(index);
        return song.getPath().isFile();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.name.equals(name)) {
            return;
        }

        String oldChecksum = this.checksum;

        // renaming a playlist also changes the checksum.
        this.checksum = Utils.checksum(name);
        this.name = name;

        this.sync();

        // remove old copy in db
        Paper.book().delete(oldChecksum);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChecksum() {
        return checksum;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

}