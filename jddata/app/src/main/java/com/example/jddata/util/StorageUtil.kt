package com.example.jddata.util

import com.example.jddata.BusHandler
import com.example.jddata.Entity.MyRowParser
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.storage.database
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction
import java.text.SimpleDateFormat

class StorageUtil {
    companion object {

        @JvmStatic fun outputDatabaseDatas() {
            BusHandler.instance.singleThreadExecutor.execute(Runnable {
                val sb = StringBuilder()
                sb.append("编号,动作组id,日期,创建时间,账号,gps位置,ip归属地,采集数据间隔,bi点位,页面位置,标题,副标题,产品,价格/秒杀价,原价/京东价,描述,数量,排行榜城市,排行榜标签,收藏数,看过数,评论,好评率,京东秒杀场次\n")
                MainApplication.getContext().database.use {
                    transaction {
                        val builder = select(GlobalInfo.TABLE_NAME)
                        val parser = MyRowParser()
                        val rows = builder.parseList(parser)
                        for (row in rows) {
                            sb.append(row.toString() + "\n")
                        }
                    }
                }
                FileUtils.writeToFile(LogUtil.getExternalFolder(), "${GlobalInfo.moveId}_data.csv", sb.toString(), false, "gb2312")
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