package com.ongyx.cassette.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.ongyx.cassette.R;

public class BaseFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(this.getLayoutId(), container, false);
    }

    // These getters have to be overridden by subclasses.
    // Otherwise, the fragment acts as a placeholder.
    public String getName() {
        return "Placeholder";
    }

    public int getNavId() {
        return 0;
    }

    public int getLayoutId() {
        return R.layout.placeholder;
    }
}
