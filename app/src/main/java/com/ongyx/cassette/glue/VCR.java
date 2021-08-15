package com.ongyx.cassette.glue;

import android.media.MediaPlayer;
import android.util.Log;

import com.ongyx.cassette.Callback;
import com.ongyx.cassette.exception.VCRException;
import com.ongyx.cassette.models.Playlist;
import com.ongyx.cassette.models.Song;

import java.io.FileInputStream;
import java.io.IOException;

/** Provides a simplified interface between the UI and MediaPlayer to play songs.
 * Use {@link #getInstance()} to get the static instance.
 * @author Ong Yong Xin
 */
public class VCR {

    private static String TAG = "VCR";

    private static VCR instance;
    private static final MediaPlayer player = new MediaPlayer();

    private Playlist playlist;

    // The current song and its index in the playlist.
    private int index;
    private Song song;

    /**
     * Invoked when a song is done playing.
     */
    public Callback<Song> onDoneCallback;
    /**
     * Invoked when a song is selected for playing.
     */
    public Callback<Song> onSelectCallback;
    /**
     * Invoked when the repeat state changes.
     */
    public Callback<Repeat> onRepeatStateCallback;

    public enum Repeat {
        NEVER, FOREVER, ONCE
    }
    private Repeat repeatState = Repeat.NEVER;

    public VCR() {
        player.setOnCompletionListener(
                (player) -> {
                    if (onDoneCallback != null) onDoneCallback.invoke(song);

                    switch (repeatState) {
                        case NEVER:
                            skipForward();
                            break;

                        case FOREVER:
                            break;

                        case ONCE:
                            repeat(Repeat.NEVER);
                    }
                }
        );
    }

    /**
     * Get the static instance of VCR.
     * @return The VCR instance.
     */
    public static VCR getInstance() {
        if (instance == null) {
            instance = new VCR();
        }

        return instance;
    }

    /**
     * Load playlist so songs can be played from it.
     * This **must** be called before any other methods.
     * @param playlist The playlist to load.
     * @return the VCR instance
     */
    public VCR with(Playlist playlist) {
        this.playlist = playlist;
        return this;
    }

    /**
     * Select a song from the playlist to play.
     * After a song is selected, {@link #play()} can be called.
     * @param index The position of the song in the playlist.
     *              It is the caller's duty to check if index is out of bounds (must be 0 <= index < {@link #length()}).
     * @return the VCR instance.
     */
    public VCR select(int index) {
        Song song = this.playlist.songs.get(index);

        if (player.isPlaying()) {
            player.stop();
        }

        player.reset();

        try {
            player.setDataSource(new FileInputStream(song.getPath()).getFD());
            player.prepare();
        } catch (IOException e) {
            player.reset();
            throw new VCRException("failed to prepare song " + song.toString());
        }

        this.index = index;
        this.song = song;

        if (onSelectCallback != null) onSelectCallback.invoke(song);

        Log.d(TAG, String.format("playing %s (duration %d)", song.getPath().toString(), player.getDuration()));

        return this;
    }

    /**
     * Start playing the song.
     * @return The VCR instance.
     */
    public VCR play() {
        player.start();

        return this;
    }

    /** Pause the playing song.
     * @return The VCR instance.
     */
    public VCR pause() {
        player.pause();

        return this;
    }

    public VCR repeat(Repeat state) {
        repeatState = state;

        switch (repeatState) {
            case NEVER:
                player.setLooping(false);
                break;

            case FOREVER:
            case ONCE:
                player.setLooping(true);
                break;
        }

        if (onRepeatStateCallback != null) onRepeatStateCallback.invoke(repeatState);

        return this;
    }

    /**
     * Play the next song in the playlist.
     * If there are no more songs after, the current song will repeat.
     * @return The VCR instance.
     */
    public VCR skipForward() {
        if (index == length() - 1) {
            // already at end of playlist, repeat current song
            player.seekTo(0);
            return this;
        }

        index++;
        select(index);

        play();

        return this;
    }

    /**
     * Play the previous song in the playlist.
     * If there are no more songs before, the current song will repeat.
     * @return The VCR instance.
     */
    public VCR skipBackward() {
        if (index == 0) {
            // already at start of playlist, repeat current song
            player.seekTo(0);
            return this;
        }

        index--;
        select(index);

        play();

        return this;
    }

    /**
     * Seek the playing song to position.
     * @param position The position to seek to in milliseconds.
     * @return The VCR instance.
     */
    public VCR seek(int position) {
        player.seekTo(position);

        return this;
    }

    /**
     * Get the total number of songs in the loaded playlist.
     * @return the total number of songs.
     */
    public int length() {
        return playlist.songs.size();
    }

    /**
     * Get the duration of the playing song.
     * @return the song duration in milliseconds.
     */
    public int duration() {
        return player.getDuration();
    }

    /**
     * Get the elapsed time of the playing song.
     * @return the elapsed time in milliseconds.
     */
    public int elapsed() {
        return player.getCurrentPosition();
    }

    /**
     * Check if a song is being played.
     * @return true if so, otherwise false
     */
    public boolean playing() {
        return player.isPlaying();
    }

    /**
     * Get the index of the playing song.
     * @return the song index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the playing song.
     * @return the song.
     */
    public Song getSong() {
        return song;
    }
}