package io.github.arpankapoor.opensesame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;


public class VideoFragment extends Fragment {

    private int mVidId;

    public VideoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mVidId = intent.getIntExtra(Intent.EXTRA_TEXT, -1);
        }
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    private Uri getVideoUri() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String centralServer = preferences.getString(getString(R.string.pref_cs_addr_key),
                getString(R.string.pref_default_cs_addr));

        URL url;
        Uri vidUri = null;
        try {
            url = new URL("http", centralServer,
                    8080, "/" + mVidId + getString(R.string.video_format));
        } catch (MalformedURLException e) {
            Log.e("VideoURL", "Malformed URL", e);
            return null;
        }

        try {
            vidUri = Uri.parse(url.toURI().toString());
        } catch (URISyntaxException e) {
            Log.e("VideoURL", "URISyntaxException", e);
        }

        return vidUri;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Uri vidUri = getVideoUri();
        if (vidUri == null) {
            Toast.makeText(getActivity(), R.string.err_video, Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.progressbar);
        final VideoView mVideoView = (VideoView) getActivity().findViewById(R.id.videoview);
        mVideoView.setVideoURI(vidUri);
        mVideoView.start();

        progressBar.setVisibility(View.VISIBLE);

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progressBar.setVisibility(View.INVISIBLE);
                mp.start();
            }
        });

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                progressBar.setVisibility(View.INVISIBLE);
                mVideoView.suspend();
                Toast.makeText(getActivity(), R.string.err_video, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
}
