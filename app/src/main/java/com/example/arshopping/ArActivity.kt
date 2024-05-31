package com.example.arshopping

import android.os.Bundle
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

class ArActivity : AppCompatActivity() {
    private lateinit var  arFragment: ArFragment
    private var faceRenderable:ModelRenderable?=null
    private var faceTexture:Texture?=null
    private var curIndex:Int=0

    private val faceNodeMap = HashMap<AugmentedFace,AugmentedFaceNode>()

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
}