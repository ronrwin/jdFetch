package com.example.jddata.action

import com.example.jddata.Entity.ActionType
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand

class LeaderboardAction : BaseAction(ActionType.LEADERBOARD) {

    init {
        appendCommand(Command(AccService.NATIVE_COMMON, ServiceCommand.LEADERBOARD_TAB))
                .append(Command(AccService.JD_HOME, ServiceCommand.LEADERBOARD))
    }
}