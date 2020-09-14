package hr.ferit.kristijankoscak.mojopg.Fragments.Adapters

import android.security.keystore.UserPresenceUnavailableException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import hr.ferit.kristijankoscak.mojopg.Fragments.InteractionListener.CommentInteractionListener
import hr.ferit.kristijankoscak.mojopg.Model.Comment
import hr.ferit.kristijankoscak.mojopg.R
import hr.ferit.kristijankoscak.mojopg.UserPreferenceManager
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.item_comment.view.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class CommentAdapter(comments: MutableList<Comment>, isLoggedIn:Boolean, commentListener: CommentInteractionListener): RecyclerView.Adapter<CommentHolder>() {

    private val comments: MutableList<Comment>
    private val commentListener: CommentInteractionListener;
    private val isLoggedIn:Boolean;

    init {
        this.comments = mutableListOf()
        this.comments.addAll(comments)
        this.commentListener = commentListener
        this.isLoggedIn = isLoggedIn
    }
    fun refreshData(comments: MutableList<Comment>) {
        this.comments.clear()
        this.comments.addAll(comments)
        this.notifyDataSetChanged()
    }
    override fun getItemCount(): Int = comments.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val commentView = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        val commentHolder = CommentHolder(commentView,isLoggedIn)
        return commentHolder
    }
    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment,commentListener)
    }
}
class CommentHolder(itemView: View, isLoggedIn: Boolean) : RecyclerView.ViewHolder(itemView) {
    val userImageBaseUrl = "http://192.168.0.174/Kopg/public/storage/user_images/"

    init{
        //toggleButtons(itemView,productType)
    }

    fun bind(comment: Comment, commentListener: CommentInteractionListener) {
        toggleDeleteButton(itemView,comment)
        Picasso.get()
            .load(userImageBaseUrl+comment.author_image)
            .fit()
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(android.R.drawable.stat_notify_error)
            .into(itemView.commentUserImage)
        itemView.commentTextValue.text = comment.comment
        itemView.commentStarValue.text = comment.grade
        itemView.commentUserNameValue.text = comment.authorName
        itemView.commentDateValue.text = formatDate(comment.created_at)
        itemView.commentDeleteButton.setOnClickListener{ commentListener.onRemove(comment.id.toInt());true;}
    }
    private fun formatDate(date:String):String{
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm")
        return formatter.format(parser.parse(date))
    }
    private fun toggleDeleteButton(view:View,comment:Comment){
        if(comment.author_id != UserPreferenceManager().retreiveUserID()){
            view.commentDeleteButton.visibility = View.INVISIBLE
        }
    }
}