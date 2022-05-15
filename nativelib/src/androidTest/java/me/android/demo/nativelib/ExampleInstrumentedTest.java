package me.android.demo.nativelib;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useNativeAppContext() {
        // Context of the app under test.
        // Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // assertEquals("me.android.demo.nativelib.test", appContext.getPackageName());

        try {
            NativeLib nativeLib = new NativeLib();
            String s = nativeLib.stringFromJNI(nativeLib);
            Log.d("NativeLib","Get the string： " + s);

        } catch (Exception exception) {
            Log.e("NativeLib", "error:" +exception.toString());
        }

    }
}