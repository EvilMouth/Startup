package com.zyhang.startup.app;

import android.content.Context;

import com.zyhang.startup.AndroidStartupTask;
import com.zyhang.startup.StartupTaskRegister;

import org.jetbrains.annotations.NotNull;

@StartupTaskRegister(
        id = "h.H",
        idDependencies = {"g.G"}
)
public class H extends AndroidStartupTask {
    @Override
    public void startup(@NotNull Context context) {
    }
}
