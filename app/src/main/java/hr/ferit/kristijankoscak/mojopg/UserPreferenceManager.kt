package hr.ferit.kristijankoscak.mojopg

import android.content.Context
import org.json.JSONObject

class UserPreferenceManager {
    companion object {
        const val PREFS_FILE = "LoginPreferences"
        const val PREFS_KEY_ID = "keyID";
        const val PREFS_KEY_NAME = "keyName";
        const val PREFS_KEY_EMAIL = "keyEmail";
        const val PREFS_KEY_USER_TYPE = "keyUserType";
        const val PREFS_KEY_CONTACT = "keyContact";
        const val PREFS_KEY_IMAGE_LINK = "keyImageLink";
        const val PREFS_KEY_COUNTY = "keyCounty";
        const val PREFS_KEY_CITY = "keyCity";
        const val PREFS_KEY_ADDRESS = "keyAddress";
        const val PREFS_KEY_USER_OPG = "keyUserOpg";
        const val PREFS_KEY_PASSWORD = "keyPassword";

    }

    fun saveUserData(jsonObject: JSONObject, password:String) {
        val sharedPreferences = UserDataApplicaton.ApplicationContext.getSharedPreferences(
            PREFS_FILE, Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putString(PREFS_KEY_ID, jsonObject.getString("id"))
        editor.putString(PREFS_KEY_NAME, jsonObject.getString("name"))
        editor.putString(PREFS_KEY_EMAIL, jsonObject.getString("email"))
        editor.putString(PREFS_KEY_USER_TYPE, jsonObject.getString("user_type"))
        editor.putString(PREFS_KEY_CONTACT, jsonObject.getString("contact"))
        editor.putString(PREFS_KEY_IMAGE_LINK, jsonObject.getString("image_link"))
        editor.putString(PREFS_KEY_COUNTY, jsonObject.getString("county"))
        editor.putString(PREFS_KEY_CITY, jsonObject.getString("city"))
        editor.putString(PREFS_KEY_ADDRESS, jsonObject.getString("address"))
        editor.putString(PREFS_KEY_USER_OPG, jsonObject.getString("user_opg"))
        editor.putString(PREFS_KEY_PASSWORD, password)
        editor.apply()
    }

    fun retreiveEmail() : String {
        val sharedPreferences = UserDataApplicaton.ApplicationContext.getSharedPreferences(
            PREFS_FILE, Context.MODE_PRIVATE
        )
        return sharedPreferences.getString(PREFS_KEY_EMAIL,"")!!
    }
    fun retreiveName() : String {
        val sharedPreferences = UserDataApplicaton.ApplicationContext.getSharedPreferences(
            PREFS_FILE, Context.MODE_PRIVATE
        )
        return sharedPreferences.getString(PREFS_KEY_NAME,"")!!
    }
    fun retreiveUserImage() : String {
        val sharedPreferences = UserDataApplicaton.ApplicationContext.getSharedPreferences(
            PREFS_FILE, Context.MODE_PRIVATE
        )
        return sharedPreferences.getString(PREFS_KEY_IMAGE_LINK,"")!!
    }
    fun retreiveUserID() : String {
        val sharedPreferences = UserDataApplicaton.ApplicationContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        return sharedPreferences.getString(PREFS_KEY_ID,"")!!
    }
    fun retreiveUserType() : String {
        val sharedPreferences = UserDataApplicaton.ApplicationContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        return sharedPreferences.getString(PREFS_KEY_USER_TYPE,"")!!
    }
    fun retreiveUserOpg() : String {
        val sharedPreferences = UserDataApplicaton.ApplicationContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        return sharedPreferences.getString(PREFS_KEY_USER_OPG,"")!!
    }
    fun retreivePassword() : String {
        val sharedPreferences = UserDataApplicaton.ApplicationContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        return sharedPreferences.getString(PREFS_KEY_PASSWORD,"")!!
    }
}