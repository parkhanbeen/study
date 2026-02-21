package com.anoncommunity.app.usecase.member

import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.usecase.CommandUseCase
import com.anoncommunity.domain.member.PreferredLanguage
import com.anoncommunity.domain.member.exception.MemberNotFoundException
import com.anoncommunity.domain.member.port.MemberCommandPort
import com.anoncommunity.domain.member.port.MemberQueryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class UpdateLanguageCommand(
    val memberId: Long,
    val language: String,
)

@Service
class UpdateLanguageUseCase(
    private val memberQueryPort: MemberQueryPort,
    private val memberCommandPort: MemberCommandPort,
) : CommandUseCase<UpdateLanguageCommand, Unit> {

    @Transactional
    override fun invoke(command: UpdateLanguageCommand) {
        val memberId = MemberId(command.memberId)
        val member = memberQueryPort.findById(memberId)
            ?: throw MemberNotFoundException(memberId)

        member.updatePreferredLanguage(PreferredLanguage(command.language))
        memberCommandPort.save(member)
    }
}
