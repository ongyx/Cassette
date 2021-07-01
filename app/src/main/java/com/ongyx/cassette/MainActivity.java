package com.ongyx.cassette;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ongyx.cassette.ui.BaseFragment;
import com.ongyx.cassette.ui.LibraryFragment;
import com.ongyx.cassette.ui.SearchFragment;
import com.ongyx.cassette.ui.SettingsFragment;

import java.util.Arrays;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private int fragmentIndex = 0;
    private List<BaseFragment> fragmentList;
    //private int fragmentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        Paper.init(this.getApplicationContext());

        // init volley request queue
        Utils.filesDir = this.getFilesDir();

        this.fragmentList = Arrays.asList(
                new LibraryFragment(),
                new SearchFragment(),
                new SettingsFragment()
        );

        BottomNavigationView navView = this.findViewById(R.id.nav_bar);

        BottomNavigationView.OnItemSelectedListener listener = item -> {
            int index = this.indexOf(item);
            this.setFragmentAt(index);

            return true;
        };

        navView.setOnItemSelectedListener(listener);

        // home fragment is the playlists page.
        this.setFragmentAt(this.fragmentIndex);

    }

    private int indexOf(MenuItem menuItem) {
        for (int i = 0; i < this.fragmentList.size(); i++) {
            BaseFragment fragment = this.fragmentList.get(i);

            if (menuItem.getItemId() == fragment.getNavId()) {
                return i;
            }
        }

        return -1;
    }

    private void setFragmentAt(int index) {
        BaseFragment fragment = this.fragmentList.get(index);

        this.setTitle(fragment.getName());

        this.fragmentIndex = index;

        this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment, "f")
                .addToBackStack("f")
                .commit();
    }

}