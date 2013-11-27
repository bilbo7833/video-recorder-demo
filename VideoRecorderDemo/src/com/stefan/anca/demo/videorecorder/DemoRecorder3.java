package com.stefan.anca.demo.videorecorder;

import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DemoRecorder3 extends Activity
{

    private int                 frontCameraId;
    private int                 backCameraId;
    private int                 currentCameraId;
    private int                 mOrientation;

    private SurfaceView         preview          = null;
    private SurfaceHolder       previewHolder    = null;
    private Camera              camera           = null;
    private MediaRecorder       recorder         = null;
    private boolean             inPreview        = false;
    private boolean             cameraConfigured = false;
    private boolean             recording = false;
    private boolean             surfaceAlive = false;

    private CamcorderProfile    camcorderProfile;

    private Button              record;

    private Uri mVideoUri;

    private int                 surfaceWidth;
    private int                 surfaceHeight;

    private final static String TAG              = "PreviewDemo";

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.demo_recorder3 );

        preview = ( SurfaceView ) findViewById( R.id.surf_video_record );
        previewHolder = preview.getHolder();
        previewHolder.addCallback( surfaceCallback );
        previewHolder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );

        setCameraIds();
        currentCameraId = frontCameraId;

        Button otherCamera = ( Button ) findViewById( R.id.btn_switch );
        otherCamera.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                releaseRecorder();
                releaseCamera();

                // swap the id of the camera to be used
                if ( currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK )
                {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                }
                else
                {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                camera = Camera.open( currentCameraId );

                cameraConfigured = false;
                prepareForRecording();
            }
        } );

        record = ( Button ) findViewById( R.id.btn_record );
        record.setOnClickListener( new View.OnClickListener()
        {

            @Override
            public void onClick( View v )
            {
                if ( recording )
                {
                    recorder.stop();
                    recording = false;
                    record.setBackgroundResource( R.drawable.select_record_button );

                    releaseRecorder();
                    try
                    {
                        camera.reconnect();
                    }
                    catch ( IOException e )
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    // Let's initRecorder so we can record again
                    initRecorder();
                    prepareRecorder();

                }
                else
                {
                    recording = true;
                    recorder.start();
                    record.setBackgroundResource( R.drawable.select_record_stop_button );
                }
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Log.d( TAG, "onResume ..." );
        camera = Camera.open( currentCameraId );
        cameraConfigured = false;
        if ( surfaceAlive )
        {
            prepareForRecording();
        }
    }

    @Override
    public void onPause()
    {
        if ( recording )
        {
            recorder.stop();
        }
        recording = false;
        inPreview = false;
        releaseRecorder();
        releaseCamera();
        super.onPause();
    }

    private void setCameraIds()
    {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for ( int camIdx = 0; camIdx < cameraCount; camIdx++ )
        {
            Camera.getCameraInfo( camIdx, cameraInfo );
            if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT )
            {
                frontCameraId = camIdx;
            }
            else if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK )
            {
                backCameraId = camIdx;
            }
        }
    }

    public static int setCameraDisplayOrientation( Activity activity, int cameraId, Camera camera )
    {
        Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        Camera.getCameraInfo( cameraId, info );
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch ( rotation )
        {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int newOrientation;
        int result;
        if ( info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT )
        {
            newOrientation = ( info.orientation + degrees ) % 360;
            result = newOrientation;
            newOrientation = ( 360 - newOrientation ) % 360; // compensate the
                                                             // mirror
        }
        else
        { // back-facing
            newOrientation = ( info.orientation - degrees + 360 ) % 360;
            result = newOrientation;
        }
        camera.setDisplayOrientation( newOrientation );
        return result;
    }

    public Camera getCameraInstance( int cameraId ) throws Exception
    {
        Camera c = Camera.open( cameraId ); // attempt to get a Camera instance
        Log.v( TAG, "Tried to get camera " + cameraId + ". Got camera: " + c );
        return c; // returns null if camera is unavailable
    }

    private Camera.Size getBestPreviewSize( int width, int height, Camera.Parameters parameters )
    {
        Camera.Size result = null;

        for ( Camera.Size size: parameters.getSupportedPreviewSizes() )
        {
            if ( size.width <= width && size.height <= height )
            {
                if ( result == null )
                {
                    result = size;
                }
                else
                {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if ( newArea > resultArea )
                    {
                        result = size;
                    }
                }
            }
        }

        return ( result );
    }

    private void initPreview()
    {
        if ( camera != null && previewHolder.getSurface() != null )
        {
            try
            {
                camera.setPreviewDisplay( previewHolder );
            }
            catch ( Throwable t )
            {
                Log.e( "PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t );
                Toast.makeText( this, t.getMessage(), Toast.LENGTH_LONG ).show();
            }

            if ( !cameraConfigured )
            {
                mOrientation = setCameraDisplayOrientation( this, currentCameraId, camera );
                Camera.Parameters parameters = camera.getParameters();
                camcorderProfile = CamcorderProfile.get( currentCameraId, CamcorderProfile.QUALITY_HIGH );
                Camera.Size size = getBestPreviewSize( camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight, parameters );

                if ( size != null )
                {
                    parameters.setPreviewSize( size.width, size.height );
                    parameters.setRecordingHint( true );
                    camera.setParameters( parameters );
                    cameraConfigured = true;
                }
            }
        }
    }

    private void startPreview()
    {
        if ( cameraConfigured && camera != null )
        {
            camera.startPreview();
            inPreview = true;
        }
    }

    private void initRecorder()
    {
        recorder = new MediaRecorder();
        camera.unlock();
        recorder.setCamera( camera );
        recorder.setAudioSource( MediaRecorder.AudioSource.DEFAULT );
        recorder.setVideoSource( MediaRecorder.VideoSource.DEFAULT );

        recorder.setProfile( camcorderProfile );

        mVideoUri = Utils.createOutputMediaFile( this );
        if ( mVideoUri != null )
        {
            recorder.setOutputFile( mVideoUri.getPath() );
        }

        recorder.setOrientationHint( mOrientation );
        recorder.setMaxDuration( 20000 ); // 20 seconds
    }

    private void prepareRecorder()
    {
        //recorder.setPreviewDisplay( previewHolder.getSurface() );

        try
        {
            recorder.prepare();
        }
        catch ( IllegalStateException e )
        {
            e.printStackTrace();
            finish();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            finish();
        }
    }

    private void prepareForRecording()
    {
        initPreview();
        startPreview();
        initRecorder();
        prepareRecorder();
    }

    public void releaseCamera()
    {
        if ( camera != null )
        {
            try
            {
                camera.reconnect();
            }
            catch ( IOException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            camera.stopPreview();
            camera.release(); // release the camera for other applications
            camera = null;
        }
    }

    public void releaseRecorder()
    {
        if ( recorder != null )
        {
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback()
   {
       public void surfaceCreated( SurfaceHolder holder )
       {
           Log.d( TAG, "surfaceCreated" );
           surfaceAlive = true;
       // no-op -- wait until
       // surfaceChanged()
       }

       public void surfaceChanged( SurfaceHolder holder,
               int format, int width, int height )
       {
           Log.v( TAG, "surfaceChanged with format: "
               + format + ", width = " + width
               + ", height = " + height );
           surfaceWidth = width;
           surfaceHeight = height;
           prepareForRecording();
       }

       public void surfaceDestroyed( SurfaceHolder holder )
       {
           Log.d( TAG, "surfaceDestroyed" );
           if (recording && recorder != null) {
               recorder.stop();
               recording = false;
           }
           if ( recorder != null )
           {
               recorder.release();
           }
       }
   };

}
