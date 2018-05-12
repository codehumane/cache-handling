package data;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.SessionListenerAdapter;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Component
public class CacheGraphClient {

    private final DefaultWebSessionManager sessionManager;
    private final CacheManager cacheManager;
    private final String cacheRegionName;
    private CacheGraph<Serializable> graph;

    public CacheGraphClient(DefaultWebSessionManager sessionManager,
                            CacheManager cacheManager,
                            String cacheRegionName) {

        this.sessionManager = sessionManager;
        this.cacheManager = cacheManager;
        this.cacheRegionName = cacheRegionName;
    }

    @PostConstruct
    public void init() {
        Cache cache = cacheManager.getCache(cacheRegionName);
        if (cache == null) {
            graph = new CacheGraph<>(null);
        } else {
            graph = new CacheGraph<>(cache);
        }
    }

    @PostConstruct
    public void setSessionListener() {
        if (sessionManager == null) {
            return;
        }

        SessionListener listener = new SessionListenerAdapter() {

            @Override
            public void onExpiration(Session session) {
                removeLink(session.getId());
            }

            @Override
            public void onStop(Session session) {
                removeLink(session.getId());
            }
        };

        Collection<SessionListener> listeners = sessionManager.getSessionListeners();
        listeners.add(listener);
    }

    /**
     * 주어진 노드를 연결
     *
     * @param from 출발 노드
     * @param to   도착 노드
     */
    public void addLink(Serializable from, Serializable to) {
        graph.addEdge(from, to);
    }

    /**
     * 주어진 노드의 모든 연결 제거
     *
     * @param from 대상 노드
     */
    @SuppressWarnings("WeakerAccess")
    public void removeLink(Serializable from) {
        List<Serializable> adjacentList = graph.getAdjacentVertexes(from);
        for (Serializable adjacent : adjacentList) {
            graph.removeEdge(from, adjacent);
        }
    }

    /**
     * 주어진 노드에 연결된 모든 노드 반환
     *
     * @param from 대상 노드
     * @return 연결된 노드 목록
     */
    public List<Serializable> getLinkedFrom(Serializable from) {
        return graph.getAdjacentVertexes(from);
    }

}

