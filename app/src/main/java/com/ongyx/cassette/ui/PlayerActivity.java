package com.ongyx.cassette.ui;

import android.media.MediaPlayer;
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
import com.ongyx.cassette.models.Playlist;
import com.ongyx.cassette.models.Song;
import com.squareup.picasso.Picasso;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends AppCompatActivity {

    private final String TAG = "PlayerActivity";

    private int currentIndex = 0;

    private final MediaPlayer player = Utils.player;
    private Playlist playlist;

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
        this.setContentView(R.layout.activity_player);
        this.setTitle("Player");
        Objects.requireNonNull(this.getSupportActionBar()).hide();
        this.overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

        this.title = this.findViewById(R.id.player_title);
        this.artist = this.findViewById(R.id.player_artist);
        this.cover = this.findViewById(R.id.player_cover);

        this.skipBackButton = this.findViewById(R.id.player_prev);
        this.playPauseButton = this.findViewById(R.id.player_pause);
        this.skipForwardButton = this.findViewById(R.id.player_next);
        this.repeatButton = this.findViewById(R.id.player_repeat);
        this.repeatStatus = this.findViewById(R.id.player_repeat_status);

        this.timeLapsed = this.findViewById(R.id.player_lapsed);
        this.timeTotal = this.findViewById(R.id.player_total);

        Bundle bundle = this.getIntent().getExtras();

        this.playlist = (Playlist) bundle.getSerializable("playlist");
        this.currentIndex = bundle.getInt("index");
        this.disableIfNeeded();

        this.player.setOnCompletionListener(
                (player) -> {
                        switch (repeatState) {
                            case NEVER:
                                playNextSong(skipForwardButton);
                                break;

                            case FOREVER:
                                break;

                            case ONCE:
                                // repeat only once
                                cycleRepeatState(repeatButton);
                                break;
                        }
                }
        );

        this.seekBar = this.findViewById(R.id.player_seekbar);
        this.seekBar.setOnSeekBarChangeListener(
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
                        Log.d(TAG, String.format("seeking from %d to %d", player.getCurrentPosition(), seekBar.getProgress()));
                        player.seekTo(seekBar.getProgress());
                        seeking = false;
                    }
                }
        );

        if (!this.player.isPlaying()) {
            // no song currently playing
            this.playSong(this.currentIndex);
        } else {
            // song already playing, display it
            this.displaySong(this.playlist.songs.get(this.currentIndex));
        }

        this.timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {

                        // don't increment the seekbar if the user is seeking
                        if (!seeking) {

                            int position;

                            try {
                                position = player.getCurrentPosition();
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
        this.timer.cancel();
        this.overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    public void minimize(View v) {
        this.finish();
        this.cleanup();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.cleanup();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.cleanup();
    }

    private void toggleButton(boolean playing) {

        int icon;

        if (playing) {
            icon = R.drawable.ic_pause;

            this.title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            this.artist.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        } else {
            icon = R.drawable.ic_play;

            this.title.setEllipsize(null);
            this.artist.setEllipsize(null);
        }

        this.playPauseButton.setImageResource(icon);

    }

    private void disableIfNeeded() {
        if (this.currentIndex == 0) {
            this.skipBackButton.setClickable(false);
            this.skipBackButton.setAlpha(0.5f);
            this.skipForwardButton.setClickable(true);
            this.skipForwardButton.setAlpha(1f);
        }

        if (this.currentIndex == this.playlist.songs.size() - 1) {
            this.skipBackButton.setClickable(true);
            this.skipBackButton.setAlpha(1f);
            this.skipForwardButton.setClickable(false);
            this.skipForwardButton.setAlpha(0.5f);
        }
    }

    public void cycleRepeatState(View view) {
        switch (this.repeatState) {
            case NEVER:
                this.repeatState = Repeat.FOREVER;
                this.player.setLooping(true);

                this.repeatButton.setAlpha(1f);

                break;

            case FOREVER:
                this.repeatState = Repeat.ONCE;

                this.repeatStatus.setVisibility(View.VISIBLE);

                break;

            case ONCE:
                this.repeatState = Repeat.NEVER;
                this.player.setLooping(false);

                this.repeatButton.setAlpha(0.5f);
                this.repeatStatus.setVisibility(View.INVISIBLE);

                break;
        }
    }

    private void displaySong(Song song) {
        this.title.setSelected(true);
        this.title.setText(song.getTitle());

        this.artist.setSelected(true);
        this.artist.setText(song.getArtist());

        Picasso.get().load(song.getCoverPath()).into(this.cover);

        int duration = this.player.getDuration();

        this.seekBar.setMax(duration);

        // Display total time
        this.timeTotal.setText(Utils.formatTime(duration));

        this.player.start();
        this.toggleButton(true);
    }

    private void playSong(int index) {

        Song song = this.playlist.songs.get(index);

        if (this.player.isPlaying()) {
            this.player.stop();
        }

        this.player.reset();

        try {
            this.player.setDataSource(new FileInputStream(song.getPath()).getFD());
            this.player.prepare();
        } catch (IOException e) {
            Log.e(TAG, "failed to load song/prepare!", e);
            this.player.reset();
        }

        this.displaySong(song);

        Log.d(TAG, String.format("playing %s (duration %d)", song.getPath().toString(), this.player.getDuration()));

    }

    public void playOrPauseSong(View view) {
        if (this.player.isPlaying()) {
            player.pause();
            this.toggleButton(false);
        } else {
            player.start();
            this.toggleButton(true);
        }
    }

    public void playPrevSong(View view) {
        if (this.currentIndex == 0) {
            return;
        }

        this.currentIndex--;
        this.disableIfNeeded();

        this.playSong(this.currentIndex);
    }

    public void playNextSong(View view) {
        int max = this.playlist.songs.size() - 1;

        if (this.currentIndex == max) {
            return;  // bail
        }

        this.currentIndex++;
        this.disableIfNeeded();

        this.playSong(this.currentIndex);
    }

}