package com.kotliny.network.api.caller.model

import com.kotliny.network.api.caller.serializer.FullType
import com.kotliny.network.api.caller.serializers.ApiSerializer

class XmlApiSerializer : ApiSerializer {

    override fun type() = ApiSerializer.Type.XML

    override fun serialize(data: Any): String {
        val model = (data as Person)

        return buildString {
            appendLine("<Person>")
            setParam("firstName", model.firstName)
            setParam("lastName", model.lastName)
            setParam("age", model.age.toString())
            setParam("gender", model.gender)
            setParam("address", model.address)
            setParam("phoneNumber", model.phoneNumber)
            setParam("email", model.email)
            setParam("occupation", model.occupation)
            setParam("nationality", model.nationality)
            setParam("birthDate", model.birthDate)
            appendLine("</Person>")
        }
    }

    override fun <T : Any> deserialize(data: String, type: FullType<T>): Result<T> {
        val person = Person(
            firstName = data.getParam("firstName"),
            lastName = data.getParam("lastName"),
            age = data.getParam("age").toInt(),
            gender = data.getParam("gender"),
            address = data.getParam("address"),
            phoneNumber = data.getParam("phoneNumber"),
            email = data.getParam("email"),
            occupation = data.getParam("occupation"),
            nationality = data.getParam("nationality"),
            birthDate = data.getParam("birthDate"),
            spouse = null
        )

        @Suppress("UNCHECKED_CAST")
        return Result.success(person as T)
    }

    private fun StringBuilder.setParam(key: String, value: String) {
        appendLine("<$key>$value</$key>")
    }

    private fun String.getParam(key: String): String {
        return substringAfter("<$key>")
            .substringBefore("</$key>")
    }
}
