package com.sanca.videorecorder;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DemoRecorder2 extends Activity
{

    private int                 frontCameraId;
    private int                 backCameraId;
    private int                 currentCameraId;
    private int                 mOrientation;

    private SurfaceView         preview          = null;
    private SurfaceHolder       previewHolder    = null;
    private Camera              camera           = null;
    private boolean             inPreview        = false;
    private boolean             cameraConfigured = false;

    private int                 surfaceWidth;
    private int                 surfaceHeight;

    private final static String TAG              = "PreviewDemo";

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.demo_recorder2 );

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
                if ( inPreview )
                {
                    camera.stopPreview();
                }
                // NB: if you don't release the current camera before switching,
                // you app will crash
                camera.release();

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
                initPreview();
                startPreview();
            }
        } );
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Log.d( TAG, "onResume ..." );
        camera = Camera.open( currentCameraId );
    }

    @Override
    public void onPause()
    {
        if ( inPreview )
        {
            camera.stopPreview();
        }

        camera.release();
        camera = null;
        inPreview = false;

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
                Log.e( TAG, "Exception in setPreviewDisplay()", t );
                Toast.makeText( DemoRecorder2.this, t.getMessage(), Toast.LENGTH_LONG ).show();
            }

            if ( !cameraConfigured )
            {
                setCameraDisplayOrientation( DemoRecorder2.this, currentCameraId, camera );
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = getBestPreviewSize( surfaceWidth, surfaceHeight, parameters );

                if ( size != null )
                {
                    parameters.setPreviewSize( size.width, size.height );
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

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback()
   {
       public void surfaceCreated( SurfaceHolder holder )
       {
           Log.d( TAG, "surfaceCreated" );
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
           initPreview();
           startPreview();
       }

       public void surfaceDestroyed( SurfaceHolder holder )
       {
           Log.d( TAG, "surfaceDestroyed" );
           // no-op
       }
   };

}
