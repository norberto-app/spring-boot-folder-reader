package com.example.napp.filereader.data.repository

import com.example.napp.filereader.data.model.Process
import org.springframework.data.jpa.repository.JpaRepository

interface ProcessDataSource : JpaRepository<Process, Int>