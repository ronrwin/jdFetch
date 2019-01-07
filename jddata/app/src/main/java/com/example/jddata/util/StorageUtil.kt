package com.example.jddata.util

import android.text.TextUtils
import com.example.jddata.BusHandler
import com.example.jddata.Entity.MyRowParser
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.storage.database
import org.jetbrains.anko.db.*
import java.text.SimpleDateFormat

class StorageUtil {
    companion object {
        @JvmStatic fun outputDatabaseDatas(dateStr: String?) {
            outputDatabaseDatas(dateStr, false)
        }

        // 输出数据表格
        @JvmStatic fun outputDatabaseDatas(dateStr: String?, origin: Boolean) {
            BusHandler.instance.singleThreadExecutor.execute {
                val sb = StringBuilder()
                sb.append("编号,动作组id,创建时间,账号,gps位置,ip归属地,采集数据间隔,bi点位,商品位置,标题,副标题,产品,价格/秒杀价,原价/京东价,描述,数量,排行榜城市,排行榜标签,收藏数,看过数,评论数,好评率,京东秒杀场次,brand,category\n")
                MainApplication.sContext.database.use {
                    transaction {
                        var builder: SelectQueryBuilder? = null
                        if (origin) {
                            builder = select(GlobalInfo.TABLE_NAME).whereArgs("${RowData.IS_ORIGIN}='0'").orderBy("date", SqlOrderDirection.ASC).orderBy("createTime", SqlOrderDirection.ASC)
                        } else {
                            if (TextUtils.isEmpty(dateStr)) {
                                builder = select(GlobalInfo.TABLE_NAME).orderBy("date", SqlOrderDirection.ASC).orderBy("createTime", SqlOrderDirection.ASC)
                            } else {
                                builder = select(GlobalInfo.TABLE_NAME).whereArgs("(date='${dateStr}') and (${RowData.IS_ORIGIN}='1')").orderBy("date", SqlOrderDirection.ASC).orderBy("createTime", SqlOrderDirection.ASC)
                            }
                        }
                        val parser = MyRowParser()
                        val rows = builder.parseList(parser)
                        for (row in rows) {
                            sb.append(row.toString() + "\n")
                        }
                    }
                }

                val preSuffix = if (origin) "原始data" else "抓取data"
                var filename = "${preSuffix}_日期${dateStr}.csv"

                if (TextUtils.isEmpty(dateStr)) {
                    filename = "all.csv"
                }
                FileUtils.writeToFile("${LogUtil.EXCEL_FILE_FOLDER}/${dateStr}", filename, sb.toString(), false, "gb2312")
            }
        }

    }
}