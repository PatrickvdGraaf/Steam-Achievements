package com.crepetete.data.network.response.base

data class ApiErrorResponse<T>(val errorMessage: String?) : ApiResponse<T>()