package com.zyhang.startup.b;

import com.zyhang.startup.StartupTask;
import com.zyhang.startup.StartupTaskRegister;

@StartupTaskRegister(
        id = "i.I",
        idDependencies = {"g.G", "h.H"}
)
public class I implements StartupTask {
    @Override
    public void startup() {

    }
}
