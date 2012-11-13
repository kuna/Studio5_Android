package com.swm.studio5.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

class VideoPreview extends SurfaceView {
    private float mAspectRatio;
    private int mHorizontalTileSize = 1;
    private int mVerticalTileSize = 1;

    /**
     * Setting the aspect ratio to this value means to not enforce an aspect ratio.
     */
    public static float DONT_CARE = 0.0f;

    public VideoPreview(Context context) {
        super(context);
    }

    public VideoPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setTileSize(int horizontalTileSize, int verticalTileSize) {
        if ((mHorizontalTileSize != horizontalTileSize)
                || (mVerticalTileSize != verticalTileSize)) {
            mHorizontalTileSize = horizontalTileSize;
            mVerticalTileSize = verticalTileSize;
            requestLayout();
            invalidate();
        }
    }

    public void setAspectRatio(int width, int height) {
        setAspectRatio(((float) width) / ((float) height));
    }

    public void setAspectRatio(float aspectRatio) {
        if (mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
            requestLayout();
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAspectRatio != DONT_CARE) {
            int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

            int width = widthSpecSize;
            int height = heightSpecSize;

            if (width > 0 && height > 0) {
                float defaultRatio = ((float) width) / ((float) height);
                if (defaultRatio < mAspectRatio) {
                    // Need to reduce height
                    height = (int) (width / mAspectRatio);
                } else if (defaultRatio > mAspectRatio) {
                    width = (int) (height * mAspectRatio);
                }
                width = roundUpToTile(width, mHorizontalTileSize, widthSpecSize);
                height = roundUpToTile(height, mVerticalTileSize, heightSpecSize);
                Log.i("VideoPreview", "ar " + mAspectRatio + " setting size: " + width + 'x' + height);
                setMeasuredDimension(width, height);
                return;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int roundUpToTile(int dimension, int tileSize, int maxDimension) {
        return Math.min(((dimension + tileSize - 1) / tileSize) * tileSize, maxDimension);
    }

	public void setOnClickListener(VideoCamera videoCamera) {
		// TODO Auto-generated method stub
		
	}
}