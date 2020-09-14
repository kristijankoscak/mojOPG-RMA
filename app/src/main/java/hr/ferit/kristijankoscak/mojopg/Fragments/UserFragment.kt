package hr.ferit.kristijankoscak.mojopg.Fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.squareup.picasso.Picasso
import hr.ferit.kristijankoscak.mojopg.API.CommentsApi
import hr.ferit.kristijankoscak.mojopg.Model.User
import hr.ferit.kristijankoscak.mojopg.R
import hr.ferit.kristijankoscak.mojopg.Respositories.CommentsRepository
import hr.ferit.kristijankoscak.mojopg.Respositories.ProductsRepository
import hr.ferit.kristijankoscak.mojopg.Respositories.UsersRepository
import hr.ferit.kristijankoscak.mojopg.UserPreferenceManager
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlin.math.roundToLong

class UserFragment: Fragment() {

    private lateinit var viewedUserID:String;
    private lateinit var viewedUser: User;
    private lateinit var commentsRecycler:RecyclerView;
    private lateinit var commentData: MutableMap<String, String>
    private lateinit var user:User;

    companion object {
        fun newInstance(): ProductsFragment {
            return ProductsFragment()
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false);


        checkAccess(view)

        return view;
    }


    private fun checkAccess(view: View){
        val bundle = this.arguments
        if(bundle != null){
            view.addCommentButton.setOnClickListener { addNewComment(view) }
            viewedUserID = bundle.getString("userID")!!
            viewedUser = UsersRepository.get(viewedUserID.toInt())!!
            identificateUser(view,"specificUser")
        }
        if(UserPreferenceManager().retreiveUserID() != "" && bundle == null){
            hideCommentBlock(view)
            identificateUser(view,"loggedUser")
        }
        if(UserPreferenceManager().retreiveUserID() == "" ){
            hideCommentBlock(view)
        }

    }
    private fun addNewComment(view:View){
        if(validateInputs(view)){
            fetchCommentData(view)
            val commentsApi = CommentsApi(context!!,commentsRecycler,fragmentManager!!,user.id)
            commentsApi.submitAddCommentRequest(Request.Method.POST,commentData)
            loadData(view,user)
        }
        else{
            Toast.makeText(context,"Jedan od unosa je prazan,ispunite ga!",Toast.LENGTH_LONG).show()
        }
    }
    private fun fetchCommentData(view: View){
        commentData = mutableMapOf()
        commentData.put("comment",view.userComment.text.toString())
        commentData.put("grade",view.userCommentGrade.rating.toString())
        commentData.put("belongs_to",viewedUserID)
        commentData.put("author_image",UserPreferenceManager().retreiveUserImage())
        commentData.put("author_id",UserPreferenceManager().retreiveUserID())
        commentData.put("author_name",UserPreferenceManager().retreiveName())
    }

    private fun validateInputs(view:View):Boolean{
        val commentIsNotEmpty = view.userComment.text.toString() != "";
        val ratingBarIsNotZero = view.userCommentGrade.rating > 0

        return commentIsNotEmpty && ratingBarIsNotZero;
    }

    private fun identificateUser(view: View,userType:String){
        if(userType == "loggedUser"){
            user = UsersRepository.get(UserPreferenceManager().retreiveUserID().toInt())!!
            loadData(view,user)
        }
        if(userType == "specificUser"){
            user = UsersRepository.get(viewedUserID.toInt())!!
            loadData(view,user)
        }
    }
    private fun hideCommentBlock(view:View){
        view.userComment.visibility = View.INVISIBLE
        view.userCommentGrade.visibility = View.INVISIBLE
        view.addCommentButton.visibility = View.INVISIBLE
    }

    private fun loadData(view: View,user:User){
        val progressDialog = ProgressDialog(context)
        displayLoadingMessage(progressDialog)
        setUpRecycler(view)
        displayComments(user.id)
        Handler().postDelayed({
            setUserImage(view,user)
            view.userName.text = user.name
            view.userCounty.text = user.county
            view.userLocation.text = user.city
            view.userAddress.text = user.address
            view.userOpg.text = user.user_opg
            view.userProducts.text = fetchUserProductNumber(user.id).toString()
            view.userGrade.text = String.format("%.2f",fetchCommentsGrade(user!!.id) )
            dismissLoadingMessage(progressDialog)
        }, 1000)
    }

    private fun setUserImage(view: View,user:User){
        val userBaseURL = "http://192.168.0.174/Kopg/public/storage/user_images/"
        Picasso.get()
            .load(userBaseURL+user.image_link)
            .fit()
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(android.R.drawable.stat_notify_error)
            .into(view.userImage)
    }

    private fun fetchUserProductNumber(id:String):Int{
        var productNumber = 0;
        val products = ProductsRepository.retreiveProducts()
        for(i in 0 until products.size){
            if(products[i].user_id == id){
                productNumber++;
            }
        }
        return productNumber;
    }

    private fun fetchCommentsGrade(loggedUserID: String):Double{
        var sum = 0.0
        var commentsNumber = 0
        val comments = CommentsRepository.retreiveComments()
        for ( comment in comments){
            if(comment.belongs_to == loggedUserID){
                sum += comment.grade.toFloat()
                commentsNumber++
            }
        }
        return sum/commentsNumber;
    }

    private fun setUpRecycler(view:View){
        commentsRecycler = view.commentsDisplay
        commentsRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL,false)
        commentsRecycler.itemAnimator = DefaultItemAnimator()
        commentsRecycler.addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
    }
    private fun displayComments(loggedUserID:String){
        val commentsApi = CommentsApi(context!!,commentsRecycler,fragmentManager!!,loggedUserID)
        commentsApi.submitGetCommentsRequest(Request.Method.POST)
    }

    private fun displayLoadingMessage(progressDialog: ProgressDialog){
        progressDialog.setTitle("Učitavanje")
        progressDialog.setMessage("Dohvaćamo korisnika i komentare,trenutak molimo.")
        progressDialog.show()
    }
    private fun dismissLoadingMessage(progressDialog: ProgressDialog){
        progressDialog.dismiss()
    }

}