package com.travel.repository;

import com.travel.entity.DestinationReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface DestinationReservationRepository extends JpaRepository<DestinationReservation, Long> {
  List<DestinationReservation> findByUserIdOrderByCreatedAtDesc(Long userId);
  List<DestinationReservation> findAllByOrderByCreatedAtDesc();
   @Query("""
    select coalesce(sum(r.totalAmount), 0)
    from DestinationReservation r
    where r.checkIn >= :start and r.checkIn <= :end
  """)
  double sumTotalAmountBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
  @Query("select r from DestinationReservation r order by r.createdAt desc")
List<DestinationReservation> findRecent(Pageable pageable);
}
