package com.shopzen.ecommerce_api.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private Long amount;
    private String currency;
    private String receipt;
    private String orderId;
    private String userId;
    private Map<String, String> notes;
}