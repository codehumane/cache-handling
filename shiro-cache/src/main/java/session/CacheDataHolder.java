package session;

import java.util.Map;

public class CacheDataHolder {

    private static ThreadLocal<Map<Object, Object>> threadLocal = new ThreadLocal<Map<Object, Object>>();

    public static Map<Object, Object> getData() {
        return threadLocal.get();
    }

    public static void set(Map<Object, Object> userSession) {
        threadLocal.set(userSession);
    }

    public static void remove() {
        threadLocal.remove();
    }
}
