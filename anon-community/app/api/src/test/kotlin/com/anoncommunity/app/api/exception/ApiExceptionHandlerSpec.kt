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
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException

class ApiExceptionHandlerSpec : DescribeSpec({

    val handler = ApiExceptionHandler()

    describe("ApiExceptionHandler") {

        it("should return 401 UNAUTHORIZED for InvalidCredentialsException") {
            val exception = InvalidCredentialsException()

            val response = handler.handleInvalidCredentials(exception)

            response.statusCode shouldBe HttpStatus.UNAUTHORIZED
            response.body!!.code shouldBe "UNAUTHORIZED"
            response.body!!.message shouldBe "Invalid credentials"
        }

        it("should return 403 FORBIDDEN for AccessDeniedException") {
            val exception = AccessDeniedException("You do not have permission")

            val response = handler.handleAccessDenied(exception)

            response.statusCode shouldBe HttpStatus.FORBIDDEN
            response.body!!.code shouldBe "FORBIDDEN"
            response.body!!.message shouldBe "You do not have permission"
        }

        it("should return 400 BAD_REQUEST for MissingRequestHeaderException") {
            val methodParam = mockk<MethodParameter>(relaxed = true)
            val exception = MissingRequestHeaderException("X-Member-Id", methodParam)

            val response = handler.handleMissingHeader(exception)

            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            response.body!!.code shouldBe "MISSING_HEADER"
            response.body!!.message shouldBe "Required header: X-Member-Id"
        }

        it("should return 404 NOT_FOUND for PostNotFoundException") {
            val exception = PostNotFoundException(PostId(1L))

            val response = handler.handlePostNotFound(exception)

            response.statusCode shouldBe HttpStatus.NOT_FOUND
            response.body!!.code shouldBe "POST_NOT_FOUND"
            response.body!!.message shouldBe "Post not found: 1"
        }

        it("should return 404 NOT_FOUND for CommentNotFoundException") {
            val exception = CommentNotFoundException(CommentId(42L))

            val response = handler.handleCommentNotFound(exception)

            response.statusCode shouldBe HttpStatus.NOT_FOUND
            response.body!!.code shouldBe "COMMENT_NOT_FOUND"
            response.body!!.message shouldBe "Comment not found: 42"
        }

        it("should return 404 NOT_FOUND for LikeNotFoundException") {
            val exception = LikeNotFoundException(PostId(10L), MemberId(20L))

            val response = handler.handleLikeNotFound(exception)

            response.statusCode shouldBe HttpStatus.NOT_FOUND
            response.body!!.code shouldBe "LIKE_NOT_FOUND"
            response.body!!.message shouldBe "Like not found for post 10 by member 20"
        }

        it("should return 404 NOT_FOUND for MemberNotFoundException") {
            val exception = MemberNotFoundException(MemberId(99L))

            val response = handler.handleMemberNotFound(exception)

            response.statusCode shouldBe HttpStatus.NOT_FOUND
            response.body!!.code shouldBe "MEMBER_NOT_FOUND"
            response.body!!.message shouldBe "Member not found: 99"
        }

        it("should return 409 CONFLICT for NicknameDuplicatedException") {
            val exception = NicknameDuplicatedException(MemberNickname("duplicatedUser"))

            val response = handler.handleNicknameDuplicated(exception)

            response.statusCode shouldBe HttpStatus.CONFLICT
            response.body!!.code shouldBe "NICKNAME_DUPLICATED"
            response.body!!.message shouldBe "Nickname already taken: duplicatedUser"
        }

        it("should return 400 BAD_REQUEST for IllegalArgumentException") {
            val exception = IllegalArgumentException("Title cannot be empty")

            val response = handler.handleBadRequest(exception)

            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            response.body!!.code shouldBe "BAD_REQUEST"
            response.body!!.message shouldBe "Invalid request"
        }

        it("should return 409 CONFLICT for IllegalStateException") {
            val exception = IllegalStateException("Account is not active")

            val response = handler.handleConflict(exception)

            response.statusCode shouldBe HttpStatus.CONFLICT
            response.body!!.code shouldBe "CONFLICT"
            response.body!!.message shouldBe "Operation conflict"
        }

        it("should return 409 CONFLICT for PostAlreadyLikedException") {
            val exception = PostAlreadyLikedException(PostId(5L), MemberId(10L))

            val response = handler.handleAlreadyLiked(exception)

            response.statusCode shouldBe HttpStatus.CONFLICT
            response.body!!.code shouldBe "ALREADY_LIKED"
            response.body!!.message shouldBe "Member 10 already liked post 5"
        }

        it("should return 400 BAD_REQUEST for MethodArgumentNotValidException with field errors") {
            val fieldError = mockk<FieldError> {
                every { field } returns "title"
                every { defaultMessage } returns "must not be blank"
            }
            val bindingResult = mockk<BindingResult> {
                every { fieldErrors } returns listOf(fieldError)
            }
            val methodParam = mockk<MethodParameter>(relaxed = true)
            val exception = MethodArgumentNotValidException(methodParam, bindingResult)

            val response = handler.handleValidation(exception)

            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            response.body!!.code shouldBe "VALIDATION_ERROR"
            response.body!!.message shouldBe "title: must not be blank"
        }

        it("should return 400 BAD_REQUEST for ConstraintViolationException") {
            val path = mockk<jakarta.validation.Path>()
            every { path.toString() } returns "title"
            val violation = mockk<ConstraintViolation<*>>()
            every { violation.propertyPath } returns path
            every { violation.message } returns "must not be blank"
            val exception = ConstraintViolationException(setOf(violation))

            val response = handler.handleConstraintViolation(exception)

            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            response.body!!.code shouldBe "VALIDATION_ERROR"
            response.body!!.message shouldBe "title: must not be blank"
        }

        it("should return 500 INTERNAL_SERVER_ERROR for unexpected Exception") {
            val exception = RuntimeException("Something went wrong")

            val response = handler.handleUnexpected(exception)

            response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
            response.body!!.code shouldBe "INTERNAL_ERROR"
            response.body!!.message shouldBe "Internal server error"
        }
    }
})
