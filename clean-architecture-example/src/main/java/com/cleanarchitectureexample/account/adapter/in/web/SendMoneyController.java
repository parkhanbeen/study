package com.cleanarchitectureexample.account.adapter.in.web;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cleanarchitectureexample.account.application.port.in.SendMoneyCommand;
import com.cleanarchitectureexample.account.application.port.in.SendMoneyUseCase;
import com.cleanarchitectureexample.account.domain.AccountId;
import com.cleanarchitectureexample.account.domain.Money;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
class SendMoneyController {

    private final SendMoneyUseCase sendMoneyUseCase;

    @PostMapping("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}")
    void sendMoney(
        @PathVariable("sourceAccountId") Long sourceAccountId,
        @PathVariable("targetAccountId") Long targetAccountId,
        @PathVariable("amount") Long amount) {

        SendMoneyCommand command = new SendMoneyCommand(
            new AccountId(sourceAccountId),
            new AccountId(targetAccountId),
            Money.of(amount));

        sendMoneyUseCase.sendMoney(command);
    }
}
