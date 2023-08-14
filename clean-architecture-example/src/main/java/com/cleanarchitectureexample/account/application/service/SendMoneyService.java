package com.cleanarchitectureexample.account.application.service;

import org.springframework.transaction.annotation.Transactional;
import com.cleanarchitectureexample.account.application.port.in.SendMoneyCommand;
import com.cleanarchitectureexample.account.application.port.in.SendMoneyUseCase;
import com.cleanarchitectureexample.account.application.port.out.LoadAccountPort;
import com.cleanarchitectureexample.account.application.port.out.UpdateAccountStatePort;
import com.cleanarchitectureexample.account.domain.AccountLock;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
class SendMoneyService implements SendMoneyUseCase {

    private final LoadAccountPort loadAccountPort;
    private final AccountLock accountLock;
    private final UpdateAccountStatePort updateAccountStatePort;

    @Override
    public boolean sendMoney(SendMoneyCommand command) {
        // TODO: 비즈니스 규칙 검증
        // TODO: 모델 상태 조작
        // TODO: 출력 값 반환
        return false;
    }
}
