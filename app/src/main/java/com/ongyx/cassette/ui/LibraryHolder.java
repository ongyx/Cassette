package com.ongyx.cassette.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.ongyx.cassette.R;
import com.ongyx.cassette.Utils;
import com.ongyx.cassette.models.Playlist;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.paperdb.Book;
import io.paperdb.Paper;

public class LibraryHolder extends RecyclerView.ViewHolder {
    private final String TAG = "LibraryHolder";

    private final Book book = Paper.book();

    protected View layout;
    protected TextView name;
    protected ImageView cover;
    protected String checksum;

    public LibraryHolder(View view) {
        super(view);

        this.layout = view;

        // enable clipping rounded corners
        this.layout.setClipToOutline(true);
        this.layout.setOnClickListener(this::onClick);

        this.name = view.findViewById(R.id.library_name);
        this.cover = view.findViewById(R.id.library_cover);
    }

    public void bindData(String checksum) {
        this.checksum = checksum;
        this.layout.setTag(checksum);

        Playlist playlist = book.read(this.checksum);
        assert playlist != null;

        this.name.setText(playlist.getName());

        // The cover path is always the first song's cover (that is, if there are any songs).
        // TODO: add setting for this?
        File cover = playlist.getCover();
        if (cover != null) {
            Log.d(TAG, "loading cover: " + cover.toString());
            Picasso.get().load(cover).into(this.cover, new Callback() {
                @Override
                public void onSuccess() {
                    colourise();
                }

                @Override
                public void onError(Exception e) {}
            });
        } else {
            // colour using the placeholder drawable.
            colourise();
        }

    }

    private void colourise() {
        // Detect the accent colour from the cover image.
        // The colour of the text also has to be changed to black or white if the accent is too light/dark.
        Bitmap bitmap = ((BitmapDrawable) this.cover.getDrawable()).getBitmap();
        Palette palette = Palette.from(bitmap).generate();
        Palette.Swatch swatch = palette.getVibrantSwatch();

        if (swatch != null) {

            int backgroundColour = swatch.getRgb();
            this.layout.getBackground().setTint(backgroundColour);

            int textColour = Utils.isDark(backgroundColour)
                    ? Color.WHITE
                    : Color.BLACK;

            this.name.setTextColor(textColour);

        }
    }

    private void onClick(View view) {
        Activity activity = (Activity) view.getContext();

        // show playlist
        // NOTE: Because the playlist is sent through the Intent, a copy of the playlist will be received by PlaylistActivity, **not** a reference to the one here.
        Intent intent = new Intent(activity, PlaylistActivity.class);
        intent.putExtra("playlist", this.checksum);

        activity.startActivity(intent);
    }


}
