package com.onpu.onpulab2

import android.Manifest
import android.R.attr.path
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.onpu.onpulab2.databinding.ActivityMainBinding
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var latestUri: Uri? = null

    private val getCameraImage = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Log.i("INFO", "Picture captured successfully")
            latestUri?.let { uri ->
                binding.iv.setImageURI(uri)
            }
        } else {
            Log.i("INFO", "Picture captured with errors")
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.i("INFO", "Camera permissions granted")
            Toast.makeText(baseContext, "We cannot do photos without your permission!", Toast.LENGTH_SHORT).show()
        } else {
            Log.i("INFO", "Camera permissions not granted")
            Toast.makeText(baseContext, "We cannot do photos without your permission!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPicture.setOnClickListener {
            grantCameraPermission()
            latestUri = getFileUri()
            getCameraImage.launch(latestUri)
        }
        binding.btnSend.setOnClickListener {
            sendMailIntent()
        }
    }

    // check for camera permission and if not granted yet, try to get permission
    private fun grantCameraPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.CAMERA
            ) -> {
                Log.i("TAG", "Camera permissions granted already")
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun getFileUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        return FileProvider.getUriForFile(this, "com.onpu.onpulab2.fileprovider", image)
    }

    private fun sendMailIntent() {
        latestUri?.let {
            val emailIntent = Intent(
                Intent.ACTION_SEND, Uri.fromParts(
                    "mailto", "mail@gmail.com", null
                )
            )
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            emailIntent.setDataAndType(it, contentResolver.getType(it))
            emailIntent.putExtra(Intent.EXTRA_STREAM, it)
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("emailto@gmail.com"))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Buryachenko O.O. AI-194")
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        }
    }

    private fun fileToByte(filePath: String): ByteArray {
        val file = File(filePath)
        val size = file.length().toInt()
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bytes
    }
}