package com.example.carview3d;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * 基于序列图的3D车模展示
 * @author Lty
 */
public class PictureCarActivity extends AppCompatActivity {

    // 当前显示的bitmap对象
    private static Bitmap bitmap;
    // 图片容器
    private ImageView imageView;
    // 开始按下位置
    private float startX;
    // 当前位置
    private float currentX;
    // 当前图片的编号
    private int scrNum;
    // 图片的总数
    private static final int maxNum = 52;
    // 每切换一帧所需的最小滑动距离（像素）
    private static final float SWIPE_STEP_PX = 16f;
    // 自动旋转间隔
    private static final long AUTO_ROTATE_INTERVAL_MS = 200L;
    // 用户停止操作后恢复自动旋转的延迟
    private static final long AUTO_ROTATE_RESUME_DELAY_MS = 3000L;
    private static final int DIRECTION_RIGHT = 1;
    private static final int DIRECTION_LEFT = -1;
    // 资源图片集合
    private final int[] srcs = new int[] { R.mipmap.p1, R.mipmap.p2,
            R.mipmap.p3, R.mipmap.p4, R.mipmap.p5, R.mipmap.p6,
            R.mipmap.p7, R.mipmap.p8, R.mipmap.p9, R.mipmap.p10,
            R.mipmap.p11, R.mipmap.p12, R.mipmap.p13, R.mipmap.p14,
            R.mipmap.p15, R.mipmap.p16, R.mipmap.p17, R.mipmap.p18,
            R.mipmap.p19, R.mipmap.p20, R.mipmap.p21, R.mipmap.p22,
            R.mipmap.p23, R.mipmap.p24, R.mipmap.p25, R.mipmap.p26,
            R.mipmap.p27, R.mipmap.p28, R.mipmap.p29, R.mipmap.p30,
            R.mipmap.p31, R.mipmap.p32, R.mipmap.p33, R.mipmap.p34,
            R.mipmap.p35, R.mipmap.p36, R.mipmap.p37, R.mipmap.p38,
            R.mipmap.p39, R.mipmap.p40, R.mipmap.p41, R.mipmap.p42,
            R.mipmap.p43, R.mipmap.p44, R.mipmap.p45, R.mipmap.p46,
            R.mipmap.p47, R.mipmap.p48, R.mipmap.p49, R.mipmap.p50,
            R.mipmap.p51, R.mipmap.p52 };

    private final Handler autoRotateHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoRotateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAutoRotating) {
                return;
            }
            if (lastRotateDirection == DIRECTION_LEFT) {
                modifySrcL();
            } else {
                modifySrcR();
            }
            if (isAutoRotating) {
                autoRotateHandler.postDelayed(this, AUTO_ROTATE_INTERVAL_MS);
            }
        }
    };
    private final Runnable resumeAutoRotateRunnable = this::startAutoRotate;
    private int lastRotateDirection = DIRECTION_RIGHT;
    private boolean isAutoRotating;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_car);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.picture_mode);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_small);
        toolbar.setNavigationOnClickListener(v -> finish());

        imageView = findViewById(R.id.imageView);
        // 初始化当前显示图片编号
        scrNum = 1;

        imageView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    stopAutoRotate();
                    autoRotateHandler.removeCallbacks(resumeAutoRotateRunnable);
                    startX = event.getX();
                    break;

                case MotionEvent.ACTION_MOVE:
                    currentX = event.getX();
                    float deltaX = currentX - startX;

                    while (deltaX >= SWIPE_STEP_PX) {
                        modifySrcR();
                        lastRotateDirection = DIRECTION_RIGHT;
                        startX += SWIPE_STEP_PX;
                        deltaX = currentX - startX;
                    }

                    while (deltaX <= -SWIPE_STEP_PX) {
                        modifySrcL();
                        lastRotateDirection = DIRECTION_LEFT;
                        startX -= SWIPE_STEP_PX;
                        deltaX = currentX - startX;
                    }

                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
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
        startAutoRotate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRotate();
        autoRotateHandler.removeCallbacks(resumeAutoRotateRunnable);
    }

    private void startAutoRotate() {
        if (isAutoRotating) {
            return;
        }
        isAutoRotating = true;
        autoRotateHandler.post(autoRotateRunnable);
    }

    private void stopAutoRotate() {
        if (!isAutoRotating) {
            return;
        }
        isAutoRotating = false;
        autoRotateHandler.removeCallbacks(autoRotateRunnable);
    }

    private void scheduleAutoRotateResume() {
        autoRotateHandler.removeCallbacks(resumeAutoRotateRunnable);
        autoRotateHandler.postDelayed(resumeAutoRotateRunnable, AUTO_ROTATE_RESUME_DELAY_MS);
    }

    /**
     * 向右滑动修改资源
     */
    private void modifySrcR() {
        if (scrNum > maxNum) {
            scrNum = 1;
        }

        if (scrNum > 0) {
            bitmap = BitmapFactory.decodeResource(getResources(), srcs[scrNum - 1]);
            imageView.setImageBitmap(bitmap);
            scrNum++;
        }
    }

    /**
     * 向左滑动修改资源
     */
    private void modifySrcL() {
        if (scrNum <= 0) {
            scrNum = maxNum;
        }

        if (scrNum <= maxNum) {
            bitmap = BitmapFactory.decodeResource(getResources(), srcs[scrNum - 1]);
            imageView.setImageBitmap(bitmap);
            scrNum--;
        }
    }
}
