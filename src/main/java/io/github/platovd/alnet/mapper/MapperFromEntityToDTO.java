package io.github.platovd.alnet.mapper;

import java.util.Collection;

public interface MapperFromEntityToDTO<T, V> {
    V toDTO(T entity);

    Collection<V> allToDTO(Collection<T> entities);
}
