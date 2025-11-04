package com.innowise.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "payments")
@CompoundIndex(name = "idx_user_status", def = "{'userId': 1, 'status': 1}")
public class Payment {
    @Id
    private String id;
    
    @Indexed(name = "idx_order_id")
    private String orderId;
    
    @Indexed(name = "idx_user_id") 
    private String userId;
    
    @Indexed(name = "idx_status")
    private String status;
    
    @Indexed(name = "idx_timestamp")
    private LocalDateTime timestamp;
    
    private Double paymentAmount;
}