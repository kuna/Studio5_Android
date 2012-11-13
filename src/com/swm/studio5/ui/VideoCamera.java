// http://code.google.com/p/sipdroid/source/browse/trunk/src/org/sipdroid/sipua/ui/VideoCamera.java
// http://pgm-progger.blogspot.kr/2011/05/android_2764.html
// http://stackoverflow.com/questions/11249642/mediarecorder-start-failed-19
// http://blog.naver.com/dnakeye/100131758666 - supporting Preview Size
// http://stackoverflow.com/questions/2933882/how-to-draw-an-overlay-on-a-surfaceview-used-by-camera-on-android - Overlay UI

package com.swm.studio5.ui;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.List;

import com.swm.studio5.R;
import com.swm.studio5.rtsp.RtpPacket;
import com.swm.studio5.rtsp.RtpSocket;
import com.swm.studio5.rtsp.SASocket;
import com.swm.studio5.tool.ExceptionTool;
import com.swm.studio5.tool.Setting;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoCamera extends CallScreen implements
		SurfaceHolder.Callback, OnClickListener, OnLongClickListener, OnErrorListener {
    private static final String TAG = "videocamera";
    private static final float VIDEO_ASPECT_RATIO = 176.0f / 144.0f;
    
    VideoPreview mVideoPreview;
    SurfaceHolder mSurfaceHolder = null;
    VideoView mVideoFrame;
    MediaController mMediaController;
	Camera mCamera;
    Context mContext = this;
    
    // for packet data
    LocalSocket receiver,sender;
    LocalServerSocket lss;

    private MediaRecorder mMediaRecorder;
	private TextView mFPS;
	private TextView mRecordingTimeView;
	
    boolean mMediaRecorderRecording = false;
    boolean isAvailableSprintFFC,useFront = true;
    boolean videoQualityHigh;
	private RtpSocket rtp_socket;
	private Thread t;
	
	private boolean initializeVideo() {
        Log.v(TAG, "initializeVideo");
        
        if (mSurfaceHolder == null) {
            Log.v(TAG, "SurfaceHolder is null");
            return false;
        }

        /** MEDIARECORDER INITIALIZE **/
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        else
            mMediaRecorder.reset();
        
        /** CAMERA INITIALIZE **/
        if (mCamera != null) {
            if (Integer.parseInt(Build.VERSION.SDK) >= 8)
            	ExceptionTool.reconnect(mCamera);
            mCamera.release();
            mCamera = null;
        }
	
        mCamera = Camera.open(); 
        //Camera.Parameters parameters = mCamera.getParameters(); 
        //parameters.set("camera-id", 2); 
        //mCamera.setParameters(parameters); 
	    
        // set preview camera pic
	    try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    // MediaRecorder setting
        mCamera.unlock();
	    mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setOutputFile(sender.getFileDescriptor());

        if (videoQualityHigh) {
            mMediaRecorder.setVideoSize(800,480);
        } else {
            mMediaRecorder.setVideoSize(320,240);
        }
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
        
        // MediaRecorder recording start
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.setOnErrorListener(this);
            mMediaRecorder.start();
        } catch (IOException exception) {
        	exception.printStackTrace();
            releaseMediaRecorder();
            finish();
            return false;
        }

        // set videopreview event handler
        //mVideoPreview.setOnLongClickListener(this);
	    //mVideoPreview.setOnClickListener(this);
	    Log.i(TAG, "event listener registered");
	    
        return true;
	}

	
	private void setScreenOnFlag() {
        Window w = getWindow();
        final int keepScreenOnFlag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        if ((w.getAttributes().flags & keepScreenOnFlag) == 0) {
            w.addFlags(keepScreenOnFlag);
        }
    }
	
	/** Activity Starting **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// change mode to fullscreen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setScreenOnFlag();
        setContentView(R.layout.video_camera);

        // set VideoPreview window
        mVideoPreview = (VideoPreview) findViewById(R.id.camera_preview);
        mVideoPreview.setAspectRatio(VIDEO_ASPECT_RATIO);
        
        // set Object size
        Display d = getWindowManager().getDefaultDisplay();
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.video_layout);
        rl.getLayoutParams().height = d.getHeight() - 80;
        mVideoPreview.getLayoutParams().width = d.getWidth();
        mVideoPreview.getLayoutParams().height = d.getHeight();

        // don't set mSurfaceHolder here. We have it set ONLY within
        // surfaceCreated / surfaceDestroyed, other parts of the code
        // assume that when it is set, the surface is also set.
        /** after creation, initalizevideo will autimatically start **/
        SurfaceHolder holder = mVideoPreview.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mRecordingTimeView = (TextView) findViewById(R.id.record_status);
        //mVideoFrame = (VideoView) findViewById(R.id.video_frame);
        Button b = (Button) findViewById(R.id.record);
        b.setOnClickListener(this);
        
        mMediaRecorderRecording = false;
        setRecordingUI();
	}
	
    @Override
	protected void onPause() {
		super.onPause();
		closeSocket();
	}


	@Override
	protected void onResume() {
		super.onResume();
		
		setSocket();
        mVideoPreview.setVisibility(View.VISIBLE);
        //if (!mMediaRecorderRecording) initializeVideo();
	}


	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	private void setSocket() {
		receiver = new LocalSocket();
		try {
			lss = new LocalServerSocket("Studio5");
			receiver.connect(new LocalSocketAddress("Studio5"));
			receiver.setReceiveBufferSize(5000000);
			receiver.setSendBufferSize(5000000);
			sender = lss.accept();
			sender.setReceiveBufferSize(5000000);
			sender.setSendBufferSize(5000000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void closeSocket() {
		try {
			lss.close();
			receiver.close();
			sender.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void releaseMediaRecorder() {
        Log.v(TAG, "Releasing media recorder.");
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            if (mCamera != null) {
                if (Integer.parseInt(Build.VERSION.SDK) >= 8)
					ExceptionTool.reconnect(mCamera);
                
                mCamera.release();
                mCamera = null;
            }
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
	
	/** return Best size for video **/
    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    
	public void surfaceChanged(SurfaceHolder arg0, int format, int w, int h) {
        // show preview movie
        Camera.Parameters parameters = mCamera.getParameters();
        List<Size> sizes = parameters.getSupportedPreviewSizes();
        Size optimalSize = getOptimalPreviewSize(sizes, w, h);
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);
        
        /**
         * by MediaRecorder, these methods become unnecessary
        mCamera.setParameters(parameters);
        mCamera.startPreview();
         */
        
        Log.i(TAG, "Camera Working");
	}


	public void surfaceCreated(SurfaceHolder arg0) {
        mSurfaceHolder = arg0;
        initializeVideo();
	}


	public void surfaceDestroyed(SurfaceHolder arg0) {
        mSurfaceHolder = null;
	}


	public void onClick(View arg0) {
		Log.i(TAG, "startVideoRecording");
		
		mMediaRecorderRecording = !mMediaRecorderRecording;
		setRecordingUI();
		if (mMediaRecorderRecording)
			startVideoRecording();
		else
			stopVideoRecording();
	}
	
	private void setRecordingUI() {
		/** set Button/TextView UI **/
		TextView t1 = (TextView) findViewById(R.id.record_status);
		TextView t2 = (TextView) findViewById(R.id.video_status);
		Button b = (Button) findViewById(R.id.record);

		if (!mMediaRecorderRecording) {
			t1.setTextColor(Color.parseColor("#FFFFFF"));
			t2.setTextColor(Color.parseColor("#FFFF00"));
			t1.setText("Press to REC");
			t2.setText("�� STOP");
			b.setBackgroundResource(R.drawable.play);
		} else {
			t1.setTextColor(Color.parseColor("#FF0000"));
			t2.setTextColor(Color.parseColor("#FF0000"));
			t1.setText("Press to STOP");
			t2.setText("�� REC");
			b.setBackgroundResource(R.drawable.stop);
			
		}
	}


	public boolean onLongClick(View arg0) {
		// TODO Auto-generated method stub
		return false;
	}


	public void onError(MediaRecorder arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	
    /** MAIN PART **/
    private void startVideoRecording() {
    	Log.v(TAG, "startVideoRecording");

		//RtpStreamSender.delay = 1;

		try {
			if (rtp_socket == null)
				rtp_socket = new RtpSocket(new SASocket(Setting.local_port),	// local video port
						InetAddress.getByName(Setting.remote_url),	// remote addr
						Setting.remote_port);	// remove video port
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}               

		(t = new Thread() {
			private int fps;
			private boolean change;

			public void run() {
				int frame_size = 1400;
				byte[] buffer = new byte[frame_size + 14];
				buffer[12] = 4;
				RtpPacket rtp_packet = new RtpPacket(buffer, 0);
				int seqn = 0;
				int num,number = 0,src,dest,len = 0,head = 0,lasthead = 0,lasthead2 = 0,cnt = 0,stable = 0;
				long now,lasttime = 0;
				double avgrate = videoQualityHigh?45000:24000;
				double avglen = avgrate/20;

				InputStream fis = null;
				try {
					fis = receiver.getInputStream();
				} catch (IOException e1) {
					e1.printStackTrace();
					rtp_socket.getDatagramSocket().close();
					return;
				}

				rtp_packet.setPayloadType(103);
				while (videoValid()) {
					num = -1;
					try {
						num = fis.read(buffer,14+number,frame_size-number);
					} catch (IOException e) {
						e.printStackTrace();
						break;
					}
					if (num < 0) {
						try {
							sleep(20);
						} catch (InterruptedException e) {
							break;
						}
						continue;                                                       
					}
					number += num;
					head += num;
					try {
						now = SystemClock.elapsedRealtime();
						if (lasthead != head+fis.available() && ++stable >= 5 && now-lasttime > 700) {
							if (cnt != 0 && len != 0)
								avglen = len/cnt;
							if (lasttime != 0) {
								fps = (int)((double)cnt*1000/(now-lasttime));
								avgrate = (double)((head+fis.available())-lasthead2)*1000/(now-lasttime);
							}
							lasttime = now;
							lasthead = head+fis.available();
							lasthead2 = head;
							len = cnt = stable = 0;
						}
					} catch (IOException e1) {
						e1.printStackTrace();
						break;
					}

					for (num = 14; num <= 14+number-2; num++)
						if (buffer[num] == 0 && buffer[num+1] == 0) break;
					if (num > 14+number-2) {
						num = 0;
						rtp_packet.setMarker(false);
					} else {        
						num = 14+number - num;
						rtp_packet.setMarker(true);
					}

					rtp_packet.setSequenceNumber(seqn++);
					rtp_packet.setPayloadLength(number-num+2);
					if (seqn > 10) try {
						rtp_socket.send(rtp_packet);
						len += number-num;
					} catch (IOException e) {
						e.printStackTrace();
						break;
					}

					if (num > 0) {
						num -= 2;
						dest = 14;
						src = 14+number - num;
						if (num > 0 && buffer[src] == 0) {
							src++;
							num--;
						}
						number = num;
						while (num-- > 0)
							buffer[dest++] = buffer[src++];
						buffer[12] = 4;

						cnt++;
						try {
							if (avgrate != 0)
								Thread.sleep((int)(avglen/avgrate*1000));
						} catch (Exception e) {
							break;
						}
						rtp_packet.setTimestamp(SystemClock.elapsedRealtime()*90);
					} else {
						number = 0;
						buffer[12] = 0;
					}
					if (change) {
						change = false;
						long time = SystemClock.elapsedRealtime();

						try {
							while (fis.read(buffer,14,frame_size) > 0 &&
									SystemClock.elapsedRealtime()-time < 3000);
						} catch (Exception e) {
						}
						number = 0;
						buffer[12] = 0;
					}
				}
				rtp_socket.getDatagramSocket().close();
				try {
					while (fis.read(buffer,0,frame_size) > 0);
				} catch (IOException e) {
				}
			}

			private boolean videoValid() {
				return true;
			}
		}).start();   
	}

    private void stopVideoRecording() {
    	t.stop();
    	rtp_socket.close();
    	rtp_socket = null;
    	
    	/*
	    Log.v(TAG, "stopVideoRecording");
	    try {
	        mMediaRecorder.setOnErrorListener(null);
	        mMediaRecorder.setOnInfoListener(null);
	        mMediaRecorder.stop();
	    } catch (RuntimeException e) {
	        Log.e(TAG, "stop fail: " + e.getMessage());
	    }
	
	    mMediaRecorderRecording = false;
	        
	    releaseMediaRecorder();*/
    }
}


