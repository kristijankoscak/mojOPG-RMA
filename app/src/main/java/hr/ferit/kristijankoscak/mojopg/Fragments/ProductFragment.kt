package hr.ferit.kristijankoscak.mojopg.Fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.squareup.picasso.Picasso
import hr.ferit.kristijankoscak.mojopg.API.ProductsApi
import hr.ferit.kristijankoscak.mojopg.Model.Product
import hr.ferit.kristijankoscak.mojopg.Model.User
import hr.ferit.kristijankoscak.mojopg.R
import hr.ferit.kristijankoscak.mojopg.Respositories.ProductsRepository
import hr.ferit.kristijankoscak.mojopg.Respositories.UsersRepository
import hr.ferit.kristijankoscak.mojopg.hasPermissionCompat
import hr.ferit.kristijankoscak.mojopg.requestPermisionCompat
import kotlinx.android.synthetic.main.fragment_product.view.*


class ProductFragment : Fragment() {

    lateinit var productID:String;
    lateinit var product : Product;
    lateinit var user: User;
    private var callPermission = Manifest.permission.CALL_PHONE
    private var callRequestCode = 10;
    private lateinit var otherUserProductsRecycler:RecyclerView;

    companion object {
        fun newInstance(): ProductsFragment {
            return ProductsFragment()
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_product, container, false);

        fetchData()
        initalizeUIElements(view)
        return view;
    }

    private fun fetchData(){
        val bundle = this.arguments
        productID = bundle!!.getString("productID")!!
        product = ProductsRepository.get(productID.toInt())!!
        user = UsersRepository.get(product.user_id.toInt())!!
    }

    private fun initalizeUIElements(view:View){
        setUpRecycler(view)
        loadProfile(view)
        view.userContanctButton.setOnClickListener { handleUserCall(view) }
        displayOtherUserProducts()
        view.userInfo.setOnClickListener { navigateToUserProfile(product.user_id) }
    }
    private fun loadProfile(view:View){
        setProductImage(view)
        view.productDetailType.text = product.detail_type
        view.productPrice.text = product.price + " " + product.quantity
        view.productLocation.text = product.location
        view.productAddress.text = user.address
        setUserImage(view)
        view.userName.text = user.name
        view.userContact.text = user.contact
    }

    private fun setProductImage(view: View){
        val productBaseURL = "http://192.168.0.174/Kopg/public/storage/product_images/"
        Picasso.get()
            .load(productBaseURL+product.image_link)
            .fit()
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(android.R.drawable.stat_notify_error)
            .into(view.productImage)
    }
    private fun setUserImage(view: View){
        val userBaseURL = "http://192.168.0.174/Kopg/public/storage/user_images/"
        Picasso.get()
            .load(userBaseURL+user.image_link)
            .fit()
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(android.R.drawable.stat_notify_error)
            .into(view.userImage)
    }

    private fun handleUserCall(view:View){
        if((activity as AppCompatActivity).hasPermissionCompat(callPermission) ){
            openPhoneCall()
        }
        else{
            (activity as AppCompatActivity).requestPermisionCompat(arrayOf(callPermission), callRequestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        when(requestCode){
            callRequestCode -> {
                if(grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openPhoneCall()
                }
                else{
                    Toast.makeText(context, R.string.permissionNotGranted, Toast.LENGTH_LONG).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    private fun openPhoneCall(){
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + user.contact.toString()))
        startActivity(intent)
    }

    private fun setUpRecycler(view:View){
        otherUserProductsRecycler = view.userProducts
        otherUserProductsRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL,false)
        otherUserProductsRecycler.itemAnimator = DefaultItemAnimator()
        otherUserProductsRecycler.addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
    }
    private fun displayOtherUserProducts(){
        val productsApi = ProductsApi(context!!,otherUserProductsRecycler,fragmentManager!!,"userProducts")
        productsApi.setUserID(user.id)
        productsApi.submitGetProductsRequest(Request.Method.POST)
    }
    private fun navigateToUserProfile(id:String){
        val userFragment = UserFragment();
        val bundle = Bundle()
        bundle.putString("userID",id)
        userFragment.arguments = bundle;
        val fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, userFragment)
        fragmentTransaction.addToBackStack("fragment")
        fragmentTransaction.commit()
    }
}