package com.example.arshopping

import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.ar.core.AugmentedFace
import com.google.ar.core.Frame
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.NotYetAvailableException
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.AugmentedFaceNode
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ArActivity : AppCompatActivity(),GestureRecognizerHelper.GestureRecognizerListener {
    private lateinit var  arFragment: ArFragment
    private var faceRenderable:ModelRenderable?=null
    private var faceTexture:Texture?=null
    private var curIndex:Int=0

    private val faceNodeMap = HashMap<AugmentedFace,AugmentedFaceNode>()


    private lateinit var backgroundExecutor: ExecutorService
    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper

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
            val frame = arFragment.arSceneView.arFrame
            if (frame != null && frame.camera.trackingState == TrackingState.TRACKING) {
                // Process the AR frame
                processFrame(frame)
            }
        }
        backgroundExecutor = Executors.newSingleThreadExecutor()
        backgroundExecutor.execute {
            gestureRecognizerHelper = GestureRecognizerHelper(
                context = applicationContext,
                runningMode = RunningMode.IMAGE,
                gestureRecognizerListener = this
            )
        }


    }

    private fun processFrame(frame: Frame) {
        try {
            // Acquire the camera image
            val image = frame.acquireCameraImage()

            // Convert the Image to a Bitmap
            val bitmap = convertImageToBitmap(image)

            // Convert the Bitmap to an MPImage
//            val mpImage = BitmapImageBuilder(bitmap).build()

            // Run the gesture recognizer on the MPImage
            gestureRecognizerHelper.recognizeImage(bitmap)

            // Close the image to avoid memory leaks
            image.close()
        } catch (e: NotYetAvailableException) {
            // Handle the case where the image is not available yet
        }
    }

    private fun convertImageToBitmap(image: Image): Bitmap {
        val buffer = image.planes[0].buffer
        val pixelStride = image.planes[0].pixelStride
        val rowStride = image.planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)

        return bitmap
    }

    override fun onError(error: String, errorCode: Int) {
        runOnUiThread {
            Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
        }
        backgroundExecutor.execute {
            gestureRecognizerHelper.clearGestureRecognizer()
            gestureRecognizerHelper.setupGestureRecognizer()
        }
    }

    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {
        runOnUiThread {
            val resultText = resultBundle.results.joinToString { it.toString() }
            Toast.makeText(this, "Gesture: $resultText", Toast.LENGTH_SHORT).show()
        }
        backgroundExecutor.execute {
            gestureRecognizerHelper.clearGestureRecognizer()
            gestureRecognizerHelper.setupGestureRecognizer()
        }
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

//
//    override fun onResume() {
//        super.onResume()
//
//        // Start the GestureRecognizerHelper again when users come back
//        // to the foreground.
//        backgroundExecutor.execute {
//            if (gestureRecognizerHelper.isClosed()) {
//                gestureRecognizerHelper.setupGestureRecognizer()
//            }
//        }
//    }
//    override fun onPause() {
//        super.onPause()
//        // Close the Gesture Recognizer helper and release resources
//        backgroundExecutor.execute { gestureRecognizerHelper.clearGestureRecognizer() }
//
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//
//        // Shut down our background executor
//        backgroundExecutor.shutdown()
//        backgroundExecutor.awaitTermination(
//            Long.MAX_VALUE, TimeUnit.NANOSECONDS
//        )
//    }

}