package com.e.movableimages

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.atan2
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var myimge: ImageView

    private lateinit var rotatebutton: ImageView
    private lateinit var closebutton: ImageView
    private lateinit var mainframe: FrameLayout
    private lateinit var addimagesfab: FloatingActionButton

    private var lastEvent: FloatArray? = null
    private var d = 0f
    private var newRot = 0f
    private var isZoomAndRotate = false
    private var isOutSide = false
    private var mode = NONE
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f
    private var xCoOrdinate = 0f
    private var yCoOrdinate = 0f

    lateinit var imageUri: Uri
    private val pickCode: Int = 1025
    val imageCaptureCode = 2005
    lateinit var currentPhotoPath: String
    private val CAMERA_REQUEST = 1888
    private val withInImageCode: Int = 2354
    var buttonwhat: String = ""

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myimge = findViewById(R.id.myimge)
        mainframe = findViewById(R.id.mainframe)
        mainframe.setTag(R.drawable.maincover)
        closebutton = mainframe.findViewById(R.id.closebutton)
        rotatebutton = mainframe.findViewById(R.id.turnbutton)
        addimagesfab = findViewById(R.id.addimagesfab)

        if (mainframe.tag != null) {
            val resourceID = mainframe.tag
        }

        addimagesfab.setOnClickListener {
            openGallery()
        }

        closebutton.setOnClickListener {
            myimge.setImageBitmap(null)
            mainframe.visibility = View.GONE
        }

        mainframe.setOnTouchListener { v, event ->
            val view = v as FrameLayout
            view.bringToFront()
            viewTransformation(view, event)
            true
        }

        rotatebutton.setOnTouchListener { v, event ->
//            newRot = rotation(event)
//            v.rotation = (v.rotation + (newRot - d))
            viewTransformation(mainframe, event)
            true
        }


    }

    private fun viewTransformation(view: View, event: MotionEvent) {
        when (event.action and MotionEvent.ACTION_MASK) {


            MotionEvent.ACTION_DOWN -> {
                xCoOrdinate = view.x - event.rawX
                yCoOrdinate = view.y - event.rawY
                start[event.x] = event.y
                isOutSide = false
                mode = DRAG
                lastEvent = null
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    midPoint(mid, event)
                    mode = ZOOM
                }

                lastEvent = FloatArray(4)
                lastEvent!![0] = event.getX(0)
                lastEvent!![1] = event.getX(1)
                lastEvent!![2] = event.getY(0)
                lastEvent!![3] = event.getY(1)
                d = rotation(event)
            }

            MotionEvent.ACTION_UP -> {
                isZoomAndRotate = false
                if (mode == DRAG) {
                    event.x
                    event.y
                }

                isOutSide = true
                mode = NONE
                lastEvent = null
                mode = NONE
                lastEvent = null
            }

            MotionEvent.ACTION_OUTSIDE -> {
                isOutSide = true
                mode = NONE
                lastEvent = null
                mode = NONE
                lastEvent = null
            }

            MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                lastEvent = null
            }

            MotionEvent.ACTION_MOVE -> if (!isOutSide) {
                if (mode == DRAG) {
                    isZoomAndRotate = false
                    view.animate().x(event.rawX + xCoOrdinate).y(event.rawY + yCoOrdinate)
                        .setDuration(0).start()
                }

                if (mode == ZOOM && event.pointerCount == 2) {
                    val newDist1 = spacing(event)
                    if (newDist1 > 10f) {
                        val scale = newDist1 / oldDist * view.scaleX
                        view.scaleX = scale
                        view.scaleY = scale
                    }

                    if (lastEvent != null) {
                        newRot = rotation(event)
                        view.rotation = (view.rotation + (newRot - d))
                    }

                }
            }
        }
    }

    private fun rotation(event: MotionEvent): Float {
        val delta_x = (event.getX(0) - event.getX(1)).toDouble()
        val delta_y = (event.getY(0) - event.getY(1)).toDouble()
        val radians = atan2(delta_y, delta_x)
        return Math.toDegrees(radians).toFloat()
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    private fun openGallery() {
        val getImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        getImage.type = "image/*"
        getImage.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(getImage, "Select Picture"), pickCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickCode) {
            mainframe.visibility = View.VISIBLE
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val imagesEncodedList = ArrayList<String>()
            if (data?.data != null) {

                imageUri = data.data!!

                // Get the cursor

                // Get the cursor
                val cursor: Cursor? = contentResolver.query(
                    imageUri,
                    filePathColumn, null, null, null
                )
                // Move to first row
                // Move to first row
                cursor?.moveToFirst()

                var imageEncoded: String? = ""

                val columnIndex: Int? = cursor?.getColumnIndex(filePathColumn[0])
                imageEncoded = columnIndex?.let { cursor.getString(it) }
                cursor?.close()
                myimge.setImageURI(imageUri)
            }
        } else if (requestCode == imageCaptureCode && resultCode == RESULT_OK) {
            mainframe.visibility = View.VISIBLE
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val imageUri = getImageUriFromBitmap(this, imageBitmap)
            myimge.setImageURI(imageUri)
            handleCameraImage(imageUri)
            if (!Uri.EMPTY.equals(imageUri)) {
                imageUri.let { contentResolver.delete(it, null, null) }
            }

        } else if (requestCode == 1024 && resultCode == RESULT_OK) {
            mainframe.visibility = View.VISIBLE
            imageUri = data?.data!!

        } else if (requestCode == 10548 && resultCode == RESULT_OK) {
            mainframe.visibility = View.VISIBLE
            val tempImageUri = data?.data!!
            myimge.setImageURI(tempImageUri)
            if (!Uri.EMPTY.equals(imageUri)) {
                imageUri.let { contentResolver.delete(it, null, null) }
            }

        } else if (requestCode == withInImageCode && resultCode == RESULT_OK) {
            mainframe.visibility = View.VISIBLE
            imageUri = data?.data!!
            myimge.setImageURI(imageUri)
            if (!Uri.EMPTY.equals(imageUri)) {
                imageUri.let { contentResolver.delete(it, null, null) }
            }
        } else if (requestCode == 368) {
            mainframe.visibility = View.VISIBLE
            when (resultCode) {
                RESULT_OK -> {
                }
                3680 -> {
                    Toast.makeText(this, "ex", Toast.LENGTH_LONG).show()
                }
                else -> print("User cancelled the CutOut screen")
            }
            if (!Uri.EMPTY.equals(imageUri)) {
                imageUri.let { contentResolver.delete(it, null, null) }
            }
        } else {
            mainframe.visibility = View.VISIBLE
            val mClipData: ClipData? = data?.getClipData()
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val imagesEncodedList = ArrayList<String?>()
            var imageEncoded: String? = null
            val mArrayUri = ArrayList<Uri>()
            if (mClipData != null) {
                for (i in 0 until mClipData.itemCount) {
                    val item = mClipData.getItemAt(i)
                    val uri = item.uri
                    mArrayUri.add(uri)
                    // Get the cursor
                    val cursor: Cursor? =
                        getContentResolver().query(uri, filePathColumn, null, null, null)
                    // Move to first row
                    if (cursor != null) {
                        cursor.moveToFirst()
                    }
                    val columnIndex = cursor?.getColumnIndex(filePathColumn.get(0))
                    if (cursor != null) {
                        imageEncoded = columnIndex?.let { cursor.getString(it) }
                    }
                    imagesEncodedList.add(imageEncoded)
                    cursor?.close()
                }
            }
        }
    }


    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                "IMG_" + Calendar.getInstance().time,
                null
            )
        Log.e("Imageuricheck", "onActivityResult: ${Uri.parse(path.toString())}")
        return Uri.parse(path.toString())
    }

    private fun handleCameraImage(currentImageUri: Uri) {
        intent.data = currentImageUri
    }


}