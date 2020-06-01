package com.example.anhquoc.mycustom;

import com.example.anhquoc.mycustom.navigation.DefaultNavigationStrategy;

/**
 * Copyright (C) 2020, VNG Corporation.
 * Created by quocha2
 * On 04/05/2020
 */
enum Injector {

    inst;

    private SimpleNavigator mNavigationFacade;

    private int mContainerId;

    public static void setContainerId(int containerId) {
        inst.mContainerId = containerId;
    }

    public static SimpleNavigator getNavigator() {
        if (inst.mNavigationFacade != null) {
            return inst.mNavigationFacade;
        } else {
            return inst.mNavigationFacade = new SimpleNavigator(new DefaultNavigationStrategy(inst.mContainerId));
        }
    }
}
