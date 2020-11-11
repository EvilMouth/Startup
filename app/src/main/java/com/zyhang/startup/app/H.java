package com.zyhang.startup.app;

import com.zyhang.startup.StartupTask;
import com.zyhang.startup.StartupTaskRegister;

@StartupTaskRegister(
        id = "h.H",
        idDependencies = {"g.G"}
)
public class H extends StartupTask {
    @Override
    public void startup() {
    }
}
