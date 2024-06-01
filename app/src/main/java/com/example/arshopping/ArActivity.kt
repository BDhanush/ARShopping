package com.example.arshopping

import android.graphics.*
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.arshopping.GestureRecognizerHelper.Companion.TAG
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
import java.io.ByteArrayOutputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService


class ArActivity : AppCompatActivity(){
    private lateinit var  arFragment: ArFragment
    private var faceRenderable:ModelRenderable?=null
    private var faceTexture:Texture?=null
    private var curIndex:Int=0

    private val faceNodeMap = HashMap<AugmentedFace,AugmentedFaceNode>()


    private lateinit var backgroundExecutor: ScheduledExecutorService

    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        curIndex = intent.getIntExtra("curIndex", 0)

        val fragment:Fragment? = supportFragmentManager.findFragmentById(R.id.fragment)
        arFragment = fragment as ArFragment
        loadModel()

        arFragment.arSceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST

        backgroundExecutor = Executors.newSingleThreadScheduledExecutor()
        backgroundExecutor.execute {
            gestureRecognizerHelper = GestureRecognizerHelper(
                context = this,
                runningMode = RunningMode.IMAGE
            )
        }
        var count=0
        arFragment.arSceneView.scene.addOnUpdateListener {
            if(faceRenderable != null /*&& faceTexture != null*/) {
                addTrackedFaces()
                removeUntrackedFaces()
            }
            val frame = arFragment.arSceneView.arFrame
            if (frame != null) {
                // Process the AR frame
                count++
                Log.i("count",count.toString())
                processFrame(frame)
            }
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

            if(bitmap!=null) {

                gestureRecognizerHelper.recognizeImage(bitmap)
                    ?.let { resultBundle ->
                        runOnUiThread {

                            // This will return an empty list if there are no gestures detected
                            if(resultBundle.results.first().gestures().isNotEmpty()) {
//                                Toast.makeText(
//                                    this,
//                                    resultBundle.results.first().gestures().first().first().categoryName(),
//                                    Toast.LENGTH_LONG
//                            ).show()
                                Log.i(
                                    "check gesture", resultBundle.results.first().gestures().first().first().categoryName()
                                )
                            } else {
//                                Toast.makeText(
//                                    this,
//                                    "Hands not detected",
//                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    } ?: run {
                    Log.e(
                        TAG, "Error running gesture recognizer."
                    )
                }

//                gestureRecognizerHelper.clearGestureRecognizer()
            }
            image.close()
        } catch (e: NotYetAvailableException) {
            // Handle the case where the image is not available yet
        }
    }

    private fun convertImageToBitmap(image: Image): Bitmap? {
        val width = image.width
        val height = image.height

        val planes = image.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)


        // U and V are swapped
        yBuffer[nv21, 0, ySize]
        vBuffer[nv21, ySize, vSize]
        uBuffer[nv21, ySize + vSize, uSize]

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

//    override fun onError(error: String, errorCode: Int) {
//        runOnUiThread {
//            Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
//        }
//        backgroundExecutor.execute {
//            gestureRecognizerHelper.clearGestureRecognizer()
//            gestureRecognizerHelper.setupGestureRecognizer()
//        }
//    }

//    override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {
//        runOnUiThread {
//            val resultText = resultBundle.results.joinToString { it.toString() }
//            Toast.makeText(this, "Gesture: $resultText", Toast.LENGTH_SHORT).show()
//        }
//        backgroundExecutor.execute {
//            gestureRecognizerHelper.clearGestureRecognizer()
//            gestureRecognizerHelper.setupGestureRecognizer()
//        }
//    }

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