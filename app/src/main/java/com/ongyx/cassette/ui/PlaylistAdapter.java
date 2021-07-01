package com.ongyx.cassette.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ongyx.cassette.R;
import com.ongyx.cassette.models.Playlist;
import com.ongyx.cassette.models.Song;

@SuppressWarnings("rawtypes")
public class PlaylistAdapter extends RecyclerView.Adapter {
    // Prepares songs for viewing as a playlist.

    protected final Playlist playlist;
    private final String TAG = "PlaylistAdapter";

    public PlaylistAdapter(Playlist playlist) {
        this.playlist = playlist;
    }

    public void addSong(String url, Context context) {
        new Song(
                url,
                context,
                (song) -> {
                    Log.d(TAG, String.format("adding song %s to playlist %s", song.getTitle(), this.playlist.getName()));
                    Log.d(TAG, this.playlist.songs.toString());

                    song.download(
                            context,
                            (s) -> ((Activity)context).runOnUiThread(
                                    () -> {
                                        this.playlist.songs.add(s);
                                        this.notifyItemInserted(this.playlist.songs.size() - 1);
                                    })
                    );

                });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

        view.setOnLongClickListener(
                (v) -> {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Delete Song")
                            .setMessage("Are you sure you want to delete this song?")
                            .setPositiveButton(
                                    "Yes",
                                    (dialog, which) -> {
                                        int position = (int)v.getTag();
                                        this.playlist.songs.remove(position);

                                        this.notifyItemRemoved(position);
                                    }
                            )
                            .setNegativeButton("No", (dialog, which) -> {})
                            .show();

                    return true;
                }
        );

        return new PlaylistHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PlaylistHolder playlistHolder = ((PlaylistHolder) holder);
        playlistHolder.bindData(this.playlist, position);
    }

    @Override
    public int getItemCount() {
        return this.playlist.songs.size();
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.playlist_item;
    }

}