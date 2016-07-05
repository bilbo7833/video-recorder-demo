package com.sanca.videorecorder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class Utils
{
    private static final String TAG = Utils.class.getName();

    /**
     * Checks if a folder exists. Method will create the folder!
     *
     * @param folder
     *            which maybe will created
     * @return always true
     */
    public static boolean assurePathExists( File folder )
    {
        if ( !folder.exists() )
        {
            if ( !folder.mkdirs() )
            {
                Log.e( TAG, "Failed to create directory " + folder );
                return false;
            }
            else
            {
                Log.d( TAG, "Created directory " + folder );
            }
        }
        return true;
    }

    public static void deleteFilesAtPath( File parentDir )
    {
        File[] files = parentDir.listFiles();
        if ( files != null && files.length > 0 )
        {
            Log.d(TAG, "Deleting Empty Files in " + parentDir );
            for ( File file: files )
            {
                if ( file.getName().endsWith( ".mp4" ) && file.length() == 0 )
                {

                    File myFile = new File( parentDir + "/" + file.getName() );
                    Log.d(TAG, "Delete file: " + myFile.getAbsolutePath());
                    myFile.delete();
                }
            }
        }
    }

    public static void deleteEmptyVideos( Context ctx )
    {
        deleteFilesAtPath( new File(getVideoDirPath( ctx )) );
    }

    public static File getSdCard()
    {
        return Environment.getExternalStorageDirectory();
    }

    public static String getVideoDirPath( Context ctx )
    {
        return getSdCard().getAbsolutePath() + "/Android/data/" + ctx.getPackageName() + "/videos";
    }

    public static String getVideoKitPath()
    {
        return getSdCard().getAbsolutePath() + "/videokit";
    }

//    public static String getCompressedVideoPath( Context ctx )
//    {
//        String timeStamp = Utils.getCurrentTimestamp( ctx );
//        return getVideoDirPath( ctx ) + "/Charades_out_" + timeStamp + ".mp4";
//    }

    public static String getCompressedVideoPath( Context ctx, String originalVideoFilePath )
    {
        String videoFileName = Uri.parse( originalVideoFilePath ).getLastPathSegment();
        return getVideoDirPath( ctx ) + "/out_" + videoFileName;
    }

    public static String getCurrentTimestamp( Context ctx )
    {
        return new SimpleDateFormat( "yyyyMMdd_HHmmss", Locale.getDefault() )
                .format( new Date() );
    }

    /**
     * Creates a file object for the temporary video file. It may happen that an
     * existing file with the same name will be overridden.
     *
     * @param ctx
     * @return The file object to the specific file.
     */
    @SuppressWarnings( "deprecation" )
    public static Uri createOutputMediaFile( Context ctx )
    {
        String timeStamp = Utils.getCurrentTimestamp( ctx );
        File mediaStorageDir = null;
        String externalState = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( externalState ) )
        {
            Log.d( TAG, "Creating image File on the external storage." );
            mediaStorageDir = new File( getVideoFolderPath( ctx ) );

        }
        else
        {
            Log.d( TAG, "Creating image File on the internal storage." );
            try
            {
                String filesDir = ctx.getFilesDir().getAbsolutePath() + File.separator + "Charades";
                mediaStorageDir = ctx.getDir( filesDir, Context.MODE_WORLD_WRITEABLE );
            }
            // TODO: This is bad. Don't catch Exception, too general
            catch ( Exception e )
            {
                Log.e( TAG, "Creating image failed", e );
                return null;
            }
        }
        // Create the storage directory if it does not exist and quit if not
        // possible
        if ( !Utils.assurePathExists( mediaStorageDir ) )
        {
            return null;
        }
        Log.d( TAG, "MediaStorage directory: " + mediaStorageDir );
        File mediaFile =
            new File( mediaStorageDir.getPath() + File.separator + "demo_" + timeStamp + ".mp4" );
        return Uri.fromFile( mediaFile );
    }

    /**
     * Removes the charades prefix of a given file. And returns the path to the file
     * @param file
     */
    public static String removePrefix(File file, Context ctx)
    {
        File f1 = new File(getVideoDirPath( ctx ),file.getName().replace( "Charades_", "" ));
        boolean result = file.renameTo(f1);
        if(result){

            Log.d( TAG , "filename without prefix: " +f1.getName());
            return f1.getAbsolutePath();
        }
        else
        {
            Log.e(TAG, "Something went wrong during removing the prefix for file: "
                    + file.getAbsolutePath() );
            return file.getAbsolutePath();
        }

    }

    public static String addPrefix(File file, Context ctx)
    {
        File f1 = new File( getVideoDirPath( ctx ),"Charades_"+file.getName() );
        boolean result = file.renameTo( f1 );
        if(result)
        {
            Log.d(TAG, "Add prefix filename: " + f1.getName());
            return f1.getAbsolutePath();
        }
        else
        {
            Log.e(TAG, "Something went wrong during adding the prefix");
            return file.getAbsolutePath();
        }
    }

    public static String getVideoFolderPath( Context ctx )
    {
        final File sdCard = Environment.getExternalStorageDirectory();
        Log.d( TAG, "SD Card Path: " + sdCard.toString() );
        final String path =
            sdCard.getAbsolutePath() + "/Android/data/" + ctx.getPackageName() + "/videos";
        Log.d( TAG, "SD Card Path to app: " + path );
        return path;
    }

    /**
     * Checks if a given path exists
     *
     * @param path
     * @return
     */
    public static boolean pathExists( final String path )
    {
        return pathExists( new File( path ) );
    }

    public static boolean pathExists( final File path )
    {
        if ( path.exists() )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
