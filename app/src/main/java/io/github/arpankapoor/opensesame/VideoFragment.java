package io.github.arpankapoor.opensesame;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;


public class VideoFragment extends Fragment {

    private VideoView mVideoView;

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
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String vidAddr = "http://192.168.0.100:8080/arp.webm";
        Uri vidUri = Uri.parse(vidAddr);

        mVideoView = (VideoView) getActivity().findViewById(R.id.videoview);
        mVideoView.setVideoURI(vidUri);
        mVideoView.start();
    }
}
