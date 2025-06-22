package com.example.napp.filereader.data.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
class Process() {

    constructor(id: Int, status: ProcessStatus) : this() {
        this.id = id
        this.status = status
    }

    @Id
    var id: Int = 0

    @JdbcTypeCode(SqlTypes.ENUM)
    var status: ProcessStatus = ProcessStatus.Pending

    @JdbcTypeCode(SqlTypes.JSON)
    var progress: Progress? = null

    var startedAt: Instant? = null
    var estimatedCompletion: Instant? = null

    @JdbcTypeCode(SqlTypes.JSON)
    var results: Results? = null
}

data class Progress(
    val totalFiles: Int = 0,
    val processedFiles: Int = 0,
    val percentage: Float,
)

data class Results(
    val totalWords: Long = 0,
    val totalLines: Long = 0,
    val mostFrequentWords: List<String> =emptyList(),
    val filesProcessed: List<String> = emptyList()
)