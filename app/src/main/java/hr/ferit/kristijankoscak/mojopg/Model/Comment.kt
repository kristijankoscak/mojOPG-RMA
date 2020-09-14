package hr.ferit.kristijankoscak.mojopg.Model

data class Comment (
    val id:String,
    val authorName:String,
    val author_id:String,
    val comment:String,
    val grade:String,
    val belongs_to:String,
    val created_at:String,
    val author_image:String
)