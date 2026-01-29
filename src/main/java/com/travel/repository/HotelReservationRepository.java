package com.travel.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import com.travel.entity.HotelReservation;

public interface HotelReservationRepository extends JpaRepository<HotelReservation, Long> {

    @Query("""
      SELECT COUNT(r) FROM HotelReservation r
      WHERE r.hotel.id = :hotelId
      AND r.checkIn < :checkOut
      AND r.checkOut > :checkIn
    """)
    long countOverlappingReservations(
        @Param("hotelId") Long hotelId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );

    List<HotelReservation> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<HotelReservation> findAllByOrderByCreatedAtDesc();
    @Query("""
    select coalesce(sum(r.totalAmount), 0)
    from HotelReservation r
    where r.checkIn >= :start and r.checkIn <= :end
  """)
  double sumTotalAmountBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
  @Query("select r from HotelReservation r order by r.createdAt desc")
List<HotelReservation> findRecent(Pageable pageable);
}
