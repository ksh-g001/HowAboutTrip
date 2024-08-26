package com.project.how.data_class.dto

data class ErrorResponse(
    val errorMessage: String,
    val httpStatus: String,
    val code: String?
)
