package com.zyhang.startup.app;

import android.content.Context;

import com.zyhang.startup.AndroidStartupTask;
import com.zyhang.startup.StartupTaskRegister;

import org.jetbrains.annotations.NotNull;

@StartupTaskRegister(
        id = "k.K",
        idDependencies = {"g.G"},
        priority = 2
)
public class K extends AndroidStartupTask {
    @Override
    public void startup(@NotNull Context context) {
    }
}
