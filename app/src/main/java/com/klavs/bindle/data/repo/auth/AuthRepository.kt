package com.klavs.bindle.data.repo.auth

import android.net.Uri
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun loginUser(email: String, password: String): Resource<AuthResult>
    suspend fun createUserWithEmailAndPassword(email: String, password: String): Resource<AuthResult>
    suspend fun signOut(uid:String)
    suspend fun signInWithGoogle(idToken: String): Resource<AuthResult>
    suspend fun checkIfUserExists(email: String): Resource<Boolean>
    suspend fun reloadUserInformation(): Resource<Boolean>
    suspend fun sendPasswordResetEmail(email: String): Resource<Boolean>
    suspend fun sendEmailVerification(): Resource<Boolean>
    suspend fun updatePassword(currentPassword: String, newPassword: String) : Resource<Boolean>
    suspend fun updateEmail(password: String, newEmail: String): Resource<Boolean>
    suspend fun updateUserPhotoUrl(imageUri: Uri?): Resource<Boolean>
    fun getCurrentUser(): Flow<FirebaseUser?>
    suspend fun deleteAccount(user: FirebaseUser, password: String): Resource<Unit>
}