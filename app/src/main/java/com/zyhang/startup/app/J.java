package com.zyhang.startup.app;

import android.content.Context;

import com.zyhang.startup.AndroidStartupTask;
import com.zyhang.startup.StartupTaskRegister;

import org.jetbrains.annotations.NotNull;

@StartupTaskRegister(
        id = "j.J",
        idDependencies = {"g.G"},
        priority = 1
)
public class J extends AndroidStartupTask {
    @Override
    public void startup(@NotNull Context context) {
    }
}
