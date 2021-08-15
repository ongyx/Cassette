package com.ongyx.cassette.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ongyx.cassette.R;
import com.ongyx.cassette.Utils;
import com.ongyx.cassette.glue.VCR;
import com.ongyx.cassette.models.Playlist;
import com.ongyx.cassette.models.Song;
import com.squareup.picasso.Picasso;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends AppCompatActivity {

    private final String TAG = "PlayerActivity";

    private VCR vcr = VCR.getInstance();

    private TextView title;
    private TextView artist;
    private ImageView cover;

    private ImageButton skipBackButton;
    private ImageButton playPauseButton;
    private ImageButton skipForwardButton;
    private ImageButton repeatButton;
    private TextView repeatStatus;

    private SeekBar seekBar;

    private TextView timeLapsed;
    private TextView timeTotal;

    // used to update seekbar every 1000 ms
    private final Timer timer = new Timer();

    // used to notify timer if user is seeking (so the seekbar won't glitch)
    private boolean seeking = false;

    private enum Repeat {
        NEVER, FOREVER, ONCE
    }

    private Repeat repeatState = Repeat.NEVER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        setTitle("Player");
        Objects.requireNonNull(getSupportActionBar()).hide();
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

        title = findViewById(R.id.player_title);
        artist = findViewById(R.id.player_artist);
        cover = findViewById(R.id.player_cover);

        skipBackButton = findViewById(R.id.player_prev);
        playPauseButton = findViewById(R.id.player_pause);
        skipForwardButton = findViewById(R.id.player_next);
        repeatButton = findViewById(R.id.player_repeat);
        repeatStatus = findViewById(R.id.player_repeat_status);

        timeLapsed = findViewById(R.id.player_lapsed);
        timeTotal = findViewById(R.id.player_total);

        seekBar = findViewById(R.id.player_seekbar);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            timeLapsed.setText(Utils.formatTime(progress));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        seeking = true;
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        Log.d(TAG, String.format("seeking from %d to %d", vcr.elapsed(), seekBar.getProgress()));
                        vcr.seek(seekBar.getProgress());
                        seeking = false;
                    }
                }
        );

        Bundle bundle = getIntent().getExtras();

        // init vcr
        Playlist playlist = (Playlist) bundle.getSerializable("playlist");
        vcr.with(playlist);

        int index = bundle.getInt("index");

        // If the song to play is not already playing, play it.
        if (vcr.getIndex() != index || vcr.getSong() == null) {
            vcr.select(index).play();
        }

        displaySong(vcr.getSong());

        timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {

                        // don't increment the seekbar if the user is seeking
                        if (!seeking) {

                            int position;

                            try {
                                position = vcr.elapsed();
                            } catch (IllegalStateException e) {
                                position = 0;
                            }

                            seekBar.setProgress(position);

                            final int pos = position;
                            runOnUiThread(() -> timeLapsed.setText(Utils.formatTime(pos)));

                        }
                    }
                },
                0,
                1000);
    }

    public void cleanup() {
        timer.cancel();
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    public void minimize(View v) {
        finish();
        cleanup();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanup();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cleanup();
    }

    private void toggleButton(boolean playing) {

        int icon;

        if (playing) {
            icon = R.drawable.ic_pause;

            title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            artist.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        } else {
            icon = R.drawable.ic_play;

            title.setEllipsize(null);
            artist.setEllipsize(null);
        }

        playPauseButton.setImageResource(icon);

    }

    public void cycleRepeatState(View view) {
        switch (repeatState) {
            case NEVER:
                vcr.repeat(VCR.Repeat.FOREVER);
                repeatButton.setAlpha(1f);
                break;

            case FOREVER:
                vcr.repeat(VCR.Repeat.ONCE);
                repeatStatus.setVisibility(View.VISIBLE);
                break;

            case ONCE:
                vcr.repeat(VCR.Repeat.NEVER);
                repeatButton.setAlpha(0.5f);
                repeatStatus.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void displaySong(Song song) {
        title.setSelected(true);
        title.setText(song.getTitle());

        artist.setSelected(true);
        artist.setText(song.getArtist());

        Picasso.get().load(song.getCoverPath()).into(cover);

        int duration = vcr.duration();

        seekBar.setMax(duration);

        // Display total time
        timeTotal.setText(Utils.formatTime(duration));

        vcr.play();
        toggleButton(true);
    }

    public void skipForward(View view) {
        vcr.skipForward();
        displaySong(vcr.getSong());
    }

    public void skipBackward(View view) {
        vcr.skipBackward();
        displaySong(vcr.getSong());
    }

    public void playOrPause(View view) {
        if (vcr.playing()) {
            vcr.pause();
            toggleButton(false);
        } else {
            vcr.play();
            toggleButton(true);
        }
    }

}