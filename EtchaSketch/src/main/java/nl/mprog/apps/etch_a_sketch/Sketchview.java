package nl.mprog.apps.etch_a_sketch;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import static java.lang.Math.abs;

/*
** class processes touches and draws lines
 */

public class SketchView extends View {

    private static final float TOUCH_TOLERANCE = 10;

    // variables to create drawing area
    private Bitmap bitmap;
    private Canvas bitmapCanvas;

    // variables to draw lines
    private Paint paintScreen;
    // line settings
    private Paint paintLine;

    // variables to follow touch paths
    private HashMap<Integer, Path> pathMap;
    private HashMap<Integer, Point> previousPointMap;

    // initialize sketchview by defaults
    public SketchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // display bitmap on screen
        paintScreen = new Paint();

        // set settings for painted lines
        paintLine = new Paint();

        // smooth edges
        paintLine.setAntiAlias(true);

        // set line color darkgrey
        paintLine.setColor(Color.GRAY);

        // set line solid
        paintLine.setStyle(Paint.Style.STROKE);

        // set default line width
        paintLine.setStrokeWidth(5);

        // set line style to round
        paintLine.setStrokeCap(Paint.Cap.ROUND);

        // maps pointer to a corresponding path object for lines currently being drawn
        pathMap = new HashMap<Integer, Path>();
        // maintains the last touch point to create a line
        previousPointMap = new HashMap<Integer, Point>();
    }

    // display bitmap and canvas when added to Activity's view
    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH){
        // create a bitmap of specified width and height
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        // draw directly onto the bitmap
        bitmapCanvas = new Canvas(bitmap);
        // fill bitmap with white pixels
        bitmap.eraseColor(Color.WHITE);
    }

    // define methods called in MainActivity

    // empties pathMap and previousPointmap
    public void clear(){

        // remove paths
        pathMap.clear();
        previousPointMap.clear();
        bitmap.eraseColor(Color.WHITE);
        // refresh the screen
        invalidate();
    }

    // set the painted line's color
    public void setDrawingColor(int color){
        paintLine.setColor(color);
    }

    // return the chosen color
    public int getDrawingColor(){
        return paintLine.getColor();
    }

    // set line width
    public void setLineWidth(int width){
        paintLine.setStrokeWidth(width);
    }

    // return line width
    public int getLineWidth(){
        return (int) paintLine.getStrokeWidth();
    }

    // redraw the view
    @Override
    protected void onDraw(Canvas canvas){
        // call drawBitmap method
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);

        // loop through each integer key in the pathMap hashMap
        for (Integer key : pathMap.keySet())
            // pass the corresponding Path to Canvas's drawPath method
            canvas.drawPath(pathMap.get(key), paintLine);
    }

    // called when the View receives a touch event
    @Override
    public boolean onTouchEvent(MotionEvent event){

        // get pointer ID, used to locate corresponding Path objects
        int action = event.getActionMasked();
        // returns an integer indes representing which finger caused the event
        int actionIndex = event.getActionIndex();


        // the user touched the screen with a new finger
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN){
            // store initial coordinates of the touch
            touchStarted(event.getX(actionIndex), event.getY(actionIndex), event.getPointerId(actionIndex));
        }

        // the user removed a finger from the screen
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
            // draw the complete path to the bitmap
            touchEnded(event.getPointerId(actionIndex));
        }

        // draw the lines
        else{
            touchMoved(event);
        }

        // inherited view method to redraw the screen
        invalidate();
        return true;
    }

    // called when a finger first touches the screen
    private void touchStarted(float x, float y, int lineID) {

        // store path for the given touch ID
        Path path;
        // store last point in path
        Point point;

        // if a path already exists, clear existing point
        if(pathMap.containsKey(lineID)){
            path = pathMap.get(lineID);
            path.reset();
            point = previousPointMap.get(lineID);
        }

        // create a new path and add to pathMap
        else{
            path = new Path();
            pathMap.put(lineID, path);
            point = new Point();
            previousPointMap.put(lineID, point);
        }

        // specify the new point's x and y values
        path.moveTo(x, y);
        point.x = (int) x;
        point.y = (int) y;

    }

    // called when user moves fingers across the screen
    private void touchMoved(MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++)
        {
            // store finger's ID and corresponding index
            int pointerID = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerID);

            // check if there already is a path associated with the pointer
            if (pathMap.containsKey(pointerID))
            {
                // get last coordinates for this drag
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                Path path = pathMap.get(pointerID);
                Point point = previousPointMap.get(pointerID);

                // calculate changes in movement
                float deltaX = abs(newX - point.x);
                float deltaY = abs(newY - point.y);

                // if significant change
                if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE)
                {
                    // // update the pointer
                    path.quadTo(point.x, point.y, (newX + point.x) / 2 , (newY + point.y) / 2);

                    // update coordinates (bug finally fixed!!!)
                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }
    }

    // called when touch is finished, gets ID of finger for which the touch ended as an argument
    private void touchEnded(int lineID)
    {
        Path path = pathMap.get(lineID);
        // draw path to bitmapCanvas
        bitmapCanvas.drawPath(path, paintLine);
        path.reset();
    }

    public void saveImage(){
        // set a file name
        String fileName = "ETCH_A_SKETCH" + System.currentTimeMillis();

        // configure new image's data setting empty ContentValues (adds a key-value pair to ContentValues object)
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName); // specify file name
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis()); // specify when it was saved
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg"); // specify its type

        // calls its insert method to get a Uniform Resource Identifier where the image will be stored
        Uri uri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try
        {
            // get an output stream to URI
            OutputStream outStream = getContext().getContentResolver().openOutputStream(uri);

            // copy bitmap to OutputStream without loss of quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);

            // force data to be written
            outStream.flush();
            // further write() or flush() invocations will cause an IOException
            outStream.close();

            // if file was saved successfully, use a Toast to indicate this
            Toast message = Toast.makeText(getContext(), R.string.message_saved, Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
            message.show();
        }

        // if something went wrong, show error message
        catch (FileNotFoundException e) {
            e.printStackTrace();

            Toast message = Toast.makeText(getContext(), R.string.message_error, Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
            message.show();
        }

        catch (IOException e) {
            e.printStackTrace();

            Toast message = Toast.makeText(getContext(), R.string.message_error, Toast.LENGTH_SHORT);
            message.setGravity(Gravity.CENTER, message.getXOffset() / 2, message.getYOffset() / 2);
            message.show();
        }
    }

}
