package com.example.jddata.action

import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.action.fetch.*
import com.example.jddata.action.unknown.*
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.util.LogUtil

class Factory {
    companion object {
        @JvmStatic fun createAction(action : String) : BaseAction? {
            return createAction(action, null)
        }

        @JvmStatic fun createAction(action : String?, map : HashMap<String, String>?) : BaseAction? {
            LogUtil.logCache(">>>>  env: ${EnvManager.sCurrentEnv?.envName}, createAction : $action, obj : ${map.toString()}")

            when (action) {
                ActionType.FETCH_MY -> return FetchMyAction()
                ActionType.FETCH_SEARCH -> return FetchSearchAction()
                ActionType.FETCH_CART -> return FetchCartAction()
                ActionType.FETCH_HOME -> return FetchHomeAction()
                ActionType.FETCH_BRAND_KILL -> return FetchBrandKillAction()
                ActionType.FETCH_LEADERBOARD -> return FetchLeaderboardAction()
                ActionType.FETCH_JD_KILL -> return FetchJdKillAction()
                ActionType.FETCH_WORTH_BUY -> return FetchWorthBuyAction()
                ActionType.FETCH_NICE_BUY -> return FetchNicebuyAction()
                ActionType.FETCH_TYPE_KILL -> return FetchTypeKillAction()

                ActionType.JD_MARKET -> return JdMarketAction()
                ActionType.JD_FRESH -> return JdFreshAction()
                ActionType.JD_ACCESS_HOME -> return JdAccessHomeAction()
                ActionType.JD_NUT -> return JdNutAction()
                ActionType.FLASH_BUY -> return FlashBuyAction()
                ActionType.COUPON -> return CouponAction()
                ActionType.PLUS -> return PlusAction()
            }

            BusHandler.instance.startCountTimeout()
            return null
        }

    }
}