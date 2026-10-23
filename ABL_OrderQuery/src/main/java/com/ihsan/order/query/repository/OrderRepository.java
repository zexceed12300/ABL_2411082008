package com.ihsan.order.query.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ihsan.order.query.entity.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByProductId(Integer productId);

    List<Order> findByCustomerId(Integer customerId);
}
