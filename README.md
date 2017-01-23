# cache-handling

`org.springframework.cache`를 활용한 사례들 기록

- Apache Shiro의 동시성 문제를 피하기 위한 `SessionCache` 구현
    + Apache Shiro Session에 담긴 데이터가 늘어나고, 동일 세션의 동시 요청이 많은 경우 `OptionalDataException` 발생
    + Shiro Session은 매 요청마다 마지막 액세스 시간을 기록하기 때문
    + 데이터 분리를 통해 동시성 발생 가능성 낮춤 (빈번한 업데이트가 발생하는 Shiro Session에는 최소한의 데이터만 유지)
    + 데이터가 분리를 위한 방안 중 하나로 세션 데이터 직접 관리
        * Shiro Session과 1:1 대응시켜 데이터 관리
        * Shiro Session와 같은 생명주기를 가지도록 함
    + 속도 향상을 위해 `ThreadLocal` 사용
- Graph 형태로 세션 데이터 관리
