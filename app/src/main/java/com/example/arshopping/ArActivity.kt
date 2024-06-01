package com.example.arshopping

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.AugmentedFaceNode
import java.util.concurrent.CompletableFuture
import kotlin.math.abs


class ArActivity : AppCompatActivity(),GestureDetector.OnGestureListener {
    private lateinit var  arFragment: ArFragment
    private var faceRenderable:ModelRenderable?=null
    private var faceTexture:Texture?=null
    private var curIndex:Int=0

    private val faceNodeMap = HashMap<AugmentedFace,AugmentedFaceNode>()


    private lateinit var gestureDetector: GestureDetector
    private val swipeThreshold = 1
    private val swipeVelocityThreshold = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        curIndex = intent.getIntExtra("curIndex", 0)

        val fragment:Fragment? = supportFragmentManager.findFragmentById(R.id.fragment)
        arFragment = fragment as ArFragment
        loadModel()

        arFragment.arSceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST
        arFragment.arSceneView.scene.addOnUpdateListener {
            if(faceRenderable != null /*&& faceTexture != null*/) {
                addTrackedFaces()
                removeUntrackedFaces()
            }
        }



        gestureDetector = GestureDetector(this)
        fragment.view?.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    private fun onSwipeRight() {
        curIndex = (curIndex - 1 + glassesArray.size)% glassesArray.size
//        loadModel()
        Toast.makeText(applicationContext,"right",Toast.LENGTH_LONG).show()
    }

    private fun onSwipeLeft() {
        curIndex++
        curIndex%= glassesArray.size
//        loadModel()
        Toast.makeText(applicationContext,"right",Toast.LENGTH_LONG).show()

    }

    private fun addTrackedFaces() {
        val session = arFragment.arSceneView.session ?: return
        val faceList = session.getAllTrackables(AugmentedFace::class.java)
        for(face in faceList) {
            if(!faceNodeMap.containsKey(face)) {
                AugmentedFaceNode(face).apply {
                    setParent(arFragment.arSceneView.scene)
                    faceRegionsRenderable = faceRenderable
//                    faceMeshTexture = faceTexture
                    faceNodeMap[face] = this
                }
            }
        }
    }

    private fun removeUntrackedFaces() {
        val entries = faceNodeMap.entries
        for(entry in entries) {
            val face = entry.key
            if(face.trackingState == TrackingState.STOPPED) {
                val faceNode = entry.value
                faceNode.setParent(null)
                entries.remove(entry)
            }
        }
    }

    private fun loadModel()
    {
        val modelRenderable = ModelRenderable.builder()
            .setSource(this, glassesArray[curIndex].modelResource)
            .build()
//        val texture = Texture.builder()
//            .set
        CompletableFuture.allOf(modelRenderable)
            .thenAccept{
                faceRenderable=modelRenderable.get().apply {
                    isShadowCaster=false
                    isShadowReceiver=false
                }
//                faceTexture=texture.get()
            }.exceptionally {
                Toast.makeText(this,"Error loading model: $it",Toast.LENGTH_LONG).show()
                null
            }
    }

    override fun onDown(e: MotionEvent): Boolean {
        TODO("Not yet implemented")
    }

    override fun onShowPress(e: MotionEvent) {
        TODO("Not yet implemented")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        TODO("Not yet implemented")
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        TODO("Not yet implemented")
    }

    override fun onLongPress(e: MotionEvent) {
        TODO("Not yet implemented")
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        try {
            val diffY = e2.y - e1?.y!!
            val diffX = e2.x - e1.x
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                    if (diffX > 0) {
                        Toast.makeText(applicationContext, "Left to Right swipe gesture", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(applicationContext, "Right to Left swipe gesture", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        catch (exception: Exception) {
            exception.printStackTrace()
        }
        return true
    }
}