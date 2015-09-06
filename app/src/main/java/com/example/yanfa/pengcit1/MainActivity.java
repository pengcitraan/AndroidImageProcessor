package com.example.yanfa.pengcit1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private TextView textViewTotalColor;
    private ImageView cameraView;
    private ImageView equalizedView;
    private Button cameraButton;
    private SeekBar pixelSeekBar;
    private SeekBar seekBar2;
    private Bitmap currentImageBitmap;
    private Boolean pictureTaken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pictureTaken = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = (ImageView)findViewById(R.id.cameraImage);
        equalizedView = (ImageView)findViewById(R.id.equalizedImage);
        textViewTotalColor = (TextView)findViewById(R.id.textViewTotalWarna);

        pixelSeekBar = (SeekBar)findViewById(R.id.pixelSeekBar);
        seekBar2 = (SeekBar)findViewById(R.id.seekBar2);

        cameraButton = (Button)findViewById(R.id.cameraButton);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                //System.out.println("width : " + pixelSeekBar.getProgress());
            }
        });

        pixelSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(pictureTaken) {
                    System.out.println("masuk kok");
                    int progressChange = (int) (seekBar.getProgress() * 1000000);
                    int progressChange2 = (int) (seekBar2.getProgress() * 1000000);
                    equalizedView.setImageBitmap(ImageProcessor.histogramEqualization(currentImageBitmap, progressChange, progressChange2));
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
            //System.out.println("pixelnya : " + currentImageBitmap.getPixel(0, 0));
            cameraView.setImageBitmap(currentImageBitmap);
            equalizedView.setImageBitmap(ImageProcessor.histogramEqualization(currentImageBitmap,0,0));
            textViewTotalColor.setText("Jumlah Warna : " + ImageProcessor.countTotalColor(currentImageBitmap));
            pictureTaken = true;
        }
    }

    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}