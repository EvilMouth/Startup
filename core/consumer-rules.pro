# init方法需要反射调用
-keepclassmembers class com.zyhang.startup.generated.StartupLoaderInit {
    void init(***);
}
-keep class * extends com.zyhang.startup.executor.ExecutorFactory