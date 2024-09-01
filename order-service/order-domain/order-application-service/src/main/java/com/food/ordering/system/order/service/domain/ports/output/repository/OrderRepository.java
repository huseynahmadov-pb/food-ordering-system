package com.food.ordering.system.order.service.domain.ports.output.repository;

import com.food.ordering.system.entity.Order;
import com.food.ordering.system.valueobject.TrackingId;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findByTrackingId(TrackingId trackingId);


}
