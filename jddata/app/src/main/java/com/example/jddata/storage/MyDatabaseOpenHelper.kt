package com.example.jddata.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import org.jetbrains.anko.db.*

class MyDatabaseOpenHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "MyDatabase", null, 1) {
    companion object {
        private var instance: MyDatabaseOpenHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): MyDatabaseOpenHelper {
            if (instance == null) {
                instance = MyDatabaseOpenHelper(ctx.getApplicationContext())
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Here you create tables
        db.createTable(GlobalInfo.TABLE_NAME, true,
                RowData.ID to INTEGER + PRIMARY_KEY + UNIQUE,
                RowData.DEVICE_ID to TEXT,
                RowData.MOVE_ID to TEXT,
                RowData.DATE to TEXT,
                RowData.CREATE_TIME to TEXT,
                RowData.MOBILE to TEXT,
                RowData.LOCATION to TEXT,
                RowData.WIFI_LOCATION to TEXT,
                RowData.MOVE_INTERVAL to TEXT,
                RowData.BI_ID to TEXT,
                RowData.ITEM_INDEX to TEXT,
                RowData.TITLE to TEXT,
                RowData.SUBTITLE to TEXT,
                RowData.PRODUCT to TEXT,
                RowData.PRICE to TEXT,
                RowData.ORIGIN_PRICE to TEXT,
                RowData.DESCRIPTION to TEXT,
                RowData.NUM to TEXT,
                RowData.LEADERBOARD_CITY to TEXT,
                RowData.LEADERBOARD_TAB to TEXT,
                RowData.MARK_NUM to TEXT,
                RowData.VIEW_NUM to TEXT,
                RowData.COMMENT to TEXT,
                RowData.GOOD_FEEDBACK to TEXT,
                RowData.JDKILL_ROUND_TIME to TEXT)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
        db.dropTable(GlobalInfo.TABLE_NAME, true)
    }
}

// Access property for Context
val Context.database: MyDatabaseOpenHelper
    get() = MyDatabaseOpenHelper.getInstance(getApplicationContext())

fun <K, V : Any?> MutableMap<K, V?>.toVarargArray(): Array<out Pair<K, V?>> =
        map({ Pair(it.key, it.value) }).toTypedArray()
