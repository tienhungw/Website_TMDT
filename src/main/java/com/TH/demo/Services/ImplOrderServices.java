package com.TH.demo.Services;

import com.TH.demo.Model.Order;
import com.TH.demo.Repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImplOrderServices implements OrderServices{
    @Autowired
    OrderRepository orderRepository;
    @Override
    public void saveOrder(Order order){
        orderRepository.save(order);
    }
}
