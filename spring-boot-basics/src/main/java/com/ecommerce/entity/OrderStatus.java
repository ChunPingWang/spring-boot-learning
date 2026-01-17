package com.ecommerce.entity;

/**
 * 訂單狀態枚舉
 */
public enum OrderStatus {
    PENDING("待付款"),
    PAID("已付款"),
    PROCESSING("處理中"),
    SHIPPED("已出貨"),
    DELIVERED("已送達"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
