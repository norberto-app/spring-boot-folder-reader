package com.example.napp.filereader.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.Instant

@ControllerAdvice
class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ProcessNotFoundException::class)
    @ResponseBody
    fun handleProcessNotFound(request: HttpServletRequest, exception: Exception): Any {
        val processNotFoundException = exception as ProcessNotFoundException?

        return hashMapOf(
            "timestamp" to Instant.now(),
            "status" to HttpStatus.NOT_FOUND.value(),
            "error" to "The requested process could not be found. Invalid process id: [${processNotFoundException?.processId}]",
            "path" to request.servletPath
        )
    }
}
