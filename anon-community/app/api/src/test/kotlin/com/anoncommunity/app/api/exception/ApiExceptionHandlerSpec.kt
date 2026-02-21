package com.anoncommunity.app.api.exception

import com.anoncommunity.app.usecase.member.InvalidCredentialsException
import com.anoncommunity.contract.exception.AccessDeniedException
import com.anoncommunity.contract.model.CommentId
import com.anoncommunity.contract.model.MemberId
import com.anoncommunity.contract.model.PostId
import com.anoncommunity.domain.comment.exception.CommentNotFoundException
import com.anoncommunity.domain.like.exception.LikeNotFoundException
import com.anoncommunity.domain.like.exception.PostAlreadyLikedException
import com.anoncommunity.domain.member.MemberNickname
import com.anoncommunity.domain.member.exception.MemberNotFoundException
import com.anoncommunity.domain.member.exception.NicknameDuplicatedException
import com.anoncommunity.domain.post.exception.PostNotFoundException
import com.anoncommunity.infra.opensearch.exception.SearchExecutionException
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException

class ApiExceptionHandlerSpec : DescribeSpec({

    val handler = ApiExceptionHandler()
    val methodParam = mockk<MethodParameter>(relaxed = true)

    describe("ApiExceptionHandler") {

        data class Case(
            val name: String,
            val status: HttpStatus,
            val code: String,
            val message: String,
            val invoke: () -> ResponseEntity<ErrorResponse>,
        )

        listOf(
            Case("InvalidCredentials → 401", HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid credentials") {
                handler.handleInvalidCredentials(InvalidCredentialsException())
            },
            Case("AccessDenied → 403", HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission") {
                handler.handleAccessDenied(AccessDeniedException("You do not have permission"))
            },
            Case("MissingHeader → 400", HttpStatus.BAD_REQUEST, "MISSING_HEADER", "Required header: X-Member-Id") {
                handler.handleMissingHeader(MissingRequestHeaderException("X-Member-Id", methodParam))
            },
            Case("PostNotFound → 404", HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "Post not found: 1") {
                handler.handlePostNotFound(PostNotFoundException(PostId(1L)))
            },
            Case("CommentNotFound → 404", HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "Comment not found: 42") {
                handler.handleCommentNotFound(CommentNotFoundException(CommentId(42L)))
            },
            Case("LikeNotFound → 404", HttpStatus.NOT_FOUND, "LIKE_NOT_FOUND", "Like not found for post 10 by member 20") {
                handler.handleLikeNotFound(LikeNotFoundException(PostId(10L), MemberId(20L)))
            },
            Case("MemberNotFound → 404", HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Member not found: 99") {
                handler.handleMemberNotFound(MemberNotFoundException(MemberId(99L)))
            },
            Case("NicknameDuplicated → 409", HttpStatus.CONFLICT, "NICKNAME_DUPLICATED", "Nickname already taken: duplicatedUser") {
                handler.handleNicknameDuplicated(NicknameDuplicatedException(MemberNickname("duplicatedUser")))
            },
            Case("IllegalArgument → 400", HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Invalid request") {
                handler.handleBadRequest(IllegalArgumentException("Title cannot be empty"))
            },
            Case("IllegalState → 409", HttpStatus.CONFLICT, "CONFLICT", "Operation conflict") {
                handler.handleConflict(IllegalStateException("Account is not active"))
            },
            Case("PostAlreadyLiked → 409", HttpStatus.CONFLICT, "ALREADY_LIKED", "Member 10 already liked post 5") {
                handler.handleAlreadyLiked(PostAlreadyLikedException(PostId(5L), MemberId(10L)))
            },
            Case("SearchExecution → 503 with generic Korean message", HttpStatus.SERVICE_UNAVAILABLE, "SEARCH_UNAVAILABLE", "검색 서비스를 일시적으로 사용할 수 없습니다") {
                handler.handleSearchExecution(SearchExecutionException("OpenSearch connection refused", RuntimeException("java.net.ConnectException: Connection refused")))
            },
            Case("Unexpected → 500", HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal server error") {
                handler.handleUnexpected(RuntimeException("Something went wrong"))
            },
        ).forEach { case ->
            it(case.name) {
                val response = case.invoke()
                response.statusCode shouldBe case.status
                response.body!!.code shouldBe case.code
                response.body!!.message shouldBe case.message
            }
        }

        it("MethodArgumentNotValid → 400 with field details") {
            val fieldError = mockk<FieldError> {
                every { field } returns "title"
                every { defaultMessage } returns "must not be blank"
            }
            val bindingResult = mockk<BindingResult> {
                every { fieldErrors } returns listOf(fieldError)
            }
            val exception = MethodArgumentNotValidException(methodParam, bindingResult)

            val response = handler.handleValidation(exception)

            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            response.body!!.code shouldBe "VALIDATION_ERROR"
            response.body!!.message shouldBe "title: must not be blank"
        }

        it("ConstraintViolation → 400 with field details") {
            val path = mockk<jakarta.validation.Path>()
            every { path.toString() } returns "title"
            val violation = mockk<ConstraintViolation<*>>()
            every { violation.propertyPath } returns path
            every { violation.message } returns "must not be blank"

            val response = handler.handleConstraintViolation(ConstraintViolationException(setOf(violation)))

            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            response.body!!.code shouldBe "VALIDATION_ERROR"
            response.body!!.message shouldBe "title: must not be blank"
        }
    }
})
