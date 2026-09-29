package com.seleris.selarium.ward.effect;

import com.seleris.selarium.ward.WardContext;

@FunctionalInterface
public interface IWardEffect {
    void tick(WardContext context);
}

