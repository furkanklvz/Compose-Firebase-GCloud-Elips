package com.klavs.bindle.data.routes

import kotlinx.serialization.Serializable


@Serializable
data object LogIn

@Serializable
data object CreateUser

@Serializable
data class CreateUserPhaseTwo(val profilePictureUri: String, val username: String, val email:String)

@Serializable
data object Greeting