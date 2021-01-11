package demo.sql.util;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;

import java.util.Hashtable;

public class DBPasswordUtil {
    private static Hashtable<String, String> hashTalbe = new Hashtable<>();

    public interface IGetDBPasswordCenter extends Library {

        IGetDBPasswordCenter INSTANCE = (IGetDBPasswordCenter) Native.loadLibrary("CryptoCPP", IGetDBPasswordCenter.class);

        int GetCredential(String paramString1, String paramString2, Memory paramMemory, int paramInt);
    }

    public static String GetPassword(String dbSource, String userID) {

        String key = dbSource + '\\' + userID;
        if (hashTalbe.containsKey(key)) {
            return hashTalbe.get(key);
        }
        try {
            return GetLastedPassword(dbSource, userID);
        } catch (InterruptedException e) {
            return "";
        }
    }

    /**
     *
     */
    public static String GetLastedPassword(String dbSource, String userID) throws InterruptedException {

        Memory memory = new Memory(50L);
        int length = 50;
        int result = IGetDBPasswordCenter.INSTANCE.GetCredential(dbSource, userID, memory, length);
        int i = 0;
        while (i < 3) {
            if (result <= 0) {
                result = IGetDBPasswordCenter.INSTANCE.GetCredential(dbSource, userID, memory, length);
            }
            if (result > memory.size()) {
                memory.clear();
                memory = new Memory(result + 5);
                length = result + 5;
                result = IGetDBPasswordCenter.INSTANCE.GetCredential(dbSource, userID, memory, length);
            }
            if (result > 0) {
                String key = dbSource + '\\' + userID;
                String password = memory.getString(0L);
                hashTalbe.put(key, password);
                return password;
            }
            i++;

            Thread.sleep(5000L);
        }
        return "";
    }
}
