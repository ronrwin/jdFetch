package com.example.jddata.action

import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Route
import com.example.jddata.action.fetch.*
import com.example.jddata.action.move.*
import com.example.jddata.action.unknown.*
import com.example.jddata.shelldroid.Env

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
                ActionType.FETCH_NICE_BUY_NEW -> return FetchNicebuyNewAction(env)
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

        @JvmStatic fun createTemplateAction(env: Env, route: Route) : TemplateMoveAction? {
            return TemplateMoveAction(env, route)
        }

        @JvmStatic fun createDayNineAction(env: Env, day9No: Int) : Action? {
            when (day9No) {
                0 -> return MoveSearchAction(env)
                1 -> return MoveSearchClickAction(env)
                2 -> return MoveSearchClickBuyAction(env)
                3 -> return MoveSearchHaifeisiClickAction(env)

                4 -> return MoveDmpQrcodeAction(env)
                5 -> return MoveDmpQrcodeClickAction(env)
                6 -> return MoveDmpQrcodeClickBuyAction(env)

                7 -> return MoveJdKillClickAction(env)
                8 -> return MoveJdKillClickBuyAction(env)
                9 -> return MoveJdKillRemindAction(env)
                10 -> return MoveJdKillWorthAction(env)
                11 -> return MoveJdKillSaleOutAction(env)
            }
            return null
        }
    }
}