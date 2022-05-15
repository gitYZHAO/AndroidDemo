#include <jni.h>
#include <string>
#include <android/log.h>
#include <filesystem>

// Android log function wrappers
static const char* kTAG = "NativeLib";
#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, kTAG, __VA_ARGS__))
#define LOGW(...) \
  ((void)__android_log_print(ANDROID_LOG_WARN, kTAG, __VA_ARGS__))
#define LOGE(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, kTAG, __VA_ARGS__))

extern "C" JNIEXPORT jstring JNICALL
Java_me_android_demo_nativelib_NativeLib_stringFromJNI(
        JNIEnv *env,
        jobject /* this */, jobject Java_object) {
    //1 . 获取 Java 对应的 Class 对象
    jclass nativeLib = env->GetObjectClass(Java_object);

    //2 . 获取 NativeLib 的 public static void getStringFromJava() 方法
    //  注意这里要使用 GetStaticMethodID 方法反射该静态方法
    jmethodID method_getStringFromJava = env->GetMethodID(nativeLib, "getStringFromJava",
                                                          "(I)Ljava/lang/String;");

    //3 . 调用 NativeLib 的 public String getStringFromJava() 方法，拿到从Java层返回的string
    const char* ss = "2...";
    va_list args;
    auto str = reinterpret_cast<jstring>(env->CallObjectMethodV(Java_object,
                                                                method_getStringFromJava,
                                                                args));

    if (str != nullptr) {
        // isCopy参数不要设置为TRUE
        std::string javaStr = env->GetStringUTFChars(str, nullptr);
        // std::string 与 char* 的转换
        LOGI("Get STRING: %s" , javaStr.data());
    } else {
        LOGE("Get STRING is null");
    }

    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}