package com.example.carview3d;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.IOException;

/**
 * 视频播放验证页面（SurfaceView方案）
 * @author Lty
 */
public class VideoCarActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = VideoCarActivity.class.getSimpleName();
    private static final float SWIPE_STEP_PX = 4f;
    private static final long AUTO_ROTATE_INTERVAL_MS = 33L;
    private static final long AUTO_ROTATE_RESUME_DELAY_MS = 3_000L;
    private static final int FRAME_STEP_MS = 33;
    private static final int DIRECTION_RIGHT = 1;
    private static final int DIRECTION_LEFT = -1;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;
    private boolean isPrepared;
    private boolean isAutoRotating;
    private boolean isUserInteracting;
    private float startX;
    private float currentX;
    private int videoDurationMs;
    private int framePositionMs;
    private int lastRotateDirection = DIRECTION_RIGHT;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable autoRotateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isPrepared || !isAutoRotating || isUserInteracting) {
                return;
            }

            if (lastRotateDirection != DIRECTION_LEFT) {
                return;
            }

            stepFrame(lastRotateDirection);
            handler.postDelayed(this, AUTO_ROTATE_INTERVAL_MS);
        }
    };
    private final Runnable resumeAutoRotateRunnable = this::startAutoRotate;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_car);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.video_mode);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_small);
        toolbar.setNavigationOnClickListener(v -> finish());

        surfaceView = findViewById(R.id.videoSurfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceView.setOnTouchListener((v, event) -> {
            if (!isPrepared) {
                return true;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isUserInteracting = true;
                    stopAutoRotate();
                    handler.removeCallbacks(resumeAutoRotateRunnable);
                    if (mediaPlayer != null) {
                        framePositionMs = mediaPlayer.getCurrentPosition();
                    }
                    startX = event.getX();
                    break;

                case MotionEvent.ACTION_MOVE:
                    currentX = event.getX();
                    float deltaX = currentX - startX;

                    while (deltaX >= SWIPE_STEP_PX) {
                        lastRotateDirection = DIRECTION_RIGHT;
                        stepFrame(DIRECTION_RIGHT);
                        startX += SWIPE_STEP_PX;
                        deltaX = currentX - startX;
                    }

                    while (deltaX <= -SWIPE_STEP_PX) {
                        lastRotateDirection = DIRECTION_LEFT;
                        stepFrame(DIRECTION_LEFT);
                        startX -= SWIPE_STEP_PX;
                        deltaX = currentX - startX;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isUserInteracting = false;
                    scheduleAutoRotateResume();
                    break;

                default:
                    break;
            }

            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPrepared && !isUserInteracting) {
            startAutoRotate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRotate();
        handler.removeCallbacks(resumeAutoRotateRunnable);
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRotate();
        handler.removeCallbacksAndMessages(null);
        releasePlayer();
        if (surfaceHolder != null) {
            surfaceHolder.removeCallback(this);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initPlayer(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged width=" + width + ", height=" + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releasePlayer();
    }

    private void initPlayer(SurfaceHolder holder) {
        releasePlayer();

        mediaPlayer = new MediaPlayer();
        isPrepared = false;

        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.car_view2);
            if (afd == null) {
                Log.e(TAG, "openRawResourceFd returned null");
                return;
            }

            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            mediaPlayer.setDisplay(holder);
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build());
            mediaPlayer.setVolume(0f, 0f);
            mediaPlayer.setLooping(true);

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                videoDurationMs = mp.getDuration();
                framePositionMs = 0;
                Log.d(TAG, "onPrepared duration=" + mp.getDuration());
                mp.start();
                mp.pause();
                seekToPosition(framePositionMs);
                startAutoRotate();
            });
            mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> {
                Log.d(TAG, "onVideoSizeChanged width=" + width + ", height=" + height);
                adjustSurfaceSize(width, height);
            });
            mediaPlayer.setOnInfoListener((mp, what, extra) -> {
                Log.d(TAG, "onInfo what=" + what + ", extra=" + extra);
                if (what == 805 && extra == -110) {
                    Log.w(TAG, "playback timed out, try to recover");
                    if (lastRotateDirection == DIRECTION_RIGHT) {
                        mp.start();
                    } else {
                        seekToPosition(framePositionMs);
                    }
                    return true;
                }
                return false;
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "onError what=" + what + ", extra=" + extra);
                return false;
            });

            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "initPlayer failed", e);
        }
    }

    private void adjustSurfaceSize(int videoWidth, int videoHeight) {
        if (videoWidth <= 0 || videoHeight <= 0) {
            return;
        }

        View parent = (View) surfaceView.getParent();
        if (parent == null) {
            return;
        }

        int parentWidth = parent.getWidth();
        if (parentWidth <= 0) {
            surfaceView.post(() -> adjustSurfaceSize(videoWidth, videoHeight));
            return;
        }

        int targetWidth = parentWidth;
        int targetHeight = Math.round((float) targetWidth * videoHeight / videoWidth);

        ViewGroup.LayoutParams currentParams = surfaceView.getLayoutParams();
        FrameLayout.LayoutParams layoutParams;
        if (currentParams instanceof FrameLayout.LayoutParams) {
            layoutParams = (FrameLayout.LayoutParams) currentParams;
        } else {
            layoutParams = new FrameLayout.LayoutParams(targetWidth, targetHeight);
        }

        layoutParams.width = targetWidth;
        layoutParams.height = targetHeight;
        layoutParams.gravity = Gravity.CENTER;
        surfaceView.setLayoutParams(layoutParams);
    }

    private void releasePlayer() {
        isPrepared = false;
        isAutoRotating = false;

        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void startAutoRotate() {
        if (!isPrepared || isAutoRotating) {
            return;
        }

        isAutoRotating = true;

        if (lastRotateDirection == DIRECTION_RIGHT) {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
            handler.removeCallbacks(autoRotateRunnable);
            return;
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        handler.post(autoRotateRunnable);
    }

    private void stopAutoRotate() {
        if (!isAutoRotating) {
            return;
        }

        isAutoRotating = false;
        handler.removeCallbacks(autoRotateRunnable);
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void scheduleAutoRotateResume() {
        handler.removeCallbacks(resumeAutoRotateRunnable);
        handler.postDelayed(resumeAutoRotateRunnable, AUTO_ROTATE_RESUME_DELAY_MS);
    }

    private void stepFrame(int direction) {
        if (!isPrepared || mediaPlayer == null || videoDurationMs <= 0) {
            return;
        }

        framePositionMs = normalizePosition(framePositionMs + (direction * FRAME_STEP_MS),
                videoDurationMs);
        seekToPosition(framePositionMs);
    }

    private void seekToPosition(int positionMs) {
        if (mediaPlayer == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer.seekTo(positionMs, MediaPlayer.SEEK_CLOSEST);
        } else {
            mediaPlayer.seekTo(positionMs);
        }
    }

    private int normalizePosition(int position, int duration) {
        if (duration <= 0) {
            return 0;
        }

        int normalized = position % duration;
        if (normalized < 0) {
            normalized += duration;
        }
        return normalized;
    }
}
