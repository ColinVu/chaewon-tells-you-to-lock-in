package com.example.socialmediausagenotifier;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.VideoView;
import android.widget.MediaController;

public class VideoPopupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Optional: make the window smaller like a popup
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        VideoView videoView = new VideoView(this);
        setContentView(videoView);

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alert);
        videoView.setVideoURI(videoUri);

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        videoView.setOnCompletionListener(mp -> finish()); // Auto-close after playing

        videoView.start();
    }
}