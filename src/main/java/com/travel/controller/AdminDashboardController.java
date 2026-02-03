package com.travel.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.travel.repository.DestinationReservationRepository;
import com.travel.repository.HotelReservationRepository;
import com.travel.repository.PackReservationRepository;
import com.travel.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminDashboardController {

  private final UserRepository userRepo;
  private final DestinationReservationRepository destRepo;
  private final HotelReservationRepository hotelRepo;
  private final PackReservationRepository packRepo;

  public AdminDashboardController(
      UserRepository userRepo,
      DestinationReservationRepository destRepo,
      HotelReservationRepository hotelRepo,
      PackReservationRepository packRepo
  ) {
    this.userRepo = userRepo;
    this.destRepo = destRepo;
    this.hotelRepo = hotelRepo;
    this.packRepo = packRepo;
  }

  @GetMapping
  public ResponseEntity<?> getDashboard() {

    long customers = userRepo.count();

    //Orders = destination + hotel + pack
    long orders = destRepo.count() + hotelRepo.count() + packRepo.count();

    // Monthly sales (last 12 months)
    List<Map<String, Object>> monthlySales = buildMonthlySalesLast12Months();

    // Target + revenue
    double target = 20000.0;
    double revenueThisMonth = revenueForMonth(YearMonth.now());

    // Today revenue
    double todayRevenue = revenueForDay(LocalDate.now());

    double targetPercent = target > 0 ? (revenueThisMonth / target) * 100.0 : 0.0;

    Map<String, Object> data = new HashMap<>();
    data.put("customers", customers);
    data.put("orders", orders);

    data.put("monthlySales", monthlySales);
    data.put("target", target);
    data.put("revenueThisMonth", revenueThisMonth);
    data.put("todayRevenue", todayRevenue);
    data.put("targetPercent", Math.min(100.0, Math.max(0.0, targetPercent)));

    data.put("statistics", buildLast7DaysRevenue());

    data.put("recentOrders", buildRecentOrders(5));

    Map<String, Object> res = new HashMap<>();
    res.put("ok", true);
    res.put("status", 200);
    res.put("message", "Dashboard data");
    res.put("data", data);

    return ResponseEntity.ok(res);
  }

  private List<Map<String, Object>> buildMonthlySalesLast12Months() {
    List<Map<String, Object>> out = new ArrayList<>();
    YearMonth now = YearMonth.now();

    for (int i = 11; i >= 0; i--) {
      YearMonth ym = now.minusMonths(i);
      double amount = revenueForMonth(ym);

      Map<String, Object> row = new HashMap<>();
      row.put("label", ym.getMonth().name().substring(0, 3)); // "JAN"
      row.put("value", amount);
      out.add(row);
    }
    return out;
  }

  private double revenueForMonth(YearMonth ym) {
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();

    double dest = destRepo.sumTotalAmountBetween(start, end);
    double hotel = hotelRepo.sumTotalAmountBetween(start, end);
    double pack = packRepo.sumTotalAmountBetween(start, end);

    return dest + hotel + pack;
  }

  private List<Map<String, Object>> buildLast7DaysRevenue() {
    List<Map<String, Object>> out = new ArrayList<>();
    LocalDate today = LocalDate.now();

    for (int i = 6; i >= 0; i--) {
      LocalDate d = today.minusDays(i);

      Map<String, Object> row = new HashMap<>();
      row.put("label", d.toString());
      row.put("value", revenueForDay(d));
      out.add(row);
    }
    return out;
  }

  private double revenueForDay(LocalDate d) {
    return destRepo.sumTotalAmountBetween(d, d)
        + hotelRepo.sumTotalAmountBetween(d, d)
        + packRepo.sumTotalAmountBetween(d, d);
  }

  static class RecentOrder {
  public String name;       // product name shown in table
  public String category;   // Hotel / Destination / Pack
  public double amount;     // totalAmount
  public String status;     // Confirmed (for now)
  public LocalDateTime createdAt;

  public RecentOrder(String name, String category, double amount, String status, LocalDateTime createdAt) {
    this.name = name;
    this.category = category;
    this.amount = amount;
    this.status = status;
    this.createdAt = createdAt;
  }
}

private List<RecentOrder> buildRecentOrders(int limit) {
  Pageable p = PageRequest.of(0, limit);

  // fetch top "limit" from each table
  var dest = destRepo.findRecent(p).stream()
      .map(r -> new RecentOrder(
          r.getDestination().getName(),
          "Destination",
          r.getTotalAmount(),
          "Confirmed",
          r.getCreatedAt()
      ))
      .toList();

  var hotels = hotelRepo.findRecent(p).stream()
      .map(r -> new RecentOrder(
          r.getHotel().getName(),
          "Hotel",
          r.getTotalAmount(),
          "Confirmed",
          r.getCreatedAt()
      ))
      .toList();

  var packs = packRepo.findRecent(p).stream()
      .map(r -> new RecentOrder(
          r.getPack().getName(),
          "Pack",
          r.getTotalAmount(),
          "Confirmed",
          r.getCreatedAt()
      ))
      .toList();

  // merge + sort by createdAt desc
  List<RecentOrder> merged = new ArrayList<>();
  merged.addAll(dest);
  merged.addAll(hotels);
  merged.addAll(packs);

  merged.sort((a, b) -> b.createdAt.compareTo(a.createdAt));

  // take top limit
  return merged.size() > limit ? merged.subList(0, limit) : merged;
}

}
