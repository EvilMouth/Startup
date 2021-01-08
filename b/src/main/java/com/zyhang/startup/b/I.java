package com.zyhang.startup.b;

import com.zyhang.startup.StartupTask;
import com.zyhang.startup.StartupTaskRegister;
import com.zyhang.startup.a.A1;

@StartupTaskRegister(
        id = I.id,
        idDependencies = {A1.id},
        process = ":pa"
)
public class I extends StartupTask {

    public static final String id = "i.I";

    @Override
    public void startup() {

    }
}
