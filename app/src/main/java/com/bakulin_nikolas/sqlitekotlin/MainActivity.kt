package com.bakulin_nikolas.sqlitekotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bakulin_nikolas.sqlitekotlin.databinding.ActivityMainBinding
import com.bakulin_nikolas.sqlitekotlin.db.MyAdapter
import com.bakulin_nikolas.sqlitekotlin.db.MyDbManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    val myDbManager = MyDbManager(this)
    val myAdapter = MyAdapter(ArrayList(), this)
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //инициализация RecyclerViewAdapter
        init()

        //инициализация поисковой строки
        initSearchView()
    }

    override fun onResume() {
        super.onResume()
        //открыли БД
        myDbManager.openDb()
        fillAdapter("")

    }

    fun onClickNew(view: View) {
        val i = Intent(this, EditActivity::class.java)
        startActivity(i)
    }

    override fun onDestroy() {
        super.onDestroy()
        //закрыли БД
        myDbManager.closeDb()
    }

    //инициализация RecyclerViewAdapter
    fun init() {
        //еще может быть GridLayoutManager(this@MainActivity, 3), то есть в три столбца
        binding.rcView.layoutManager = LinearLayoutManager(this)
        val swapHelper = getSwapMg()
        swapHelper.attachToRecyclerView(binding.rcView)
        binding.rcView.adapter = myAdapter
    }

    //поисковая строка
    private fun initSearchView() {
        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            //поиск при нажатии на кнопку или enter для поиска
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            //поиск при написании
            override fun onQueryTextChange(text: String?): Boolean {
                fillAdapter(text!!)
                return true
            }
        })
    }

    private fun fillAdapter(text: String) {

        //если предыдущий еще не отработал, то остановить и запустить новый
        job?.cancel()
        //создать и запусть корутину на основном потоке
        job = CoroutineScope(Dispatchers.Main).launch {
            //readDbData запускается на второстепенном потоке
            val list = myDbManager.readDbData(text)
            myAdapter.updateAdapter(list)
            if(list.size > 0) {
                binding.tvNoElements.visibility = View.GONE
            } else {
                binding.tvNoElements.visibility = View.VISIBLE
            }
        }

    }

    //с помощью свайпа удалить элемент из списка
    private fun getSwapMg(): ItemTouchHelper {
        return ItemTouchHelper(object:ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            //ViewHolder для каждого элемента списка свой
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                myAdapter.removeItem(viewHolder.adapterPosition, myDbManager)
            }
        })
    }
}