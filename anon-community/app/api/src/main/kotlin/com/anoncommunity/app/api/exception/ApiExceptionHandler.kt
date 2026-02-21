package com.anoncommunity.app.api.exception

import com.anoncommunity.app.usecase.member.InvalidCredentialsException
import com.anoncommunity.contract.exception.AccessDeniedException
import com.anoncommunity.domain.comment.exception.CommentNotFoundException
import com.anoncommunity.domain.like.exception.LikeNotFoundException
import com.anoncommunity.domain.like.exception.PostAlreadyLikedException
import com.anoncommunity.domain.member.exception.MemberNotFoundException
import com.anoncommunity.domain.member.exception.NicknameDuplicatedException
import com.anoncommunity.domain.post.exception.PostNotFoundException
import com.anoncommunity.infra.opensearch.exception.SearchExecutionException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(e: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(code = "UNAUTHORIZED", message = "Invalid credentials"))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(code = "FORBIDDEN", message = e.message ?: "Access denied"))
    }

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingHeader(e: MissingRequestHeaderException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = "MISSING_HEADER", message = "Required header: ${e.headerName}"))
    }

    @ExceptionHandler(PostNotFoundException::class)
    fun handlePostNotFound(e: PostNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(code = "POST_NOT_FOUND", message = e.message ?: "Post not found"))
    }

    @ExceptionHandler(CommentNotFoundException::class)
    fun handleCommentNotFound(e: CommentNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(code = "COMMENT_NOT_FOUND", message = e.message ?: "Comment not found"))
    }

    @ExceptionHandler(LikeNotFoundException::class)
    fun handleLikeNotFound(e: LikeNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(code = "LIKE_NOT_FOUND", message = e.message ?: "Like not found"))
    }

    @ExceptionHandler(MemberNotFoundException::class)
    fun handleMemberNotFound(e: MemberNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(code = "MEMBER_NOT_FOUND", message = e.message ?: "Member not found"))
    }

    @ExceptionHandler(NicknameDuplicatedException::class)
    fun handleNicknameDuplicated(e: NicknameDuplicatedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(code = "NICKNAME_DUPLICATED", message = e.message ?: "Nickname already taken"))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        log.warn("Bad request: {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = "BAD_REQUEST", message = "Invalid request"))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleConflict(e: IllegalStateException): ResponseEntity<ErrorResponse> {
        log.warn("Conflict: {}", e.message)
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(code = "CONFLICT", message = "Operation conflict"))
    }

    @ExceptionHandler(PostAlreadyLikedException::class)
    fun handleAlreadyLiked(e: PostAlreadyLikedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(code = "ALREADY_LIKED", message = e.message ?: "Already liked"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = "VALIDATION_ERROR", message = errors))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(e: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errors = e.constraintViolations.joinToString(", ") {
            "${it.propertyPath}: ${it.message}"
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = "VALIDATION_ERROR", message = errors))
    }

    @ExceptionHandler(SearchExecutionException::class)
    fun handleSearchExecution(e: SearchExecutionException): ResponseEntity<ErrorResponse> {
        log.error("Search service unavailable", e)
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ErrorResponse(code = "SEARCH_UNAVAILABLE", message = "검색 서비스를 일시적으로 사용할 수 없습니다"))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(code = "INTERNAL_ERROR", message = "Internal server error"))
    }
}

data class ErrorResponse(
    val code: String,
    val message: String,
)
