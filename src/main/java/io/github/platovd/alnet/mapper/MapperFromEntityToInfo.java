package io.github.platovd.alnet.mapper;

import java.util.Collection;

public interface MapperFromEntityToInfo<T, V> {
    V toInfo(T entity);

    Collection<V> allToInfo(Collection<T> entities);
}
