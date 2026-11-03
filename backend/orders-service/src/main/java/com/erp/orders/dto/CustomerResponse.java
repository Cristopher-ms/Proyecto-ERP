package com.erp.orders.dto;

import com.erp.orders.domain.enums.DocumentType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private DocumentType documentType;
    private String documentNumber;
    private String address;
    private Boolean active;
    private LocalDateTime createdAt;
}
