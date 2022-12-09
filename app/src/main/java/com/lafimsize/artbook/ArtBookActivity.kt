package com.lafimsize.artbook

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.get
import com.google.android.material.snackbar.Snackbar
import com.lafimsize.artbook.databinding.ActivityArtBookBinding
import java.io.ByteArrayOutputStream

class ArtBookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArtBookBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedBitmap: Bitmap?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityArtBookBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        registerLauncher()

    }

    fun saveButtonClicked(view: View){
        val artName=binding.artName.text.toString()
        val artistName=binding.artistName.text.toString()

        if (selectedBitmap!=null){
            val smallBitmap=makeSmallerBitmap(selectedBitmap!!)
            binding.imageView.setImageBitmap(smallBitmap)

            //outputStream ile byte bitmapin byte dizisini alıyoruz.
            val outputStream= ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray=outputStream.toByteArray()

            //veritabanına yazalım....
            try {
                val database=this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
                database.execSQL("create table if not exists arts(id INTEGER PRIMARY KEY,artname VARCHAR,artistname VARCHAR, image BLOB)")

                //Sql kodunu hemen çalıştırmasın diye statement kullanıyoruz. Önce ? işaretlerine değerleri atayacağız. (bind ile bağlama yapcaz)
                val sqlString="insert into arts(artname,artistname,image) values(?,?,?)"
                val statement=database.compileStatement(sqlString)
                statement.bindString(1,artName)
                statement.bindString(2,artistName)
                statement.bindBlob(3,byteArray)
                //şimdi çalıştırabiliriz.
                statement.execute()


            }catch (e:java.lang.Exception){
                e.printStackTrace()
            }

            val intent=Intent(this@ArtBookActivity,MainActivity::class.java)
            //flag bayrak demek, aşağıdaki kod ise önceki bütün activityleri sonlandırır.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }



    }

    fun selectImageClicked(view: View){
            //izinler sağlanmış değil ise...
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) {

            val intentToGallery= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            //ilk olarak izni snackbar ile gösterme mantığı geçerli mi?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){

                //rationale, mantık...
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give permission", View.OnClickListener {
                    //request permission
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()

            }else{
                //request permission
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }else{
            val intentToGallery= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            //intent
            activityResultLauncher.launch(intentToGallery)
        }

    }


    private fun registerLauncher() {
        activityResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->

            if (result.resultCode== RESULT_OK){

                val intentFromResult=result.data

                if (intentFromResult!=null){

                    val imageData=intentFromResult.data
                    binding.imageView.setImageURI(imageData)

                    if (imageData!=null){
                        try {
                            if (Build.VERSION.SDK_INT>=28){
                                val source=ImageDecoder.createSource(this@ArtBookActivity.contentResolver, imageData)
                                selectedBitmap=ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }else{
                                selectedBitmap=MediaStore.Images.Media.getBitmap(this@ArtBookActivity.contentResolver, imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }



                        }catch (e: Exception){

                            e.printStackTrace()

                        }
                    }

                }
            }

        }
        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){ result->
            if (result){
                //permission granted
                val intentToGallery=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }else{
                //permisson denied
                Toast.makeText(this, "Permission needed!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun makeSmallerBitmap(image: Bitmap):Bitmap{
        val width=image.width
        val height=image.height

        val width2=(width.toDouble()/6).toInt()
        val height2=(height.toDouble()/6).toInt()
        return Bitmap.createScaledBitmap(image, width2, height2, true)
    }
}