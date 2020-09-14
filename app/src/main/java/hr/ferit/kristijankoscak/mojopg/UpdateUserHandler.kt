package hr.ferit.kristijankoscak.mojopg

class UpdateUserHandler {
    private var userData:MutableMap<String,String>

    init {
        userData = mutableMapOf()
        userData.put("nameSurname","")
        userData.put("email","")
        userData.put("password","")
        userData.put("rePassword","")
        userData.put("phoneNumber","")
        userData.put("place","")
        userData.put("address","")
        userData.put("image","")
        userData.put("familyFarm","")
        userData.put("county","")
        userData.put("userType","")
    }


    fun setKey(key:String,value:String){
        userData.put(key,value)
    }
    fun getUserData():MutableMap<String,String>{
        return userData;
    }

    fun checkPasswordsCorrectness():Boolean{
        var areSame = false;
        val password = userData.get("password");
        val rePassword = userData.get("rePassword");
        if(password == rePassword){
            areSame = true;
        }
        return areSame;
    }
    fun getPasswordLength():Int{
        val passwordLength = userData.get("password")!!.length;
        return passwordLength;
    }
    fun checkEmailCorrectness():Boolean{
        var isNormal = false;
        val email = userData.get("email");
        if((email!!.contains("@",false))&&(email!!.contains(".com",false))){
            isNormal = true;
        }
        return isNormal;
    }
    fun checkCountySelection():Boolean{
        var isSelected = false;
        var county = userData.get("county")
        if(county != "Odaberite županiju"){
            isSelected = true;
        }
        return isSelected;
    }
}