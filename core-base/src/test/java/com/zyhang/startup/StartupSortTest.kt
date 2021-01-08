//package com.zyhang.startup
//
//import com.zyhang.startup.executor.CPUExecutor
//import com.zyhang.startup.executor.IOExecutor
//import com.zyhang.startup.model.STData
//import org.junit.Test
//
//import org.junit.Assert.*
//
///**
// * Example local unit test, which will execute on the development machine (host).
// *
// * See [testing documentation](http://d.android.com/tools/testing).
// */
//class StartupSortTest {
//
//    @Test
//    fun sort_isCorrect() {
//        StartupCore(null).run {
//            configDebug(true)
//
//            register(Task4())
//            register(Task2())
//            register(Task3())
//            register(Task1())
//
//            startup()
//        }
//
//        assertEquals(4, 2 + 2)
//    }
//
//    class Task1 : STData("Task1", emptyArray()) {
//        override fun startup() {
//            Thread.sleep(100)
//        }
//    }
//
//    class Task2 : STData(
//        "Task2",
//        arrayOf("Task1"),
//        executorFactory = IOExecutor.Factory::class.java,
//        blockWhenAsync = true
//    ) {
//        override fun startup() {
//            Thread.sleep(200)
//        }
//    }
//
//    class Task3 : STData(
//        "Task3", arrayOf("Task1", "Task2"),
//        executorFactory = CPUExecutor.Factory::class.java,
//        blockWhenAsync = true
//    ) {
//        override fun startup() {
//            Thread.sleep(300)
//        }
//    }
//
//    class Task4 : STData(
//        "Task4", arrayOf("Task1", "Task2", "Task3"),
//        executorFactory = IOExecutor.Factory::class.java,
//        blockWhenAsync = true
//    ) {
//        override fun startup() {
//            Thread.sleep(400)
//        }
//    }
//}