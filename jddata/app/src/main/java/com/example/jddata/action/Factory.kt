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

                ActionType.MOVE_JD_KILL_REMIND -> return MoveJdKillRemindAction(env)
                ActionType.MOVE_JD_KILL_WORTH -> return MoveJdKillWorthAction(env)
                ActionType.MOVE_JD_KILL_SALE_OUT -> return MoveJdKillSaleOutAction(env)

                // 三期
                // 1. 16.  搜索剃须刀
                ActionType.MOVE_SEARCH_RAZOR -> return MoveSearchRazorAction(env)
                // 2. 搜索剃须刀，点击吉列
                ActionType.MOVE_SEARCH_RAZOR_CLICK_JILIE -> return MoveSearchRazorClickJilieAction(env)
                // 3. 搜索剃须刀，点击吉列，加购
                ActionType.MOVE_SEARCH_RAZOR_CLICK_JILIE_BUY -> return MoveSearchRazorClickJilieBuyAction(env)
                // 4. 搜索吉列，店铺
                ActionType.MOVE_SEARCH_JILIE_SHOP -> return MoveSearchJilieShopAction(env)
                // 5. 吉列sku
                ActionType.MOVE_JILIE_QRCODE -> return MoveJilieQrcodeAction(env)
                // 6. 搜索吉列，点击吉列
                ActionType.MOVE_SEARCH_JILIE_CLICK_JILIE -> return MoveSearchJilieClickJilieAction(env)
                // 7. 搜哦引力盒，点击吉列
                ActionType.MOVE_SEARCH_YINLIHE_CLICK_JILIE -> return MoveSearchYinliheClickJilieAction(env)
                // 8. 搜索引力盒，点击吉列，加购
                ActionType.MOVE_SEARCH_YINLIHE_CLICK_JILIE_BUY -> return MoveSearchYinliheClickJilieBuyAction(env)
                // 9. DMP,吉列sku
                ActionType.MOVE_DMP_QRCODE_JILIE -> return MoveDmpQrcodeJilieAction(env)
                // 10. DMP,吉列sku,点击
                ActionType.MOVE_DMP_QRCODE_JILIE_CLICK -> return MoveDmpQrcodeJilieClickAction(env)
                // 11. DMP,吉列sku，点击，加购
                ActionType.MOVE_DMP_QRCODE_JILIE_CLICK_BUY -> return MoveDmpQrcodeJilieClickBuyAction(env)
                // 12.25. 京东秒杀，点击商品
                ActionType.MOVE_JD_KILL_CLICK -> return MoveJdKillClickAction(env)
                // 13.26. 京东秒杀，点击商品，加购
                ActionType.MOVE_JD_KILL_CLICK_BUY -> return MoveJdKillClickBuyAction(env)
                // 14. 收藏吉列店铺
                ActionType.MOVE_SEARCH_JILIE_SHOP_MARK -> return MoveSearchJilieShopMarkAction(env)
                // 15. 收藏吉列引力盒单品
                ActionType.MOVE_SEARCH_JILIE_YINLIHE_MARK -> return MoveSearchJilieYinliheMarkAction(env)

                // 17.搜索剃须刀，点击博朗
                ActionType.MOVE_SEARCH_RAZOR_CLICK_BOLANG -> return MoveSearchRazorClickBolangAction(env)
                // 18. 搜索剃须刀，点击博朗，加购
                ActionType.MOVE_SEARCH_RAZOR_CLICK_BOLANG_BUY -> return MoveSearchRazorClickBolangBuyAction(env)
                // 19. 搜索博朗，点击店铺
                ActionType.MOVE_SEARCH_BOLANG_SHOP -> return MoveSearchBolangShopAction(env)
                // 20. 博朗sku
                ActionType.MOVE_BOLANG_QRCODE -> return MoveBolangQrcodeAction(env)
                // 21. 搜索博朗，点击博朗
                ActionType.MOVE_SEARCH_BOLANG_CLICK_BOLANG -> return MoveSearchBolangClickBolangAction(env)
                // 22. 博朗dmp，联名礼盒
                ActionType.MOVE_DMP_QRCODE_BOLANG -> return MoveDmpQrcodeBolangAction(env)
                // 23. 博朗dmp，联名礼盒
                ActionType.MOVE_DMP_QRCODE_BOLANG_CLICK -> return MoveDmpQrcodeBolangClickAction(env)
                // 24. 博朗dmp，联名礼盒
                ActionType.MOVE_DMP_QRCODE_BOLANG_CLICK_BUY -> return MoveDmpQrcodeBolangClickBuyAction(env)
                // 27. 收藏博朗店铺
                ActionType.MOVE_SEARCH_BOLANG_SHOP_MARK -> return MoveSearchBolangShopMarkAction(env)
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