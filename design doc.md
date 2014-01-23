# Native App: Etch a Sketch

####Update Design doc Thurs 23-01-2014!

Goal: Implement the classic 80s/90s drawing tool 'Etch a Sketch' for touch devices

###*Features and how to implement them:*

There are basically 2 main classes: the <b>main activity</b> provides the settings menus and message dialogs. Furthermore it handles any accelerometer events: it
calculates changes in the devices acceleration and reacts to these events. 
The second class <b>sketchview</b> inherits from the main activity, handles all touching events and enables the user to draw lines.

1. Drawing area on screen (sketchview class)
	* draw lines onto Bitmaps: associate the view of the drawing area with a bitmap
	(http://www.edumobile.org/android/android-beginner-tutorials/using-bitmap-class-object-to-draw-an-image/)
	* Create methods to handle touches: touchStarted, touchChanged, touchEnded, store paths somehow and convert this into a view
	
![alt text](https://github.com/marissasieuwerts/etch_a_sketch/blob/master/homescreen.png?raw=true "Mockup drawing area, still need to add buttons to bring up menu dialogs")
	

2. Change colors (main activity class)
	* specify dark, medium and light grey components with a seekBar in a popup (dialog)
	* specify transparency from transparent to completely opaque
	* onSeekBarChangeListener to track adjustments -> setDrawingColor();
	* sketchview class inherits these values -> getDrawingColor();
	
![alt text](https://github.com/marissasieuwerts/etch_a_sketch/blob/master/menu_colors.png?raw=true "Mockup color menu, there is supposed to be a preview of the selection in the blue field")
	
3.	Change width of pencil (main activity class)
	* specify line width with seekBar in a dialog
	* onSeekBarChangeListener to track adjustments -> setLineWidth();
	* sketchview class inherits these values -> getLineWidth();
	
![alt text](https://github.com/marissasieuwerts/etch_a_sketch/blob/master/menu_linewidth.png?raw=true "Mockup width menu")
	   
3. Save image (sketchview class)
	* copy bitmap to device's memory using OutputStream? Not sure yet
		
4. Shake device to erase drawing (main activity class)
	* use the accelerometer!
	* Refer to the device's SensorManager
	* create a SensorEventListener to process changes in acceleration and calculate if there is enough change in acceleration to consider this a shaking event
	* prompt users to confirm erasing of their drawings with a dialog
	
###*Frameworks, languages, libraries or other technologies:*
* Java source code for all activities, XML code for all views
* AndroidStudio as development environment
* My own Android device (LG L5 with Android 4.1.2) for testing purposes
