package com.food.ordering.system.service;

import com.food.ordering.system.entity.Order;
import com.food.ordering.system.entity.Product;
import com.food.ordering.system.entity.Restaurant;
import com.food.ordering.system.event.OrderCancelledEvent;
import com.food.ordering.system.event.OrderCreatedEvent;
import com.food.ordering.system.event.OrderPaidEvent;
import com.food.ordering.system.exception.OrderDomainException;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class OrderDomainServiceImpl implements OrderDomainService {
    private static final String UTC = "UTC";

    @Override
    public OrderCreatedEvent validateAndInitiateOrder(Order order, Restaurant restaurant) {
        validateRestaurant(restaurant);
        setOrderProductInformation(order, restaurant);
        order.validateOrder();
        order.initializeOrder();
        log.info("Order with id: {} is initiated", order.getId().getValue());
        return new OrderCreatedEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    private void setOrderProductInformation(Order order, Restaurant restaurant) {
        Map<Product, Product> restaurantProductMap = new HashMap<>();
        for (Product restaurantProduct : restaurant.getProducts()) {
            restaurantProductMap.put(restaurantProduct, restaurantProduct);
        }

        order.getItems().forEach(orderItem -> {
            Product currentProduct = orderItem.getProduct();
            Product matchingProduct = restaurantProductMap.get(currentProduct);
            if (matchingProduct != null) {
                currentProduct.updateWithConfirmedNameAndPrice(matchingProduct.getName(), matchingProduct.getPrice());
            }
        });
    }

    private void validateRestaurant(Restaurant restaurant) {
        if (!restaurant.isActive()) {
            throw new OrderDomainException("Restaurant with id: " + restaurant.getId().getValue() + " is not active");
        }
    }

    @Override
    public OrderPaidEvent payOrder(Order order) {
        order.pay();
        log.info("Order with id: {} is paid", order.getId().getValue());
        return new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public void approveOrder(Order order) {
        order.approve();
        log.info("Order with id: {} is approved", order.getId().getValue());
    }

    @Override
    public OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages) {
        order.initCancel(failureMessages);
        log.info("Order payment is cancelling for order id: {}", order.getId().getValue());
        return new OrderCancelledEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public void cancelOrder(Order order, List<String> failureMessages) {
        order.cancel(failureMessages);
        log.info("Order with id: {} is cancelled", order.getId().getValue());
    }
}
