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
                RowData.DEVICE_ID to TEXT,              // 设备编号
                RowData.DEVICE_CREATE_TIME to TEXT,     // 设备创建时间
                RowData.IMEI to TEXT,                   // imei
                RowData.MOVE_ID to TEXT,                // 动作id
                RowData.CREATE_TIME to TEXT,            // 单条记录抓取时间
                RowData.LOCATION to TEXT,               // gps位置
                RowData.BI_ID to TEXT,                  // bi点位
                RowData.ITEM_INDEX to TEXT,             // 第几个
                RowData.TITLE to TEXT,                  // 标题
                RowData.SUBTITLE to TEXT,               // 副标题
                RowData.PRODUCT to TEXT,                // 产品名
                RowData.SKU to TEXT,                    // 产品sku
                RowData.PRICE to TEXT,                  // 价格
                RowData.ORIGIN_PRICE to TEXT,           // 原价
                RowData.DESCRIPTION to TEXT,            // 描述
                RowData.NUM to TEXT,                    // 数量
                RowData.CITY to TEXT,                   // 城市
                RowData.TAB to TEXT,                    // 标签
                RowData.MARK_NUM to TEXT,               // 收藏数
                RowData.VIEW_NUM to TEXT,               // 看过数
                RowData.COMMENT to TEXT,                // 评论数
                RowData.FROM to TEXT,                   // 出处
                RowData.GOOD_FEEDBACK to TEXT,          // 好评率
                RowData.LIKE_NUM to TEXT,               // 喜欢数
                RowData.JDKILL_ROUND_TIME to TEXT,      // 京东秒杀场次
                RowData.BRAND to TEXT,                  // brand
                RowData.CATEGORY to TEXT,               // category
                RowData.IS_ORIGIN to TEXT)              // 是否原始数据
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
