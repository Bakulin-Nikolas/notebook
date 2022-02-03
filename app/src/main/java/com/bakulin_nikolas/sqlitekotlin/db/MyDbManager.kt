package com.bakulin_nikolas.sqlitekotlin.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MyDbManager(context: Context) {
    val myDbHelper = MyDbHelper(context)
    var db: SQLiteDatabase? = null

    //открыть БД для записи
    fun openDb() {
        db = myDbHelper.writableDatabase
    }

    //запись в БД
    //нельзя каждое поле записывать отдельно в БД, а нужно подготв=овленную строку сразу с помощью ContentValues
    suspend fun insertToDb(title: String, content: String, uri: String, time: String) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(MyDbNameClass.COLUMN_NAME_TITLE, title)
            put(MyDbNameClass.COLUMN_NAME_CONTENT, content)
            put(MyDbNameClass.COLUMN_NAME_IMAGE_URI, uri)
            put(MyDbNameClass.COLUMN_NAME_TIME, time)
        }
        db?.insert(MyDbNameClass.TABLE_NAME, null, values)
    }

    //обновить элемент
    suspend fun updateItem(title: String, content: String, uri: String, id: Int, time: String) = withContext(Dispatchers.IO) {
        //_id=5 например
        var selection = BaseColumns._ID + "=$id"
        val values = ContentValues().apply {
            put(MyDbNameClass.COLUMN_NAME_TITLE, title)
            put(MyDbNameClass.COLUMN_NAME_CONTENT, content)
            put(MyDbNameClass.COLUMN_NAME_IMAGE_URI, uri)
            put(MyDbNameClass.COLUMN_NAME_TIME, time)
        }
        db?.update(MyDbNameClass.TABLE_NAME, values, selection, null)
    }

    fun removeItemFromDb(id: String) {
        //_id=5 например
        var selection = BaseColumns._ID + "=$id"
        db?.delete(MyDbNameClass.TABLE_NAME, selection, null)
    }

    suspend fun readDbData(searchText: String) : ArrayList<ListItem> = withContext(Dispatchers.IO) {
        val dataList = ArrayList<ListItem>()
        val selection = "${MyDbNameClass.COLUMN_NAME_TITLE} like ?"
        //получаем таблицу необходимую
        val cursor = db?.query(
            MyDbNameClass.TABLE_NAME, // The table to query
            null, // The array of columns to return (pass null to get all)
            selection, // The columns for the WHERE clause
            arrayOf("%$searchText%"), // The values for the WHERE clause
            null, // don't group the rows
            null, // don't filter by row groups
            null // The sort order
        )


        //если cursor не null то получить следующий элемент, вернее bool значение есть следующий элемент или нет
        while(cursor?.moveToNext()!!) {
            //получаем элементы поочереди и записываем в массив dataList
            val dataTitle = cursor.getString(cursor.getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_TITLE))
            val dataContent = cursor.getString(cursor.getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_CONTENT))
            val dataUri = cursor.getString(cursor.getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_IMAGE_URI))
            val dataTime = cursor.getString(cursor.getColumnIndexOrThrow(MyDbNameClass.COLUMN_NAME_TIME))
            val dataId = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))

            val item = ListItem()
            item.title = dataTitle
            item.desc = dataContent
            item.uri = dataUri
            item.time = dataTime
            item.id = dataId
            dataList.add(item)
        }

        cursor.close()

        return@withContext dataList
    }

    fun closeDb() {
        myDbHelper.close()
    }
}