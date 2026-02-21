package com.anoncommunity.infra.rdb.repository

import com.anoncommunity.infra.rdb.entity.MemberEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MemberJpaRepository : JpaRepository<MemberEntity, Long>
