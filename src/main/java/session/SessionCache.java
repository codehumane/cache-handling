package session;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.SessionListenerAdapter;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.cache.CacheManager;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 세션 데이터를 지정된 캐시 영역에 저장하고 조회한다.
 */
public class SessionCache implements Cache {

    private static final String SESSION_KEY_LABEL = "SHIRO_SESSION_ID";

    private DefaultWebSessionManager sessionManager;
    private org.springframework.cache.Cache cache;
    private final CacheManager cacheManager;
    private final String cacheRegionName;

    /**
     * SessionCache 초기화
     *
     * @param cacheManager Cache 데이터 접근하기 위한 의존성
     * @param cacheRegionName Cache Region 이름
     */
    public SessionCache(CacheManager cacheManager, String cacheRegionName) {
        this.cacheManager = cacheManager;
        this.cacheRegionName = cacheRegionName;
    }

    /**
     * SHIRO SESSION 만료 시 SessionCache도 함께 evict하기 위한 의존성 주입 <br/>
     * 주입은 선택적이며, 주입 안 할 경우 만료 처리 생략됨
     *
     * @param defaultWebSessionManager
     */
    public void setDefaultWebSessionManager(DefaultWebSessionManager defaultWebSessionManager) {
        this.sessionManager = defaultWebSessionManager;
    }

    /**
     * 캐시를 사용할 준비를 마친다. <br/>
     * - cacheRegionName으로 {@link org.springframework.cache.Cache} 인스턴스화
     * - defaultWebSessionManager에 {@link SessionListenerAdapter} 등록
     */
    public void init() {
        initCacheWithRegionName();
        registerSessionExpirationListener();
    }

    private void initCacheWithRegionName() {
        this.cache = cacheManager.getCache(cacheRegionName);
    }

    private void registerSessionExpirationListener() {
        if (sessionManager == null) {
            return;
        }

        SessionListener listener = new SessionListenerAdapter() {

            @Override
            public void onExpiration(Session session) {
                CacheDataHolder.remove();
                cache.evict(session.getId());
            }

            @Override
            public void onStop(Session session) {
                CacheDataHolder.remove();
                cache.evict(session.getId());
            }
        };

        Collection<SessionListener> listeners = sessionManager.getSessionListeners();
        listeners.add(listener);
    }

    /**
     * 주어진 키에 값을 할당
     *
     * @param key
     * @param value
     */
    public void set(Object key, Object value) {
        Map<Object, Object> data = getData();
        if (data == null) {
            data = new HashMap<>();
            markCachedLocalForCurrentShiroSession(data);
        }

        data.put(key, value);
        putData(data);
    }

    /**
     * 주어진 데이터를 현재 세션의 소유로 지정
     *
     * @param data 로컬에 저장된 세션 데이터
     */
    private void markCachedLocalForCurrentShiroSession(Map<Object, Object> data) {
        data.put(SESSION_KEY_LABEL, getShiroSessionId());
    }

    private void putData(Map<Object, Object> data) {
        CacheDataHolder.set(data);
        cache.put(getShiroSessionId(), data);
    }

    /**
     * 주어진 키로 값 조회
     *
     * @param key
     * @return
     */
    public Object get(Object key) {
        Map<Object, Object> data = getData();
        if (data == null) {
            return null;
        }

        return data.get(key);
    }

    private Map<Object, Object> getData() {
        Map<Object, Object> localData = CacheDataHolder.getData();
        if (localData != null && checkLocalDataForCurrentShiroSession(localData)) {
            return localData;
        }

        org.springframework.cache.Cache.ValueWrapper valueWrapper = cache.get(getShiroSessionId());
        if (valueWrapper == null) {
            return null;
        }

        localData = (Map<Object, Object>) valueWrapper.get();
        CacheDataHolder.set(localData);
        return localData;
    }

    /**
     * 로컬에 저장된 세션 데이터가 현재 세션의 소유가 맞는지 확인
     *
     * @param data 로컬에 저장된 세션 데이터
     * @return
     */
    private boolean checkLocalDataForCurrentShiroSession(Map<Object, Object> data) {
        return getShiroSessionId().equals(data.get(SESSION_KEY_LABEL));
    }

    private Serializable getShiroSessionId() {
        return SecurityUtils.getSubject().getSession(true).getId();
    }
}
