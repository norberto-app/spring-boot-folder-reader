package com.example.napp.filereader.data.repository

import com.example.napp.filereader.data.model.Process
import org.springframework.stereotype.Repository

@Repository
class ProcessRepository(private val dataSource: ProcessDataSource) {
    fun save(process: Process): Process = dataSource.save(process)

    fun findAll(): List<Process> {
        return dataSource.findAll()
    }

    fun existsById(id: Int) = dataSource.existsById(id)

    fun findById(id: Int): Process = dataSource.findById(id).orElseThrow()
}