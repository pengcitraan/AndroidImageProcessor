package com.example.yanfa.pengcit1;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQ_CODE_PICK_IMAGE = 2;

    private TextView textViewTotalColor;
    private ImageView cameraView;
    private ImageView equalizedView;
    private Button cameraButton;
    private Button imagePickerButton;
    private SeekBar pixelSeekBar;
    private SeekBar seekBar2;
    private Bitmap currentImageBitmap;
    private Boolean pictureTaken;
    private ImageProcessor imageProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        imageProcessor = new ImageProcessor();
        pictureTaken = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = (ImageView)findViewById(R.id.cameraImage);
        equalizedView = (ImageView)findViewById(R.id.equalizedImage);
        textViewTotalColor = (TextView)findViewById(R.id.textViewTotalWarna);

        pixelSeekBar = (SeekBar)findViewById(R.id.pixelSeekBar);
        cameraButton = (Button)findViewById(R.id.cameraButton);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                //System.out.println("width : " + pixelSeekBar.getProgress());
            }
        });

        imagePickerButton = (Button)findViewById(R.id.imageChooserButton);
        imagePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(v);
            }
        });

        pixelSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(pictureTaken) {
                    //System.out.println("masuk kok");
                    int progressChange = (int) (seekBar.getProgress() * 2);
                    //int progressChange2 = (int) (seekBar2.getProgress() * 1000000);
                    equalizedView.setImageBitmap(ImageProcessor.histogramEqualization(currentImageBitmap, progressChange));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap tempBitmap = (Bitmap) extras.get("data");
            currentImageBitmap = ImageProcessor.toGrayscale(tempBitmap);
            cameraView.setImageBitmap(currentImageBitmap);
            equalizedView.setImageBitmap(ImageProcessor.edgeHomogen(currentImageBitmap));
            textViewTotalColor.setText("Jumlah Warna : " + ImageProcessor.countTotalColor(currentImageBitmap));
            pictureTaken = true;
        }
        else if(requestCode == REQ_CODE_PICK_IMAGE && resultCode == RESULT_OK){
            InputStream stream = null;
            try {
                // recyle unused bitmaps
                if (currentImageBitmap != null) {
                    currentImageBitmap.recycle();
                }
                stream = getContentResolver().openInputStream(data.getData());
                currentImageBitmap = BitmapFactory.decodeStream(stream);

                cameraView.setImageBitmap(currentImageBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (stream != null)
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }

            // Versi khusus untuk plat karena ada perubahan warna
            // currentImageBitmap = imageProcessor.toGrayscale(imageProcessor.fastblur(imageProcessor.toGrayscale(currentImageBitmap), (float) 0.5, 1), 0)
            // List<String> chainCode = imageProcessor.getChaineCodes(ImageProcessor.toGrayscale(currentImageBitmap, 0));

            // Versi buat digit biasa untuk modeling
            // List<String> chainCode = imageProcessor.getChaineCodes(currentImageBitmap);
//
//            List<String> digit = new ArrayList<String>();
//            for(int i = 0; i < chainCode.size(); i++){
//                if(chainCode.get(i).length() > 50){
//                    digit.add(imageProcessor.detectPattern(chainCode.get(i)));
//                }
//            }
//            textViewTotalColor.setText("Digit : " + digit);

            cameraView.setImageBitmap(ImageProcessor.toGrayscale(currentImageBitmap));
            //currentImageBitmap = imageProcessor.toGrayscale(imageProcessor.fastblur(imageProcessor.toGrayscale(currentImageBitmap), (float) 0.5, 1), 0);
            equalizedView.setImageBitmap(ImageProcessor.clusterFace(ImageProcessor.toGrayscale(ImageProcessor.twoDegree(currentImageBitmap, 1), new Float(0.4))));
            // equalizedView.setImageBitmap(ImageProcessor.twoDegree(currentImageBitmap, 1));

            // False artinya untuk plat, True artinya untuk model
            //textViewTotalColor.setText("Point: " + imageProcessor.getExtremePoints(currentImageBitmap, 0, 0, false));
            //imageProcessor.gridFullObject(currentImageBitmap, false);
        }
    }

    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    private void pickImage(View View) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQ_CODE_PICK_IMAGE);
    }
}
