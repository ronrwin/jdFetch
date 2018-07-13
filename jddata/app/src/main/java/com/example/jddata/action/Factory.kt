package com.example.jddata.action

import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.util.LogUtil

class Factory {
    companion object {
        @JvmStatic fun createAction(action : String) : BaseAction? {
            return createAction(action, null)
        }

        @JvmStatic fun createAction(action : String?, map : HashMap<String, String>?) : BaseAction? {
            LogUtil.writeLog(">>>>  env: ${EnvManager.sCurrentEnv?.envName}, createAction : $action, obj : ${map.toString()}")

            when (action) {
                ActionType.FETCH_SEARCH -> {
                    return FetchSearchAction(map)
                }
                ActionType.SEARCH -> {
                    return SearchAction(map)
                }
                ActionType.SEARCH_CLICK_AND_SHOP -> {
                    return SearchClickAndShopAction(map)
                }
                ActionType.SEARCH_AND_CLICK -> {
                    return SearchAndClickAction(map)
                }
                ActionType.CART -> return FetchCartAction()
                ActionType.HOME -> return FetchHomeAction()
                ActionType.BRAND_KILL -> return FetchBrandKillAction()
                ActionType.BRAND_KILL_CLICK -> return BrandKillClickAction()
                ActionType.BRAND_KILL_AND_SHOP -> return BrandKillShopAction()
                ActionType.LEADERBOARD -> return FetchLeaderboardAction()
                ActionType.JD_KILL -> return FetchJdKillAction()
                ActionType.WORTH_BUY -> return FetchWorthBuyAction()
                ActionType.NICE_BUY -> return FetchNicebuyAction()
                ActionType.TYPE_KILL -> return FetchTypeKillAction()
                ActionType.DMP -> return DmpAction()
                ActionType.DMP_CLICK_SHOP -> return DmpClickShopAction()
                ActionType.DMP_CLICK -> return DmpClickProductAction()
            }

            BusHandler.instance.startCountTimeout()
            return null
        }

    }
}