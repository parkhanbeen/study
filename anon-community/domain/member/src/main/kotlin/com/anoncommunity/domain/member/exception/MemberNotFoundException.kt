package com.anoncommunity.domain.member.exception

import com.anoncommunity.contract.model.MemberId

class MemberNotFoundException(
    val memberId: MemberId,
) : RuntimeException("Member not found: ${memberId.value}")
