package com.example.jddata.util

import android.text.TextUtils
import com.example.jddata.BusHandler
import com.example.jddata.Entity.MyRowParser
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.storage.database
import org.jetbrains.anko.db.SelectQueryBuilder
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction
import java.text.SimpleDateFormat

class StorageUtil {
    companion object {
        @JvmStatic fun outputDatabaseDatas(dateStr: String?) {
            outputDatabaseDatas(dateStr, false)
        }

        @JvmStatic fun outputDatabaseDatas(dateStr: String?, origin: Boolean) {
            BusHandler.instance.singleThreadExecutor.execute(Runnable {
                val sb = StringBuilder()
                sb.append("编号,设备创建时间,imei,动作组id,日期,创建时间,账号,gps位置,ip归属地,采集数据间隔,bi点位,商品位置,标题,副标题,产品,价格/秒杀价,原价/京东价,描述,数量,排行榜城市,排行榜标签,收藏数,看过数,评论数,好评率,京东秒杀场次,brand,category\n")
                MainApplication.getContext().database.use {
                    transaction {
                        var builder: SelectQueryBuilder? = null
                        if (origin) {
                            val isOrigin = if (origin) "0" else "1"
                            builder = select(GlobalInfo.TABLE_NAME).whereArgs("date='${dateStr}' and ${RowData.IS_ORIGIN}='${isOrigin}'")
                        } else {
                            if (TextUtils.isEmpty(dateStr)) {
                                builder = select(GlobalInfo.TABLE_NAME)
                            } else {
                                builder = select(GlobalInfo.TABLE_NAME).whereArgs("date='${dateStr}' and ${RowData.IS_ORIGIN}='1'")
                            }
                        }
                        val parser = MyRowParser()
                        val rows = builder!!.parseList(parser)
                        for (row in rows) {
                            sb.append(row.toString() + "\n")
                        }
                    }
                }
                val computerNum = SharedPreferenceHelper.getInstance().getValue(GlobalInfo.COMPUTER_NUM)
                val preSuffix = if (origin) "原始data" else "抓取data"
                var filename = "${preSuffix}_${computerNum}号机器_${GlobalInfo.emulatorId}号手机_日期${dateStr}.csv"
                if (TextUtils.isEmpty(dateStr)) {
                    filename = "${preSuffix}_${computerNum}号机器_${GlobalInfo.emulatorId}号手机_all.csv"
                }
                FileUtils.writeToFile(LogUtil.EXCEL_FILE_FOLDER, filename, sb.toString(), false, "gb2312")
            })
        }

        // 删除当天某个动作组，某个Bi点数据
        // val numRowsDeleted = delete("User", "_id = {userID}", "userID" to 37)
        @JvmStatic fun deleteMobile() {
            BusHandler.instance.singleThreadExecutor.execute(Runnable {
                MainApplication.getContext().database.use {
                    transaction {
                        val dateStr = ExecUtils.getCurrentTimeString(SimpleDateFormat("MM-dd"))
                        delete(GlobalInfo.TABLE_NAME,
                                "${RowData.DATE} = ? and ",  arrayOf(
                                dateStr

                        ))
                    }
                }
            })
        }

    }
}