package com.zyhang.startup.plugin

class StartupCompatIssue(s: String = "oops, compat issue! please contact me.") : IllegalStateException(s)