package hr.ferit.kristijankoscak.mojopg.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Request
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import hr.ferit.kristijankoscak.mojopg.*
import hr.ferit.kristijankoscak.mojopg.API.UsersApi
import hr.ferit.kristijankoscak.mojopg.Fragments.*
import hr.ferit.kristijankoscak.mojopg.UserPreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_header.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {

    lateinit var toggleIcon : ActionBarDrawerToggle;
    lateinit var drawerLayout: DrawerLayout;
    lateinit var navigationMenu : NavigationView;
    lateinit var fragmentManager:FragmentManager;
    lateinit var fragmentTransaction: FragmentTransaction;

    lateinit var usersApi: UsersApi;

    override fun onCreate(savedInstanceState: Bundle?) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeUIElements()
        identificateUser()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggleIcon.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawerLayout.closeDrawers()
        if(item.itemId == R.id.menu_products){
            val productsFragment = ProductsFragment();
            val bundle = Bundle()
            bundle.putString("productsType","all")
            productsFragment.arguments = bundle;
            replaceFragment(productsFragment)
        }
        if(item.itemId == R.id.menu_geo_products){
            replaceFragment(GeoProductsFragment())
        }
        if(item.itemId == R.id.menu_my_products){
            val myProductsFragment = ProductsFragment();
            val bundle = Bundle()
            bundle.putString("productsType","userProducts")
            myProductsFragment.arguments = bundle;
            replaceFragment(myProductsFragment)
        }
        if(item.itemId == R.id.menu_add_product){
            replaceFragment(AddProductFragment())
        }
        if(item.itemId == R.id.menu_my_profile){
            replaceFragment(UserFragment())
        }
        if(item.itemId == R.id.menu_edit_profile){
            replaceFragment(UserUpdateFragment())
        }
        if(item.itemId == R.id.menu_logout){
            handleLogout()
        }
        return true;
    }

    private fun initializeUIElements(){
        drawerLayout = findViewById(R.id.drawerLayout) ;
        navigationMenu = findViewById(R.id.navigationMenu)
        navigationMenu.setNavigationItemSelectedListener(this)
        toggleIcon = ActionBarDrawerToggle(this,drawerLayout, R.string.openMenu, R.string.closeMenu)
        drawerLayout.addDrawerListener(toggleIcon);
        toggleIcon.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadInitialFragment(){
        val productsFragment = ProductsFragment();
        val bundle = Bundle()
        fragmentManager = supportFragmentManager
        fragmentTransaction = fragmentManager.beginTransaction()
        bundle.putString("productsType","all")
        productsFragment.arguments = bundle;
        fragmentTransaction.add(R.id.fragmentContainer, productsFragment)
        fragmentTransaction.commit()
    }

    private fun replaceFragment(fragment:Fragment){
        fragmentManager = supportFragmentManager;
        fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer,fragment)
        fragmentTransaction.addToBackStack("fragment")
        fragmentTransaction.commit()
    }

    private fun handleLogout(){
        val entry = supportFragmentManager.getBackStackEntryAt(0)
        supportFragmentManager.popBackStack(entry.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.executePendingTransactions()
        UserPreferenceManager().saveUserData(retrieveEmptyJSONObject(),"")
        val welcomeIntent: Intent = Intent(this, WelcomeActivity::class.java)
        startActivity((welcomeIntent))
    }

    private fun retrieveEmptyJSONObject():JSONObject{
        val emptyJSONObject = JSONObject();
        emptyJSONObject.put("id","")
        emptyJSONObject.put("name","")
        emptyJSONObject.put("email","")
        emptyJSONObject.put("user_type","")
        emptyJSONObject.put("contact","")
        emptyJSONObject.put("image_link","")
        emptyJSONObject.put("county","")
        emptyJSONObject.put("city","")
        emptyJSONObject.put("address","")
        emptyJSONObject.put("user_opg","")
        return emptyJSONObject;
    }
    private fun identificateUser() {
        val prefsEmail = UserPreferenceManager().retreiveEmail();
        loadUsers()
        if(prefsEmail != ""){
            loadUserData("member")
        }
        else {
            loadUserData("guest")
        }
    }
    private fun loadUsers(){
        usersApi = UsersApi(this)
        usersApi.submitGetUsersRequest(Request.Method.POST)
        loadInitialFragment()
    }

    private fun loadUserData(type:String){
        val progressDialog = ProgressDialog(this)
        val prefsUserType = UserPreferenceManager().retreiveUserType()
        displayLoadingMessage(progressDialog)
        Handler().postDelayed({
            if(type == "member"){
                setGuestMode(false)
                setUserData()
                if(prefsUserType == "buyer"){
                    setBuyerMode()
                }
            }
            else{
                setGuestMode(true)
                setGuestData()
            }
            dismissLoadingMessage(progressDialog)
        }, 2000)
    }

    private fun setGuestMode(state:Boolean){
        navigationMenu.menu.setGroupVisible(R.id.group_user_options,!state)
    }
    private fun setBuyerMode(){
        navigationMenu.menu.findItem(R.id.menu_my_products).isVisible = false
        navigationMenu.menu.findItem(R.id.menu_add_product).isVisible = false
        navigationMenu.menu.findItem(R.id.menu_my_profile).isVisible = false
    }

    private fun setGuestData(){
        headerUserName.text = "goste !";
        headerUserImage.setImageResource(R.drawable.ic_no_user)
        headerLoginText.setOnClickListener{ navigateToLoginActivity()}
        headerRegisterText.setOnClickListener{navigateToRegisterActivity()}
    }
    private fun setUserData(){
        val baseImageUrl = "http://192.168.0.174/Kopg/public/storage/user_images/"
        headerUserName.text = UserPreferenceManager().retreiveName() +" !";
        Picasso.get()
            .load(baseImageUrl+ UserPreferenceManager().retreiveUserImage())
            .fit()
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(android.R.drawable.stat_notify_error)
            .into(headerUserImage)
        headerLoginText.visibility = View.INVISIBLE
        headerRegisterText.visibility = View.INVISIBLE
    }

    private fun displayLoadingMessage(progressDialog: ProgressDialog){
        progressDialog.setTitle("Učitavanje")
        progressDialog.setMessage("Postavljamo korisničke podatke.")
        progressDialog.show()
    }
    private fun dismissLoadingMessage(progressDialog: ProgressDialog){
        progressDialog.dismiss()
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


}
