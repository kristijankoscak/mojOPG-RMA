package hr.ferit.kristijankoscak.mojopg.API

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import hr.ferit.kristijankoscak.mojopg.Model.User
import hr.ferit.kristijankoscak.mojopg.R
import hr.ferit.kristijankoscak.mojopg.Respositories.UsersRepository
import org.json.JSONArray
import org.json.JSONObject

class UsersApi (context: Context): ApiHandler(context) {

    private var getUsersApi = context.resources.getString(R.string.getUsersUrl);
    private var context: Context;
    lateinit var usersList:MutableList<User>
    init {
        this.context = context;
    }

    fun submitGetUsersRequest(method: Int){
        val request = StringRequest(
            method, getUsersApi,
            Response.Listener { response ->
                val jsonArray = JSONArray(response)
                fillUserList(jsonArray)
                UsersRepository.saveUsers(usersList)
            },
            Response.ErrorListener { error->
                Toast.makeText(this.context, error.message, Toast.LENGTH_LONG).show();
            }
        )
        queue.add(request)
    };

    fun fillUserList(productsArray: JSONArray) {
        usersList = mutableListOf()
        for (i in 0 until productsArray.length()) {
            var jsonProductObject = JSONObject(productsArray[i].toString())
            usersList.add(
                User(
                    jsonProductObject.getString("id"),
                    jsonProductObject.getString("name"),
                    jsonProductObject.getString("email"),
                    jsonProductObject.getString("user_type"),
                    jsonProductObject.getString("contact"),
                    jsonProductObject.getString("image_link"),
                    jsonProductObject.getString("county"),
                    jsonProductObject.getString("city"),
                    jsonProductObject.getString("address"),
                    jsonProductObject.getString("user_opg")
                )
            )
        }
    }



}