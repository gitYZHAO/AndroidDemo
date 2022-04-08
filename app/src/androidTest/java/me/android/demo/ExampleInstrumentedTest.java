package me.android.demo;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.util.Pair;

import org.junit.Test;
import org.junit.runner.RunWith;

import me.android.demo.util.JavaCrashHandler;
import me.android.demo.util.ShellUtils;

import static org.junit.Assert.*;

import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;

import java.io.File;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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

        //Crash Handler Test Code
        // JavaCrashHandler handler = new JavaCrashHandler();
        // handler.uncaughtException(Thread.currentThread(), new IllegalArgumentException("Test"));

        //Here throw the exception can Not handle to application CrashHandler
        // throw new IllegalStateException("Throw a test exception");

        //Dump information
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand(new String[]{"dumpsys input"});
        Log.d("useAppContext", commandResult.toString());
    }


    @Test
    public void EncryptTest() {
        int keySize = 1024;
        Pair<String, String> publicPrivateKey = null;
        try {
            publicPrivateKey = genKeyPair(keySize);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String publicKey = publicPrivateKey.first;
        String privateKey = publicPrivateKey.second;
        String dataRSA = "PUBLIC";
        System.out.println("publicKeyBase64:" + publicKey);
        System.out.println("privateKeyBase64:" + privateKey);

        byte[] bytes = FileIOUtils.readFile2BytesByStream("/sdcard/plantuml-demo.png");
        byte[] rsasBytes = EncryptUtils.encryptRSA(bytes,
                EncodeUtils.base64Decode(publicKey.getBytes()),
                keySize,
                "RSA/None/PKCS1Padding");
        String encFilePath = PathUtils.getInternalAppDataPath() + "/" + "plantuml-demo-enc.png";
        String deEncFilePath = PathUtils.getInternalAppDataPath() + "/" + "plantuml-demo-deec.png";
        boolean isExistsFile = FileUtils.createOrExistsFile(encFilePath);
        if (isExistsFile) {
            // 将加密数据生成文件
            boolean rsa = FileIOUtils.writeFileFromBytesByStream(encFilePath, rsasBytes);
            Log.d("EncryptTest", "rsa:" + rsa);

            // 将解密的数据生成文件
            byte[] decryptRSA = EncryptUtils.decryptRSA(rsasBytes,
                    EncodeUtils.base64Decode(privateKey.getBytes()),
                    keySize,
                    "RSA/None/PKCS1Padding");
            if (decryptRSA != null) {
                FileIOUtils.writeFileFromBytesByStream(deEncFilePath, decryptRSA);
            }
        }

    }

    private Pair<String, String> genKeyPair(int size) throws NoSuchAlgorithmException {

        if (size == 1024) {
            return Pair.create(
                    "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCYHGvdORdwsK5i+s9rKaMPL1O5eDK2XwNHRUWaxmGB/cxLxeinJrrqdAN+mME7XtGN9bklnOR3MUBQLVnWIn/IU0pnIJY9DpPTVc7x+1zFb8UUq1N0BBo/NpUG5olxuQULuAAHZOg28pnP/Pcb5XVEvpNKL0HaWjN8pu/Dzf8gZwIDAQAB",
                    "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJgca905F3CwrmL6z2spow8vU7l4MrZfA0dFRZrGYYH9zEvF6Kcmuup0A36YwTte0Y31uSWc5HcxQFAtWdYif8hTSmcglj0Ok9NVzvH7XMVvxRSrU3QEGj82lQbmiXG5BQu4AAdk6Dbymc/89xvldUS+k0ovQdpaM3ym78PN/yBnAgMBAAECgYAFdX+pgNMGiFC53KZ1AhmIAfrPPTEUunQzqpjE5Tm6oJEkZwXiedFbeK5nbLQCnXSH07nBT9AjNvFH71i6BqLvT1l3/ezPq9pmRPriHfWQQ3/J3ASf1O9F9CkYbq/s/qqkXEFcl8PdYQV0xU/kS4jZPP+60Lv3sPkLg2DpkhM+AQJBANTl+/v6sBqqQSS0Anl5nE15Ck3XGBcq0nvATHfFkJYtG9rrXz3ZoRATLxF1iJYwGSAtirhev9W7qFayjci0ztcCQQC25/kkFbeMEWT6/kyV8wcPIog1mKy8RVB9+2l6C8AzbWBPZYtLlB7uaGSJeZBTEGfvRYzpFm5xO0JqwCfDddjxAkBmxtgM3wqg9MwaAeSn6/Nu2x4EUfBJTtzp7P19XJzeQsyNtM73ttYwQnKYhRr5FiMrC5FKTENj1QIBSJV17QNlAkAL5cUAAuWgl9UQuo/yxQ81fdKMYfUCfiPBPiRbSv5imf/Eyl8oOGdWrLW1d5HaxVttZgHHe60NcoRce0la3oSRAkAe8OqLsm9ryXNvBtZxSG+1JUvePVxpRSlJdZIAUKxN6XQE0S9aEe/IkNDBgVeiUEtop76R2NkkGtGTwzbzl0gm"
            );
        } else if (size == 2048) {
            return Pair.create(
                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjLLeJZIO7dfQKb6tHE+TlhvD1m3UdTefKvl4uNQboDXy2ztgPcksjLDXxsT+znxMBh4RpXxfVPgnrcSLewGVhTb3uXh9sWo6tvvshNaMKBTebaZePhE7grq+LHH3NILscVssK24rDSvIquZ4nUbDipF/Iscge4LwnypcCuun/3RCn4HYzXW+0YFFZC8Vq4zabIxtzzkvgZlAlvuD6tT76Uuo5kD8b36yYNALI+ZStOj283wlL8PgyyitRGaqCH+MjWYqDb5C0DN31kcoSU7ARTGWgNNAoexAdNujkBvVRFyR2cH9FpjJDu18Oa8v9uSjlRftVWPj0OQXE7vRUsrrawIDAQAB",
                    "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCMst4lkg7t19Apvq0cT5OWG8PWbdR1N58q+Xi41BugNfLbO2A9ySyMsNfGxP7OfEwGHhGlfF9U+CetxIt7AZWFNve5eH2xajq2++yE1owoFN5tpl4+ETuCur4scfc0guxxWywrbisNK8iq5nidRsOKkX8ixyB7gvCfKlwK66f/dEKfgdjNdb7RgUVkLxWrjNpsjG3POS+BmUCW+4Pq1PvpS6jmQPxvfrJg0Asj5lK06PbzfCUvw+DLKK1EZqoIf4yNZioNvkLQM3fWRyhJTsBFMZaA00Ch7EB026OQG9VEXJHZwf0WmMkO7Xw5ry/25KOVF+1VY+PQ5BcTu9FSyutrAgMBAAECggEAHJQ4i2kfnzA3GEOi5h1D3TnGjcfBYA3sRs5ltyVedyx+KAnngqVaZzmEmtto5ohY6OUysGqS8q91X9aMfm/T7zs7FnFjFqZ9Rq3lXRY3YezbQWqJuhHGBMfp2R1NGV1+qYfbcPbvx70dBZnK5id5kKv9JxNLhcsTFUGFcLJtbXXixY2CGiS/dIbFvFHGMbAz3+9l9HXaL4AS7KQXvnauwJW1a5vIAVFYZVBj0qY9Viy2vq6ShH+9pdxOSsWBt08WpxIhjkTr+ZkFck67la2Jn0SBlClB0FIygTqbAmsM3p1nqcR55jdx3hfs31rIfM1Rx5epMm48KYErb2ktowngAQKBgQDL9FEumMMagPy4+EjR1puFHNvADlAi8tIUNt1W5zKKnd+T6gYGn8nqiiy5pvwLLUp8JISmq50tMC3cgAPw+G4kIe5zoBO2EU9X6aPhMd/ScUlVdk0IzEMXa3kMAOjOInWvoevJ4cwWcBPH2aRuDg5wZdh3TpB9LQP4uQ0QHwmE3wKBgQCwmkL6rJDrNo1GNUsjw+WIsXkuS3PYJahbg/uhRdGSsX2BRIPQVCRJP7MkgaUMhZRilt1ROfQy4d2BPxTxvUiGJcKfpsW8xi39PrYWZC5TvEA839q39Uak+ISCsYtZaHk5dvzmE9nF5gv0ivjCr81N2/1KwXO8VmNofzWUqNd+9QKBgQCs39QICRgm2Ppd1qXyp1N/SuzBJ+CpHuUOmUqXpLRkZljiSVT+PGar1J8AZhfxaVxfSZzeoUxCxzm4UxIEKK9DFTfG7gKHKrj0LWfpM5siB0A/nlzBflHIAiLCF+s8/lx+mGMB5dBVnH5HwaTsXCHFB66pwgAa+hMJueDmr0gkRQKBgDKhd1Rwxvd4Y1ZejxVI43SmFOzt2t98JGFgXHLnFmdtFWNLJlNC3EhXx99Of+gwH9OIFxljeRxhXuTgFfwcXT+AceTdplExrBuvr/qJbDK7hNsu/oDBBCjlyu/BQQc4CZEtCOJZjJTNGF5avWjrh/urd1nITosPZV6fIdhl86pFAoGAfOwK0Wte6gO5glAHP9RNktDeyFJCfFH1KUFiAG7XUww6bRpL2fEAqBIcDVgsS565ihxDSbUjgQgg/Ckh2+iBrwf1K9ViO4XUuwWqRS26rn4Is/W5kbPtnC4HS5cQIH1aWi3xUMJcWxV4ZrwiMVdw91leYWC0IbXC/yrc/PBW+sE="
            );
        }

        SecureRandom secureRandom = new SecureRandom();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");

        keyPairGenerator.initialize(size, secureRandom);

        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        Key publicKey = keyPair.getPublic();

        Key privateKey = keyPair.getPrivate();

        byte[] publicKeyBytes = publicKey.getEncoded();
        byte[] privateKeyBytes = privateKey.getEncoded();

        String publicKeyBase64 = EncodeUtils.base64Encode2String(publicKeyBytes);
        String privateKeyBase64 = EncodeUtils.base64Encode2String(privateKeyBytes);

        return Pair.create(publicKeyBase64, privateKeyBase64);
    }
}
