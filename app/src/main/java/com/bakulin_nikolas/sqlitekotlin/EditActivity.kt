package com.bakulin_nikolas.sqlitekotlin

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bakulin_nikolas.sqlitekotlin.databinding.EditActivityBinding
import com.bakulin_nikolas.sqlitekotlin.db.MyDbManager
import com.bakulin_nikolas.sqlitekotlin.db.MyIntentConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {

    lateinit var binding: EditActivityBinding
    val myDbManager = MyDbManager(this)
    var tempImageUri = "empty"
    var id = 0
    var isEditState = false
    private val launcherGetImage: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { imageUri: Uri? ->
        binding.imMainImage.setImageURI(imageUri)
        tempImageUri = imageUri.toString()
        contentResolver.takePersistableUriPermission(imageUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getMyIntents()
    }

    override fun onResume() {
        super.onResume()
        //открыли БД
        myDbManager.openDb()

    }

    fun onClickAddImage(view: View) {
        with(binding) {
            mainImageLayout.visibility = View.VISIBLE
            fbAddImage.visibility = View.GONE
        }
    }

    fun onClickDeleteImage(view: View) {
        with(binding) {
            mainImageLayout.visibility = View.GONE
            fbAddImage.visibility = View.VISIBLE
            tempImageUri = "empty"
        }
    }

    fun onClickChooseImage(view: View) {
        launcherGetImage.launch(arrayOf("image/*"))
    }

    fun onClickSave(view: View) {

        val myTitle = binding.edTitle.text.toString()
        val myDesc = binding.edDesc.text.toString()

        if (myTitle != "" && myDesc != "") {

            CoroutineScope(Dispatchers.Main).launch {
                if(isEditState) {
                    myDbManager.updateItem(myTitle, myDesc, tempImageUri, id, getCurrentTime())
                } else {
                    myDbManager.insertToDb(myTitle, myDesc, tempImageUri, getCurrentTime())
                }

                finish()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //закрыли БД
        myDbManager.closeDb()
    }

    fun getMyIntents() {

        binding.fbEdit.visibility = View.GONE
        val i = intent

        if (i != null) {

            if(i.getStringExtra(MyIntentConstants.I_TITLE_KEY) != null) {

                with(binding) {
                    fbAddImage.visibility = View.GONE
                    edTitle.setText(i.getStringExtra(MyIntentConstants.I_TITLE_KEY))
                    edDesc.setText(i.getStringExtra(MyIntentConstants.I_DESC_KEY))
                    isEditState = true
                    edTitle.isEnabled = false
                    edDesc.isEnabled = false
                    fbEdit.visibility = View.VISIBLE
                    //если getInt то нужно указывать значение по умолчанию
                    id = i.getIntExtra(MyIntentConstants.I_ID_KEY, 0)

                    if(i.getStringExtra(MyIntentConstants.I_URI_KEY) != "empty") {
                        mainImageLayout.visibility = View.VISIBLE
                        tempImageUri = i.getStringExtra(MyIntentConstants.I_URI_KEY)!!
                        imMainImage.setImageURI(Uri.parse(tempImageUri))
                        imButtonDeleteImage.visibility = View.GONE
                        imButtonEditImage.visibility = View.GONE
                    }
                }


            }
        }
    }

    fun onEditEnable(view: View) {
        with(binding) {
            edTitle.isEnabled = true
            edDesc.isEnabled = true
            fbEdit.visibility = View.GONE
            fbAddImage.visibility = View.VISIBLE

            if(tempImageUri == "empty") return
            imButtonEditImage.visibility = View.VISIBLE
            imButtonDeleteImage.visibility = View.VISIBLE
        }
    }

    private fun getCurrentTime():String {
        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd-MM-yy kk:mm", Locale.getDefault())
        val fTime = formatter.format(time)
        return fTime
    }
}