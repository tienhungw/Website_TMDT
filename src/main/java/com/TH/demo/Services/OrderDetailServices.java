package com.TH.demo.Services;

import com.TH.demo.Model.Order;
import com.TH.demo.Model.OrderDetail;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderDetailServices {
    public List<OrderDetail> getOrderDetailByOrder(Order order);
}
