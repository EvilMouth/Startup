package com.zyhang.startup.app;

import android.content.Context;

import com.zyhang.startup.AndroidStartupTask;
import com.zyhang.startup.StartupTaskRegister;

import org.jetbrains.annotations.NotNull;

@StartupTaskRegister(
        id = "m.M"
)
public class M extends AndroidStartupTask {
    @Override
    public void startup(@NotNull Context context) {
    }
}
