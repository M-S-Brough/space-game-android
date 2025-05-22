package com.mb.spacegame

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout

@SuppressLint("ClickableViewAccessibility")
class SpaceshipController(
    private val spaceship: ImageView,
    private val planets: List<Planets>,
    private val gameViewModel: GameViewModel,
    private val gameLayout: ConstraintLayout
) {

    private var initialX = 0f
    private var initialY = 0f
    var allPlanetsGoneListener: OnAllPlanetsGoneListener? = null
    private val collisionDialogBuilder = AlertDialog.Builder(spaceship.context)
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val sensitivityFactorX = 2f
    private val sensitivityFactorY = 2f
    private var restTiltX = 0f
    private var restTiltY = 0f
    private var currentTiltX = 0f
    private var currentTiltY = 0f

    init {
        setupTouchListener()
        sensorManager = spaceship.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        calibrateTilt()


    }

    private fun setupTouchListener() {
        spaceship.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> captureInitialTouchPoints(event)
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialX
                    val deltaY = event.rawY - initialY
                    moveSpaceship(deltaX, deltaY)
                    initialX = event.rawX
                    initialY = event.rawY
                }
            }
            true
        }
    }

    private fun captureInitialTouchPoints(event: MotionEvent) {
        initialX = event.rawX
        initialY = event.rawY
    }

    /*private fun handleSpaceshipMovement(event: MotionEvent) {
        moveSpaceship(event)
        checkCollision()
    }*/

    private fun moveSpaceship(deltaX: Float, deltaY: Float) {
        val newPosX = spaceship.x + deltaX
        val newPosY = spaceship.y + deltaY

        // Clamp the position to keep the spaceship within the gameLayout bounds
        val clampedPosX = newPosX.coerceIn(0f, gameLayout.width - spaceship.width.toFloat())
        val clampedPosY = newPosY.coerceIn(0f, gameLayout.height - spaceship.height.toFloat())

        spaceship.x = clampedPosX
        spaceship.y = clampedPosY

        checkCollision()
    }

    fun calibrateTilt() {
        accelerometer?.also { sensor ->

            restTiltX = currentTiltX
            restTiltY = currentTiltY
            Log.d("CalibrateTilt", "Tilt calibrated. restTiltX: $restTiltX, restTiltY: $restTiltY")

        }
    }

    private fun checkCollision() {
        val spaceshipRect = Rect()
        spaceship.getHitRect(spaceshipRect)

        planets.firstOrNull { planet ->
            planet.imageView.visibility == View.VISIBLE && Rect.intersects(spaceshipRect, getPlanetRect(planet))
        }?.let { handleCollisionWithPlanet(it) }
    }

    private fun getPlanetRect(planet: Planets): Rect {
        val planetRect = Rect()
        planet.imageView.getHitRect(planetRect)
        return planetRect
    }

    private fun handleCollisionWithPlanet(planet: Planets) {
        disableControls()
        gameViewModel.pauseGame()
        planet.imageView.visibility = View.GONE
        checkAllPlanetsGone()
        showCollisionDialog(planet)

    }

    private fun checkAllPlanetsGone() {
        if (planets.all { it.imageView.visibility == View.GONE }) {
            allPlanetsGoneListener?.onAllPlanetsGone()
        }
    }

    private fun showCollisionDialog(planet: Planets) {
        collisionDialogBuilder.setTitle(planet.name)
            .setMessage(planet.info)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                gameViewModel.resumeGame()
                enableControls()
            }   .setCancelable(false)
                .create().show()

    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            currentTiltX = event.values[0] - restTiltX // Compensate for resting position
            currentTiltY = event.values[1] - restTiltY // Compensate for resting position

            // The rest of your code remains the same
            val tiltX = currentTiltX // Use the compensated values
            val tiltY = currentTiltY // Use the compensated values
            val deltaX = -tiltX * sensitivityFactorX
            val deltaY = tiltY * sensitivityFactorY
            moveSpaceship(deltaX, deltaY)

        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }


    fun enableControls() {
        accelerometer?.also { sensor ->
            sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
        setupTouchListener()



    }

    private fun disableControls() {
        sensorManager.unregisterListener(sensorListener)
        spaceship.setOnTouchListener(null)

    }

    interface OnAllPlanetsGoneListener {
        fun onAllPlanetsGone()
    }
}
