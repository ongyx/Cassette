package com.ongyx.cassette.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ongyx.cassette.R;
import com.ongyx.cassette.models.Song;
import com.squareup.picasso.Picasso;

public class SearchHolder extends RecyclerView.ViewHolder {

    private final TextView title;
    private final TextView artist;
    private final ImageView cover;
    private final LinearLayout layout;

    public SearchHolder(@NonNull View view) {
        super(view);

        this.title = view.findViewById(R.id.search_title);
        this.artist = view.findViewById(R.id.search_artist);
        this.cover = view.findViewById(R.id.search_cover);
        this.layout = view.findViewById(R.id.search_item);
    }

    public void bindData(Song song) {
        this.title.setText(song.getTitle());
        this.artist.setText(song.getArtist());
        Picasso.get().load(song.getCover()).into(this.cover);
    }
}
