package com.crs.iamservice.repository;

import com.crs.iamservice.entity.PasswordHistory;
import com.crs.iamservice.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.user = :user ORDER BY ph.changeAt DESC")
    List<PasswordHistory> findTopNByUser(@Param("user") User user, Pageable pageable);
}
