package hr.ferit.kristijankoscak.mojopg.Activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import hr.ferit.kristijankoscak.mojopg.R
import hr.ferit.kristijankoscak.mojopg.UserPreferenceManager
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    lateinit var logoAnimation: Animation;
    lateinit var welcomeAnimation: Animation;
    lateinit var buttonsAnimation: Animation;

    lateinit var logoImage: ImageView;
    lateinit var welcomeMessage: TextView;
    lateinit var loginButton: Button;
    lateinit var registerButton: Button;
    lateinit var guestMode:TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initializeUIElements()
        startAnimation()
        setListeners()
        identificateUser()
    }

    private fun initializeUIElements(){
        logoAnimation = AnimationUtils.loadAnimation(this,
            R.anim.zoom_in
        )
        welcomeAnimation = AnimationUtils.loadAnimation(this,
            R.anim.top_to_bottom
        )
        buttonsAnimation = AnimationUtils.loadAnimation(this,
            R.anim.bottom_to_top
        )

        this.logoImage = findViewById(R.id.logoImage)             //findViewById(R.id.logo_image)
        this.welcomeMessage = findViewById(R.id.welcomeMessage)    //findViewById(R.id.welcome_message)
        this.loginButton = findViewById(R.id.loginButton)          //findViewById(R.id.login_button)
        this.registerButton = findViewById(R.id.registerButton)     //findViewById(R.id.register_button)
        this.guestMode = guestModeText
    }
    private fun startAnimation(){
        logoImage.startAnimation(logoAnimation)
        welcomeMessage.startAnimation(welcomeAnimation)
        loginButton.startAnimation(buttonsAnimation)
        registerButton.startAnimation(buttonsAnimation)
        guestMode.startAnimation(buttonsAnimation)
    }
    private fun setListeners(){
        loginButton.setOnClickListener { navigateToLoginActivity() }
        registerButton.setOnClickListener { navigateToRegisterActivity() }
        guestMode.setOnClickListener{ navigateToMainActivity()}
    }

    private fun navigateToLoginActivity(){
        val loginIntent: Intent = Intent(this,
            LoginActivity::class.java)
        startActivity((loginIntent))
    }
    private fun navigateToRegisterActivity(){
        val registerIntent: Intent = Intent(this,
            RegisterActivity::class.java)
        startActivity((registerIntent))
    }
    private fun navigateToMainActivity(){
        val mainIntent: Intent = Intent(this,
            MainActivity::class.java)
        startActivity((mainIntent))
    }

    private fun identificateUser(){
        var prefsEmail = UserPreferenceManager().retreiveEmail();
        if(prefsEmail != ""){
            navigateToMainActivity()
        }
    }

    override fun onResume() {
        identificateUser()
        super.onResume()
    }

    override fun onBackPressed() {
        var count = supportFragmentManager.backStackEntryCount
        if(count == 0){

        }
        else{
            supportFragmentManager.popBackStack()
        }
        super.onBackPressed()
    }
}
