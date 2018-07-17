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
                ActionType.MOVE_SEARCH -> {
                    return MoveSearchAction(map)
                }
                ActionType.MOVE_SEARCH_CLICK_AND_SHOP -> {
                    return MoveSearchClickAndShopAction(map)
                }
                ActionType.MOVE_SEARCH_AND_CLICK -> {
                    return MoveSearchAndClickAction(map)
                }
                ActionType.FETCH_CART -> return FetchCartAction()
                ActionType.FETCH_HOME -> return FetchHomeAction()
                ActionType.FETCH_BRAND_KILL -> return FetchBrandKillAction()
                ActionType.MOVE_BRAND_KILL_CLICK -> return MoveBrandKillClickAction()
                ActionType.MOVE_BRAND_KILL_AND_SHOP -> return MoveBrandKillShopAction()
                ActionType.FETCH_LEADERBOARD -> return FetchLeaderboardAction()
                ActionType.FETCH_JD_KILL -> return FetchJdKillAction()
                ActionType.FETCH_WORTH_BUY -> return FetchWorthBuyAction()
                ActionType.FETCH_NICE_BUY -> return FetchNicebuyAction()
                ActionType.FETCH_TYPE_KILL -> return FetchTypeKillAction()
                ActionType.MOVE_DMP -> return MoveDmpAction()
                ActionType.MOVE_DMP_CLICK_SHOP -> return MoveDmpClickShopAction()
                ActionType.MOVE_DMP_CLICK -> return MoveDmpClickProductAction()
                ActionType.MOVE_JD_KILL_CLICK -> return MoveJdKillClickAction()
                ActionType.MOVE_JD_KILL_AND_SHOP -> return MoveJdKillShopAction()
            }

            BusHandler.instance.startCountTimeout()
            return null
        }

    }
}