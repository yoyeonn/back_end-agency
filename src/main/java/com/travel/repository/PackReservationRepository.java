package com.travel.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import com.travel.entity.PackReservation;

public interface PackReservationRepository extends JpaRepository<PackReservation, Long> {
  List<PackReservation> findByUserIdOrderByCreatedAtDesc(Long userId);
  List<PackReservation> findAllByOrderByCreatedAtDesc();
  @Query("""
    select coalesce(sum(r.totalAmount), 0)
    from PackReservation r
    where r.checkIn >= :start and r.checkIn <= :end
  """)
  double sumTotalAmountBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
    @Query("select r from PackReservation r order by r.createdAt desc")
  List<PackReservation> findRecent(Pageable pageable);
}
