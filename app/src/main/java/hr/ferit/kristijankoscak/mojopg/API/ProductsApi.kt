package hr.ferit.kristijankoscak.mojopg.API

import android.app.Fragment
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import hr.ferit.kristijankoscak.mojopg.Fragments.Adapters.ProductAdapter
import hr.ferit.kristijankoscak.mojopg.Fragments.AddProductFragment
import hr.ferit.kristijankoscak.mojopg.Fragments.InteractionListener.ProductInteractionListener
import hr.ferit.kristijankoscak.mojopg.Fragments.ProductFragment
import hr.ferit.kristijankoscak.mojopg.Fragments.ProductsFragment
import hr.ferit.kristijankoscak.mojopg.Model.Product
import hr.ferit.kristijankoscak.mojopg.R
import hr.ferit.kristijankoscak.mojopg.Respositories.ProductsRepository
import hr.ferit.kristijankoscak.mojopg.UserPreferenceManager
import org.json.JSONArray
import org.json.JSONObject

class ProductsApi(context: Context, recyclerView: RecyclerView, fragmentManager: FragmentManager, accessProducts:String): ApiHandler(context) {

    private var addProductApi = context.resources.getString(R.string.addProductUrl);
    private var editProductApi = context.resources.getString(R.string.editProductUrl)
    private var deleteProductApi = context.resources.getString(R.string.deleteProductUrl);
    private var reportProductApi = context.resources.getString(R.string.reportProductUrl);
    private var getProductsApi =context.resources.getString(R.string.getProductsUrl);

    private var recyclerView:RecyclerView;
    private var context: Context;
    private var fragmentManager : FragmentManager
    private var accessProducts:String
    lateinit private var progressDialog: ProgressDialog;
    lateinit private var userID : String;

    lateinit private var selectedCounty:String;
    lateinit private var selectedType:String


    init{
        this.recyclerView = recyclerView
        this.context = context;
        this.fragmentManager = fragmentManager
        this.accessProducts = accessProducts
    }

    fun submitGetProductsRequest(method: Int){
        if(ProductsRepository.retreiveProducts().size == 0) {
            displayLoadingMessage()
            val request = StringRequest(method, getProductsApi,
                Response.Listener { response ->
                    var jsonArray = JSONArray(response)
                    ProductsRepository.emptyList()
                    insertProductsInRepository(jsonArray)
                    displayData(method)
                    dismissLoadingMessage()
                },
                Response.ErrorListener { error ->
                    dismissLoadingMessage()
                    Toast.makeText(this.context, error.message, Toast.LENGTH_LONG).show();
                }
            )
            queue.add(request)
        }
        else{
            displayData(method)
        }
    };
    fun submitGetFilteredProducts(method: Int){
        if(ProductsRepository.retreiveProducts().size == 0) {
            displayLoadingMessage()
            val request = StringRequest(method, getProductsApi,
                Response.Listener { response ->
                    var jsonArray = JSONArray(response)
                    ProductsRepository.emptyList()
                    insertProductsInRepository(jsonArray)
                    displayFilteredData(method)
                    dismissLoadingMessage()
                },
                Response.ErrorListener { error ->
                    dismissLoadingMessage()
                    Toast.makeText(this.context, error.message, Toast.LENGTH_LONG).show();
                }
            )
            queue.add(request)
        }
        else{
            displayFilteredData(method)
        }
    };
    fun setFilterInputs(county: String,type: String){
        selectedCounty = county
        selectedType = type
    }

