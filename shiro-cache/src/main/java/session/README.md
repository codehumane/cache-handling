# Apache Shiro의 동시성 문제를 피하기 위한 SessionCache 구현

자세한 내용은 [여기](http://codehumane.github.io/2017/07/15/OptionalDataException/) 참고.

## 현상

1. [Apache Shiro Session](https://shiro.apache.org/static/1.2.3/apidocs/src-html/org/apache/shiro/session/mgt/SimpleSession.html)에 담긴 데이터가 늘어나고,
2. 동일 세션의 동시 요청이 많은 경우 [OptionalDataException](https://docs.oracle.com/javase/7/docs/api/java/io/OptionalDataException.html) 발생.
3. [여기의 Oracle 문서](https://docs.oracle.com/javase/7/docs/api/java/io/OptionalDataException.html)에 따르면 아래 경우에 발생.

> 1. An attempt was made to read an object when the next element in the stream is primitive data. In this case, the OptionalDataException’s length field is set to the number of bytes of primitive data immediately readable from the stream, and the eof field is set to false.
>
> 2. An attempt was made to read past the end of data consumable by a class-defined readObject or readExternal method. In this case, the OptionalDataException’s eof field is set to true, and the length field is set to 0.

- 이를 실험해 볼 수 있는 코드는 [여기](https://github.com/codehumane/troubleshoot-java/blob/master/optional-data-exception/src/OptionalDataExceptionTest.java)에 등록함.

## 원인

1. HTTP 요청이 들어올 때 마다, `SimpleSession` 업데이트가 발생함.
2. 이는 Apache Shiro의 [Session Timeout](https://shiro.apache.org/session-management.html#session-timeout) 기능 때문임.
3. 매 HTTP 요청마다 세션 데이터에 대한 접근이 발생하고,
4. 이 때 마다 `SimpleSession`의 `lastAccessTime` 필드가 현재시간으로 갱신됨.
5. 그리고 이를 저장소(이 경우에는 캐시 서버)에 반영하기 위해 write(serialization) 발생함.
6. 자세한 내용은 [`AbstractShiroFilter`의 `updateSessionLastAccessTime`](https://shiro.apache.org/static/1.2.3/apidocs/src-html/org/apache/shiro/web/servlet/AbstractShiroFilter.html#line.307)을 참고.
7. 당시의 어플리케이션은 동일한 세션을 사용하는 여러 개의 요청을 동시에 처리해야 하는 구조였음.
8. 따라서, 동일한 세션 데이터에 대한 write 도중 read가 발생할 수 있음.

## 해결

1. Martin Fowler의 [PoEAA](https://martinfowler.com/eaaCatalog/)에 따르면, 동시성 문제가 발생하는 조건을 다음과 같이 정의함.

> 둘 이상의 사용자가 동일한 데이터를 사용하려 하는 경우

2. 다시 말해서, 대상 데이터에 대해 한 번에 한 사용자만 접근을 허용하거나, 데이터를 읽기 전용으로 만들면 동시성 발생 X.
3. 전자는 격리<sup>Isolation</sup>, 후자는 불변성<sup>Immutable</sup>.
4. 여러 방법이 있었지만 가장 현실적인 방법으로 동시성 문제가 되는 데이터 자체를 없애기로 함.
5. 문제가 되던 데이터는 `SimpleSession`의 `attributes`라는 어플리케이션의 선택적 데이터.
6. 이를 `SimpleSession`과 1:1로 대응시키되, 별도의 저장소에서 직접 관리하기로.
7. 더불어, [SessionListenerAdapter](http://shiro.apache.org/static/1.3.2/apidocs/org/apache/shiro/session/SessionListenerAdapter.html)를 활용해 동일한 생명주기를 가지게 함.
8. 성능 개선을 위해 [ThreadLocal](https://docs.oracle.com/javase/7/docs/api/java/lang/ThreadLocal.html) 활용.
