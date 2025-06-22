package com.example.napp.filereader.controller

import com.example.napp.filereader.data.model.Process
import com.example.napp.filereader.data.model.Progress
import com.example.napp.filereader.data.repository.ProcessRepository
import com.example.napp.filereader.exception.ProcessNotFoundException
import jakarta.validation.constraints.Positive
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@Validated
class ProcessController(
    private val repository: ProcessRepository,
    private val fileService: FileProcessorService,
    @Value("\${com.example.napp.filereader.folder.name}") private val folderPath: String
) {

    @PostMapping("/process/start")
    fun start(): Process {
        logger.debug("Starting a new process for analyzing files ...")

        return fileService.processFolder(path = folderPath)
    }

    @PostMapping("/process/stop/{id}")
    fun stop(@PathVariable("id") @Positive(message = "Process Id must be a positive number") id: Int): Process {
        return fileService.cancelProcess(id)
    }

    @RequestMapping("/process/status/{id}")
    fun status(
        @PathVariable("id") @Positive(message = "Process Id must be a positive number") id: Int
    ): ProcessStatusResponse {

        if (repository.existsById(id)) {
            return repository.findById(id).run { ProcessStatusResponse(id = id, status = status.name) }
        } else {
            throw ProcessNotFoundException(processId = id)
        }
    }

    @RequestMapping("/process/list")
    fun getAll(): List<ProcessListResponse> {
        return repository.findAll().map {
            ProcessListResponse(
                id = it.id,
                status = it.status.name,
                startedAt = it.startedAt,
                estimatedCompletion = it.estimatedCompletion,
                progress = it.progress
            )
        }
    }

    @RequestMapping("/process/results/{id}")
    fun getResults(
        @PathVariable("id") @Positive(message = "Process Id must be a positive number") id: Int
    ): Process {
        if (repository.existsById(id)) {
            return repository.findById(id)
        } else {
            throw ProcessNotFoundException(processId = id)
        }
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(ProcessController::class.java)
    }
}

data class ProcessStatusResponse(
    val id: Int,
    val status: String
)

data class ProcessListResponse(
    val id: Int,
    val status: String,
    var startedAt: Instant?,
    var estimatedCompletion: Instant?,
    var progress: Progress? = null
)