package me.android.demo.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;

import com.blankj.utilcode.util.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class JavaCrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String LOG_FILE_NAME_SUFFIX = ".log";
    private static final String CRASH_FOLDER_NAME = "crash";
    private static Thread.UncaughtExceptionHandler mDefaultCrashHandler;
    private static Context mContext;

    private JavaCrashHandler() {
    }

    public static void init(@NonNull Context context) {
        // 默认为：RuntimeInit#KillApplicationHandler
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        mContext = context.getApplicationContext();
        Thread.setDefaultUncaughtExceptionHandler(new JavaCrashHandler());
    }

    /*** 当程序中有未被捕获的异常，系统将会调用这个方法 ** @param t 出现未捕获异常的线程 * @param e 得到异常信息 */
    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        try {
            // 自行处理：保存本地
            String file = dealException(thread, throwable);
            // 上传服务器
            // ......
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            // 交给系统默认程序处理
            if (mDefaultCrashHandler != null) {
                mDefaultCrashHandler.uncaughtException(thread, throwable);
            }
        }
    }

    /*** 导出异常信息到SD卡 ** @param e */
    private String dealException(Thread thread, Throwable throwable) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String crashFilePathName = mContext.getExternalCacheDir().getAbsoluteFile()
                + CRASH_FOLDER_NAME + File.separator
                + time + LOG_FILE_NAME_SUFFIX;
        String dirName = FileUtils.getDirName(crashFilePathName);
        if (FileUtils.createOrExistsDir(dirName)) {
            // 删除旧文件
            List<File> files = FileUtils.listFilesInDir(dirName);
            for (File file :
                    files) {
                FileUtils.delete(file);
            }

            // 生成新文件
            if (FileUtils.createOrExistsFile(crashFilePathName)) {
                return crashFilePathName;
            }
        }
        return null;
    }

    private String getPhoneInfo() {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = null;
        StringBuilder sb = new StringBuilder();

        try {
            pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);

            // App版本
            sb.append("App Version: ");
            sb.append(pi.versionName);
            sb.append("_");
            sb.append(pi.versionCode + "\n");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Android版本号
        sb.append("OS Version: ");
        sb.append(Build.VERSION.RELEASE);
        sb.append("_");
        sb.append(Build.VERSION.SDK_INT + "\n");

        // 手机制造商
        sb.append("Vendor: ");
        sb.append(Build.MANUFACTURER + "\n");

        // 手机型号
        sb.append("Model: ");
        sb.append(Build.MODEL + "\n");

        // CPU架构
        sb.append("CPU: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sb.append(Arrays.toString(Build.SUPPORTED_ABIS));
        } else {
            sb.append(Build.CPU_ABI);
        }
        return sb.toString();
    }
}
