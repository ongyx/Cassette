package com.ongyx.cassette.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ongyx.cassette.R;

import io.paperdb.Book;
import io.paperdb.Paper;

public class SearchFragment extends BaseFragment {

    private Book book;

    private SearchAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        this.book = Paper.book("search");
        this.adapter = new SearchAdapter();

        RecyclerView recyclerView = view.findViewById(R.id.search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        recyclerView.setAdapter(this.adapter);

        return view;
    }

    @Override
    public String getName() {
        return "Search";
    }

    @Override
    public int getNavId() {
        return R.id.nav_search;
    }

    @Override
    public int getLayoutId() {
        return R.layout.search;
    }

}