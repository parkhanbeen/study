package com.cleanarchitectureexample.account.adapter.out.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "activity")
public class ActivityJpaEntity {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime timestamp;

    private Long ownerAccountId;

    private Long sourceAccountId;

    private Long targetAccountId;

    private Long amount;

    protected ActivityJpaEntity() {
    }

    public ActivityJpaEntity(
        Long id,
        LocalDateTime timestamp,
        Long ownerAccountId,
        Long sourceAccountId,
        Long targetAccountId,
        Long amount) {
        this.id = id;
        this.timestamp = timestamp;
        this.ownerAccountId = ownerAccountId;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
    }
}
