package com.example.futbolito

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(), SensorEventListener {
    private val gravity = FloatArray(3)
    private  val linear_acceleration = FloatArray(3)
    private var sensorAcelerometer: Sensor? = null
    private var mSensor: Sensor? = null
    private lateinit var sensorManager: SensorManager
    lateinit var miViewDibujado: MiViewDibujado
    private var width: Int = 0
    private var height: Int = 0

    val sensorEventListener : SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val alpha: Float = 0.8f

            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event!!.values[0]
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event!!.values[1]
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event!!.values[2]

            // Remove the gravity contribution with the high-pass filter.
            linear_acceleration[0] = event.values[0] - gravity[0]
            linear_acceleration[1] = event.values[1] - gravity[1]
            linear_acceleration[2] = event.values[2] - gravity[2]

            Log.d("ACELERE", "x=${linear_acceleration[0]} ; y=${linear_acceleration[1]} ; " +
                    "z=${linear_acceleration[2]}")
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            //TODO("Not yet implemented")
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN

        // Ocultar la barra de título
        supportActionBar?.hide()

        // Establecer la vista de la actividad en pantalla completa
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //SACAMOS MEDIDAS DE LA PANTALLA
        val valoresPantalla = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(valoresPantalla)
        height = valoresPantalla.heightPixels
        width = valoresPantalla.widthPixels

        miViewDibujado = MiViewDibujado(this, width, height)
        setContentView(miViewDibujado)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val deviceSensors =   sensorManager.getSensorList(Sensor.TYPE_ALL)

        deviceSensors.forEach {
            Log.i("MisSensores", it.toString())
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            // Success! There's a magnetometer.
            Log.i("MisSensores", "MAGNETOMETRO ENCONTRADO")
        } else {
            // Failure! No magnetometer.
            Log.i("MisSensores", "MAGNETOMETRO NO ENCONTRADO")
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            val gravSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_GRAVITY)
            // Use the version 3 gravity sensor.
            mSensor = gravSensors.firstOrNull { it.vendor.contains("Google LLC") && it.version == 3 }
        }
        if (mSensor == null) {
            // Use the accelerometer.
            mSensor = if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            } else {
                // Sorry, there are no accelerometers on your device.
                // You can't play this game.
                null
            }
        }

        //Log.i("MisSensores", mSensor.toString())

        sensorAcelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        sensorAcelerometer?.also {
            sensorManager.registerListener(miViewDibujado,it,
                SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        sensorManager.unregisterListener(miViewDibujado)
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        //TODO("Not yet implemented")


    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorEventListener)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class  MiViewDibujado (ctx: Context, width: Int, height: Int) : View(ctx), SensorEventListener {

    var xPos = width/2f
    var yPos = height/2f
    var xAcceleration:Float = 0f
    var xVelocity:Float = 0.0f
    var yAcceleration:Float = 0f
    var yVelocity:Float = 0.0f
    var golTop = 0
    var golBottom = 0

    var pincel = Paint()
    var goles = Paint()

    val bitCancha = BitmapFactory.decodeResource(resources, R.drawable.cancha)
    val cRect = Rect(0, 0, width, height)

    val bitBalon = BitmapFactory.decodeResource(resources, R.drawable.balon2)


    private var gravity = FloatArray(3)
    private  var linear_acceleration = FloatArray(3)


    init {
        pincel.color = Color.BLACK
        goles.color = Color.argb( 190, 170, 170, 170)
        goles.textSize = 500f;
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //SE DIBUJA LA CANCHA
        canvas!!.drawBitmap(bitCancha, null, cRect, null)

        //SE DIBUJAN LOS MARCADORES
        canvas.drawText("$golTop", width / 2f - 135, height / 2f - 200, goles)
        canvas.drawText("$golBottom", width / 2f - 135, height / 2f + 600, goles)

        //SE DIBUJA LA PELOTITA
        val cRect2 = Rect(xPos.toInt(), yPos.toInt(), xPos.toInt()  + 100, yPos.toInt() + 100)
        canvas.drawBitmap(bitBalon, null, cRect2, null)
        //canvas.drawCircle(xPos, yPos,50.0F, pincel)
        invalidate()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val alpha = 0.8f

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event!!.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0]   //x
        linear_acceleration[1] = event.values[1] - gravity[1]    //y
        linear_acceleration[2] = event.values[2] - gravity[2]   //z

        moverPelota(linear_acceleration[0], linear_acceleration[1] * -1)
    }

    private fun moverPelota( xOrientation: Float,  yOrientation: Float) {
        //TODO("Not yet implemented")
        xAcceleration = xOrientation
        yAcceleration = yOrientation
        updateMarcador()
        updateX()
        updateY()
    }

    fun updateMarcador(){
        if (xPos >= width / 2f - 70 && xPos <= width / 2f + 60){
            if (yPos <= 140){
                golBottom++
                xPos = width / 2f
                yPos = height / 2f
            }else if (yPos >= height - 255){
                golTop++
                xPos = width / 2f
                yPos = height / 2f
            }
        }
    }

    fun updateX() {
        if (xPos >= 105 && xPos <= width - 120){
            xVelocity -= xAcceleration * 5f
            xPos += xVelocity
        }else{
            xPos -= xVelocity
        }
    }

    fun updateY() {
        if (yPos >= 138 && yPos <= height - 258){
            yVelocity -= yAcceleration * 5f
            yPos += yVelocity
        }else{
            yPos -= yVelocity
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //TODO("Not yet implemented")
    }
}