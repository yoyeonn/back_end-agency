package com.travel.repository;

import com.travel.entity.Packs;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacksRepository extends JpaRepository<Packs, Long> {
    List<Packs> findAllByDeletedFalse();

    java.util.Optional<Packs> findByIdAndDeletedFalse(Long id);
}
