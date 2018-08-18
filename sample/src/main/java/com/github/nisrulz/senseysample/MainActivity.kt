/*
 * Copyright (C) 2016 Nishant Srivastava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nisrulz.senseysample

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.TextView
import com.github.nisrulz.sensey.ChopDetector.ChopListener
import com.github.nisrulz.sensey.FlipDetector.FlipListener
import com.github.nisrulz.sensey.LightDetector.LightListener
import com.github.nisrulz.sensey.MovementDetector.MovementListener
import com.github.nisrulz.sensey.OrientationDetector.OrientationListener
import com.github.nisrulz.sensey.PickupDeviceDetector.PickupDeviceListener
import com.github.nisrulz.sensey.ProximityDetector.ProximityListener
import com.github.nisrulz.sensey.RotationAngleDetector.RotationAngleListener
import com.github.nisrulz.sensey.ScoopDetector.ScoopListener
import com.github.nisrulz.sensey.Sensey
import com.github.nisrulz.sensey.ShakeDetector.ShakeListener
import com.github.nisrulz.sensey.SoundLevelDetector.SoundLevelListener
import com.github.nisrulz.sensey.StepDetectorUtil
import com.github.nisrulz.sensey.StepListener
import com.github.nisrulz.sensey.TiltDirectionDetector
import com.github.nisrulz.sensey.TiltDirectionDetector.TiltDirectionListener
import com.github.nisrulz.sensey.WaveDetector.WaveListener
import com.github.nisrulz.sensey.WristTwistDetector.WristTwistListener
import kotlinx.android.synthetic.main.activity_main.btn_touchevent
import kotlinx.android.synthetic.main.activity_main.linearlayout_controls
import kotlinx.android.synthetic.main.activity_main.switchMainActivitySound
import kotlinx.android.synthetic.main.activity_main.textView_result
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), OnCheckedChangeListener, ShakeListener, FlipListener, LightListener, OrientationListener, ProximityListener, WaveListener, SoundLevelListener, MovementListener, ChopListener, WristTwistListener, RotationAngleListener, TiltDirectionListener, StepListener, ScoopListener, PickupDeviceListener {

    private var hasRecordAudioPermission = false
    private val recordAudioPermission = permission.RECORD_AUDIO
    private val LOGTAG = javaClass.canonicalName

    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hasRecordAudioPermission = RuntimePermissionUtil.checkPermissonGranted(this, recordAudioPermission)

        // Init UI controls,views and handler
        handler = Handler()

        // Setup switches
        setAllSwitchesToFalseState()
        setOnCheckedChangeListenerForAllSwitches()

        btn_touchevent.setOnClickListener { startActivity(Intent(this@MainActivity, TouchActivity::class.java)) }
    }

    override fun onPause() {
        super.onPause()

        // Stop Detections
        stopAllDetectors()

        // Set the all switches to off position
        setAllSwitchesToFalseState()

        // Reset the result view
        resetResultInView(textView_result)

        // *** IMPORTANT ***
        // Stop Sensey and release the context held by it
        Sensey.getInstance().stop()
    }


    override fun onResume() {
        super.onResume()

        // Init Sensey
        Sensey.getInstance().init(this)
    }

    private fun setAllSwitchesToFalseState() {
        var v: View
        for (i in 0 until linearlayout_controls.childCount) {
            v = linearlayout_controls.getChildAt(i)
            //do something with your child element
            if (v is SwitchCompat) {
                v.isChecked = false
            }
        }
    }

    private fun setOnCheckedChangeListenerForAllSwitches() {
        var v: View
        for (i in 0 until linearlayout_controls.childCount) {
            v = linearlayout_controls.getChildAt(i)
            //do something with your child element
            if (v is SwitchCompat) {
                v.setOnCheckedChangeListener(this)
            }
        }
    }


    private fun stopAllDetectors() {
        Sensey.getInstance().stopShakeDetection(this)
        Sensey.getInstance().stopFlipDetection(this)
        Sensey.getInstance().stopOrientationDetection(this)
        Sensey.getInstance().stopProximityDetection(this)
        Sensey.getInstance().stopLightDetection(this)
        Sensey.getInstance().stopWaveDetection(this)
        Sensey.getInstance().stopSoundLevelDetection()
        Sensey.getInstance().stopMovementDetection(this)
        Sensey.getInstance().stopChopDetection(this)
        Sensey.getInstance().stopWristTwistDetection(this)
        Sensey.getInstance().stopRotationAngleDetection(this)
        Sensey.getInstance().stopTiltDirectionDetection(this)
        Sensey.getInstance().stopStepDetection(this)
        Sensey.getInstance().stopPickupDeviceDetection(this)
        Sensey.getInstance().stopScoopDetection(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == 100) {
            RuntimePermissionUtil.onRequestPermissionsResult(grantResults, object : RPResultListener {
                override fun onPermissionDenied() {
                    // do nothing
                }

                override fun onPermissionGranted() {
                    if (RuntimePermissionUtil.checkPermissonGranted(this@MainActivity, recordAudioPermission)) {
                        hasRecordAudioPermission = true
                        switchMainActivitySound.isChecked = true
                    }
                }
            })
        }
    }

    override fun onBottomSideUp() {
        setResultTextView("Bottom Side UP", false)
    }

    @SuppressLint("MissingPermission")
    override fun onCheckedChanged(switchbtn: CompoundButton, isChecked: Boolean) {
        when (switchbtn.text) {

            resources.getString(R.string.shake_gesture) -> if (isChecked) {
                Sensey.getInstance().startShakeDetection(10f, 2000, this)
            } else {
                Sensey.getInstance().stopShakeDetection(this)
            }
            resources.getString(R.string.flip_gesture) -> if (isChecked) {
                Sensey.getInstance().startFlipDetection(this)
            } else {
                Sensey.getInstance().stopFlipDetection(this)
            }
            resources.getString(R.string.orientation_gesture) -> if (isChecked) {
                Sensey.getInstance().startOrientationDetection(this)
            } else {
                Sensey.getInstance().stopOrientationDetection(this)
            }
            resources.getString(R.string.proximity_gesture) -> if (isChecked) {
                Sensey.getInstance().startProximityDetection(this)
            } else {
                Sensey.getInstance().stopProximityDetection(this)
            }
            resources.getString(R.string.light_gesture) -> if (isChecked) {
                Sensey.getInstance().startLightDetection(10f, this)
            } else {
                Sensey.getInstance().stopLightDetection(this)
            }

            resources.getString(R.string.wave_gesture) -> if (isChecked) {
                Sensey.getInstance().startWaveDetection(this)
            } else {
                Sensey.getInstance().stopWaveDetection(this)
            }

            resources.getString(R.string.sound_level_detection) -> if (isChecked) {
                if (hasRecordAudioPermission) {
                    Sensey.getInstance().startSoundLevelDetection(this, this)
                } else {
                    RuntimePermissionUtil.requestPermission(this, recordAudioPermission, 100)
                }

            } else {
                Sensey.getInstance().stopSoundLevelDetection()
            }
            resources.getString(R.string.movement_detection) -> if (isChecked) {
                Sensey.getInstance().startMovementDetection(this)
            } else {
                Sensey.getInstance().stopMovementDetection(this)
            }
            resources.getString(R.string.chop_detector) -> if (isChecked) {
                Sensey.getInstance().startChopDetection(30f, 500, this)
            } else {
                Sensey.getInstance().stopChopDetection(this)
            }
            resources.getString(R.string.wrist_twist_detection) -> if (isChecked) {
                Sensey.getInstance().startWristTwistDetection(this)
            } else {
                Sensey.getInstance().stopWristTwistDetection(this)
            }

            resources.getString(R.string.rotation_angle_detection) -> if (isChecked) {
                Sensey.getInstance().startRotationAngleDetection(this)
            } else {
                Sensey.getInstance().stopRotationAngleDetection(this)
            }

            resources.getString(R.string.tilt_direction_detection) -> if (isChecked) {
                Sensey.getInstance().startTiltDirectionDetection(this)
            } else {
                Sensey.getInstance().stopTiltDirectionDetection(this)
            }
            resources.getString(R.string.step_detector) -> if (isChecked) {
                Sensey.getInstance().startStepDetection(this, this, StepDetectorUtil.MALE)
            } else {
                Sensey.getInstance().stopStepDetection(this)
            }

            resources.getString(R.string.pickup_device_detector) -> if (isChecked) {
                Sensey.getInstance().startPickupDeviceDetection(this)
            } else {
                Sensey.getInstance().stopPickupDeviceDetection(this)
            }

            resources.getString(R.string.scoop_detector) -> if (isChecked) {
                Sensey.getInstance().startScoopDetection(this)
            } else {
                Sensey.getInstance().stopScoopDetection(this)
            }

            else -> {
            }
        }// Do nothing
    }

    override fun onChop() {
        setResultTextView("Chop Detected!", false)
    }

    override fun onDark() {
        setResultTextView("Dark", false)
    }

    override fun onDevicePickedUp() {
        setResultTextView("Device Picked up Detected!", false)
    }

    override fun onDevicePutDown() {
        setResultTextView("Device Put down Detected!", false)
    }

    override fun onFaceDown() {
        setResultTextView("Face Down", false)
    }

    override fun onFaceUp() {
        setResultTextView("Face UP", false)
    }

    override fun onFar() {
        setResultTextView("Far", false)
    }

    override fun onLeftSideUp() {
        setResultTextView("Left Side UP", false)
    }

    override fun onLight() {
        setResultTextView("Not Dark", false)
    }

    override fun onMovement() {
        setResultTextView("Movement Detected!", false)
    }

    override fun onNear() {
        setResultTextView("Near", false)
    }

    override fun onRightSideUp() {
        setResultTextView("Right Side UP", false)
    }

    override fun onRotation(angleInAxisX: Float, angleInAxisY: Float, angleInAxisZ: Float) {
        val data = "Rotation in Axis Detected(deg):\nX=$angleInAxisX,\nY=$angleInAxisY,\nZ=$angleInAxisZ"
        setResultTextView(data, true)
    }

    override fun onScooped() {
        setResultTextView("Scoop Gesture Detected!", false)
    }

    override fun onShakeDetected() {
        setResultTextView("Shake Detected!", false)
    }

    override fun onShakeStopped() {
        setResultTextView("Shake Stopped!", false)
    }

    override fun onSoundDetected(level: Float) {
        val data = "${DecimalFormat("##.##").format(level.toDouble())} dB"
        setResultTextView(data, true)
    }

    override fun onStationary() {
        setResultTextView("Device Stationary!", false)
    }

    override fun onTiltInAxisX(direction: Int) {
        displayResultForTiltDirectionDetector(direction, "X")
    }

    override fun onTiltInAxisY(direction: Int) {
        displayResultForTiltDirectionDetector(direction, "Y")
    }

    override fun onTiltInAxisZ(direction: Int) {
        displayResultForTiltDirectionDetector(direction, "Z")
    }

    override fun onTopSideUp() {
        setResultTextView("Top Side UP", false)
    }

    override fun onWave() {
        setResultTextView("Wave Detected!", false)
    }

    override fun onWristTwist() {
        setResultTextView("Wrist Twist Detected!", false)
    }

    override fun stepInformation(noOfSteps: Int, distanceInMeter: Float, stepActivityType: Int) {
        val typeOfActivity: String = when (stepActivityType) {
            StepDetectorUtil.ACTIVITY_RUNNING -> "Running"
            StepDetectorUtil.ACTIVITY_WALKING -> "Walking"
            else -> "Still"
        }
        val data = "Steps: $noOfSteps\nDistance: $distanceInMeter m\nActivity Type: $typeOfActivity"
        setResultTextView(data, true)
    }

    private fun displayResultForTiltDirectionDetector(direction: Int, axis: String) {
        val dir: String
        if (direction == TiltDirectionDetector.DIRECTION_CLOCKWISE) {
            dir = "ClockWise"
        } else {
            dir = "AntiClockWise"
        }
        setResultTextView("Tilt in $axis Axis: $dir", false)
    }

    private fun resetResultInView(txt: TextView?) {
        handler.postDelayed({ txt?.text = getString(R.string.results_show_here) }, 3000)
    }

    private fun setResultTextView(text: String, realtime: Boolean) {
        if (textView_result != null) {
            runOnUiThread {
                textView_result.text = text
                if (!realtime) {
                    resetResultInView(textView_result)
                }
            }

            if (BuildConfig.DEBUG) {
                Log.d(LOGTAG, text)
            }
        }
    }
}