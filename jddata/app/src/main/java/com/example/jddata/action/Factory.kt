package com.example.jddata.action

import com.example.jddata.Entity.ActionType

class Factory {
    companion object {
        @JvmStatic fun createAction(action : String) : BaseAction? {
            return createAction(action, null)
        }

        @JvmStatic fun createAction(action : String, obj : Any?) : BaseAction? {
            when (action) {
                ActionType.SEARCH -> return NormalSearchAction(obj as String)
                ActionType.SEARCH_AND_SHOP -> return SearchAndShopAction(obj as String)
                ActionType.CART -> return CartAction()
                ActionType.HOME -> return HomeAction()
                ActionType.BRAND_KILL -> return BrandKillAction()
                ActionType.BRAND_KILL_AND_SHOP -> return BrandKillShopAction()
                ActionType.LEADERBOARD -> return LeaderboardAction()
                ActionType.JD_KILL -> return JdKillAction()
                ActionType.WORTH_BUY -> return WorthBuyAction()
                ActionType.NICE_BUY -> return NicebuyAction()
                ActionType.TYPE_KILL -> return TypeKillAction()
                ActionType.DMP -> return DmpAction()
                ActionType.DMP_AND_SHOP -> return DmpShopAction()
            }

            return null
        }

    }
}