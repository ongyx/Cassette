package com.ongyx.cassette.ui;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ongyx.cassette.R;
import com.ongyx.cassette.models.Playlist;
import com.ongyx.cassette.models.Song;
import com.squareup.picasso.Picasso;

public class PlaylistHolder extends RecyclerView.ViewHolder {
    // A song preview.

    private final TextView title;
    private final TextView artist;
    private final ImageView cover;
    private final LinearLayout layout;
    private Playlist playlist;

    public PlaylistHolder(View view) {
        super(view);

        this.title = view.findViewById(R.id.song_title);
        this.artist = view.findViewById(R.id.song_artist);
        this.cover = view.findViewById(R.id.song_cover);
        this.layout = view.findViewById(R.id.search_item);
    }

    public void bindData(Playlist playlist, int position) {
        this.playlist = playlist;
        this.layout.setTag(position);

        Song song = this.playlist.songs.get(position);

        this.title.setText(song.getTitle());
        this.artist.setText(song.getArtist());
        Picasso.get().load(song.getCoverPath()).into(this.cover);

        this.layout.setOnClickListener(
                view -> {
                    // start the player
                    Intent intent = new Intent(view.getContext(), PlayerActivity.class);
                    intent.putExtra("playlist", this.playlist);
                    intent.putExtra("index", position);

                    view.getContext().startActivity(intent);
                }
        );
    }
}
