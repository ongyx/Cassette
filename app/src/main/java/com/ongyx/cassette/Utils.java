package com.ongyx.cassette;

import android.content.Context;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import androidx.core.graphics.ColorUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class Utils {

    static final String TAG = "Utils";

    static final String YT_OEMBED_URL = "https://www.youtube.com/oembed?url=%s&format=json";
    static final Type YT_OEMBED_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    static final Gson gson = new Gson();

    static OkHttpClient client = new OkHttpClient();

    public static MediaPlayer player = new MediaPlayer();

    public static File filesDir;

    static final MessageDigest algo;

    // Java won't let me have checksums the easy way...
    static {
        try {
            algo = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // should not happen
            throw new AssertionError(e);
        }
    }

    // https://stackoverflow.com/a/943963
    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

    public static String checksum(String str) {
        return Utils.toHex(algo.digest(str.getBytes()));
    }

    public static File joinPaths(File root, String... children) {
        return new File(root, TextUtils.join(File.separator, children));
    }

    public static void handleError(String message, IOException error, Context context) {
        Log.e(TAG, message, error);
        Toast.makeText(context, message + ": " + error.toString(), Toast.LENGTH_LONG).show();
    }

    public static void download(String url, String to, Context context) {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Android")
                .build();

        client.newCall(request).enqueue(
                new com.squareup.okhttp.Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        handleError(String.format("Couldn't download %s", url), e, context);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        FileUtils.copyInputStreamToFile(response.body().byteStream(), new File(to));
                    }
                }
        );
    }

    public static void downloadFromYoutube(String url, String to, Context context) {
        new YouTubeExtractor(context) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                YtFile ytFile = ytFiles.get(140);
                download(ytFile.getUrl(), to, context);

                Log.d(
                        TAG,
                        String.format(
                            "downloaded Youtube %s video with format %s",
                            ytFile.toString(),
                            ytFile.getFormat().toString()
                        )
                );
            }
        }.extract(url);
    }

    // https://stackoverflow.com/a/48267387
    public static boolean isDark(int colour) {
        return ColorUtils.calculateLuminance(colour) < 0.5;
    }

    public static void extractYoutubeInfo(String url, Callback<Map<String, Object>> callback, Context context) {
        String queryUrl = String.format(YT_OEMBED_URL, TextUtils.htmlEncode(url));

        Request request = new Request.Builder()
                .url(queryUrl)
                .build();

        client.newCall(request).enqueue(
                new com.squareup.okhttp.Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        handleError("Couldn't extract video info", e, context);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        String data = response.body().string();
                        Log.d(TAG, data);

                        callback.invoke(gson.fromJson(data, YT_OEMBED_TYPE));
                    }
                }
        );
    }

    public static String extractYoutubeId(String url) throws URISyntaxException {
        URI parsedUrl = new URI(url.replace("www.", ""));
        String host = parsedUrl.getHost();

        if (host == null) {
            return null;
        }

        if (host.equals("youtu.be")) {
            return parsedUrl.getPath().replace("/", "");

        } else if (host.equals("youtube.com")) {

            String query = parsedUrl.getQuery();
            if (query == null) {
                return null;
            }

            String id = null;

            for (String pair : query.split("&")) {
                String[] keyval = pair.split("=");
                String key = keyval[0];

                if (key.equals("v")) {
                    id = keyval[1];
                    break;
                }
            }

            return id;

        } else {
            return null;
        }
    }

    // Given time in miliseconds, return a human-readable timestamp.
    // https://stackoverflow.com/a/53090348
    public static String formatTime(int ms) {
        int hours = (int) (TimeUnit.MILLISECONDS.toHours(ms) % 24);
        int minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(ms) % 60);
        int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(ms) % 60);

        String formatted = String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds);

        if (hours > 0) {
            formatted = hours + ":" + formatted;
        }

        return formatted;
    }
}