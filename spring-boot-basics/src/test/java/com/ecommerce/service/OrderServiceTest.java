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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 訂單服務單元測試
 *
 * 展示複雜業務邏輯的測試方式
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("訂單服務單元測試")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
            .id(1L)
            .name("測試商品")
            .price(new BigDecimal("1000"))
            .stockQuantity(50)
            .active(true)
            .build();
    }

    @Nested
    @DisplayName("創建訂單測試")
    class CreateOrderTests {

        @Test
        @DisplayName("創建訂單 - 成功")
        void createOrder_ShouldCreateOrderAndDeductStock() {
            // Arrange
            CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("王小明")
                .customerEmail("test@example.com")
                .shippingAddress("台北市信義區")
                .items(List.of(
                    OrderItemRequest.builder()
                        .productId(1L)
                        .quantity(2)
                        .build()
                ))
                .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });

            // Act
            OrderDTO result = orderService.createOrder(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getCustomerName()).isEqualTo("王小明");
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.getItems()).hasSize(1);

            // 驗證庫存被扣減
            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(productCaptor.capture());
            assertThat(productCaptor.getValue().getStockQuantity()).isEqualTo(48);  // 50 - 2

            // 驗證訂單被保存
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("創建訂單 - 計算正確的總金額")
        void createOrder_ShouldCalculateCorrectTotalAmount() {
            // Arrange
            Product product1 = Product.builder()
                .id(1L).name("商品A").price(new BigDecimal("100")).stockQuantity(10).build();
            Product product2 = Product.builder()
                .id(2L).name("商品B").price(new BigDecimal("200")).stockQuantity(10).build();

            CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("客戶")
                .customerEmail("test@example.com")
                .shippingAddress("地址")
                .items(List.of(
                    OrderItemRequest.builder().productId(1L).quantity(3).build(),  // 100 * 3 = 300
                    OrderItemRequest.builder().productId(2L).quantity(2).build()   // 200 * 2 = 400
                ))
                .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setId(1L);
                return o;
            });

            // Act
            OrderDTO result = orderService.createOrder(request);

            // Assert: 總金額應為 300 + 400 = 700
            assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("700"));
        }

        @Test
        @DisplayName("創建訂單 - 庫存不足時應拋出異常")
        void createOrder_WhenInsufficientStock_ShouldThrowException() {
            // Arrange
            testProduct.setStockQuantity(5);  // 只有 5 件庫存

            CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("客戶")
                .customerEmail("test@example.com")
                .shippingAddress("地址")
                .items(List.of(
                    OrderItemRequest.builder()
                        .productId(1L)
                        .quantity(10)  // 要買 10 件
                        .build()
                ))
                .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            // Act & Assert
            assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("庫存不足");

            // 驗證訂單沒有被保存
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("創建訂單 - 商品不存在時應拋出異常")
        void createOrder_WhenProductNotFound_ShouldThrowException() {
            // Arrange
            CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("客戶")
                .customerEmail("test@example.com")
                .shippingAddress("地址")
                .items(List.of(
                    OrderItemRequest.builder()
                        .productId(999L)
                        .quantity(1)
                        .build()
                ))
                .build();

            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("商品");
        }
    }

    @Nested
    @DisplayName("取消訂單測試")
    class CancelOrderTests {

        @Test
        @DisplayName("取消訂單 - 成功恢復庫存")
        void cancelOrder_ShouldRestoreStock() {
            // Arrange
            OrderItem orderItem = OrderItem.builder()
                .product(testProduct)
                .productName("測試商品")
                .quantity(5)
                .unitPrice(new BigDecimal("1000"))
                .build();

            Order order = Order.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .status(OrderStatus.PENDING)
                .items(List.of(orderItem))
                .build();
            orderItem.setOrder(order);

            testProduct.setStockQuantity(45);  // 已扣減過

            when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            OrderDTO result = orderService.cancelOrder(1L);

            // Assert
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);

            // 驗證庫存被恢復
            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(productCaptor.capture());
            assertThat(productCaptor.getValue().getStockQuantity()).isEqualTo(50);  // 45 + 5
        }

        @Test
        @DisplayName("取消已出貨訂單 - 應拋出異常")
        void cancelOrder_WhenShipped_ShouldThrowException() {
            // Arrange
            Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.SHIPPED)
                .items(List.of())
                .build();

            when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

            // Act & Assert
            assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("已出貨");
        }

        @Test
        @DisplayName("取消已取消的訂單 - 應拋出異常")
        void cancelOrder_WhenAlreadyCancelled_ShouldThrowException() {
            // Arrange
            Order order = Order.builder()
                .id(1L)
                .status(OrderStatus.CANCELLED)
                .items(List.of())
                .build();

            when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

            // Act & Assert
            assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("已被取消");
        }
    }
}
