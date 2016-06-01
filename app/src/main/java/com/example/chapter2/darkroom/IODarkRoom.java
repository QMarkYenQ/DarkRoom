package com.example.chapter2.darkroom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.IOException;
import java.util.Arrays;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
public class IODarkRoom extends AppCompatActivity {
    private static final int SELECT_PICTURE = 1;
    private static final String  TAG = "Ex::IODarkRoom";
    private String selectedImagePath;
    Mat sampledImage=null;
    Mat originalImage=null;
    Mat greyImage=null;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iodark_room);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.iodark_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_openGallary) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,
                    "Select Picture"), SELECT_PICTURE);
            return true;
        }
        else if (id == R.id.action_Hist) {
            if(sampledImage==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat histImage=new Mat();
            sampledImage.copyTo(histImage);
            calcHist(histImage);
            displayImage(histImage);
            return true;
        }
        else if (id == R.id.action_togs) {
            if(sampledImage==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            greyImage=new Mat();
            Imgproc.cvtColor(sampledImage, greyImage, Imgproc.COLOR_RGB2GRAY);
            displayImage(greyImage);
            return true;
        }
        else if (id == R.id.action_egs) {
            if(greyImage==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "You need to convert the image to greyscale first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat eqGS=new Mat();
            Imgproc.equalizeHist(greyImage, eqGS);
            displayImage(eqGS);
            return true;
        }
        else if (id == R.id.action_HSV) {
            if(sampledImage==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat V=new Mat(sampledImage.rows(),sampledImage.cols(),CvType.CV_8UC1);
            Mat S=new Mat(sampledImage.rows(),sampledImage.cols(),CvType.CV_8UC1);

            Mat HSV=new Mat();
            Imgproc.cvtColor(sampledImage, HSV, Imgproc.COLOR_RGB2HSV);
            byte [] Vs=new byte[3];
            byte [] vsout=new byte[1];
            byte [] ssout=new byte[1];

            for(int i=0;i<HSV.rows();i++){
                for(int j=0;j<HSV.cols();j++)
                {
                    HSV.get(i, j,Vs);
                    V.put(i,j,new byte[]{Vs[2]});
                    S.put(i,j,new byte[]{Vs[1]});
                }
            }
            Imgproc.equalizeHist(V, V);
            Imgproc.equalizeHist(S, S);

            for(int i=0;i<HSV.rows();i++){
                for(int j=0;j<HSV.cols();j++)
                {
                    V.get(i, j,vsout);
                    S.get(i, j,ssout);
                    HSV.get(i, j,Vs);
                    Vs[2]=vsout[0];
                    Vs[1]=ssout[0];
                    HSV.put(i, j,Vs);
                }
            }
            Mat enhancedImage=new Mat();
            Imgproc.cvtColor(HSV,enhancedImage,Imgproc.COLOR_HSV2RGB);
            displayImage(enhancedImage);
            return true;
        }
        else if(id==R.id.action_ER)
        {
            if(sampledImage==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat redEnhanced=new Mat();
            sampledImage.copyTo(redEnhanced);
            Mat redMask=new Mat(sampledImage.rows(),sampledImage.cols(),sampledImage.type(),new Scalar(1,0,0,0));
            enhanceChannel(redEnhanced,redMask);

            displayImage(redEnhanced);
            return true;
        }
        else if(id==R.id.action_EG)
        {
            if(sampledImage==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat greenEnhanced=new Mat();
            sampledImage.copyTo(greenEnhanced);
            Mat greenMask=new Mat(sampledImage.rows(),sampledImage.cols(),sampledImage.type(),new Scalar(0,1,0,0));
            enhanceChannel(greenEnhanced,greenMask);

            displayImage(greenEnhanced);
            return true;
        }
        else if(id==R.id.action_ERG)
        {
            if(sampledImage==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat enhanced=new Mat();
            sampledImage.copyTo(enhanced);
            Mat greenMask=new Mat(sampledImage.rows(),sampledImage.cols(),sampledImage.type(),new Scalar(0,1,0,0));
            enhanceChannel(enhanced,greenMask);

            Mat redMask=new Mat(sampledImage.rows(),sampledImage.cols(),sampledImage.type(),new Scalar(1,0,0,0));
            enhanceChannel(enhanced,redMask);

            displayImage(enhanced);
            return true;
        }
        else if (id == R.id.action_revert) {
            if(sampledImage==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "You need to load an image first!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            displayImage(sampledImage);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                Log.i(TAG, "selectedImagePath: " + selectedImagePath);
                loadImage(selectedImagePath);
                displayImage(sampledImage);
            }
        }
    }

    /**
     * helper to retrieve the path of an image URI
     */
    private String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    private static double calculateSubSampleSize(Mat srcImage,int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = srcImage.height();
        final int width = srcImage.width();
        double inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final double heightRatio = (double) reqHeight / (double) height;
            final double widthRatio = (double) reqWidth / (double) width;

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
    private void calcHist(Mat image)
    {
        //Matrix will hold the histogram values
        Mat hist = new Mat();
        //Number of Histogram bins
        int mHistSizeNum = 25;
        //A matrix of one column and one row holding the number of histogram bins
        MatOfInt mHistSize = new MatOfInt(mHistSizeNum);
        //A float array to hold the histogram values
        float []mBuff = new float[mHistSizeNum];
        //A matrix of one column and two rows holding the histogram range
        MatOfFloat histogramRanges = new MatOfFloat(0f, 256f);
        //A mask just in case you wanted to calculate the histogram for a specific area in the image
        Mat mask=new Mat();
        //Three colors for the red , green, and blue channel histogram
        Scalar mColorsRGB[] = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
        org.opencv.core.Point mP1 = new org.opencv.core.Point();
        org.opencv.core.Point mP2 = new org.opencv.core.Point();

        int thikness = (int) (image.width() / (mHistSizeNum+10)/3);
        if(thikness > 3) thikness = 3;

        Size sizeRgba = image.size();
        MatOfInt mChannels[] = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };

        int offset = (int) ((sizeRgba.width - (3*mHistSizeNum+30)*thikness));
        // RGB
        for(int c=0; c<3; c++) {
            Imgproc.calcHist(Arrays.asList(image), mChannels[c], mask, hist, mHistSize, histogramRanges);
            //set a limit to the maximum histogram value, so you can display it on your device screen
            Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
            //get the histogram values for channel C
            hist.get(0, 0, mBuff);
            for(int h=0; h<mHistSizeNum; h++) {
                //calculate the starting x position related to channel C plus 10 pixels spacing mul the thickness
                mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thikness;
                mP1.y = sizeRgba.height-1;
                mP2.y = mP1.y - (int)mBuff[h];
                Imgproc.line(image, mP1, mP2, mColorsRGB[c], thikness);
            }
        }
    }
    private void displayImage(Mat image)
    {
        // convert to bitmap:
        Bitmap bitMap = Bitmap.createBitmap(image.cols(), image.rows(),Bitmap.Config.RGB_565);
        Utils.matToBitmap(image, bitMap);

        // find the imageview and draw it!
        ImageView iv = (ImageView) findViewById(R.id.IODarkRoomImageView);
        iv.setImageBitmap(bitMap);
    }
    private void loadImage(String path)
    {
        originalImage = Imgcodecs.imread(path);
        Mat rgbImage=new Mat();
        sampledImage=new Mat();

        Imgproc.cvtColor(originalImage, rgbImage, Imgproc.COLOR_BGR2RGB);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        double downSampleRatio=calculateSubSampleSize(rgbImage,width,height);

        Imgproc.resize(rgbImage, sampledImage, new Size(),downSampleRatio,downSampleRatio,Imgproc.INTER_AREA);

        try {
            ExifInterface exif = new ExifInterface(selectedImagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            switch (orientation )
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    //get the mirrored image
                    sampledImage=sampledImage.t();
                    //flip on the y-axis
                    Core.flip(sampledImage, sampledImage, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    //get up side down image
                    sampledImage=sampledImage.t();
                    //Flip on the x-axis
                    Core.flip(sampledImage, sampledImage, 0);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void enhanceChannel(Mat imageToEnhance,Mat mask)
    {

        Mat channel=new Mat(sampledImage.rows(),sampledImage.cols(),CvType.CV_8UC1);
        sampledImage.copyTo(channel,mask);

        Imgproc.cvtColor(channel, channel, Imgproc.COLOR_RGB2GRAY,1);
        Imgproc.equalizeHist(channel, channel);
        Imgproc.cvtColor(channel, channel, Imgproc.COLOR_GRAY2RGB,3);
        channel.copyTo(imageToEnhance,mask);
    }

}
