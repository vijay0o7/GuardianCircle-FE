package com.example.gaurdiancircle.utils

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


object Utils {

    private const val PREF_NAME = "MyAppPrefs"

    // Keys
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_PRIVACY_POLICY_ACCEPTED = "privacy_policy_accepted"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_ROLE = "user_role"

    private const val KEY_PHONE = "phone_number"
    private const val KEY_LATITUDE = "user_latitude"
    private const val KEY_LONGITUDE = "user_longitude"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Login status
    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Privacy policy
    fun setPrivacyPolicyAccepted(context: Context, accepted: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_PRIVACY_POLICY_ACCEPTED, accepted).apply()
    }

    fun isPrivacyPolicyAccepted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PRIVACY_POLICY_ACCEPTED, false)
    }

    // User details
    fun setUserDetails(context: Context, name: String?, email: String?, id: Int?, role: String?) {
        getPrefs(context).edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putInt(KEY_USER_ID, id ?: -1)
            .putString(KEY_USER_ROLE, role)
            .apply()
    }

    fun getUserLocation(context: Context): Pair<Double, Double> {
        val prefs = getPrefs(context)
        return Pair(
            prefs.getFloat("user_lat", 28.6139f).toDouble(), // default Delhi
            prefs.getFloat("user_lng", 77.2090f).toDouble()
        )
    }

    fun getUserId(context: Context): Int = getPrefs(context).getInt(KEY_USER_ID, -1)
    fun getUserName(context: Context): String? = getPrefs(context).getString(KEY_USER_NAME, null)
    fun getUserEmail(context: Context): String? = getPrefs(context).getString(KEY_USER_EMAIL, null)
    fun getUserRole(context: Context): String? = getPrefs(context).getString(KEY_USER_ROLE, null)
//    fun getUserPhone(context: Context): String? = getPrefs(context).getString(KEY_PHONE), null)

    // User location
    fun setUserLocation(context: Context, latitude: Double, longitude: Double) {
        getPrefs(context).edit()
            .putFloat(KEY_LATITUDE, latitude.toFloat())
            .putFloat(KEY_LONGITUDE, longitude.toFloat())
            .apply()
    }

    fun getLatitude(context: Context): Double {
        return getPrefs(context).getFloat(KEY_LATITUDE, 0f).toDouble()
    }

    fun getLongitude(context: Context): Double {
        return getPrefs(context).getFloat(KEY_LONGITUDE, 0f).toDouble()
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun saveLoginData(
        context: Context,
        name: String?,
        email: String?,
        userId: Int?,
        role: String?,
        phone: String? // ✅ Added phone parameter
    ) {
        getPrefs(context).edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .putInt("user_id", userId ?: -1)
            .putString("user_role", role)
            .putString("phone_number", phone) // ✅ save phone correctly
            .putBoolean("is_logged_in", true)
            .apply()
    }

    // ✅ Getter
    fun getUserPhone(context: Context): String? {
        return getPrefs(context).getString("phone_number", null)
    }


    fun clearUserSession(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun setPermissionState(context: Context, permission: String, isEnabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("perm_$permission", isEnabled)
            .apply()
    }

    fun getPermissionState(context: Context, permission: String): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean("perm_$permission", false)
    }

}
