package hr.ferit.kristijankoscak.mojopg.Activities

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.widget.*
import com.android.volley.Request
import hr.ferit.kristijankoscak.mojopg.*
import hr.ferit.kristijankoscak.mojopg.API.CountiesApi
import hr.ferit.kristijankoscak.mojopg.API.UserApi
import hr.ferit.kristijankoscak.mojopg.Fragments.UserUpdateFragment
import kotlinx.android.synthetic.main.activity_register.*
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class RegisterActivity : AppCompatActivity() {

    lateinit var progressBar:ProgressBar;
    lateinit var nameSurname:EditText;
    lateinit var email:EditText;
    lateinit var password:EditText;
    lateinit var repassword:EditText;
    lateinit var counties: Spinner;
    lateinit var familyFarm:EditText;
    lateinit var phoneNumber:EditText;
    lateinit var place:EditText;
    lateinit var address:EditText;
    lateinit var image:ImageView;
    lateinit var userTypeGroup: RadioGroup;
    lateinit var userType:String;
    lateinit var emailWarning:TextView;
    lateinit var passwordLengthWarning:TextView;
    lateinit var samePasswordWarning:TextView;
    lateinit var countyWarning:TextView;

    lateinit var countiesAdapter:ArrayAdapter<String>

    lateinit var btnSelectImage:Button;
    lateinit var btnRegister:Button

    private val externalStoragePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    private val externalStorageRequestCode = 999;

    private var imageIsSelected = false;

     lateinit var imageFilePath: Uri;
     lateinit var imageInputStream:InputStream;
     lateinit var imageBitMap:Bitmap;

    lateinit var countiesApiHandler: CountiesApi;
    lateinit var updateUserHandler: UpdateUserHandler;
    lateinit var userApi: UserApi;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initializeUIElements()
        fetchUserType()
        setUpProgressBar()
        setListeners()
        fetchCounties()
    }

    private fun initializeUIElements(){
        progressBar = registerProgressBar
        nameSurname = registerUserName
        email = registerUserEmail
        password = registerUserPassword
        repassword = registerUserPasswordRepeat
        counties = registerUserCounty
        familyFarm = registerUserFarm
        phoneNumber = registerUserPhone
        place = registerUserPlace
        address = registerUserAddress
        image = registerUserImage
        userTypeGroup = registerUserGroup
        btnSelectImage = buttonSelectUserImage
        btnRegister = buttonRegister
        emailWarning = registerUserEmailWarning
        passwordLengthWarning = registerPasswordLengthWarning
        samePasswordWarning = registerPasswordSameWarning
        countyWarning = registerCountyWarning
    }

    private fun fetchUserType(){
        userType = findViewById<RadioButton>(userTypeGroup.checkedRadioButtonId).text.toString();
    }

    private fun setUpProgressBar(){
        progressBar.max = 10 ;
        progressBar.progress = 0;
    }

    private fun setListeners(){
        listenTextInput(nameSurname)
        listenTextInput(email)
        listenTextInput(password)
        listenTextInput(repassword)
        listenTextInput(familyFarm)
        listenTextInput(phoneNumber)
        listenTextInput(place)
        listenTextInput(address)
        listenCountySelection(counties)
        userTypeGroup.setOnCheckedChangeListener { group, checkedId -> listenUserGroupSelection(group.checkedRadioButtonId) }
        btnSelectImage.setOnClickListener { handleImageSelection() }
        btnRegister.setOnClickListener { handleRegistration() }
    }

    private fun fetchCounties(){
        countiesApiHandler = CountiesApi(this,counties);
        countiesApiHandler.submitGetCountiesRequest(Request.Method.POST);

    }

    private fun listenTextInput(editText:EditText){
        var inputIsEmpty = true;
        editText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) { }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(inputIsEmpty){
                    updateProgress(1)
                    inputIsEmpty = false;
                }
                if(s.toString().equals("") && progressBar.progress>0){
                    updateProgress(-1)
                    inputIsEmpty = true;
                }
            }
        })
    }

    private fun handleRegistration(){
        updateUserHandler = UpdateUserHandler();
        fetchUserInputs()
        if(validateUserInputs()){
            userApi = UserApi(this, updateUserHandler.getUserData(), countyWarning);
            userApi.submitRegisterRequest(Request.Method.POST)
        }
    }

    private fun validateUserInputs():Boolean{
        val minimumPasswordLength = 8;
        var inputsAreValid = false;

        val emailIsValid = updateUserHandler.checkEmailCorrectness();
        val passwordsAreSame = updateUserHandler.checkPasswordsCorrectness();
        val passwordLength = updateUserHandler.getPasswordLength();
        val countyIsValid = updateUserHandler.checkCountySelection()

        if(!emailIsValid) {
            displayWarningMessage(emailWarning,"Neispravna email adresa!")
        }
        if(!passwordsAreSame) {
            displayWarningMessage(samePasswordWarning,"Lozinke se ne podudaraju!")
        }
        if(passwordLength<minimumPasswordLength) {
            displayWarningMessage(passwordLengthWarning,"Minimalno 8 znakova!")
        }
        if(!countyIsValid) {
            displayWarningMessage(countyWarning,"Odaberite županiju!")
        }
        if(emailIsValid && passwordsAreSame && (passwordLength>=minimumPasswordLength) && countyIsValid){
                inputsAreValid =true;
        }
        return inputsAreValid;
    }
    private fun displayWarningMessage(textView:TextView,message:String){
        textView.text = message
        Handler().postDelayed({
            textView.text = "";
        }, 3000)
    }
    private fun fetchUserInputs(){
        updateUserHandler.setKey("nameSurname",nameSurname.text.toString())
        updateUserHandler.setKey("email",email.text.toString())
        updateUserHandler.setKey("password",password.text.toString())
        updateUserHandler.setKey("rePassword",repassword.text.toString())
        updateUserHandler.setKey("phoneNumber",phoneNumber.text.toString())
        updateUserHandler.setKey("place",place.text.toString())
        updateUserHandler.setKey("address",address.text.toString())
        updateUserHandler.setKey("image",imageToString(imageBitMap))
        updateUserHandler.setKey("familyFarm",familyFarm.text.toString())
        updateUserHandler.setKey("county",counties.selectedItem.toString())
        updateUserHandler.setKey("userType",userType)
    }
    private fun listenUserGroupSelection(radioButtonId:Int){
        val radioButton = findViewById<RadioButton>(radioButtonId)
        userType = radioButton.text.toString();
        handleUserGroupSelection(userType);
        toggleRegisterButton();
    }
    private fun handleUserGroupSelection(type:String){
        if(type == "Kupac"){
            familyFarm.visibility = View.INVISIBLE;
            progressBar.max = 9;
            if(familyFarm.text.toString()!="") {
                familyFarm.text.clear()
                updateProgress(1)
            }
        }
        if(type == "Prodavač"){
            familyFarm.visibility = View.VISIBLE;
            progressBar.max = 10;
            if(familyFarm.text.toString() != ""){
                updateProgress(1)
            }
        }
    }

    private fun handleImageSelection(){
        if(hasPermissionCompat(externalStoragePermission)){
            openGallery()
        }
        else{
            requestPermisionCompat(arrayOf(externalStoragePermission),externalStorageRequestCode)
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
                    Toast.makeText(this, R.string.permissionNotGranted, Toast.LENGTH_LONG).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK && requestCode == externalStorageRequestCode){
            imageFilePath = data?.data!!;
            try {
                imageInputStream = contentResolver.openInputStream(imageFilePath)!!
                imageBitMap = BitmapFactory.decodeStream(imageInputStream)
                image.setImageBitmap(imageBitMap)
            }
            catch (e:FileNotFoundException){
                e.printStackTrace();
            }

            if(!imageIsSelected){
                updateProgress(1);
                imageIsSelected = true;
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun imageToString(bitMap:Bitmap):String{
        var outputStream:ByteArrayOutputStream = ByteArrayOutputStream();
        bitMap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        var imageBytes = outputStream.toByteArray();

        var encodedImage = Base64.encodeToString(imageBytes,Base64.DEFAULT)
        return encodedImage;
    }

    private fun listenCountySelection(spinner:Spinner){
        var countyIsSelected = false
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                if(!countyIsSelected){
                    updateProgress(1)
                    countyIsSelected = true;
                }
                if( spinner.selectedItem.toString() == "Odaberite županiju"){
                    updateProgress(-1)
                    countyIsSelected = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun updateProgress(value:Int){
        progressBar.progress+=value;
        displayProgress()
        toggleRegisterButton()
    }
    private fun displayProgress(){
        ObjectAnimator.ofInt(progressBar,"progress",progressBar.progress)
            .start()
    }
    private fun toggleRegisterButton(){
        if(userType == "Prodavač"){
            if(progressBar.progress ==10){
                btnRegister.visibility = View.VISIBLE;
            }
            else{
                btnRegister.visibility = View.INVISIBLE;
            }
        }
        if(userType == "Kupac"){
            if(progressBar.progress == 9){
                btnRegister.visibility = View.VISIBLE;
            }
            else{
                btnRegister.visibility = View.INVISIBLE;
            }
        }
    }
}
