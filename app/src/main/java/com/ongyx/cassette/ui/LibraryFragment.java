package com.ongyx.cassette.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ongyx.cassette.R;

import io.paperdb.Book;
import io.paperdb.Paper;

public class LibraryFragment extends BaseFragment {

    private final String TAG = "LibraryFragment";

    private Book book;

    private TextView notice;
    private LibraryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        this.book = Paper.book();

        this.adapter = new LibraryAdapter();

        RecyclerView recyclerView = view.findViewById(R.id.library);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        recyclerView.setAdapter(this.adapter);

        this.notice = view.findViewById(R.id.notice);
        if (this.book.getAllKeys().size() != 0) {
            this.notice.setVisibility(View.GONE);
        }

        FloatingActionButton button = view.findViewById(R.id.fab);
        button.setOnClickListener(this::onCreatePlaylist);

        return view;
    }

    private void onCreatePlaylist(View view) {
        EditText input = new EditText(view.getContext());
        input.setSingleLine(true);

        new AlertDialog.Builder(this.getContext())
                .setTitle("Create Playlist")
                .setMessage("Enter the new playlist's name.")
                .setView(input)
                .setPositiveButton("Ok", (dialog, which) -> {
                    this.adapter.createPlaylist(input.getText().toString(), this.getContext());
                    this.notice.setVisibility(View.GONE);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .show();
    }

    @Override
    public String getName() {
        return "Library";
    }

    @Override
    public int getNavId() {
        return R.id.nav_library;
    }

    @Override
    public int getLayoutId() {
        return R.layout.library;
    }
}
