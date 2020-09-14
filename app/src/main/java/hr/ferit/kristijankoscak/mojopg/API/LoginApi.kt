package hr.ferit.kristijankoscak.mojopg.API

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.widget.TextView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import hr.ferit.kristijankoscak.mojopg.Activities.MainActivity
import hr.ferit.kristijankoscak.mojopg.R
import hr.ferit.kristijankoscak.mojopg.UserPreferenceManager
import org.json.JSONObject

class LoginApi(context: Context, warningMessage: TextView): ApiHandler(context) {

    private var loginApi = context.resources.getString(R.string.loginUserUrl);

    private var context: Context;
    lateinit private var email:String;
    lateinit private var password:String;
    lateinit private var progressDialog: ProgressDialog;
    private var warningMessage: TextView;

    init {
        this.context = context;
        this.warningMessage = warningMessage
    }


    fun submitLoginRequest(method: Int){
        displayLoadingMessage()
        val request = object: StringRequest(method, loginApi,
            Response.Listener { response ->
                Log.d("user",response)
                if(response != "no user"){
                    dismissLoadingMessage()
                    UserPreferenceManager().saveUserData(JSONObject(response),password)
                    handleLogin(true)
                }
                else{
                    dismissLoadingMessage()
                    handleLogin(false)
                }
            },
            Response.ErrorListener { error->
                dismissLoadingMessage()
            })
        {
            override fun getParams(): MutableMap<String, String> {
                var map = HashMap<String,String>();
                map.put("email",email);
                map.put("password",password);
                return map;
            }
        }
        queue.add(request)
    };

    fun setUserEntries(password:String,email:String){
        this.password = password;
        this.email = email;
    }

    private fun displayLoadingMessage(){
        progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Učitavanje")
        progressDialog.setMessage("Provjeravamo vaše podatke.")
        progressDialog.show()
    }
    private fun dismissLoadingMessage(){
        progressDialog.dismiss()
    }
    private fun handleLogin(state: Boolean){
        if(state){
            navigateToMainActivity()
        }
        else{
            Handler().postDelayed({
                warningMessage.text = "Neispravan email ili lozinka!";
            }, 500)
            removeWarningMessage()
        }
    }
    private fun navigateToMainActivity(){
        val mainIntent: Intent = Intent(context, MainActivity::class.java)
        context.startActivity((mainIntent))
    }
    private fun removeWarningMessage(){
        Handler().postDelayed({
            warningMessage.text = "";
        }, 3000)
    }

}