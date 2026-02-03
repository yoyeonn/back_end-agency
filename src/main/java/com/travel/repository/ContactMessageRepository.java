package com.travel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.entity.ContactMessage;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}
