package com.ahirst.doodaddash;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

import com.ahirst.doodaddash.fragment.MainMenuFragment;
import com.ahirst.doodaddash.fragment.MenuFragment;
import com.ahirst.doodaddash.fragment.SignInFragment;
import com.ahirst.doodaddash.util.CameraUtil;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Size;
import com.otaliastudios.cameraview.SizeSelector;
import com.otaliastudios.cameraview.SizeSelectors;

import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.OverlayView;
import org.tensorflow.demo.TensorFlowImageClassifier;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;

import java.util.List;

public class CameraActivity extends org.tensorflow.demo.CameraActivity implements ImageReader.OnImageAvailableListener {

    CameraView mCameraView;
    Fragment mFragment;


    private static final android.util.Size DESIRED_PREVIEW_SIZE = new android.util.Size(720, 1280);


    boolean paused;
    boolean pollCamera;

    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private Integer sensorOrientation;
    private Classifier classifier;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private static final boolean MAINTAIN_ASPECT = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.overlay_fragment, new MenuFragment());
        ft.commit();
    }

    @Override
    protected void processImage() {
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

        if (!paused) {
            runInBackground(
                    new Runnable() {
                        @Override
                        public void run() {
                            final long startTime = SystemClock.uptimeMillis();
                            if (Program.cameraPollListener != null) {
                                final String result = Program.mClassifier.getMostLikely(croppedBitmap);
                                try {
                                    Program.cameraPollListener.onObjectUpdate(result);
                                } catch (Exception e) {}
                                // Here we catch an exception in case cameraPollListener is set to
                                // null during inference
                            }
                            long endTime = SystemClock.uptimeMillis();
                            while (endTime - startTime < Program.CAMERA_POLL_DURATION) {
                                endTime = SystemClock.uptimeMillis();
                                try {
                                    Thread.sleep(100);
                                } catch (Exception e) {
                                }
                            }
                            requestRender();
                            readyForNextImage();
                        }
                    });
        }
    }

    @Override
    protected void onPreviewSizeChosen(android.util.Size size, int rotation) {

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();

        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888);

        frameToCropTransform = ImageUtils.getTransformationMatrix(
                previewWidth, previewHeight,
                224, 224,
                sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);
    }

    @Override
    protected android.util.Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_camera_fragment;
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        paused = false;
    }

    @Override
    public synchronized void onPause() {
        Program.safeDiscconnect();

        paused = true;

        super.onPause();
    }
}
