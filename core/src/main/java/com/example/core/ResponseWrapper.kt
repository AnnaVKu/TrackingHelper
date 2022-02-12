package com.example.core

class ResponseWrapper<T> private constructor(
var status: Status,
val data: T?,
message: String?
){
    enum class Status{
        SUCCESS, ERROR, INACTIVE
    }

    private var message: String? = null

    fun getMessage(): String? {
        return message
    }
    companion object {
        fun<T> success(data: T): ResponseWrapper<T> {
            return ResponseWrapper(Status.SUCCESS, data, null)
        }

        fun<T> error(msg: String?, data: T?): ResponseWrapper<T> {
            return ResponseWrapper(Status.ERROR, data, message = msg)
        }

        fun<T> inactive(): ResponseWrapper<T> {
            return ResponseWrapper(Status.INACTIVE, null, null)
        }
    }
}