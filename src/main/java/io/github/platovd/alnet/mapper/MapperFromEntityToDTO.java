package io.github.platovd.alnet.mapper;

import java.util.Collection;

public interface MapperFromEntityToDTO<E, D> {
    D toDTO(E entity);

    Collection<D> allToDTO(Collection<E> entities);
}
