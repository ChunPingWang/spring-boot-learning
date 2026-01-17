package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 訂單倉儲介面
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 根據訂單編號查詢
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * 根據客戶 Email 查詢訂單（分頁）
     */
    Page<Order> findByCustomerEmail(String customerEmail, Pageable pageable);

    /**
     * 根據狀態查詢訂單
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * 查詢指定時間前的待處理訂單（用於超時取消）
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :cutoffTime")
    List<Order> findByStatusAndCreatedAtBefore(@Param("status") OrderStatus status,
                                                @Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 使用 JOIN FETCH 避免 N+1 查詢問題
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
}
