package com.zyhang.startup.app;

import com.zyhang.startup.StartupTask;
import com.zyhang.startup.StartupTaskRegister;
import com.zyhang.startup.executor.IOExecutor;

@StartupTaskRegister(id = "g.G", executorFactory = IOExecutor.Factory.class)
public class G extends StartupTask {
    @Override
    public void startup() {
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
