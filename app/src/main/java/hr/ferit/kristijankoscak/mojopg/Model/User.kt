package hr.ferit.kristijankoscak.mojopg.Model

data class User (
    val id:String,
    val name:String,
    val email:String,
    val user_type:String,
    val contact:String,
    val image_link:String,
    val county:String,
    val city:String,
    val address:String,
    val user_opg:String
)