package com.gaura.sketch

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dailog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawing_view.setSizeForBrush(10.toFloat())
        ib_brush.setOnClickListener { showBrushDialog() }

        mImageButtonCurrentPaint = ll_paint_colors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_pressed
            )
        )

        ib_gallery.setOnClickListener {
            if (isReadStorageAllowed()) {
                val pickPhoto = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(pickPhoto, GALLERY)
            } else {
                requestStoragePermission()
            }
        }

        ib_undo.setOnClickListener {
            drawing_view.onClickUndo()
        }

        ib_save.setOnClickListener {
            if (isReadStorageAllowed()) {
                BitmapAsyncTask(getBitmapFromView(fl_drawing_view_container)).execute()
            } else {
                requestStoragePermission()
            }
        }
    }

    /**
     * This is override method and the method will be called when the user will tap on allow or deny
     *
     * Determines whether the delegate should handle
     * {@link ActivityCompat#requestPermissions(Activity, String[], int)}, and request
     * permissions if applicable. If this method returns true, it means that permission
     * request is successfully handled by the delegate, and platform should not perform any
     * further requests for permission.
     *
     * @param activity The target activity.
     * @param permissions The requested permissions. Must me non-null and not empty.
     * @param requestCode Application specific request code to match with a result reported to
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this@MainActivity,
                    "Permission granted now you can read the storage files.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                    this@MainActivity,
                    "Oops you just denied the permission.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                try {
                    if (data!!.data != null) {
                        iv_background.visibility = View.VISIBLE
                        iv_background.setImageURI(data.data)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Error in parsing the image or its corrupted.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )
        ) {

        }

        /**
         * Requests permissions to be granted to this application. These permissions
         * must be requested in your manifest, otherwise they will not be granted to your app.
         */

        //And finally ask for the permission
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            STORAGE_PERMISSION_CODE
        )
    }

    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }

    fun showBrushDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dailog_brush_size)
        dialog.setTitle("Brush size: ")
        dialog.ib_small_brush.setOnClickListener {
            drawing_view.setSizeForBrush(5.toFloat())
            dialog.dismiss()
        }
        dialog.ib_medium_brush.setOnClickListener {
            drawing_view.setSizeForBrush(10.toFloat())
            dialog.dismiss()
        }
        dialog.ib_large_brush.setOnClickListener {
            drawing_view.setSizeForBrush(15.toFloat())
            dialog.dismiss()
        }
        dialog.show()
    }

    fun paintClicked(view: View) {
        if (view !== mImageButtonCurrentPaint) {
            // Update the color
            val imageButton = view as ImageButton
            // Here the tag is used for swaping the current color with previous color.
            // The tag stores the selected view
            val colorTag = imageButton.tag.toString()
            // The color is set as per the selected tag here.
            drawing_view.setColor(colorTag)
            // Swap the backgrounds for last active and currently active image button.
            imageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_normal
                )
            )

            //Current view is updated with selected view in the form of ImageButton.
            mImageButtonCurrentPaint = view
        }
    }

    fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)

        return returnedBitmap
    }

    private inner class BitmapAsyncTask(val mBitmap: Bitmap?) :
        AsyncTask<Any, Void, String>() {

        @Suppress("DEPRECATION")
        private lateinit var mDialog: Dialog

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any): String {

            var result = ""
            if (mBitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    val f = File(
                        externalCacheDir!!.absoluteFile.toString()
                                + File.separator + "SketchApp_" + System.currentTimeMillis() / 1000 + ".png"
                    )
                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result = f.absolutePath
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            cancelProgressDialog()
            if (!result.isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "File saved successfully :$result",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Something went wrong while saving the file.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            /*MediaScannerConnection provides a way for applications to pass a
            newly created or downloaded media file to the media scanner service.
            The media scanner service will read metadata from the file and add
            the file to the media content provider.
            The MediaScannerConnectionClient provides an interface for the
            media scanner service to return the Uri for a newly scanned file
            to the client of the MediaScannerConnection class.*/

            /*scanFile is used to scan the file when the connection is established with MediaScanner.*/
            MediaScannerConnection.scanFile(
                this@MainActivity, arrayOf(result), null
            ) { path, uri ->
                // This is used for sharing the image after it has being stored in the storage.
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                // A content: URI holding a stream of data associated with the Intent, used to supply the data being sent.
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                // The MIME type of the data being handled by this intent.
                shareIntent.type = "image/png"
                startActivity(Intent.createChooser(shareIntent, "Share"))
            }
        }

        private fun showProgressDialog() {
            mDialog = Dialog(this@MainActivity)
            mDialog.setContentView(R.layout.dialog_custom_progress)
            mDialog.show()
        }

        private fun cancelProgressDialog() {
            mDialog.dismiss()
        }
    }


}