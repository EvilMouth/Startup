package com.zyhang.startup.b;

import com.zyhang.startup.StartupTask;
import com.zyhang.startup.StartupTaskRegister;

@StartupTaskRegister(
        id = "i.I",
        idDependencies = {"g.G", "h.H"}
)
public class I extends StartupTask {
    @Override
    public void startup() {

    }
}
