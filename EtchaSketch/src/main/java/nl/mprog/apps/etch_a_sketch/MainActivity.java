package nl.mprog.apps.etch_a_sketch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends Activity {

    // refer to sketchview class
    private Sketchview sketchView;
    private SensorManager sensorManager;

    // variables to calculate changes in device's acceleration (shake event)
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    private AtomicBoolean dialogIsDisplayed = new AtomicBoolean();

    //menu
    private static final int COLOR_MENU_ID = menu.FIRST;
    private static final int WIDTH_MENU_ID = menu.FIRST +1;
    private static final int ERASE_MENU_ID = menu.FIRST +2;
    private static final int CLEAR_MENU_ID = menu.FIRST +3;
    private static final int SAVE_MENU_ID = menu.FIRST +4;

    // determine whether user shook the device to erase
    private static final int ACCELERATION_TRESHOLD = 15000;

    // dialog to display menus
    private Dialog currentDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get reference to Sketchview class
        sketchView = (Sketchview) findViewById(sketchView);

        // initialize acceleration values
        acceleration = 0.00f;
        // acceleration due to gravity on earth
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        // handles shaking
        enableAccelerometerListening();
    }

    // listen for accelerometer events
    private void enableAccelerometerListening() {
        // retrieve system's sensorManager service
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // register to receive accelerometer events
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
    }


    // when on pause, disable shaking handler
    protected void OnPause(){
        super.onPause();
        disableAccelerometerListening();
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
                acceleration = currentAcceleration + (currentAcceleration - lastAcceleration);

                // if acceleration is above certain value, assume the user shaked the device and wants to erase his drawing (compare to constant TRESHOLD)
                if (acceleration > ACCELERATION_TRESHOLD){
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
                            Sketchview.clear();
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
        menu.add(Menu.NONE, COLOR_MENU_ID, Menu.NONE, R.string.menuitem_color);
        menu.add(Menu.NONE, WIDTH_MENU_ID, Menu.NONE, R.string.menuitem_line_width);
        menu.add(Menu.NONE, ERASE_MENU_ID, Menu.NONE, R.string.menuitem_erase);
        menu.add(Menu.NONE, CLEAR_MENU_ID, Menu.NONE, R.string.menuitem_clear);
        menu.add(Menu.NONE, SAVE_MENU_ID,  Menu.NONE, R.string.menuitem_save_image);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId())
        {
            // display menu to change color
            case COLOR_MENU_ID:
                showColorDialog();
                return true;
            case WIDTH_MENU_ID:
                showLineWidthDialog();
                return true;

        }

    return super.onOptionsItemSelected(item);
    }


}