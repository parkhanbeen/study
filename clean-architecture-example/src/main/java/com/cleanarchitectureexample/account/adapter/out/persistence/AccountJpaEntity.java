package com.cleanarchitectureexample.account.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "account")
class AccountJpaEntity {

    @Id
    @GeneratedValue
    private Long id;

    protected AccountJpaEntity() {
    }
}
