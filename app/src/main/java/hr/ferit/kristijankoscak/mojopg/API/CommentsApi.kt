package hr.ferit.kristijankoscak.mojopg.API

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import hr.ferit.kristijankoscak.mojopg.Fragments.Adapters.CommentAdapter
import hr.ferit.kristijankoscak.mojopg.Fragments.InteractionListener.CommentInteractionListener
import hr.ferit.kristijankoscak.mojopg.Fragments.UserFragment
import hr.ferit.kristijankoscak.mojopg.Model.Comment
import hr.ferit.kristijankoscak.mojopg.R
import hr.ferit.kristijankoscak.mojopg.Respositories.CommentsRepository
import hr.ferit.kristijankoscak.mojopg.Respositories.UsersRepository
import hr.ferit.kristijankoscak.mojopg.UserPreferenceManager
import org.json.JSONArray
import org.json.JSONObject

class CommentsApi(context: Context, recyclerView: RecyclerView, fragmentManager: FragmentManager,viewedProfileID:String): ApiHandler(context) {

    private var addCommentApiUrl = context.resources.getString(R.string.addCommentUrl);
    private var deleteCommentApiUrl = context.resources.getString(R.string.deleteCommentUrl);
    private var getCommentsApiUrl = context.resources.getString(R.string.getCommentsUrl);

    private var context:Context;
    private var recyclerView: RecyclerView
    private var viewedProfileID:String;
    private var fragmentManager:FragmentManager

    private lateinit var profileID:String;

    init {
        this.context=context;
        this.recyclerView = recyclerView
        this.viewedProfileID = viewedProfileID
        this.fragmentManager = fragmentManager
    }

    fun submitGetCommentsRequest(method:Int){
        if(CommentsRepository.retreiveComments().size == 0){
            val request = StringRequest(
                method, getCommentsApiUrl,
                Response.Listener { response ->
                    val jsonArray = JSONArray(response)
                    insertCommentsInRepository(jsonArray)
                    displayComments(method)
                },
                Response.ErrorListener { error->
                    Toast.makeText(context, error.message, Toast.LENGTH_LONG).show();
                }
            )
            super.queue.add(request)
        }
        else{
            displayComments(method)
        }
    }

    fun submitDeleteCommentRequest(method:Int,commentID:String){
        val request = object: StringRequest(method, deleteCommentApiUrl,
            Response.Listener { response ->
                Toast.makeText(this.context, "Komentar obrisan!", Toast.LENGTH_LONG).show();
                navigateToSpecificFragment("userID",viewedProfileID.toInt(),UserFragment())
            },
            Response.ErrorListener { error->
                Toast.makeText(this.context, error.message, Toast.LENGTH_LONG).show();
            })
        {
            override fun getParams(): MutableMap<String, String> {
                var map = HashMap<String,String>();
                map.put("id",commentID.toString());
                return map;
            }
        }
        super.queue.add(request)
    }

    fun submitAddCommentRequest(method:Int,commentData: MutableMap<String, String>){
        CommentsRepository.emptyList()
        val request = object : StringRequest(method, addCommentApiUrl,
            Response.Listener { response ->
                Toast.makeText(context,"Komentar dodan!",Toast.LENGTH_LONG).show()
                displayComments(method)
            },
            Response.ErrorListener { error ->
                Toast.makeText(context,error.toString(),Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return commentData;
            }
        }
        super.queue.add(request)
    }


    private fun insertCommentsInRepository(commentsArray: JSONArray) {
        for (i in 0 until commentsArray.length()) {
            var jsonProductObject = JSONObject(commentsArray[i].toString())
            CommentsRepository.add(
                Comment(
                    jsonProductObject.getString("id"),
                    jsonProductObject.getString("author_name"),
                    jsonProductObject.getString("author_id"),
                    jsonProductObject.getString("comment"),
                    jsonProductObject.getString("grade"),
                    jsonProductObject.getString("belongs_to"),
                    jsonProductObject.getString("created_at"),
                    jsonProductObject.getString("author_image")
                )
            )
        }
    }

    private fun displayComments(method:Int){
        val commentListener = object: CommentInteractionListener {
            override fun onRemove(id: Int) {
                val url = "http://192.168.0.174/Kopg/public/api/comment-delete";
                submitDeleteCommentRequest(method,id.toString())
                CommentsRepository.remove(id)
                (recyclerView.adapter as CommentAdapter).refreshData(fetchUserComments())
            }
        }
        recyclerView.adapter = CommentAdapter(fetchUserComments(),true,commentListener)
    }

    fun fetchUserComments():MutableList<Comment>{
        val comments = CommentsRepository.retreiveComments()
        val userComments:MutableList<Comment> = mutableListOf()
        for (i in 0 until comments.size){
            if(comments[i].belongs_to == viewedProfileID){
                userComments.add(comments[i])
            }
        }
        return userComments;
    }
    private fun navigateToSpecificFragment(key:String,productID:Int,fragment:androidx.fragment.app.Fragment){
        val bundle = Bundle()
        bundle.putString(key,productID.toString())
        fragment.arguments = bundle;
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.addToBackStack("fragment")
        fragmentTransaction.commit()
    }

}