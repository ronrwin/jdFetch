package com.example.jddata.action

import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Route
import com.example.jddata.GlobalInfo
import com.example.jddata.action.fetch.*
import com.example.jddata.action.move.*
import com.example.jddata.action.unknown.*
import com.example.jddata.shelldroid.Env
import java.util.*

class Factory {
    companion object {
        @JvmStatic fun createAction(env: Env, actionType : String?) : Action? {
            when (actionType) {
                ActionType.FETCH_MY -> return FetchMyAction(env)
                ActionType.FETCH_SEARCH -> return FetchSearchAction(env)
                ActionType.FETCH_CART -> return FetchCartAction(env)
                ActionType.FETCH_HOME -> return FetchHomeAction(env)
                ActionType.FETCH_BRAND_KILL -> return FetchBrandKillActionNoSku(env)
                ActionType.FETCH_LEADERBOARD -> return FetchLeaderboardActionNoSku(env)
                ActionType.FETCH_JD_KILL -> return FetchJdKillAction(env)
                ActionType.FETCH_WORTH_BUY -> return FetchWorthBuyActionNoSku(env)
                ActionType.FETCH_NICE_BUY -> return FetchNicebuyAction(env)
                ActionType.FETCH_TYPE_KILL -> return FetchTypeKillActionNoSku(env)
                ActionType.FETCH_DMP -> return FetchDmpAction(env)
                ActionType.FETCH_GOOD_SHOP -> return FetchGoodShopActionNoSku(env)

                ActionType.JD_MARKET -> return JdMarketAction(env)
                ActionType.JD_FRESH -> return JdFreshAction(env)
                ActionType.JD_ACCESS_HOME -> return JdAccessHomeAction(env)
                ActionType.JD_NUT -> return JdNutAction(env)
                ActionType.FLASH_BUY -> return FlashBuyAction(env)
                ActionType.COUPON -> return CouponAction(env)
                ActionType.PLUS -> return PlusAction(env)
                // day 9
                ActionType.MOVE_SEARCH -> return MoveSearchAction(env)
                ActionType.MOVE_SEARCH_YAGAO -> return MoveSearchYagaoAction(env)
                ActionType.MOVE_SEARCH_CLICK -> return MoveSearchClickAction(env)
                ActionType.MOVE_SEARCH_CLICK_BUY -> return MoveSearchClickBuyAction(env)
                ActionType.MOVE_SEARCH_HAIFEISI -> return MoveSearchHaifeisiAction(env)
                ActionType.MOVE_SEARCH_HAIFEISI_CLICK -> return MoveSearchHaifeisiClickAction(env)
                ActionType.MOVE_SEARCH_HAIFEISI_CLICK_BUY -> return MoveSearchHaifeisiClickBuyAction(env)
                ActionType.MOVE_SEARCH_HAIFEISI_SHOP -> return MoveSearchHaifeisiShopAction(env)

                ActionType.MOVE_SEARCH_QUXIE -> return MoveSearchQuxieAction(env)
                ActionType.MOVE_SEARCH_QUXIE_CLICK -> return MoveSearchQuxieiClickAction(env)
                ActionType.MOVE_SEARCH_QUXIE_CLICK_BUY -> return MoveSearchQuxieClickBuyAction(env)

                ActionType.MOVE_DMP_QRCODE -> return MoveDmpQrcodeAction(env)
                ActionType.MOVE_DMP_QRCODE_CLICK -> return MoveDmpQrcodeClickAction(env)
                ActionType.MOVE_DMP_QRCODE_CLICK_BUY -> return MoveDmpQrcodeClickBuyAction(env)

                ActionType.MOVE_JD_KILL_CLICK -> return MoveJdKillClickAction(env)
                ActionType.MOVE_JD_KILL_CLICK_BUY -> return MoveJdKillClickBuyAction(env)
                ActionType.MOVE_JD_KILL_REMIND -> return MoveJdKillRemindAction(env)
                ActionType.MOVE_JD_KILL_WORTH -> return MoveJdKillWorthAction(env)
                ActionType.MOVE_JD_KILL_SALE_OUT -> return MoveJdKillSaleOutAction(env)
            }

            return null
        }

        @JvmStatic fun createTemplateAction(env: Env, route: Route) : TemplateMoveAction? {
            val action = TemplateMoveAction(env, route)
            action.setState(GlobalInfo.LIMIT, Random().nextInt(3) + 4)
            return action
        }

        @JvmStatic private fun getMoveType(day9No: Int): String {
            when (day9No) {
                0 -> return ActionType.MOVE_SEARCH
                1 -> return ActionType.MOVE_SEARCH_CLICK
                2 -> return ActionType.MOVE_SEARCH_CLICK_BUY
                3 -> return ActionType.MOVE_SEARCH_HAIFEISI
                4 -> return ActionType.MOVE_SEARCH_HAIFEISI_CLICK
                5 -> return ActionType.MOVE_SEARCH_HAIFEISI_CLICK_BUY
                6 -> return ActionType.MOVE_SEARCH_HAIFEISI_SHOP
                7 -> return ActionType.MOVE_SEARCH_QUXIE
                8 -> return ActionType.MOVE_SEARCH_QUXIE_CLICK
                9 -> return ActionType.MOVE_SEARCH_QUXIE_CLICK_BUY
            }
            return ""
        }

        @JvmStatic fun createDayNineAction(env: Env) : Action? {
            return createAction(env, getMoveType(env.moveId!!.toInt()))
        }
    }
}