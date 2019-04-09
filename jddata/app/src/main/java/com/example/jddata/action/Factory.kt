package com.example.jddata.action

import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Route
import com.example.jddata.MainApplication
import com.example.jddata.action.fetch.*
import com.example.jddata.action.unknown.*
import com.example.jddata.shelldroid.Env
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.util.LogUtil

class Factory {
    companion object {
        @JvmStatic fun createAction(env: Env, actionType : String?) : Action? {
            when (actionType) {
                ActionType.FETCH_MY -> return FetchMyAction(env)
                ActionType.FETCH_SEARCH -> return FetchSearchAction(env)
                ActionType.FETCH_CART -> return FetchCartAction(env)
                ActionType.FETCH_HOME -> return FetchHomeAction(env)
                ActionType.FETCH_BRAND_KILL -> return FetchBrandKillAction(env)
                ActionType.FETCH_LEADERBOARD -> return FetchLeaderboardAction(env)
                ActionType.FETCH_JD_KILL -> return FetchJdKillAction(env)
                ActionType.FETCH_WORTH_BUY -> return FetchWorthBuyAction(env)
                ActionType.FETCH_NICE_BUY -> return FetchNicebuyAction(env)
                ActionType.FETCH_TYPE_KILL -> return FetchTypeKillAction(env)
                ActionType.FETCH_DMP -> return FetchDmpAction(env)

                ActionType.JD_MARKET -> return JdMarketAction(env)
                ActionType.JD_FRESH -> return JdFreshAction(env)
                ActionType.JD_ACCESS_HOME -> return JdAccessHomeAction(env)
                ActionType.JD_NUT -> return JdNutAction(env)
                ActionType.FLASH_BUY -> return FlashBuyAction(env)
                ActionType.COUPON -> return CouponAction(env)
                ActionType.PLUS -> return PlusAction(env)
            }

            BusHandler.instance.startCountTimeout()
            return null
        }

        @JvmStatic fun createTemplateAction(env: Env, route: Route) : Action? {
            return MoveAction(env, route)
        }
    }
}