package com.anoncommunity.domain.member.exception

class ProfileLimitExceededException(limit: Int = 5) : RuntimeException("Profile limit exceeded: max $limit")
