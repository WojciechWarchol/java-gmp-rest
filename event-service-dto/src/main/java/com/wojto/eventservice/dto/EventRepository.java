package com.wojto.eventservice.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAll();

    Optional<Event> findById(Long aLong);

    Page<Event> findAll(Pageable pageable);

    Page<Event> findByTitleContaining(String title, Pageable pageable);

    boolean existsById(Long aLong);

    Event save(Event event);

    void deleteById(Long aLong);
}
