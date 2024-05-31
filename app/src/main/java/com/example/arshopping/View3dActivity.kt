package com.example.arshopping

import DragTransformableNode
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformationSystem

class View3dActivity : AppCompatActivity() {

    private lateinit var sceneView:SceneView
    private lateinit var skuProgressBar:ProgressBar
    private var curIndex:Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_3d)

        sceneView=findViewById(R.id.sceneView)
        skuProgressBar=findViewById(R.id.skuProgressBar)

        curIndex = intent.getIntExtra("curIndex", 0)

        renderLocalObject()

    }

    private fun renderLocalObject() {

        skuProgressBar.visibility = View.VISIBLE
        ModelRenderable.builder()
            .setSource(this, glassesArray[curIndex].modelResource)
            .build()
            .thenAccept { modelRenderable: ModelRenderable ->
                skuProgressBar.visibility = View.GONE
                addNodeToScene(modelRenderable)
            }
            .exceptionally {
                Toast.makeText(this,"Error loading model: $it", Toast.LENGTH_LONG).show()
                null
            }
    }

    override fun onPause() {
        super.onPause()
        sceneView.pause()
    }

    private fun addNodeToScene(model: ModelRenderable) {
        val transformationSystem = makeTransformationSystem()
        var dragTransformableNode = DragTransformableNode(1f, transformationSystem)
        dragTransformableNode.renderable = model
        sceneView.scene.addChild(dragTransformableNode)
        dragTransformableNode.select()
        sceneView.scene
            .addOnPeekTouchListener { hitTestResult: HitTestResult?, motionEvent: MotionEvent? ->
                transformationSystem.onTouch(
                    hitTestResult,
                    motionEvent
                )
            }
    }

    private fun makeTransformationSystem(): TransformationSystem {
        val footprintSelectionVisualizer = FootprintSelectionVisualizer()
        return TransformationSystem(resources.displayMetrics, footprintSelectionVisualizer)
    }


    override fun onResume() {
        super.onResume()
        try {
            sceneView.resume()
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            sceneView.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
