package com.bakulin_nikolas.sqlitekotlin.db

import android.content.Context
import android.content.Intent
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bakulin_nikolas.sqlitekotlin.EditActivity
import com.bakulin_nikolas.sqlitekotlin.R
import com.bakulin_nikolas.sqlitekotlin.databinding.RcItemBinding

class MyAdapter(listMain: ArrayList<ListItem>, contextM: Context) : RecyclerView.Adapter<MyAdapter.MyHolder>() {
    //взять массив со списком элементов из конструктора
    var listArray = listMain
    var context = contextM

    class MyHolder(itemView: View, contextV: Context) : RecyclerView.ViewHolder(itemView) {

        val context = contextV
        val binding = RcItemBinding.bind(itemView)
        fun setData(item: ListItem) {
            binding.apply {
                tvTitle.text = item.title
                tvTime.text = item.time

                itemView.setOnClickListener {
                    val intent = Intent(context, EditActivity::class.java).apply {
                        putExtra(MyIntentConstants.I_TITLE_KEY, item.title)
                        putExtra(MyIntentConstants.I_DESC_KEY, item.desc)
                        putExtra(MyIntentConstants.I_URI_KEY, item.uri)
                        putExtra(MyIntentConstants.I_TIME_KEY, item.time)
                        putExtra(MyIntentConstants.I_ID_KEY, item.id)
                    }
                    context.startActivity(intent)

                }

            }
        }

    }

    //создаем шаблон для одного элемента списка (берем xml и готовим для рисования)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        //создается view элемент из layout файла
        // взять контекст от родителя (чтобы из активити не передавать) и надуть его
        //в inflate передается
        //resource - ID layout-файла, который будет использован для создания View
        //root – родительский ViewGroup-элемент для создаваемого View. LayoutParams от этого ViewGroup присваиваются создаваемому View.
        //attachToRoot – присоединять ли создаваемый View к root. Если true, то root становится родителем создаваемого View. Т.е. это равносильно команде root.addView(View).  Если false – то создаваемый View просто получает LayoutParams от root, но его дочерним элементом не становится.
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.rc_item, parent, false)
        return MyHolder(inflater, context)
    }

    //получаем созданный holder и заполняем шаблон данными
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.setData(listArray.get(position))
    }

    //возвращает сколько элементов в нашем списке
    override fun getItemCount(): Int {
        return listArray.size
    }

    fun updateAdapter(listItems: List<ListItem>) {
        listArray.clear()
        listArray.addAll(listItems)
        //сообщить адаптеру, что данные изменились
        notifyDataSetChanged()
    }

    //удалить элемент из списка
    fun removeItem(pos: Int, dbManager: MyDbManager) {
        //удалить элемент из базы
        dbManager.removeItemFromDb(listArray[pos].id.toString())
        //удаляем элемент из списка, который невидем
        listArray.removeAt(pos)
        //говорим адаптеру, что количество/порядок элементов в списке изменился
        notifyItemRangeChanged(0, listArray.size)
        //говорим адаптеру, что удален элемент из списка, чтобы визуально его не было
        notifyItemRemoved(pos)
    }

}