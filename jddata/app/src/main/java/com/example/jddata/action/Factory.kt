package com.example.jddata.action

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
                ActionType.FETCH_BRAND_KILL -> return FetchBrandKillActionNoSkuTitle(env)
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
                ActionType.MOVE_SEARCH_CLICK -> return MoveSearchClickAction(env)
                ActionType.MOVE_SEARCH_CLICK_BUY -> return MoveSearchClickBuyAction(env)
                ActionType.MOVE_SEARCH_HAIFEISI_CLICK -> return MoveSearchHaifeisiClickAction(env)

                ActionType.MOVE_DMP_QRCODE -> return MoveDmpQrcodeAction(env)
                ActionType.MOVE_DMP_QRCODE_CLICK -> return MoveDmpQrcodeClickAction(env)
                ActionType.MOVE_DMP_QRCODE_CLICK_BUY -> return MoveDmpQrcodeClickBuyAction(env)

                ActionType.MOVE_JD_KILL_CLICK -> return MoveJdKillClickAction(env)
                ActionType.MOVE_JD_KILL_CLICK_BUY -> return MoveJdKillClickBuyAction(env)
                ActionType.MOVE_JD_KILL_REMIND -> return MoveJdKillRemindAction(env)
                ActionType.MOVE_JD_KILL_WORTH -> return MoveJdKillWorthAction(env)
                ActionType.MOVE_JD_KILL_SALE_OUT -> return MoveJdKillSaleOutAction(env)

                // 三期
                ActionType.MOVE_SEARCH_RAZOR -> return MoveSearchRazorAction(env)
                ActionType.MOVE_SEARCH_RAZOR_CLICK_JILIE -> return MoveSearchRazorClickJilieAction(env)
                ActionType.MOVE_SEARCH_RAZOR_CLICK_BOLANG -> return MoveSearchRazorClickBolangAction(env)
                ActionType.MOVE_SEARCH_RAZOR_CLICK_JILIE_BUY -> return MoveSearchRazorClickJilieBuyAction(env)
                ActionType.MOVE_SEARCH_RAZOR_CLICK_BOLANG_BUY -> return MoveSearchRazorClickBolangBuyAction(env)
                ActionType.MOVE_SEARCH_JILIE_SHOP -> return MoveSearchJilieShopAction(env)
                ActionType.MOVE_SEARCH_JILIE_SHOP_MARK -> return MoveSearchJilieShopMarkAction(env)
                ActionType.MOVE_SEARCH_BOLANG_SHOP -> return MoveSearchBolangShopAction(env)
                ActionType.MOVE_SEARCH_JILIE_CLICK_JILIE -> return MoveSearchJilieClickJilieAction(env)
                ActionType.MOVE_SEARCH_YINLIHE_CLICK_JILIE -> return MoveSearchYinliheClickJilieAction(env)
                ActionType.MOVE_SEARCH_YINLIHE_CLICK_JILIE_BUY -> return MoveSearchYinliheClickJilieBuyAction(env)
                ActionType.MOVE_SEARCH_BOLANG_CLICK_BOLANG -> return MoveSearchBolangClickBolangAction(env)
                ActionType.MOVE_SEARCH_JILIE_YINLIHE_MARK -> return MoveSearchJilieYinliheMarkAction(env)
                ActionType.MOVE_SEARCH_BOLANG_SHOP_MARK -> return MoveSearchBolangShopMarkAction(env)
                ActionType.MOVE_JILIE_DMP_QRCODE -> return MoveJilieQrcodeAction(env)
                ActionType.MOVE_DMP_QRCODE_JILIE -> return MoveDmpQrcodeJilieAction(env)
                ActionType.MOVE_DMP_QRCODE_JILIE_CLICK -> return MoveDmpQrcodeJilieClickAction(env)
                ActionType.MOVE_DMP_QRCODE_JILIE_CLICK_BUY -> return MoveDmpQrcodeJilieClickBuyAction(env)
                ActionType.MOVE_DMP_QRCODE_BOLANG -> return MoveDmpQrcodeBolangAction(env)
                ActionType.MOVE_DMP_QRCODE_BOLANG_CLICK -> return MoveDmpQrcodeBolangClickAction(env)
                ActionType.MOVE_DMP_QRCODE_BOLANG_CLICK_BUY -> return MoveDmpQrcodeBolangClickBuyAction(env)
            }

            return null
        }

        @JvmStatic fun createTemplateAction(env: Env, route: Route) : TemplateMoveAction? {
            return TemplateMoveAction(env, route)
        }

        @JvmStatic private fun getMoveType(day9No: Int): String {
            when (day9No) {
                1 -> return ActionType.MOVE_SEARCH
                2 -> return ActionType.MOVE_SEARCH_CLICK
                3 -> return ActionType.MOVE_SEARCH_CLICK_BUY
                4 -> return ActionType.MOVE_SEARCH_HAIFEISI_CLICK

                5 -> return ActionType.MOVE_DMP_QRCODE
                6 -> return ActionType.MOVE_DMP_QRCODE_CLICK
                7 -> return ActionType.MOVE_DMP_QRCODE_CLICK_BUY

                8 -> return ActionType.MOVE_JD_KILL_CLICK
                9 -> return ActionType.MOVE_JD_KILL_CLICK_BUY
                10 -> return ActionType.MOVE_JD_KILL_REMIND
                11 -> return ActionType.MOVE_JD_KILL_WORTH
                12 -> return ActionType.MOVE_JD_KILL_SALE_OUT
            }
            return ""
        }

        @JvmStatic fun createDayNineAction(env: Env, day9No: Int) : Action? {
            return createAction(env, getMoveType(day9No))
        }
    }
}