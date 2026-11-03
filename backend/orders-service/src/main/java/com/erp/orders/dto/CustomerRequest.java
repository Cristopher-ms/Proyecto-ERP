package com.erp.orders.dto;

import com.erp.orders.domain.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "El apellido o razón social es obligatorio")
    @Size(max = 100)
    private String lastName;

    private String email;

    private String phone;

    @NotNull(message = "El tipo de documento es obligatorio")
    private DocumentType documentType;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 30)
    private String documentNumber;

    private String address;
}
