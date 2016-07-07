package session;

import javax.servlet.*;
import java.io.IOException;

/**
 * SessionCache를 사용하기 위해 필요한 작업들을 진행합니다. <br/>
 * 필터의 순서는 세션을 더 이상 사용하지 않는 시점 이후로 추가하면 됩니다.
 */
public class SessionCacheFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        return;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        filterChain.doFilter(servletRequest, servletResponse);
        CacheDataHolder.remove();
    }

    @Override
    public void destroy() {
        return;
    }
}
