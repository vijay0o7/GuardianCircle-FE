package com.example.gaurdiancircle.retrofit

import com.example.gaurdiancircle.data.EmergencyContactListResponse
import com.example.gaurdiancircle.data.GuardianMessage
import com.example.gaurdiancircle.model.BasicResponse
import com.example.gaurdiancircle.models.GuardianListResponse
import com.example.gaurdiancircle.responses.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // LOGIN
    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("email_or_phone") emailOrPhone: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    // SIGNUP
    @FormUrlEncoded
    @POST("signup.php")
    fun signup(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("phone_number") phone: String,
        @Field("password") password: String,
        @Field("role") role: String
    ): Call<SignUpResponse>

    // DASHBOARD DATA
    @GET("dashboard.php")
    fun getDashboard(
        @Query("user_id") userId: Int
    ): Call<DashboardResponse>

    // ✅ Add Guardian
    @FormUrlEncoded
    @POST("add_guardian.php")
    fun addGuardian(
        @Field("user_id") userId: Int,
        @Field("full_name") fullName: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("gender") gender: String,
        @Field("user_name") userName: String
    ): Call<BasicResponse>

    // ✅ Get Guardians List
    @FormUrlEncoded
    @POST("get_guardians.php")
    fun getGuardians(
        @Field("user_id") userId: Int
    ): Call<GuardianListResponse>

    // ✅ Delete Guardian
    @FormUrlEncoded
    @POST("delete_guardians.php")
    fun deleteGuardian(
        @Field("guardian_id") guardianId: String
    ): Call<BasicResponse>

    // ✅ Update Guardian
    @FormUrlEncoded
    @POST("update_guardian.php")
    fun updateGuardian(
        @Field("guardian_id") guardianId: String,
        @Field("full_name") name: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("gender") gender: String
    ): Call<BasicResponse>

    // ✅ Add Emergency Contact
    @FormUrlEncoded
    @POST("add_emergency_contact.php")
    fun addEmergencyContact(
        @Field("user_id") userId: Int,
        @Field("name") name: String,
        @Field("phone") phone: String,
        @Field("relation") relation: String
    ): Call<BasicResponse>

    // ✅ Get Emergency Contacts
    @FormUrlEncoded
    @POST("get_emergency_contacts.php")
    fun getEmergencyContacts(
        @Field("user_id") userId: Int
    ): Call<EmergencyContactListResponse>

    // ✅ Delete Emergency Contact
    @FormUrlEncoded
    @POST("delete_emergency_contact.php")
    fun deleteEmergencyContact(
        @Field("contact_id") contactId: String
    ): Call<BasicResponse>

    // ✅ Update Live Location
    @FormUrlEncoded
    @POST("update_location.php")
    fun updateLocation(
        @Field("user_id") userId: Int,
        @Field("shared_with") sharedWith: String,
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double
    ): Call<BasicResponse>

    // ✅ Get Latest Location of User (for guardian)
    @FormUrlEncoded
    @POST("get_location.php")
    fun getLocation(
        @Field("user_id") userId: Int,
        @Field("shared_with") sharedWith: String
    ): Call<LocationResponse>

    // ✅ Share live location with a guardian
    @FormUrlEncoded
    @POST("share_location.php")
    fun shareLocation(
        @Field("user_id") userId: Int,
        @Field("shared_with") sharedWith: String,
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double
    ): Call<BasicResponse>

    @GET("fetch_shared_locations.php")
    fun getSharedLocation(
        @Query("shared_with") guardianEmail: String
    ): Call<GuardedLocationResponse>

    @FormUrlEncoded
    @POST("get_user_id.php")
    suspend fun getUserName(
        @Field("email") email: String
    ): Response<UserResponse>

    data class UserResponse(
        val status: String,
        val user_names: List<String>?
    )

    @FormUrlEncoded
    @POST("accept_user.php")
    suspend fun acceptUser(
        @Field("email") email: String,
        @Field("userName") userName: String
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("reject_user.php")
    suspend fun rejectUser(
        @Field("email") email: String,
        @Field("userName") userName: String
    ): Response<GenericResponse>

    data class GenericResponse(
        val status: String,
        val message: String?
    )

    @FormUrlEncoded
    @POST("send_message.php")
    fun sendMessage(
        @Field("user_id") userId: Int,
        @Field("guardian_email") guardianEmail: String,
        @Field("message") message: String
    ): Call<ApiResponse>

    @GET("get_messages.php")
    fun getMessagesByEmail(
        @Query("guardian_email") guardianEmail: String
    ): Call<List<GuardianMessage>>
}
