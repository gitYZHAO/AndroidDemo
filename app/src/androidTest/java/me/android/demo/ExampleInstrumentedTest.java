package me.android.demo;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import me.android.demo.util.JavaCrashHandler;
import me.android.demo.util.ShellUtils;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        //assertEquals("me.android.demo", appContext.getPackageName());

        // Crash Handler
        //JavaCrashHandler handler = new JavaCrashHandler();
        //handler.uncaughtException(Thread.currentThread(), new IllegalArgumentException("Test"));

        ShellUtils.CommandResult commandResult = ShellUtils.execCommand(new String[]{"dumpsys input"});
        Log.d("useAppContext", commandResult.toString());
    }
}
