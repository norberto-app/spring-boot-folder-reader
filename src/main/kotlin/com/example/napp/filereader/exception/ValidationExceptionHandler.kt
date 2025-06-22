package com.example.napp.filereader.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.Instant

@ControllerAdvice
class ValidationExceptionHandler : ResponseEntityExceptionHandler() {

    override fun handleMethodArgumentNotValid(
        exception: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<in Any>? {
        val responseBody = hashMapOf<String, Any>()
        responseBody.put("timestamp", Instant.now())
        responseBody.put("status", status.value())
        responseBody.put("errors", exception.bindingResult.fieldErrors.map { it.defaultMessage })

        return ResponseEntity(responseBody, headers, status)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class, ConstraintViolationException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleRequestPathVariablesValidationException(exception: Exception, request: HttpServletRequest): Any {
        return hashMapOf(
            "timestamp" to Instant.now(),
            "status" to HttpStatus.BAD_REQUEST.value(),
            "error" to exception.message,
            "path" to request.servletPath
        )
    }
}