    fun submitDeleteProductRequest(method: Int,productID: Int){
        ProductsRepository.emptyList()
        val request = object: StringRequest(method, deleteProductApi,
            Response.Listener { response ->
                Toast.makeText(this.context, "Proizvod obrisan!", Toast.LENGTH_LONG).show();
                submitGetProductsRequest(method)
            },
            Response.ErrorListener { error->
                Toast.makeText(this.context, error.message, Toast.LENGTH_LONG).show();
            })
        {
            override fun getParams(): MutableMap<String, String> {
                var map = HashMap<String,String>();
                map.put("id",productID.toString());
                return map;
            }
        }
        queue.add(request)
    }
    fun submitReportProductRequest(method: Int,productID:Int){
        val request = object: StringRequest(method, reportProductApi,
            Response.Listener { response ->
                Toast.makeText(this.context, "Proizvod prijavljen!", Toast.LENGTH_LONG).show();
            },
            Response.ErrorListener { error->
                Toast.makeText(this.context, error.message, Toast.LENGTH_LONG).show();
            })
        {
            override fun getParams(): MutableMap<String, String> {
                var map = HashMap<String,String>();
                map.put("id",productID.toString());
                return map;
            }
        }
        queue.add(request)
    }
    fun submitAddProductRequest(method:Int,productData:MutableMap<String, String>){
        val request = object : StringRequest(method, addProductApi,
            Response.Listener { response ->
                Toast.makeText(context,"Proizvod dodan!",Toast.LENGTH_LONG).show()
                navigateToProductsFragment()
            },
            Response.ErrorListener { error ->
                Toast.makeText(context,error.toString(),Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return productData;
            }
        }
        queue.add(request)
    }
    fun submitEditProductRequest(method:Int,productData:MutableMap<String, String>){
        val request = object : StringRequest(method, editProductApi,
            Response.Listener { response ->
                Toast.makeText(context,"Proizvod dodan!",Toast.LENGTH_LONG).show()
                navigateToProductsFragment()
            },
            Response.ErrorListener { error ->
                Toast.makeText(context,error.toString(),Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return productData;
            }
        }
        queue.add(request)
    }

    private fun displayFilteredData(method: Int) {

        val productListener = object :
            ProductInteractionListener {
            override fun onEdit(id: Int) {
                navigateToSpecificFragment(id, AddProductFragment())
            }

            override fun onRemove(id: Int) {
                submitDeleteProductRequest(method, id)
            }

            override fun onShowDetails(id: Int) {
                navigateToSpecificFragment(id, ProductFragment())
            }

            override fun onReport(id: Int) {
                submitReportProductRequest(method, id)
            }
        }

        if (accessProducts == "all") {
            val products = fetchFilteredProducts(selectedCounty,selectedType)
            recyclerView.adapter = ProductAdapter(products, accessProducts, productListener)

        }
    }
    private fun displayData(method: Int){

        val productListener = object:
            ProductInteractionListener {
            override fun onEdit(id: Int) {
                navigateToSpecificFragment(id,AddProductFragment())
            }
            override fun onRemove(id: Int) {
                submitDeleteProductRequest(method,id)
            }
            override fun onShowDetails(id: Int) {
                navigateToSpecificFragment(id,ProductFragment())
            }
            override fun onReport(id: Int) {
                submitReportProductRequest(method,id)
            }
        }

        if(accessProducts == "all"){
            recyclerView.adapter = ProductAdapter(ProductsRepository.retreiveProducts(),accessProducts,productListener)
        }
        if(accessProducts =="userProducts"){
            recyclerView.adapter = ProductAdapter(fetchUserProducts(),accessProducts,productListener)
        }
    }

    private fun insertProductsInRepository(productsArray: JSONArray) {
        for (i in 0 until productsArray.length()) {
            var jsonProductObject = JSONObject(productsArray[i].toString())
            var product = Product(
                jsonProductObject.getString("id").toInt(),
                jsonProductObject.getString("type"),
                jsonProductObject.getString("detail_type"),
                jsonProductObject.getString("price"),
                jsonProductObject.getString("county"),
                jsonProductObject.getString("location"),
                jsonProductObject.getString("contact"),
                jsonProductObject.getString("user"),
                jsonProductObject.getString("user_id"),
                jsonProductObject.getString("user_opg"),
                jsonProductObject.getString("image_link"),
                jsonProductObject.getString("quantity"),
                jsonProductObject.getString("is_reported")
            )
            ProductsRepository.add(product)
        }
    }
    private fun fetchUserProducts():MutableList<Product>{
        var tempList:MutableList<Product> = mutableListOf()
        var allProducts = ProductsRepository.retreiveProducts()
        for (product in allProducts) {
            if(this::userID.isInitialized){
                if(product.user_id == userID){
                    tempList.add(product)
                }
            }
            else{
                if(product.user_id == UserPreferenceManager().retreiveUserID()){
                    tempList.add(product)
                }
            }
        }
        return tempList;
    }

    private fun navigateToSpecificFragment(productID:Int,fragment:androidx.fragment.app.Fragment){
        val bundle = Bundle()
        bundle.putString("productID",productID.toString())
        fragment.arguments = bundle;
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.addToBackStack("fragment")
        fragmentTransaction.commit()
    }
    private fun navigateToProductsFragment(){
        val fragment = ProductsFragment()
        val bundle = Bundle()
        bundle.putString("productsType","all")
        fragment.arguments = bundle;
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.addToBackStack("fragment")
        fragmentTransaction.commit()
    }

    fun setUserID(id:String){
        userID = id;
    }

    private fun displayLoadingMessage(){
        progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Učitavanje")
        progressDialog.setMessage("Dohvaćamo proizvode.")
        progressDialog.show()
    }
    private fun dismissLoadingMessage(){
        progressDialog.dismiss()
    }
    private fun fetchFilteredProducts(county:String,type:String):MutableList<Product>{
        val products = ProductsRepository.retreiveProducts()
        val filteredProducts = mutableListOf<Product>()
        if(county != "Odaberite županiju"){
            for(product in products){
                if(product.type == type && product.county == county){
                    filteredProducts.add(product)
                }
            }
        }
        if(county == "Odaberite županiju"){
            for(product in products){
                if(product.type == type){
                    filteredProducts.add(product)
                }
            }

        }
        return filteredProducts;
    }
}