package com.travel.controller;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.travel.repository.DestinationReservationRepository;
import com.travel.repository.HotelReservationRepository;
import com.travel.repository.PackReservationRepository;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminOrdersController {

  private final DestinationReservationRepository destRepo;
  private final HotelReservationRepository hotelRepo;
  private final PackReservationRepository packRepo;

  public AdminOrdersController(
      DestinationReservationRepository destRepo,
      HotelReservationRepository hotelRepo,
      PackReservationRepository packRepo
  ) {
    this.destRepo = destRepo;
    this.hotelRepo = hotelRepo;
    this.packRepo = packRepo;
  }

  public static class OrderRow {
    public String name;
    public String category; // Hotel / Destination / Pack
    public double amount;
    public String status;
    public LocalDateTime createdAt;

    public OrderRow(String name, String category, double amount, String status, LocalDateTime createdAt) {
      this.name = name;
      this.category = category;
      this.amount = amount;
      this.status = status;
      this.createdAt = createdAt;
    }
  }

  @GetMapping
  public ResponseEntity<?> list(
      @RequestParam(defaultValue = "ALL") String type,   // ALL / HOTEL / DESTINATION / PACK
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    Pageable pageable = PageRequest.of(0, Math.min(500, Math.max(1, size))); // fetch enough then slice

    List<OrderRow> merged = new ArrayList<>();

    if (type.equalsIgnoreCase("ALL") || type.equalsIgnoreCase("DESTINATION")) {
      merged.addAll(destRepo.findRecent(pageable).stream()
          .map(r -> new OrderRow(
              r.getDestination().getName(),
              "Destination",
              r.getTotalAmount(),
              "Confirmed",
              r.getCreatedAt()
          ))
          .toList());
    }

    if (type.equalsIgnoreCase("ALL") || type.equalsIgnoreCase("HOTEL")) {
      merged.addAll(hotelRepo.findRecent(pageable).stream()
          .map(r -> new OrderRow(
              r.getHotel().getName(),
              "Hotel",
              r.getTotalAmount(),
              "Confirmed",
              r.getCreatedAt()
          ))
          .toList());
    }

    if (type.equalsIgnoreCase("ALL") || type.equalsIgnoreCase("PACK")) {
      merged.addAll(packRepo.findRecent(pageable).stream()
          .map(r -> new OrderRow(
              r.getPack().getName(),
              "Pack",
              r.getTotalAmount(),
              "Confirmed",
              r.getCreatedAt()
          ))
          .toList());
    }

    merged.sort((a, b) -> b.createdAt.compareTo(a.createdAt));

    int from = Math.max(0, page * size);
    int to = Math.min(merged.size(), from + size);

    List<OrderRow> content = from >= merged.size() ? List.of() : merged.subList(from, to);

    Map<String, Object> res = new HashMap<>();
    res.put("ok", true);
    res.put("status", 200);
    res.put("data", Map.of(
        "items", content,
        "page", page,
        "size", size,
        "total", merged.size()
    ));

    return ResponseEntity.ok(res);
  }
}
