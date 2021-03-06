package lee.andrew.cerner.facial_recognition_search;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.facebook.Session;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private FragmentActivity fa;
    
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private File mPhotoFile;
    private static ProgressDialog progress;
    
    private PictureCallback mPicture = new PictureCallback() {
        
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(1);
            if (pictureFile == null){
                Log.d("Lee", "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                RotateImage(pictureFile, 90);
                deleteDialogue();
                String name=null;
                deleteDialogue();
                new FacebookPoster().requestUserInfo(Session.getActiveSession(), fa, pictureFile);
//                new FacebookPoster().getUserInfo(fa);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("Lee", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("Lee", "Error accessing file: " + e.getMessage());
            } 
        }
    };
    public class TakePictureTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPostExecute(Void result) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mCamera.takePicture(null, null, getPictureCallback());
            return null;
        }
    }
    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        fa=(FragmentActivity) context;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.xf
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("Lee", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        try {
            Camera.Parameters parameters = mCamera.getParameters();
            List<Size> sizes = parameters.getSupportedPreviewSizes();
            parameters.setPreviewSize(sizes.get(1).width, sizes.get(1).height);
            
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
           
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("Lee", "Error starting camera preview: " + e.getMessage());
        }
    }

    

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "tempfolder");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
    
    public static void RotateImage(File source, float angle)
    {
        Bitmap bm = BitmapFactory.decodeFile(source.getAbsolutePath());
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap bmr = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(source);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bmr.compress(Bitmap.CompressFormat.PNG, 90, out);
    }

    public PictureCallback getPictureCallback(){
        return mPicture;
    }
    
    public File getPhotoFile(){
        return mPhotoFile;
    }
    
   public static void makeDialogue(Context context, String title, String message){
       progress = new ProgressDialog(context);
       progress.setTitle(title);
       progress.setMessage(message);
       progress.setCanceledOnTouchOutside(false);
       progress.setCancelable(false);
       progress.show();
    }
   
   public static void deleteDialogue(){
       progress.dismiss();
   }
}