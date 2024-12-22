package com.example.man_delivery_food.Model;

public class Order {
    private String orderId;
    private int numberOfItems;
    private String orderDate;
    private String orderTime;

    public Order(String orderId, int numberOfItems, String orderDate, String orderTime) {
        this.orderId = orderId;
        this.numberOfItems = numberOfItems;
        this.orderDate = orderDate;
        this.orderTime = orderTime;
    }

    public String getOrderId() {
        return orderId;
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getOrderTime() {
        return orderTime;
    }
}
