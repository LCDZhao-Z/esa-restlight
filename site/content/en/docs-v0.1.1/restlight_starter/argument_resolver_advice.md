---
tags: ["feature"]
title: "ArgumentResolverAdvice"
linkTitle: "ArgumentResolverAdvice"
weight: 60
---

`ArgumentResolverAdvice`允许用户在`ArgumentResolver`参数解析器解析参数的前后添加业务逻辑以及修改解析后的参数。

## 接口定义

```java
public interface ArgumentResolverAdvice {

    /**
     * 在ArgumentResolver.resolve()之前被调用
     */
    void beforeResolve(AsyncRequest request, AsyncResponse response);

    /**
     * 在ArgumentResolver.resolve()之后被调用， 并使用此方法的返回值作为参数绑定到对应的Controller参数上
     */
    Object afterResolved(Object arg, AsyncRequest request, AsyncResponse response);
}
```

## 自定义`ArgumentResolverAdvice`

与`ArgumentResovler`相同， `ArgumentResolverAdvice`自定应时同样需要实现`ArgumentResolverAdviceAdapter`或`ArgumentResolverAdviceFactory`接口

### 方式1：实现 `ArgumentResolverAdviceAdapter`

接口定义

```java
public interface ArgumentResolverAdviceAdapter
        extends ArgumentResolverPredicate, ArgumentResolverAdvice, Ordered {

    @Override
    default void beforeResolve(AsyncRequest request, AsyncResponse response) {
    }

    @Override
    default Object afterResolved(Object arg, AsyncRequest request, AsyncResponse response) {
        return arg;
    }

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
```

### 方式2：实现`ArgumentResolverAdviceFactory`

接口定义

```java
public interface ArgumentResolverAdviceFactory extends ArgumentResolverPredicate, Ordered {
    /**
     * 生成ArgumentResolverAdvice
     */
    ArgumentResolverAdvice createResolverAdvice(Param param, ArgumentResolver resolver);

    @Override
    default int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
```

`ArgumentResolverAdviceAdapter`接口以及`ArgumentResolverAdviceFactory`接口与`ArgumentResolver`中的`ArgumentResolverAdapter`接口以及`ArgumentResolverFactory`接口的使用方式相同， 这里不过多赘述。

{{< alert title="Note" >}}
`ArgumentResolverAdvice`与`ArgumentResolver`生命周期是相同的, 即应用初始化的时候便会决定每个参数的`ArgumentResolverAdvice`
{{< /alert >}}
