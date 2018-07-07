package com.example.jddata.action

import com.example.jddata.service.*

class SearchAndShopAction(searchText: String) : SearchAction(searchText) {
    init {
        appendCommand(Command(ServiceCommand.SEARCH_DATA_RANDOM_BUY).addScene(AccService.PRODUCT_LIST))
    }



}