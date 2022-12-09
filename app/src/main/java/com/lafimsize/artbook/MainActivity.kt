package com.lafimsize.artbook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lafimsize.artbook.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var artList:ArrayList<Art>
    private lateinit var artAdapter: ArtAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        artList= ArrayList<Art>()
        artAdapter= ArtAdapter(artList)
        binding.recyclerViewItems.layoutManager=LinearLayoutManager(this)
        binding.recyclerViewItems.adapter=artAdapter

        try {
            val database=this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

            val cursor=database.rawQuery("select * from arts",null)

            //indexleri bulalım
            val idIx=cursor.getColumnIndex("id")
            println("id sütunu: "+idIx)
            val artNameIx=cursor.getColumnIndex("artname")
            println("artname sütunu: "+artNameIx)

            while (cursor.moveToNext()){
                val id=cursor.getInt(idIx)
                val name=cursor.getString(artNameIx)

                //art diye bir model oluşturulacak.
                val art=Art(name,id)
                //şimdide arraylist oluşturulacak yukarıdaki sınıfları eklemek için.
                artList.add(art)
            }

            artAdapter.notifyDataSetChanged()//veri değişti art adaptöre haber sal demek(bu sayede art adaptörü bir daha tanımlamıyoruz.)

            cursor.close()//verileri çektik şimdi recycler rowa eklecez ama adaptör de lazım.

        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.uploadArt){
            val intent=Intent(this@MainActivity,ArtBookActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}