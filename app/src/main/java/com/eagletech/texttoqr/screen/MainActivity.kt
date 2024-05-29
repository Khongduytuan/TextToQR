package com.eagletech.texttoqr.screen

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.eagletech.texttoqr.databinding.ActivityMainBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.Manifest
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.eagletech.texttoqr.data.ManagerData

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var myData: ManagerData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myData = ManagerData.getInstance(this)

        // Request storage permission if not already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }
        Log.d("myData", myData.isPremium.toString())
        listenerClick()
    }

    private fun listenerClick() {
        binding.button.setOnClickListener {
            val text = binding.editText.text.toString()
            if (text.isNotEmpty()) {
                val bitmap = generateQRCode(text)
                binding.imageView.setImageBitmap(bitmap)
                binding.editText.setText("")
            }
        }
        binding.topBar.dowloadIcon.setOnClickListener {
            if (binding.imageView.drawable != null) {
                val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
                if (myData.isPremium == true) {
                    saveImageToGallery(bitmap)
                }
                if (myData.getB() > 0) {
                    saveImageToGallery(bitmap)
                    myData.removeB()
                } else {
                    Toast.makeText(
                        this,
                        "Please buy saves",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this, MainActivity2::class.java)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(
                    this,
                    "you have to create QR first",
                    Toast.LENGTH_SHORT
                ).show()
            }
//            if (myData.isPremium == true) {
//                val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
//                if (bitmap != null) {
//                    Log.d("bitmaptrue", bitmap.toString())
//                    saveImageToGallery(bitmap)
//                } else {
//                    Log.d("bitmapfasle", bitmap.toString())
//                    Toast.makeText(
//                        this,
//                        "you have to create QR first",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//            } else if (myData.getB() > 0) {
//                val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
//                if (bitmap != null) {
//                    saveImageToGallery(bitmap)
//                    myData.removeB()
//                } else {
//                    Toast.makeText(
//                        this,
//                        "you have to create QR first",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//            } else {
//                Toast.makeText(
//                    this,
//                    "Please buy saves",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }

        }
        binding.topBar.infoIcon.setOnClickListener {
            showInfoBuy()
        }
        binding.topBar.menuIcon.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, proceed as usual
            } else {
                Toast.makeText(
                    this,
                    "Permission denied to write to external storage",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun generateQRCode(text: String): Bitmap? {
        val size = 512 // kích thước của mã QR code
        val qrCodeWriter = QRCodeWriter()
        return try {
            val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    )
                }
            }
            bmp
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {

        val filename = "QRCode_${System.currentTimeMillis()}.png"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            filename
        )
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            Toast.makeText(this, "QR Code saved to Gallery!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save QR Code", Toast.LENGTH_SHORT).show()
        }


    }

    private fun showInfoBuy() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Your save information")
            .setPositiveButton("Confirm") { dialog, _ -> dialog.dismiss() }
            .create()
        if (myData.isPremium == true) {
            dialog.setMessage("You have successfully registered for a long term")
        } else {
            dialog.setMessage("You have ${myData.getB()} times left to save the QR code")
        }
        dialog.show()
    }

}