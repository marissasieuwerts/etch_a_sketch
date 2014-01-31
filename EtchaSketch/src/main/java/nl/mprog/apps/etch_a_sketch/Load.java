package nl.mprog.apps.etch_a_sketch;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.SensorManager;
import android.os.Bundle;

/**
 * Created by Marissa on 31/01/14.
 */
public class Load extends Activity {

    public SketchView sketchView;
    public Canvas bitmapCanvas;
    public Bitmap bitmap = (Bitmap) getIntent().getExtras().get("bitmap");


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get reference to SketchView class
        sketchView = (SketchView) findViewById(R.id.sketchView);
    }

    public void setImage(){
        bitmapCanvas = new Canvas(bitmap);
    }
}


