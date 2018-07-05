package com.example.jddata.action;

import com.example.jddata.Entity.ActionType;
import com.example.jddata.service.BaseAction;

public class SearchAction extends BaseAction {
    public String searchText;
    public SearchAction() {
        super(ActionType.SEARCH);
    }
}
