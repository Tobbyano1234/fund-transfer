package com.ecobank.fundtransferservice.model.domain;


import com.ecobank.fundtransferservice.enums.TransactionLimitType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "account_limit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountLimit {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type", length = 50)
    private TransactionLimitType limitType;

    @Column(name = "limit_value")
    private BigDecimal value;
}
