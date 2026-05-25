package com.TH.demo.Services;

import org.springframework.stereotype.Service;

@Service
public interface PaymentService {
    boolean confirmPayment(Long orderId, String username);
}
