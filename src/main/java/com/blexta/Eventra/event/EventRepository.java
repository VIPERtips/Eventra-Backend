package com.blexta.Eventra.event;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.blexta.Eventra.common.enums.Category;

public interface EventRepository extends JpaRepository<Event, Long> {

	Page<Event> findByCategory(Category category, Pageable pageable);

	Optional<Event> findByTitleAndLocation(String title, String location);
	@Query("SELECT e FROM Event e WHERE e.eventId NOT IN " +
		       "(SELECT r.event.eventId FROM EventRegistration r " +
		       "WHERE r.user.userId = :userId AND r.status = 'REGISTERED')")
		Page<Event> findAllExcludingUserRegisteredEvents(@Param("userId") Long userId, Pageable pageable);


}
