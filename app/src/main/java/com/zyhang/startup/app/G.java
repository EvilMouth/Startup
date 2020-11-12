package com.zyhang.startup.app;

import android.content.Context;

import com.zyhang.startup.AndroidStartupTask;
import com.zyhang.startup.StartupTaskRegister;
import com.zyhang.startup.executor.IOExecutor;

import org.jetbrains.annotations.NotNull;

@StartupTaskRegister(id = "g.G", executorFactory = IOExecutor.Factory.class)
public class G extends AndroidStartupTask {
    @Override
    public void startup(@NotNull Context context) {
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
