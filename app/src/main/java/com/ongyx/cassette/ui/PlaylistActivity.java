package com.ongyx.cassette.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.ongyx.cassette.R;
import com.ongyx.cassette.models.Playlist;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.paperdb.Book;
import io.paperdb.Paper;

public class PlaylistActivity extends AppCompatActivity {

    private final String TAG = "PlaylistActivity";
    private static final int REQUEST_SONG = 1337;

    private Book book;

    private PlaylistAdapter adapter;
    private ImageView playlistCover;
    private EditText playlistTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_playlist);

        // PlaylistActivity may crash when re-inited if this is not done.
        Paper.init(this.getApplicationContext());
        this.book = Paper.book();

        this.playlistCover = this.findViewById(R.id.playlist_cover);
        this.playlistTitle = this.findViewById(R.id.playlist_title);

        this.playlistTitle.setOnFocusChangeListener(
                (v, hasFocus) -> {
                    EditText editor = (EditText)v;

                    if (!hasFocus) {
                        String name = editor.getText().toString().trim();

                        if (name.length() != 0) {
                            Log.d(TAG, String.format("renaming playlist from %s to %s", this.adapter.playlist.getName(), name));
                            this.adapter.playlist.setName(name);
                        }
                    }
                }
        );

        Bundle bundle = this.getIntent().getExtras();
        Playlist playlist = this.book.read(bundle.getString("playlist"));

        this.setTitle("Songs");
        this.playlistTitle.setText(playlist.getName());

        File cover = playlist.getCover();
        if (cover != null) {
            Picasso.get().load(cover).into(this.playlistCover);
        }

        this.adapter = new PlaylistAdapter(playlist);
        RecyclerView recyclerView = this.findViewById(R.id.playlist);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(this.adapter);

        Button button = this.findViewById(R.id.add_song_button);
        button.setOnClickListener(this::onAddSong);
    }

    private void onAddSong(View view) {
        EditText input = new EditText(view.getContext());
        input.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle("Add song")
                .setMessage("Youtube url")
                .setView(input)
                .setPositiveButton("Ok", (dialog, which) -> this.adapter.addSong(input.getText().toString(), this))
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, String.format("syncing playlist to db (songs: %s)", this.adapter.playlist.songs.toString()));
        this.adapter.playlist.sync();
    }

    public void onShare(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, new Gson().toJson(this.adapter.playlist));
        sendIntent.setType("text/*");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        this.startActivity(shareIntent);
    }
}