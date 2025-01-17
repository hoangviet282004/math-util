package com.project.KoiBookingSystem.repository;

import com.project.KoiBookingSystem.entity.Account;
import com.project.KoiBookingSystem.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // lấy Account của người dùng
    Account findAccountByUsername(String username);

    Account findAccountByUserID(String userID);

    Account findAccountByRole(Role role);

    long countByRole(Role role);

    List<Account> findAccountByStatusTrue();
}
