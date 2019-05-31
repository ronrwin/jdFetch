package com.example.jddata.storage

import android.content.Context
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
                    filename = "all_data_${biggest}.csv"
                }


                val title = "id,设备编号,设备创建时间,date,imei,动作组,记录创建时间,gps位置,bi点位,商品位置,标题,副标题,产品,sku,价格/秒杀价,原价/京东价,描述,数量,城市,标签,店铺,收藏数（关注数）,看过数,评论数,出处,好评率,喜欢数,热卖指数,是否自营,已售,京东秒杀场次,brand,category,是否原始数据\n";

                FileUtils.writeToFile(LogUtil.EXTERNAL_FILE_FOLDER, filename, title, false, "gb2312")

                var count = 0L
                MainApplication.sContext.database.use {
                    val cursor = this.rawQuery("select count(*) from ${GlobalInfo.TABLE_NAME}", null)
                    cursor.moveToFirst()
                    count = cursor.getLong(0)
                    Log.d("zfr", "count: ${count}")
                }

                val limitCount = 1000
                for (offset in 0..count.toInt() step 1000) {
                    val sb = StringBuilder()
                    MainApplication.sContext.database.use {
                        transaction {
                            var builder: SelectQueryBuilder?
                            if (origin) {
                                builder = select(GlobalInfo.TABLE_NAME)
                                        .whereArgs("${RowData.IS_ORIGIN}='true'")
                                        .orderBy("date", SqlOrderDirection.ASC)
                                        .orderBy("createTime", SqlOrderDirection.ASC)
                                        .limit(offset, limitCount)
                            } else {
                                if (TextUtils.isEmpty(dateStr)) {
                                    builder = select(GlobalInfo.TABLE_NAME)
                                            .orderBy("date", SqlOrderDirection.ASC)
                                            .orderBy("createTime", SqlOrderDirection.ASC)
                                            .limit(offset, limitCount)
                                } else {
                                    builder = select(GlobalInfo.TABLE_NAME)
                                            .whereArgs("date='${dateStr}'")
                                            .orderBy("date", SqlOrderDirection.ASC)
                                            .orderBy("createTime", SqlOrderDirection.ASC)
                                            .limit(offset, limitCount)
                                }
                            }
                            val parser = MyRowParser()
                            val rows = builder.parseList(parser)
                            for (row in rows) {
                                sb.append(row.toString() + "\n")
                            }
                        }
                    }

                    // 输出到一级目录
                    FileUtils.writeToFile(LogUtil.EXTERNAL_FILE_FOLDER, filename, sb.toString(), true, "gb2312")
                    MainApplication.sMainHandler.post {
                        Toast.makeText(MainApplication.sContext, "output data: ${offset} - ${offset+limitCount}", Toast.LENGTH_SHORT).show()
                    }
                }
                MainApplication.sMainHandler.post {
                    Toast.makeText(MainApplication.sContext, "output data done", Toast.LENGTH_LONG).show()
                }
            }
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
