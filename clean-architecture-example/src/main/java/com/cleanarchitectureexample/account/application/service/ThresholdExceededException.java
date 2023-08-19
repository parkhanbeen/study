package com.cleanarchitectureexample.account.application.service;

import com.cleanarchitectureexample.account.domain.Money;
import jakarta.validation.constraints.NotNull;

public class ThresholdExceededException extends RuntimeException {

    public ThresholdExceededException(Money threshold, Money actual) {
        super(String.format("Maximum threshold for transferring money exceeded: tried to transfer %s but threshold is %s!", actual, threshold));
    }
}
