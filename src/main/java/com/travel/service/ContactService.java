package com.travel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travel.dto.ContactMessageDTO;
import com.travel.entity.ContactMessage;
import com.travel.repository.ContactMessageRepository;

@Service
public class ContactService {

    private final ContactMessageRepository repository;
    private final BrevoEmailService emailService;

    public ContactService(ContactMessageRepository repository, BrevoEmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    @Transactional
    public ContactMessage create(ContactMessageDTO dto) {
        ContactMessage entity = new ContactMessage();
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setSubject(dto.getSubject());
        entity.setMessage(dto.getMessage());

        ContactMessage saved = repository.save(entity);

        // Send confirmation email (after save)
        emailService.sendContactReceivedEmail(saved.getEmail(), saved.getName());

        return saved;
    }
}
