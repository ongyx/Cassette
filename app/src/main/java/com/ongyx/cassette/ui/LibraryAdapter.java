package com.ongyx.cassette.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ongyx.cassette.R;
import com.ongyx.cassette.models.Playlist;

import java.util.ArrayList;

import io.paperdb.Book;
import io.paperdb.Paper;

@SuppressWarnings("rawtypes")
public class LibraryAdapter extends RecyclerView.Adapter {

    private final String TAG = "LibraryAdapter";

    private final Book book = Paper.book();

    protected final ArrayList<String> checksums;

    public LibraryAdapter() {
        // Because the playlists are stored as a map, the keys have to be converted into a list
        // TODO: Implement sorting?
        this.checksums = new ArrayList<>(book.getAllKeys());
    }

    protected void createPlaylist(String name, Context context) {

        String checksum = Playlist.create(name, "");

        if (checksum == null) {
            Toast.makeText(context, "Playlist already exists", Toast.LENGTH_SHORT).show();

        } else {
            this.checksums.add(checksum);
            this.notifyItemInserted(this.checksums.size() - 1);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

        view.setOnLongClickListener(
                (v) -> {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Delete Playlist")
                            .setMessage("Are you sure you want to delete this playlist?")
                            .setPositiveButton(
                                    "Yes",
                                    (dialog, which) -> {
                                        String checksum = (String)v.getTag();

                                        this.checksums.remove(checksum);
                                        this.book.delete(checksum);

                                        this.notifyItemRemoved(this.checksums.indexOf(checksum));
                                    }
                            )
                            .setNegativeButton("No", (dialog, which) -> {})
                            .show();

                    return true;
                }
        );

        return new LibraryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LibraryHolder libraryHolder = ((LibraryHolder) holder);

        String checksum = this.checksums.get(position);
        libraryHolder.bindData(checksum);
    }

    @Override
    public int getItemCount() {
        return this.checksums.size();
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.library_item;
    }
}