package com.security.repository;

import com.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 使用者 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根據使用者名稱查詢
     */
    Optional<User> findByUsername(String username);

    /**
     * 根據電子郵件查詢
     */
    Optional<User> findByEmail(String email);

    /**
     * 檢查使用者名稱是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 檢查電子郵件是否存在
     */
    boolean existsByEmail(String email);
}
