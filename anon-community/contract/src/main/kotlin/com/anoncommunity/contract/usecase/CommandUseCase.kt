package com.anoncommunity.contract.usecase

interface CommandUseCase<Command, Return> {
    operator fun invoke(command: Command): Return
}
