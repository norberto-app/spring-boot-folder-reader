package com.example.napp.filereader.controller

import com.example.napp.filereader.data.model.Process
import com.example.napp.filereader.data.model.ProcessStatus
import com.example.napp.filereader.data.model.Progress
import com.example.napp.filereader.data.model.Results
import com.example.napp.filereader.data.repository.ProcessRepository
import com.example.napp.filereader.exception.ProcessNotFoundException
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

/**
 * File Processing Service
 *
 * @param repository The process repository responsible for storing process related data.
 * @property supportedFileExtensions List of file extensions to process (default: text files)
 */
@Service
class FileProcessorService(
    private val repository: ProcessRepository,
    @Value("\${com.example.napp.filereader.folder.fileExtensions}") private val supportedFileExtensions: List<String>
) : CoroutineScope {

    // Store active jobs for cancellation
    private val activeJobs = mutableMapOf<Int, Job>()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    /**
     * Processes all files in a given folder and returns a Process object with results
     * @param path Path to the folder containing files to process.
     * @return the Process object with complete results.
     */
    fun processFolder(path: String): Process {

        // Create the new process object and save it.
        val process = Process(
            id = Random.nextInt(from = 1, until = Int.MAX_VALUE),
            status = ProcessStatus.Pending,
        ).apply {
            startedAt = Instant.now()
        }

        val progressCallback: ((Progress) -> Unit) = { progress ->
            val progressText = "${progress.processedFiles}/${progress.totalFiles} (${progress.percentage.toInt()}%)"
            logger.debug("Process {} is analyzing files. Progress: {} {}", process.id, progressText, progress)
        }

        repository.save(process)

        // Start processing in a new coroutine
        val job = launch {
            try {
                process.status = ProcessStatus.Running
                repository.save(process)

                readFolder(process, path, progressCallback)
            } catch (cancellation: CancellationException) {
                process.status = ProcessStatus.Stopped
                logger.warn("Process ${process.id} was cancelled: ${cancellation.message}")
            } catch (exception: Exception) {
                process.status = ProcessStatus.Failed
                logger.error("Unexpected exception thrown by process ${process.id}", exception)
            } finally {
                // Save the current process state.
                repository.save(process)

                // Clean up active job tracking and notify completion
                activeJobs.remove(process.id)
            }
        }

        // Store the job for cancellation
        activeJobs[process.id] = job

        return process
    }

    /**
     * Internal suspend function that does the actual processing work.
     * Reads all the files in the given folder and calculate the required stats based on the content read from the files.
     */
    private suspend fun readFolder(
        process: Process,
        path: String,
        progressCallback: ((Progress) -> Unit)
    ) = withContext(Dispatchers.IO) {

        try {
            // Store the current job for cancellation
            val folder = File(path)
            if (!folder.exists() || !folder.isDirectory) {
                throw IllegalArgumentException("Invalid folder path: $path")
            }

            // Check for cancellation
            ensureActive()

            // Get all supported files
            val files = folder.walkTopDown()
                .filter { it.isFile && it.extension.lowercase() in supportedFileExtensions }
                .toList()

            if (files.isEmpty()) {
                process.status = ProcessStatus.Completed
                process.results = Results()
                process.progress = Progress(percentage = 100f)
                return@withContext
            }

            // Check for cancellation
            ensureActive()

            val totalFiles = files.size
            var currentFile = 0
            val processedFileNames = mutableListOf<String>()

            // Estimate completion time (rough estimate: 100ms per file)
            val estimatedDurationMs = totalFiles * 100L
            process.estimatedCompletion = process.startedAt?.plus(estimatedDurationMs, ChronoUnit.MILLIS)

            // Process files concurrently with controlled parallelism
            val fileResults = files.map {
                async {
                    // Check for cancellation before processing each file
                    ensureActive()
                    val fileResult = processFile(it)
                    currentFile++

                    val percentage = (currentFile.toFloat() / totalFiles) * 100f

                    // Update progress
                    val currentProgress = Progress(totalFiles, currentFile, percentage)
                    process.progress = currentProgress
                    progressCallback.invoke(currentProgress)

                    // Small delay to simulate realistic processing time
                    try {
                        delay(100000)
                        ensureActive()
                    } catch (exception: CancellationException) {
                        throw exception // Re-throw to properly handle cancellation
                    }
                    fileResult
                }
            }.awaitAll()

            // Final cancellation check before setting results
            ensureActive()

            val wordFrequency: MutableMap<String, Int> = mutableMapOf()
            var totalLines = 0L
            var totalWords = 0L

            fileResults.forEach { result ->
                result.wordFrequency.forEach { word ->
                    wordFrequency[word.key] = wordFrequency.getOrDefault(word.key, 0) + word.value
                }
                totalLines += result.totalLines
                totalWords += result.totalWords
                processedFileNames.add(result.fileName)
            }
            val result = FileResult(
                fileName = "summary",
                totalWords = totalWords,
                totalLines = totalLines,
                wordFrequency = wordFrequency
            )
            logger.debug("Process folder analysis result: {}", result)

            // Calculate most frequent words (top 10)
            val mostFrequentWords = wordFrequency
                .toList()
                .sortedByDescending { it.second }
                .take(10)
                .map { it.first }

            // Set final results
            process.results = Results(
                totalWords = totalWords,
                totalLines = totalLines,
                mostFrequentWords = mostFrequentWords,
                filesProcessed = processedFileNames.toList()
            )

            process.status = ProcessStatus.Completed

        } catch (cancellation: CancellationException) {
            process.status = ProcessStatus.Stopped
            logger.warn("Processing cancelled for process ID: ${process.id}")
            throw cancellation // Re-throw to maintain cancellation semantics
        } catch (exception: Exception) {
            process.status = ProcessStatus.Failed
            logger.error("Error processing files: ${exception.message}", exception)
        } finally {
            // Clean up active job tracking
            activeJobs.remove(process.id)
        }

        repository.save(process)
        process
    }

    /**
     * Processes a single file and updates the shared counters
     */
    private suspend fun processFile(file: File): FileResult {
        val wordFrequency: MutableMap<String, Int> = mutableMapOf()
        var totalWords: Long = 0
        var totalLines: Long = 0
        var fileName = ""

        withContext(Dispatchers.IO) {

            // Check for cancellation before processing
            ensureActive()

            val content = file.readText()
            val lines = content.lines()
            val words = content.lowercase()
                .split(Regex("[\\s\\p{Punct}]+"))
                .filter { it.isNotBlank() && it.length > 2 } // Filter out short words

            // Check for cancellation after reading the file
            ensureActive()

            // Update counters atomically
            totalLines += lines.size.toLong()
            totalWords += words.size.toLong()

            // Update word frequency
            words.forEach { word ->
                wordFrequency[word] = wordFrequency.getOrDefault(word, 0) + 1
            }
            fileName = file.name
        }

        return FileResult(
            fileName = fileName,
            totalWords = totalWords,
            totalLines = totalLines,
            wordFrequency = wordFrequency
        )
    }

    /**
     * Cancels the processing for a specific process ID
     * @param processId The ID of the process to cancel
     * @return true if cancellation was initiated, false if the process was not found
     */
    fun cancelProcess(processId: Int): Process {
        val job = activeJobs[processId]

        if (job != null) {
            job.cancel(CancellationException("Process $processId cancelled by user"))
            logger.warn("Cancellation requested for process ID: $processId")

            val process = repository.findById(processId)

            if (process == null) {
                throw ProcessNotFoundException(processId = processId)
            } else {
                process.status = ProcessStatus.Stopped
                repository.save(process)
                return process
            }

        } else {
            throw ProcessNotFoundException(processId = processId)
        }
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(FileProcessorService::class.java)
    }
}

private data class FileResult(
    val fileName: String,
    var totalWords: Long = 0,
    var totalLines: Long = 0,
    var wordFrequency: MutableMap<String, Int> = mutableMapOf()
)