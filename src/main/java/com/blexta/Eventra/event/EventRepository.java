package com.blexta.Eventra.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blexta.Eventra.common.enums.Category;

public interface EventRepository extends JpaRepository<Event, Long> {

	Page<Event> findByCategory(Category category, Pageable pageable);

}
