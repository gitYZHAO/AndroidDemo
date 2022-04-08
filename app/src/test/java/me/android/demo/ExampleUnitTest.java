package me.android.demo;

import android.support.v4.util.TimeUtils;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;

import org.junit.Test;

import java.io.File;
import java.util.List;

import me.android.demo.util.DateFormatHelper;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void MD5Test() {
        List<File> files = FileUtils.listFilesInDir("C:\\Work\\tanssion\\FB-Crash\\com.facebook.katana\\lib-compressed");
        for (File file :
                files) {
            System.out.println("File:" + file.getName());
            System.out.println(DateFormatHelper.formatDate(Long.toString(file.lastModified()), DateFormatHelper.DateFormat.DATE_4));

            // MD5（MessageDIgest 即信息摘要），用于生成文件散列码，是一种非常安全的单向哈希函数，它可以接受任意长度的数据，并将其转化为固定长度的数值。
            //
            // 它的具体过程如下：
            //
            // 首先创建一个新对象并通过getInstance（）函数来进行实例化和初始化，然后调用 update 方法处理数据。任何时候都可以调用 reset 方法重置摘要。
            // 一旦所有需要更新的数据都已经被更新了，最后再调用 digest 方法完成哈希计算并返回结果。
            // 示例：
            // libxplat_FBMessaging只修改了最后一个byte 变成后面的libxplat_test.so ， libxplat_test-1.so和libxplat_test.so是同一个文件，只是文件名不同。
            // 结果是 只有一个byte不一样，其MD5相差很大，但后面两个so库的MD5值一样，因为仅文件名不同。
            // File:libxplat_FBMessaging...so
            // MD5:AB3B150B6A3FE6DBCDBB3FB153CFC4E9
            // File:libxplat_test-1.so
            // MD5:F4F5AA1A26BA0BED15CF654CCE47136B
            // File:libxplat_test.so
            // MD5:F4F5AA1A26BA0BED15CF654CCE47136B
            System.out.println("MD5:" + FileUtils.getFileMD5ToString(file));


        }
    }

}