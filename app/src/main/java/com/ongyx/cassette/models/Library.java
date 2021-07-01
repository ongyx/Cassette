package com.ongyx.cassette.models;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ongyx.cassette.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Library {

    private final transient String TAG = "library";

    public Map<String, Playlist> playlists = new HashMap<>();

    private final File path;
    private final File manifest;

    private final Gson parser = new Gson();

    private static Library instance;

    public Library(Context context) {
        this.path = context.getFilesDir();
        this.manifest = Utils.joinPaths(this.path, "manifest.json");

        try {
            if (!this.manifest.createNewFile()) {
                // file already exists, read
                Type parserType = new TypeToken<Map<String, Playlist>>() {
                }.getType();

                FileReader reader = new FileReader(this.manifest);
                Map<String, Playlist> playlists = this.parser.fromJson(
                        reader,
                        parserType
                );

                if (playlists != null) {
                    this.playlists = playlists;
                }

                reader.close();

            }
        } catch (IOException e) {
            Log.e(TAG, "failed creating/loading manifest.json", e);
        }

    }

    public static Library getInstance(Context context) {
        if (instance == null) {
            instance = new Library(context);
        }

        return instance;
    }

    /**
     * Create a new playlist in the library.
     *
     * @param name the name of the new playlist
     * @return The checksum (unique identifier) of the playlist, or null if a playlist by the same name exists.
     */
    public String createPlaylist(String name) {

        if (name.isEmpty()) throw new IllegalArgumentException("name cannot be empty");

        // This is used as a filename-safe name to store playlists as folders.
        String checksum = Utils.checksum(name);

        if (this.playlists.get(checksum) != null) {
            // playlist exists or collision detected.
            return null;
        }

        File path = Utils.joinPaths(this.path, checksum);
        Playlist playlist = new Playlist(name, "", path.toString());

        this.playlists.put(checksum, playlist);

        Log.d(TAG, String.format("created playlist %s with checksum %s", name, checksum));

        return checksum;
    }

    public void sync() throws IOException {
        FileWriter writer = new FileWriter(this.manifest);
        this.parser.toJson(this.playlists, writer);

        // Flush writer to properly sync
        writer.flush();
        writer.close();

        Log.d(TAG, "syncing to disk: " + this.playlists.toString());
    }

}