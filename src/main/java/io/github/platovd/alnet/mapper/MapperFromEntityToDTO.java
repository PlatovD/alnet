package io.github.platovd.alnet.mapper;

import java.util.Collection;

/**
 * Общий интерфейс для мапперов, преобразующих сущности в DTO.
 * Определяет стандартные методы для преобразования единичных объектов и коллекций.
 *
 * @param <E> тип сущности (Entity)
 * @param <D> тип DTO (Data Transfer Object)
 *
 * @author PlatovD
 * @version 1.0
 */
public interface MapperFromEntityToDTO<E, D> {

    /**
     * Преобразует сущность в DTO.
     *
     * @param entity сущность для преобразования
     * @return объект DTO с данными из сущности
     */
    D toDTO(E entity);

    /**
     * Преобразует коллекцию сущностей в коллекцию DTO.
     *
     * @param entities коллекция сущностей для преобразования
     * @return коллекция объектов DTO
     */
    Collection<D> allToDTO(Collection<E> entities);
}