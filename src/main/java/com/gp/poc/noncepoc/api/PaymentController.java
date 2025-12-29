package com.gp.poc.noncepoc.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping("/execute")
    public String pay() {
        return "Payment processed securely ?";
    }
}