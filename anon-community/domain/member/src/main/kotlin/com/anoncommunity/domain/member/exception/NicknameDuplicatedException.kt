package com.anoncommunity.domain.member.exception

import com.anoncommunity.domain.member.MemberNickname

class NicknameDuplicatedException(
    val nickname: MemberNickname,
) : RuntimeException("Nickname already taken: ${nickname.value}")
