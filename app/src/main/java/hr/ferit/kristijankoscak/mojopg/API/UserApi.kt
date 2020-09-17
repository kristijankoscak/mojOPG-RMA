package hr.ferit.kristijankoscak.mojopg.API

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import hr.ferit.kristijankoscak.mojopg.Activities.MainActivity
import hr.ferit.kristijankoscak.mojopg.Fragments.ProductsFragment
import hr.ferit.kristijankoscak.mojopg.R
import hr.ferit.kristijankoscak.mojopg.Respositories.UsersRepository
import hr.ferit.kristijankoscak.mojopg.UserPreferenceManager
import org.json.JSONObject

class UserApi (context: Context, userData:MutableMap<String,String>, warningMessage: TextView): ApiHandler(context){

    private var registerApi = context.resources.getString(R.string.registerUserUrl);
    private var getCountiesApi = context.resources.getString(R.string.updateUserUrl);
    private var userData:MutableMap<String,String>;
    private var context: Context;
    private var warningMessage: TextView;
    lateinit var progressDialog: ProgressDialog;

    init {
        this.warningMessage = warningMessage;
        this.userData = userData;
        this.context = context;
    }

    fun submitRegisterRequest(method: Int){
        displayLoadingMessage()
        val request = object: StringRequest(
            method, registerApi, Response.Listener { response ->
                if(response != "user exist"){
                    UserPreferenceManager()
                        .saveUserData(JSONObject(response),userData.get("password")!!)
                    dismissLoadingMessage()
                    navigateToMainActivity()
                }
                else{
                    dismissLoadingMessage()
                    displayEmailWarning()
                }
            },
            Response.ErrorListener { error->
                Toast.makeText(context,"error: " +error.toString(), Toast.LENGTH_LONG).show()
                dismissLoadingMessage()
            })
        {
            override fun getParams(): MutableMap<String, String> {
                return userData;
            }
        }
        queue.add(request)
    };
    fun submitUpdateRequest(method: Int){
        displayLoadingMessage()
        val request = object: StringRequest(
            method, getCountiesApi, Response.Listener { response ->
                    UserPreferenceManager().saveUserData(JSONObject(response),userData.get("password")!!)
                    dismissLoadingMessage()
                    navigateToMainActivity()
            },
            Response.ErrorListener { error->
                Toast.makeText(context,"error: " +error.toString(), Toast.LENGTH_LONG).show()
                dismissLoadingMessage()
            })
        {
            override fun getParams(): MutableMap<String, String> {
                return userData;
            }
        }
        queue.add(request)
    };

    private fun displayLoadingMessage(){
        progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Učitavanje")
        progressDialog.setMessage("Provjeravamo podatke.")
        progressDialog.show()
    }
    private fun displayEmailWarning(){

        Handler().postDelayed({
            warningMessage.text ="Email se koristi!"
        }, 500)
        Handler().postDelayed({
            warningMessage.text = "";
        }, 3000)
    }

    private fun dismissLoadingMessage(){
        progressDialog.dismiss()
    }

    private fun navigateToMainActivity(){
        val mainIntent: Intent = Intent(context, MainActivity::class.java)
        context.startActivity((mainIntent))
    }
}