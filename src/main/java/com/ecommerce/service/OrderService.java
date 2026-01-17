package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.OrderItemRequest;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 訂單服務
 *
 * 展示複雜業務邏輯與事務管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    /**
     * 創建訂單
     *
     * @Transactional: 整個方法在同一個事務中執行
     * - 如果任何步驟失敗（如庫存不足），整個訂單創建會回滾
     */
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        log.info("創建訂單，客戶: {}", request.getCustomerEmail());

        Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .customerName(request.getCustomerName())
            .customerEmail(request.getCustomerEmail())
            .shippingAddress(request.getShippingAddress())
            .status(OrderStatus.PENDING)
            .build();

        // 處理訂單項目
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("商品", "id", itemRequest.getProductId()));

            // 檢查並扣減庫存
            if (!product.hasStock(itemRequest.getQuantity())) {
                throw new InsufficientStockException(
                    product.getId(),
                    itemRequest.getQuantity(),
                    product.getStockQuantity()
                );
            }
            product.decreaseStock(itemRequest.getQuantity());
            productRepository.save(product);

            // 創建訂單項目
            OrderItem orderItem = OrderItem.fromProduct(product, itemRequest.getQuantity());
            order.addItem(orderItem);
        }

        // 計算訂單總金額
        order.calculateTotalAmount();

        Order saved = orderRepository.save(order);
        log.info("訂單創建成功，訂單編號: {}", saved.getOrderNumber());

        return OrderDTO.fromEntity(saved);
    }

    /**
     * 根據 ID 查詢訂單
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        log.debug("查詢訂單 ID: {}", id);
        Order order = orderRepository.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("訂單", "id", id));
        return OrderDTO.fromEntity(order);
    }

    /**
     * 根據訂單編號查詢訂單
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderByNumber(String orderNumber) {
        log.debug("查詢訂單編號: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("訂單", "orderNumber", orderNumber));
        return OrderDTO.fromEntity(order);
    }

    /**
     * 查詢客戶訂單（分頁）
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByCustomer(String customerEmail, Pageable pageable) {
        log.debug("查詢客戶 {} 的訂單", customerEmail);
        return orderRepository.findByCustomerEmail(customerEmail, pageable)
            .map(OrderDTO::fromEntity);
    }

    /**
     * 更新訂單狀態
     */
    @Transactional
    public OrderDTO updateOrderStatus(Long id, OrderStatus newStatus) {
        log.info("更新訂單 {} 狀態為: {}", id, newStatus);

        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("訂單", "id", id));

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        log.info("訂單狀態更新成功，訂單編號: {}", saved.getOrderNumber());
        return OrderDTO.fromEntity(saved);
    }

    /**
     * 取消訂單
     *
     * @Transactional(rollbackFor = Exception.class): 任何異常都會導致回滾
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO cancelOrder(Long id) {
        log.info("取消訂單 ID: {}", id);

        Order order = orderRepository.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("訂單", "id", id));

        if (order.getStatus() == OrderStatus.SHIPPED ||
            order.getStatus() == OrderStatus.DELIVERED ||
            order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("訂單已出貨，無法取消");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("訂單已被取消");
        }

        // 恢復庫存
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.increaseStock(item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        log.info("訂單取消成功，訂單編號: {}", saved.getOrderNumber());
        return OrderDTO.fromEntity(saved);
    }

    /**
     * 取消超時未付款訂單
     */
    @Transactional
    public int cancelUnpaidOrders(int hoursThreshold) {
        log.info("開始取消超過 {} 小時未付款的訂單", hoursThreshold);

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursThreshold);
        List<Order> unpaidOrders = orderRepository.findByStatusAndCreatedAtBefore(
            OrderStatus.PENDING, cutoffTime);

        int cancelledCount = 0;
        for (Order order : unpaidOrders) {
            try {
                cancelOrder(order.getId());
                cancelledCount++;
            } catch (Exception e) {
                log.error("取消訂單失敗: {}", order.getOrderNumber(), e);
            }
        }

        log.info("已取消 {} 筆超時未付款訂單", cancelledCount);
        return cancelledCount;
    }

    /**
     * 生成訂單編號
     */
    private String generateOrderNumber() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + dateStr + "-" + uuid;
    }
}
