package com.travel.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.travel.dto.ContactMessageDTO;
import com.travel.entity.ContactMessage;
import com.travel.service.ContactService;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<?> send(@Valid @RequestBody ContactMessageDTO dto) {

        ContactMessage saved = contactService.create(dto);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Message received",
                "id", saved.getId()
        ));
    }
}
