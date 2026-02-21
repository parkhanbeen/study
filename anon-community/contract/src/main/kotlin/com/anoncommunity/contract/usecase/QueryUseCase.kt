package com.anoncommunity.contract.usecase

interface QueryUseCase<Query, Return> {
    operator fun invoke(query: Query): Return
}
