package com.project.KoiBookingSystem.repository;

import com.project.KoiBookingSystem.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transactions, Long> {
}
