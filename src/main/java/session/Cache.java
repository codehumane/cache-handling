package session;

/**
 * 데이터를 캐시 저장소에 저장하고 조회한다.
 */
public interface Cache {

    /**
     * 주어진 키에 값 저장
     *
     * @param key
     * @param value
     */
    void set(Object key, Object value);

    /**
     * 주어진 키의 값 조회
     *
     * @param key
     * @return
     */
    Object get(Object key);

}
