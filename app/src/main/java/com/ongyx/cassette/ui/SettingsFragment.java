package com.ongyx.cassette.ui;

import com.ongyx.cassette.R;

public class SettingsFragment extends BaseFragment {

    @Override
    public String getName() {
        return "Settings";
    }

    @Override
    public int getNavId() {
        return R.id.nav_settings;
    }

    @Override
    public int getLayoutId() {
        return R.layout.settings;
    }

}