package hr.ferit.kristijankoscak.mojopg.Fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.security.keystore.UserPresenceUnavailableException
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.squareup.picasso.Picasso
import hr.ferit.kristijankoscak.mojopg.*
import hr.ferit.kristijankoscak.mojopg.API.CountiesApi
import hr.ferit.kristijankoscak.mojopg.API.UserApi
import hr.ferit.kristijankoscak.mojopg.Model.User
import hr.ferit.kristijankoscak.mojopg.Respositories.CountiesRepository
import hr.ferit.kristijankoscak.mojopg.Respositories.UsersRepository
import kotlinx.android.synthetic.main.fragment_add_product.view.*
import kotlinx.android.synthetic.main.fragment_user_update.view.*
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class UserUpdateFragment : Fragment() {

    lateinit private var loggedUser:User;
    lateinit private var imageBitMap:Bitmap;
    lateinit private var updateUserHandler:UpdateUserHandler;
    private var imageIsPicked = false;
    lateinit private var userType:String;

    private val externalStoragePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    private val externalStorageRequestCode = 999;


    companion object {
        fun newInstance(): ProductsFragment {
            return ProductsFragment()
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_update, container, false);

        fetchCounties(view)
        loadUserData(view)
        setListeners(view)

        return view;
    }

    private fun setListeners(view: View){
        view.editUserType.setOnCheckedChangeListener { group, checkedId -> listenUserTypeSelection(view,group.checkedRadioButtonId) }
        view.editUserButton.setOnClickListener { handleUserUpdate(view) }
        view.editUserImageButton.setOnClickListener { handleImageSelection() }
    }

    private fun fetchCounties(view:View){
        val countiesApiHandler = CountiesApi(context!!,view.editUserCounties);
        countiesApiHandler.submitGetCountiesRequest(Request.Method.POST);
    }
    private fun loadUserData(view:View){
        loggedUser = UsersRepository.get(UserPreferenceManager().retreiveUserID().toInt())!!
        Log.d("user",loggedUser.toString())
        Handler().postAtTime({
            loadImage(view)
        },1000)
        setUserType(view)
        setUserCounty(view)
        loadRest(view)

    }
    private fun loadImage(view:View){
        val userBaseUrl = "http://192.168.0.174/Kopg/public/storage/user_images/"
        Log.d("tag",userBaseUrl+loggedUser.image_link)
        Picasso.get()
            .load(userBaseUrl+loggedUser.image_link)
            //.fit()
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(android.R.drawable.stat_notify_error)
            .into(object: com.squareup.picasso.Target{
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    view.editUserImage.setImageBitmap(bitmap)
                    imageBitMap = bitmap!!;
                    Log.d("tag",imageBitMap.toString())
                    imageIsPicked = true;
                }
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
            })
    }
    private fun setUserType(view:View) {
        Log.d("userTyp",loggedUser.user_type)
        if (loggedUser.user_type == "buyer") {
            view.buyer.isChecked = true;
            view.seller.isChecked = false;
            view.editUserFarm.visibility = View.INVISIBLE;
            userType = "Kupac"
        }
        if (loggedUser.user_type == "seller") {
            view.buyer.isChecked = false;
            view.seller.isChecked = true;
            view.editUserFarm.visibility = View.VISIBLE;
            userType = "Prodavač"
        }
    }
    private fun setUserCounty(view:View){
        Log.d("user",loggedUser.county)
        Handler().postDelayed({
            view.editUserCounties.setSelection(CountiesRepository.retreiveCounties().indexOf(loggedUser.county))
        }, 500)

    }
    private fun loadRest(view: View){
        view.editUserName.setText(loggedUser.name)
        view.editUserEmail.setText(loggedUser.email)
        view.editUserPassword.setText(UserPreferenceManager().retreivePassword())
        view.editUserRepeatPassword.setText(UserPreferenceManager().retreivePassword())
        view.editUserPhone.setText(loggedUser.contact)
        view.editUserPlace.setText(loggedUser.city)
        view.editUserAddress.setText(loggedUser.address)
        view.editUserFarm.setText(loggedUser.user_opg)
    }

    private fun listenUserTypeSelection(view: View,buttonID:Int){
        val radioButton = activity!!.findViewById<RadioButton>(buttonID)
        if(radioButton.text == "Kupac"){
            view.editUserFarm.visibility = View.INVISIBLE
            userType = "Kupac"
        }
        if(radioButton.text == "Prodavač"){
            view.editUserFarm.visibility = View.VISIBLE;
            userType = "Prodavač"
        }
    }

    private fun handleUserUpdate(view:View){
        fetchUserInputs(view)
        if(validateUserInputs(view)){
            val userApi = UserApi(context!!,updateUserHandler.getUserData(),view.editCountyWarningText)
            userApi.submitUpdateRequest(Request.Method.POST)
        }
    }
    private fun validateUserInputs(view: View):Boolean{
        val minimumPasswordLength = 8;
        var inputsAreValid = false;

        val emailIsValid = updateUserHandler.checkEmailCorrectness();
        val passwordsAreSame = updateUserHandler.checkPasswordsCorrectness();
        val passwordLength = updateUserHandler.getPasswordLength();
        val countyIsValid = updateUserHandler.checkCountySelection()

        if(!emailIsValid) {
            displayWarningMessage(view.editEmailWarningText,"Neispravna email adresa!")
        }
        if(!passwordsAreSame) {
            displayWarningMessage(view.editSamePasswordWarning,"Lozinke se ne podudaraju!")
        }
        if(passwordLength<minimumPasswordLength) {
            displayWarningMessage(view.editPasswordLengthWarning,"Minimalno 8 znakova!")
        }
        if(!countyIsValid) {
            displayWarningMessage(view.editCountyWarningText,"Odaberite županiju!")
        }
        if(emailIsValid && passwordsAreSame && (passwordLength>=minimumPasswordLength) && countyIsValid && imageIsPicked){
            inputsAreValid =true;
        }
        return inputsAreValid;
    }
    private fun fetchUserInputs(view:View){
        updateUserHandler = UpdateUserHandler()
        updateUserHandler.setKey("nameSurname",view.editUserName.text.toString())
        updateUserHandler.setKey("email",view.editUserEmail.text.toString())
        updateUserHandler.setKey("password",view.editUserPassword.text.toString())
        updateUserHandler.setKey("rePassword",view.editUserRepeatPassword.text.toString())
        updateUserHandler.setKey("phoneNumber",view.editUserPhone.text.toString())
        updateUserHandler.setKey("place",view.editUserPlace.text.toString())
        updateUserHandler.setKey("address",view.editUserAddress.text.toString())
        updateUserHandler.setKey("image",imageToString(imageBitMap))
        updateUserHandler.setKey("familyFarm",view.editUserFarm.text.toString())
        updateUserHandler.setKey("county",view.editUserCounties.selectedItem.toString())
        Log.d("userType",userType)
        updateUserHandler.setKey("userType",userType)
        updateUserHandler.setKey("userID",UserPreferenceManager().retreiveUserID())
    }

    private fun handleImageSelection(){
        if((activity as AppCompatActivity).hasPermissionCompat(externalStoragePermission)){
            openGallery()
        }
        else{
            (activity as AppCompatActivity).requestPermisionCompat(arrayOf(externalStoragePermission),externalStorageRequestCode)
        }
    }

    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, externalStorageRequestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        when(requestCode){
            externalStorageRequestCode -> {
                if(grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openGallery()
                }
                else{
                    Toast.makeText(context, R.string.permissionNotGranted, Toast.LENGTH_LONG).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK && requestCode == externalStorageRequestCode){
            val imageFilePath = data?.data!!;
            try {
                val imageInputStream = context!!.contentResolver.openInputStream(imageFilePath)!!
                imageBitMap = BitmapFactory.decodeStream(imageInputStream)
                view!!.editUserImage.setImageBitmap(imageBitMap)
            }
            catch (e: FileNotFoundException){
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun imageToString(bitMap:Bitmap):String{
        var outputStream: ByteArrayOutputStream = ByteArrayOutputStream();
        bitMap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        var imageBytes = outputStream.toByteArray();
        var encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        return encodedImage;
    }
    private fun displayWarningMessage(textView: TextView, message:String){
        textView.text = message
        Handler().postDelayed({
            textView.text = "";
        }, 3000)
    }
}