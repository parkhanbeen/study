package com.cleanarchitectureexample.account.application.service;

import com.cleanarchitectureexample.account.domain.Money;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MoneyTransferProperties {

    private Money maximumTransferThreshold = Money.of(1_000_000L);
}
