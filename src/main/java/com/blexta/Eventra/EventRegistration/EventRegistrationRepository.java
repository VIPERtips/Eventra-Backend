package com.blexta.Eventra.EventRegistration;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blexta.Eventra.common.enums.RegistrationStatus;
import com.blexta.Eventra.event.Event;
import com.blexta.Eventra.user.User;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    boolean existsByUserAndEvent(User attendee, Event event);

    Page<EventRegistration> findByUser(User user, Pageable pageable);

    Page<EventRegistration> findByEvent(Event event, Pageable pageable);

	long countByEventAndStatus(Event event, RegistrationStatus registered);

	long countByEvent(Event event);
}
