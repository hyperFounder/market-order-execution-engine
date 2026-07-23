package com.hft.engine.marketorderexecutionengine.repository;

import com.hft.engine.marketorderexecutionengine.entity.OrderEntity;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOrderRepository {

    // Thread safe in memory key-value store (OrderId -> OrderEntity)
    private final Map<String, OrderEntity> orderStore = new ConcurrentHashMap<>();

    public OrderEntity save(OrderEntity order){
        orderStore.put(order.getOrderId(), order);
        return order;
    }

    public Optional<OrderEntity> findById(String orderId){
        return Optional.ofNullable(orderStore.get(orderId));
    }

    public Map<String, OrderEntity> findAll(){
        return orderStore;
    }
}
