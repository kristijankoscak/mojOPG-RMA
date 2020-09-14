package hr.ferit.kristijankoscak.mojopg.Activities


import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.android.volley.Request
import hr.ferit.kristijankoscak.mojopg.API.LoginApi
import hr.ferit.kristijankoscak.mojopg.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    lateinit var email:EditText;
    lateinit var password:EditText;
    lateinit var loginButton: Button;
    lateinit var warningMessage: TextView;
    lateinit var loginApiHandler: LoginApi;

    override fun onCreate(savedInstanceState: Bundle?) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeElemenets()
        setUpLoginApiHandler()
        setListener()
    }

    private fun initializeElemenets(){
        email = etEmail;
        password = etPassword;
        loginButton = buttonLogin;
        warningMessage = tvWarningMessage
    }
    private fun setUpLoginApiHandler(){
        loginApiHandler = LoginApi(this, warningMessage);
    }
    private fun setListener(){
        loginButton.setOnClickListener { checkUserEntries() }
    }


    private fun checkUserEntries(){
        if( (email.text.toString() != "") &&(password.text.toString()!="")) {
            loginApiHandler.setUserEntries(password.text.toString(),etEmail.text.toString())
            loginApiHandler.submitLoginRequest(Request.Method.POST)
        }
        else{
            warningMessage.text = "Unesite email i lozinku!"
            removeWarningMessage()
        }
    }

    private fun removeWarningMessage(){
        Handler().postDelayed({
            warningMessage.text = "";
        }, 3000)
    }

}
