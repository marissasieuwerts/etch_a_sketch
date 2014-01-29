package nl.mprog.apps.etch_a_sketch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.app.Dialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends Activity {

    // refer to sketchview class
    private SensorManager sensorManager;
    private SketchView sketchView;

    // variables to calculate changes in device's acceleration (shake event)
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    private AtomicBoolean dialogIsDisplayed = new AtomicBoolean();

    //menu
    private static final int COLOR_MENU_ID = Menu.FIRST;
    private static final int WIDTH_MENU_ID = Menu.FIRST +1;
    private static final int ERASE_MENU_ID = Menu.FIRST +2;
    private static final int CLEAR_MENU_ID = Menu.FIRST +3;
    private static final int SAVE_MENU_ID = Menu.FIRST +4;

    // determine whether user shook the device to erase
    private static final int ACCELERATION_THRESHOLD = 15000;

    // dialog to display menus
    private Dialog currentDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get reference to SketchView class
        sketchView = (SketchView) findViewById(R.id.sketchView);

        // initialize acceleration values
        acceleration = 0.00f;
        // acceleration due to gravity on earth
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        // handles shaking
        enableAccelerometerListening();
    }

    // when on pause, disable shaking handler
    @Override
    protected void onPause(){
        super.onPause();
        disableAccelerometerListening();
    }

    // listen for accelerometer events
    private void enableAccelerometerListening() {
        // retrieve system's sensorManager service
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // register to receive accelerometer events
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
    }


    private void disableAccelerometerListening() {
        if (sensorManager != null)
        {
            // stop listening for events
            sensorManager.unregisterListener(sensorEventListener, sensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER));
            // set to null because it is unsure whether the app will return to foreground
            sensorManager = null;
        }

    }

    // create an event listener to process accelerometer events
    private SensorEventListener sensorEventListener = new SensorEventListener()
    {

        // was the movement enough to consider a shake event?
        @Override
        public void onSensorChanged(SensorEvent event) {
            // first check if there isn't any other message on the screen
            if (!dialogIsDisplayed.get()){
                // get values for the sensor event
                float x = event.values[0]; // left/right direction
                float y = event.values[1]; // up/down direction
                float z = event.values[2]; // forward/backward direction

                // save this value
                lastAcceleration = currentAcceleration;

                // calculate current
                currentAcceleration = x * x + y * y + z * z;

                // calculate change
                acceleration = currentAcceleration * (currentAcceleration - lastAcceleration);

                // if acceleration is above certain value, assume the user shaked the device and wants to erase his drawing (compare to constant TRESHOLD)
                if (acceleration > ACCELERATION_THRESHOLD){
                    // set message to confirm
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(R.string.message_erase);
                    builder.setCancelable(true);

                    // add 'erase' button
                    builder.setPositiveButton(R.string.button_erase, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialogIsDisplayed.set(false);
                            // call clear screen function in Sketchview class
                            sketchView.clear();
                        }
                    }
                    );

                    // add 'cancel' button
                    builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialogIsDisplayed.set(false);
                            // go back
                            dialog.cancel();
                        }
                    }
                    );

                    // won't display another dialog even if the device is shaken again
                    dialogIsDisplayed.set(true);
                    builder.show();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);

        // add options to menu
        menu.add(Menu.NONE, COLOR_MENU_ID, Menu.NONE, R.string.menuitem_color); // no groups, so menu.NONE
        menu.add(Menu.NONE, WIDTH_MENU_ID, Menu.NONE, R.string.menuitem_line_width);
        menu.add(Menu.NONE, ERASE_MENU_ID, Menu.NONE, R.string.menuitem_erase);
        menu.add(Menu.NONE, CLEAR_MENU_ID, Menu.NONE, R.string.menuitem_clear);
        menu.add(Menu.NONE, SAVE_MENU_ID,  Menu.NONE, R.string.menuitem_save_image);

        return true;
    }

    // When user touches a menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId())
        {
            // display menu to change color
            case COLOR_MENU_ID:
                showColorDialog();
                return true;
            // display menu to adjust line width
            case WIDTH_MENU_ID:
                showLineWidthDialog();
                return true;
            case ERASE_MENU_ID:
                // set line color to white to 'erase'
                sketchView.setDrawingColor(Color.WHITE);
                return true;
            case CLEAR_MENU_ID:
                // call function clear in sketchview class
                sketchView.clear();
                return true;
            case SAVE_MENU_ID:
                // call function saveImage in sketchview class
                sketchView.saveImage();
                return true;
        }
    return super.onOptionsItemSelected(item);
    }

    // inflates color_dialog.xml
    private void showColorDialog(){
        // set up a new dialog
        currentDialog = new Dialog(this);
        currentDialog.setContentView(R.layout.color_dialog);
        currentDialog.setTitle(R.string.title_color_dialog);
        currentDialog.setCancelable(true);

        // get onChangeListeners for the seekbars
        final SeekBar transparencySeekBar = (SeekBar) currentDialog.findViewById(R.id.transparencySeekBar);
        final SeekBar darkgreySeekBar = (SeekBar) currentDialog.findViewById(R.id.darkgreySeekBar);
        final SeekBar mediumgreySeekBar = (SeekBar) currentDialog.findViewById(R.id.mediumgreySeekBar);
        final SeekBar lightgreySeekBar = (SeekBar) currentDialog.findViewById(R.id.lightgreySeekBar);

        // register SeekBar event listeners
        transparencySeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        darkgreySeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        mediumgreySeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        lightgreySeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);

        // set values
        final int color = sketchView.getDrawingColor();
        transparencySeekBar.setProgress(Color.alpha(color));
        darkgreySeekBar.setProgress(Color.red(color));
        mediumgreySeekBar.setProgress(Color.green(color));
        lightgreySeekBar.setProgress(Color.blue(color));

        // set onclicklistener to select color
        Button setColorButton = (Button) currentDialog.findViewById(R.id.setColorButton);
        setColorButton.setOnClickListener(setColorButtonListener);

        // check if there isnt already a dialog displayed
        dialogIsDisplayed.set(true);
        currentDialog.show();
    }

    // create a onSeekBarChangeListener for the color dialog
   private SeekBar.OnSeekBarChangeListener colorSeekBarChanged = new SeekBar.OnSeekBarChangeListener() {
        // respond to seekbar events registered in SeekBarChangeListeners
        @Override
        // called when the position of a SeekBar thumb changes
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            SeekBar transparencySeekBar = (SeekBar) currentDialog.findViewById(R.id.transparencySeekBar);
            SeekBar darkgreySeekBar = (SeekBar) currentDialog.findViewById(R.id.darkgreySeekBar);
            SeekBar mediumgreySeekBar = (SeekBar) currentDialog.findViewById(R.id.mediumgreySeekBar);
            SeekBar lightgreySeekBar = (SeekBar) currentDialog.findViewById(R.id.lightgreySeekBar);

            // display current color
            View colorView = (View) currentDialog.findViewById(R.id.colorView);

            // update the color preview
            colorView.setBackgroundColor(Color.argb(transparencySeekBar.getProgress(), darkgreySeekBar.getProgress(), mediumgreySeekBar.getProgress(), lightgreySeekBar.getProgress()));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    // set color
    private View.OnClickListener setColorButtonListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            // get the SeekBar values
            SeekBar transparencySeekBar = (SeekBar) currentDialog.findViewById(R.id.transparencySeekBar);
            SeekBar darkgreySeekBar = (SeekBar) currentDialog.findViewById(R.id.darkgreySeekBar);
            SeekBar mediumgreySeekBar = (SeekBar) currentDialog.findViewById(R.id.mediumgreySeekBar);
            SeekBar lightgreySeekBar = (SeekBar) currentDialog.findViewById(R.id.lightgreySeekBar);

            // set the line color
            sketchView.setDrawingColor(Color.argb(transparencySeekBar.getProgress(), darkgreySeekBar.getProgress(), mediumgreySeekBar.getProgress(), lightgreySeekBar.getProgress()));
            dialogIsDisplayed.set(false);
            currentDialog.dismiss();
            currentDialog = null;
        }
    };

    // set dialog to change line width
    private void showLineWidthDialog(){
        currentDialog = new Dialog(this);
        currentDialog.setContentView(R.layout.width_dialog);
        currentDialog.setTitle(R.string.title_width_dialog);
        currentDialog.setCancelable(true);

        // configure SeekBar
        SeekBar widthSeekBar = (SeekBar) currentDialog.findViewById(R.id.widthSeekBar);
        widthSeekBar.setOnSeekBarChangeListener(widthSeekBarChanged);
        widthSeekBar.setProgress(sketchView.getLineWidth());

        // create an apply changes button
        Button setLineWidthButton = (Button) currentDialog.findViewById(R.id.widthDoneButton);
        setLineWidthButton.setOnClickListener(setLineWidthButtonListener);

        dialogIsDisplayed.set(true);
        currentDialog.show();
    }

    // OnSeekBarChanged listener to respond to widthseekbar events
    private SeekBar.OnSeekBarChangeListener widthSeekBarChanged = new SeekBar.OnSeekBarChangeListener() {

        // each pixel is stored on 4 bytes
        Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
        // canvas holds writing into the bitmap (associated with each other)
        Canvas canvas = new Canvas(bitmap);

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ImageView widthImageView = (ImageView) currentDialog.findViewById(R.id.widthImageView);

            // get current SeekBar value with a Paint object (holds style and color information)
            Paint p = new Paint();
            p.setColor(sketchView.getDrawingColor());
            // set brush to round
            p.setStrokeCap(Paint.Cap.ROUND);
            // adjust width to selected progress
            p.setStrokeWidth(progress);

            // redraw the line
            bitmap.eraseColor(Color.WHITE);
            canvas.drawLine(30, 50, 370, 50, p);
            widthImageView.setImageBitmap(bitmap);

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    // OnClickListener to apply changes in linewidth

   private View.OnClickListener setLineWidthButtonListener = new View.OnClickListener() {
       @Override
       public void onClick(View v) {
           SeekBar widthSeekBar = (SeekBar) currentDialog.findViewById(R.id.widthSeekBar);

           // apply changes
           sketchView.setLineWidth(widthSeekBar.getProgress());
           dialogIsDisplayed.set(false);
           currentDialog.dismiss();
           currentDialog = null;
       }
   };
}