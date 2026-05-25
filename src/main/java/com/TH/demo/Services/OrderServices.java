package com.TH.demo.Services;

import com.TH.demo.Model.Order;
import org.springframework.stereotype.Service;

@Service
public interface OrderServices {
    public void saveOrder(Order order);
}
