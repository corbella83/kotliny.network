package com.kotliny.network.api.caller.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Person(
    @SerialName("firstName") val firstName: String,
    @SerialName("lastName") val lastName: String,
    @SerialName("age") val age: Int,
    @SerialName("gender") val gender: String,
    @SerialName("address") val address: String,
    @SerialName("phoneNumber") val phoneNumber: String,
    @SerialName("email") val email: String,
    @SerialName("occupation") val occupation: String,
    @SerialName("nationality") val nationality: String,
    @SerialName("birthDate") val birthDate: String,
    @SerialName("spouse") val spouse: Person?,
)

@Serializable
data class Error(
    @SerialName("code") val code: String,
    @SerialName("message") val message: String
)
