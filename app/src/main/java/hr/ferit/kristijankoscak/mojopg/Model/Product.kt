package hr.ferit.kristijankoscak.mojopg.Model

data class Product (
    val id:Int,
    val type:String,
    val detail_type:String,
    val price:String,
    val county:String,
    val location:String,
    val contact:String,
    val user:String,
    val user_id:String,
    val user_opg:String,
    val image_link:String,
    val quantity:String,
    val is_reported:String
)