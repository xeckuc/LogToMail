package com.examples.logtomail

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private var logPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dummyException = IOException("CRASHHHHH")

        tvError.setOnClickListener {

            showErrorDialog(dummyException)
        }
    }

    @Throws(IOException::class)
    private fun createLogFile(): File {

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val logDir = this.filesDir

        return File.createTempFile(
            "LOG_${timeStamp}",
            ".txt",
            logDir
        ).also {
            logPath = it.absolutePath
        }
    }

    private fun sendLogToEmail(e: Exception) {

        Intent(Intent.ACTION_SEND).also { emailIntent ->

            val logFile = try {

                createLogFile()
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when creating log file: ${e.message}")
                null
            }

            logFile?.also {

                it.writeText(e.message!!)

                val logUri: Uri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID,
                    it
                )

                emailIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                emailIntent.type = "message/*"
                emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("support@logtomail.com"))
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "App crashed")
                emailIntent.putExtra(Intent.EXTRA_STREAM, logUri)
                startActivity(Intent.createChooser(emailIntent, "Share File with..."))
            }
        }
    }

    private fun showErrorDialog(e: Exception) {

        val builder = AlertDialog.Builder(this)
        builder.run {
            title = "Error"
            setMessage("Error Occurred - Send Report?")
            setPositiveButton("Send") { _, _ ->

                createLogFile().also {
                    sendLogToEmail(e)
                }
            }

            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()
    }
}
