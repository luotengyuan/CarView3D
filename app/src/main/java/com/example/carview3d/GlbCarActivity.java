package com.example.carview3d;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.collision.Box;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Light;
import com.google.ar.sceneform.rendering.ModelRenderable;

/**
 * 基于GLB模型的3D车模展示
 * @author Lty
 */
public class GlbCarActivity extends AppCompatActivity {

    private static final float SWIPE_STEP_PX = 8f;
    private static final float SWIPE_STEP_DEG = 1.8f;
    private static final float AUTO_ROTATE_STEP_DEG = 0.6f;
    private static final long AUTO_ROTATE_INTERVAL_MS = 16L;
    private static final long AUTO_ROTATE_RESUME_DELAY_MS = 3_000L;
    private static final float MODEL_SCALE_MULTIPLIER = 1f;
    private static final int DIRECTION_RIGHT = 1;
    private static final int DIRECTION_LEFT = -1;

    private SceneView sceneView;
    private Node modelNode;
    private float startX;
    private float currentX;
    private float currentYaw;
    private int lastRotateDirection = DIRECTION_RIGHT;
    private boolean isAutoRotating;
    private boolean isModelReady;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable autoRotateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAutoRotating || !isModelReady) {
                return;
            }
            rotateBy(lastRotateDirection * AUTO_ROTATE_STEP_DEG);
            handler.postDelayed(this, AUTO_ROTATE_INTERVAL_MS);
        }
    };
    private final Runnable resumeAutoRotateRunnable = this::startAutoRotate;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glb_car);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.glb_title);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.glb_title);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_small);
        toolbar.setNavigationOnClickListener(v -> finish());
        centerNavigationIcon(toolbar);

        sceneView = findViewById(R.id.sceneView);
        setupScene();
        loadModel();

        sceneView.setOnTouchListener((v, event) -> {
            if (!isModelReady) {
                return true;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    stopAutoRotate();
                    handler.removeCallbacks(resumeAutoRotateRunnable);
                    startX = event.getX();
                    break;

                case MotionEvent.ACTION_MOVE:
                    currentX = event.getX();
                    float deltaX = currentX - startX;

                    while (deltaX >= SWIPE_STEP_PX) {
                        rotateBy(SWIPE_STEP_DEG);
                        lastRotateDirection = DIRECTION_RIGHT;
                        startX += SWIPE_STEP_PX;
                        deltaX = currentX - startX;
                    }

                    while (deltaX <= -SWIPE_STEP_PX) {
                        rotateBy(-SWIPE_STEP_DEG);
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

    private void setupScene() {
        try {
            if (sceneView == null || sceneView.getRenderer() == null) {
                return;
            }
            sceneView.getRenderer().setClearColor(new Color(1f, 1f, 1f, 1f));
        } catch (Exception ignored) {
        }

        addSceneLights();

        Camera camera = sceneView.getScene().getCamera();
        Vector3 cameraPosition = new Vector3(0f, 0.7f, 3.0f);
        Vector3 target = new Vector3(0f, 0f, 0f);
        Vector3 forward = Vector3.subtract(target, cameraPosition).normalized();
        Quaternion rotation = Quaternion.lookRotation(forward, Vector3.up());
        camera.setWorldPosition(cameraPosition);
        camera.setWorldRotation(rotation);
    }

    private void addSceneLights() {
        Node keyLightNode = new Node();
        keyLightNode.setLight(Light.builder(Light.Type.DIRECTIONAL)
                .setColor(new Color(1f, 1f, 1f))
                .setIntensity(60_000f)
                .setShadowCastingEnabled(false)
                .build());
        keyLightNode.setWorldRotation(Quaternion.lookRotation(
                new Vector3(-0.3f, -1f, -0.2f).normalized(), Vector3.up()));
        sceneView.getScene().addChild(keyLightNode);

        Node fillLightNode = new Node();
        fillLightNode.setLight(Light.builder(Light.Type.POINT)
                .setColor(new Color(1f, 1f, 1f))
                .setIntensity(2500f)
                .setFalloffRadius(12f)
                .build());
        fillLightNode.setWorldPosition(new Vector3(0f, 1.8f, 2.2f));
        sceneView.getScene().addChild(fillLightNode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sceneView != null) {
            try {
                sceneView.resume();
            } catch (Exception ignored) {
            }
        }
        if (isModelReady) {
            startAutoRotate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRotate();
        handler.removeCallbacks(resumeAutoRotateRunnable);
        if (sceneView != null) {
            try {
                sceneView.pause();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRotate();
        handler.removeCallbacksAndMessages(null);
        if (sceneView != null) {
            sceneView.destroy();
        }
    }

    private void loadModel() {
        ModelRenderable.builder()
                .setSource(this, R.raw.stylized_car_unlit_noground)
                .setIsFilamentGltf(true)
                .setRegistryId("stylized_car_unlit_noground.glb")
                .build()
                .thenAccept(renderable -> {
                    applyRenderableColorOverride(renderable);

                    modelNode = new Node();
                    modelNode.setRenderable(renderable);

                    Vector3 localPosition = new Vector3(0f, 0f, 0f);
                    float scale = 1f;
                    if (renderable.getCollisionShape() instanceof Box) {
                        Box box = (Box) renderable.getCollisionShape();
                        Vector3 center = box.getCenter();
                        Vector3 extents = box.getExtents();
                        float maxExtent = Math.max(extents.x, Math.max(extents.y, extents.z));
                        if (maxExtent > 0f) {
                            scale = 0.9f / maxExtent;
                        }
                        scale *= MODEL_SCALE_MULTIPLIER;
                        localPosition = new Vector3(-center.x * scale, -center.y * scale, -center.z * scale);
                    }

                    modelNode.setLocalScale(new Vector3(scale, scale, scale));
                    modelNode.setLocalPosition(localPosition);
                    currentYaw = 180f;
                    modelNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), currentYaw));
                    sceneView.getScene().addChild(modelNode);
                    isModelReady = true;
                    startAutoRotate();
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> Toast.makeText(this, "模型加载失败", Toast.LENGTH_SHORT).show());
                    return null;
                });
    }

    private void applyRenderableColorOverride(ModelRenderable renderable) {
        for (int i = 0; i < renderable.getSubmeshCount(); i++) {
            try {
                com.google.ar.sceneform.rendering.Material material = renderable.getMaterial(i).makeCopy();
                material.setFloat4("baseColorFactor", new Color(0.85f, 0.85f, 0.85f, 1f));
                material.setFloat3("emissiveFactor", new Color(0.35f, 0.35f, 0.35f));
                renderable.setMaterial(i, material);
            } catch (Exception ignored) {
            }
        }
    }

    private void rotateBy(float deltaDeg) {
        if (modelNode == null) {
            return;
        }
        currentYaw = normalizeAngle(currentYaw + deltaDeg);
        modelNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), currentYaw));
    }

    private float normalizeAngle(float angle) {
        float normalized = angle % 360f;
        if (normalized < 0f) {
            normalized += 360f;
        }
        return normalized;
    }

    private void startAutoRotate() {
        if (!isModelReady || isAutoRotating) {
            return;
        }
        isAutoRotating = true;
        handler.post(autoRotateRunnable);
    }

    private void stopAutoRotate() {
        if (!isAutoRotating) {
            return;
        }
        isAutoRotating = false;
        handler.removeCallbacks(autoRotateRunnable);
    }

    private void scheduleAutoRotateResume() {
        handler.removeCallbacks(resumeAutoRotateRunnable);
        handler.postDelayed(resumeAutoRotateRunnable, AUTO_ROTATE_RESUME_DELAY_MS);
    }

    private void centerNavigationIcon(MaterialToolbar toolbar) {
        toolbar.post(() -> {
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                if (toolbar.getChildAt(i) instanceof ImageButton) {
                    Toolbar.LayoutParams params = (Toolbar.LayoutParams) toolbar.getChildAt(i).getLayoutParams();
                    params.gravity = android.view.Gravity.CENTER_VERTICAL;
                    toolbar.getChildAt(i).setLayoutParams(params);
                    return;
                }
            }
        });
    }
}
