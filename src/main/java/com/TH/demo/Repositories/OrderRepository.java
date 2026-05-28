package com.TH.demo.Repositories;

import com.TH.demo.Model.Order;
import com.TH.demo.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(Users user);

    List<Order> findByUserOrderByIdDesc(Users user);

    // ADMIN: lấy tất cả order mới nhất lên đầu
    List<Order> findAllByOrderByIdDesc();
}