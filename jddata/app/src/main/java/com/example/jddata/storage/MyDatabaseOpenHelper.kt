package com.example.jddata.storage

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.jddata.BusHandler
import com.example.jddata.Entity.MyRowParser
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.util.FileUtils
import com.example.jddata.util.LogUtil
import org.jetbrains.anko.db.*
import kotlin.math.min

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

        @JvmStatic fun outputDatabaseDatas(dateStr: String?) {
            outputDatabaseDatas(dateStr, false)
        }

        @JvmStatic fun changeId() {
            BusHandler.instance.singleThreadExecutor.execute {
                var biggest = 0
                var minist = 500
                for (env in EnvManager.envs) {
                    val id = env.id
                    if (id!!.contains("_")) {
                        val ids = id.split("_")
                        val num = ids[0].toInt()
                        if (num > biggest) {
                            biggest = num
                        }
                        if (num < minist) {
                            minist = num
                        }
                    }

                    val name = env.envName
                    MainApplication.sContext.database.use {
                        update(GlobalInfo.TABLE_NAME, RowData.DEVICE_ID to id)
                                .whereArgs("${RowData.DEVICE_ID}='${name}'")
                                .exec()
                    }
                }
                FileUtils.writeToFile(LogUtil.EXTERNAL_FILE_FOLDER + "/changeid",  "${minist}_${biggest}_done", "", false)
            }
        }

        // 输出数据表格
        @JvmStatic fun outputDatabaseDatas(dateStr: String?, origin: Boolean) {
            BusHandler.instance.singleThreadExecutor.execute {
                val preSuffix = if (origin) "原始data" else "抓取data"
                var filename = "${preSuffix}_日期${dateStr}.csv"

                if (TextUtils.isEmpty(dateStr)) {
                    var biggest = 0
                    for (env in EnvManager.envs) {
                        val id = env.id
                        if (id!!.contains("_")) {
                            val ids = id.split("_")
                            val num = ids[0].toInt()
                            if (num > biggest) {
                                biggest = num
                            }
                        }
                    }
                    filename = "${preSuffix}_日期${dateStr}_data_${biggest}.csv"
                }

                var title = "设备编号,设备创建时间,date,imei,动作组,记录创建时间,gps位置,bi点位,商品位置,标题,副标题,产品,sku,价格/秒杀价,原价/京东价,描述,数量,城市,标签,店铺,收藏数（关注数）,看过数,评论数,出处,好评率,喜欢数,热卖指数,是否自营,已售,京东秒杀场次,brand,category,是否原始数据\n";
                title = ""

                MainApplication.sContext.database.use {
                    transaction {
                        var builder: SelectQueryBuilder?
                        if (origin) {
                            builder = select(GlobalInfo.TABLE_NAME)
                                    .whereArgs("${RowData.IS_ORIGIN}='true'")
                                    .orderBy("date", SqlOrderDirection.ASC)
                                    .orderBy("createTime", SqlOrderDirection.ASC)
                        } else {
                            if (TextUtils.isEmpty(dateStr)) {
                                builder = select(GlobalInfo.TABLE_NAME)
                                        .orderBy("date", SqlOrderDirection.ASC)
                                        .orderBy("createTime", SqlOrderDirection.ASC)
                            } else {
                                builder = select(GlobalInfo.TABLE_NAME)
                                        .whereArgs("date='${dateStr}'")
                                        .orderBy("date", SqlOrderDirection.ASC)
                                        .orderBy("createTime", SqlOrderDirection.ASC)
                            }
                        }
                        val parser = MyRowParser()
                        builder.exec {
                            val count = this.count
                            if (count > 0) {
                                FileUtils.writeToFile(LogUtil.EXTERNAL_FILE_FOLDER, filename, title, false, "gb2312")
                                FileUtils.delete(LogUtil.EXTERNAL_FILE_FOLDER + "/${filename}_done")
                            }
                            Log.d("zfr", "count: ${count}")
                            val limitCount = 20000
                            moveToFirst()
                            var sb = StringBuilder()
                            for (index in 1..count) {
                                while (!isAfterLast) {
                                    val row = parser.parseRow(readColumnsMap(this))
                                    val value = row.toString()
                                    Log.d("zfr", "${row.id}," + value)
                                    sb.append(value + "\n")
                                    moveToNext()
                                }
                                if (index % limitCount == 0 || index == count) {
                                    // 最后处理
                                    // 输出到一级目录
                                    FileUtils.writeToFile(LogUtil.EXTERNAL_FILE_FOLDER, filename, sb.toString(), true, "gb2312")
                                    sb = StringBuilder()
                                    MainApplication.sMainHandler.post {
                                        Toast.makeText(MainApplication.sContext, "all count: ${count}, output data: ${index}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }

                FileUtils.writeToFile(LogUtil.EXTERNAL_FILE_FOLDER, filename +  "_done", "", false)
                MainApplication.sMainHandler.post {
                    Toast.makeText(MainApplication.sContext, "output data done", Toast.LENGTH_LONG).show()
                }
            }
        }

        @JvmStatic private fun getColumnValue(cursor: Cursor, index: Int): Any? {
            if (cursor.isNull(index)) return null

            return when (cursor.getType(index)) {
                Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(index)
                Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(index)
                Cursor.FIELD_TYPE_STRING -> cursor.getString(index)
                Cursor.FIELD_TYPE_BLOB -> cursor.getBlob(index)
                else -> null
            }
        }

        @JvmStatic private fun readColumnsMap(cursor: Cursor): Map<String, Any?> {
            val count = cursor.columnCount
            val map = hashMapOf<String, Any?>()
            for (i in 0..(count - 1)) {
                map.put(cursor.getColumnName(i), getColumnValue(cursor, i))
            }
            return map
        }
    }



    override fun onCreate(db: SQLiteDatabase) {
        // Here you create tables
        db.createTable(GlobalInfo.TABLE_NAME, true,
                RowData.ID to INTEGER + PRIMARY_KEY + UNIQUE,
                RowData.DEVICE_ID to TEXT,              // 设备编号
                RowData.DEVICE_CREATE_TIME to TEXT,     // 设备创建时间
                RowData.DATE to TEXT,                   // 日期
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
                RowData.SHOP to TEXT,                   // 店铺
                RowData.MARK_NUM to TEXT,               // 收藏数
                RowData.VIEW_NUM to TEXT,               // 看过数
                RowData.COMMENT to TEXT,                // 评论数
                RowData.FROM to TEXT,                   // 出处
                RowData.GOOD_FEEDBACK to TEXT,          // 好评率
                RowData.LIKE_NUM to TEXT,               // 喜欢数
                RowData.SALE_PERCENT to TEXT,           // 热卖指数
                RowData.IS_SELF_SALE to TEXT,           // 是否自营
                RowData.HAS_SALE_PERCENT to TEXT,       // 已售
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
