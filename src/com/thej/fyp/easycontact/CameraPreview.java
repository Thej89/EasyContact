package com.thej.fyp.easycontact;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	
	private SurfaceHolder mHolder;
	private Camera mCamera;

	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;

		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//		this.setCameraDisplayOrientation(g, 0, mCamera);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.setDisplayOrientation(90); 
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d("cam", "Error setting camera preview: " + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

		if (mHolder.getSurface() == null) {
			return;
		}

		try {
			mCamera.stopPreview();
		} catch (Exception e) {
		}

		try {
			mCamera.setPreviewDisplay(mHolder);
//			mCamera.setDisplayOrientation(90);
			mCamera.startPreview();

		} catch (Exception e) {
			Log.d("cam", "Error starting camera preview: " + e.getMessage());
		}
	}
}