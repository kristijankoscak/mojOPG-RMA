package hr.ferit.kristijankoscak.mojopg.Fragments

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.squareup.picasso.Picasso
import hr.ferit.kristijankoscak.mojopg.API.CountiesApi
import hr.ferit.kristijankoscak.mojopg.API.ProductsApi
import hr.ferit.kristijankoscak.mojopg.Model.Product
import hr.ferit.kristijankoscak.mojopg.R
import hr.ferit.kristijankoscak.mojopg.Respositories.CountiesRepository
import hr.ferit.kristijankoscak.mojopg.Respositories.ProductsRepository
import hr.ferit.kristijankoscak.mojopg.UserPreferenceManager
import hr.ferit.kristijankoscak.mojopg.hasPermissionCompat
import hr.ferit.kristijankoscak.mojopg.requestPermisionCompat
import kotlinx.android.synthetic.main.fragment_add_product.*
import kotlinx.android.synthetic.main.fragment_add_product.view.*
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream


class AddProductFragment: Fragment() {

    private val externalStoragePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    private val externalStorageRequestCode = 999;

    lateinit private var imageFilePath: Uri;
    lateinit private var imageInputStream: InputStream;
    lateinit private var imageBitMap:Bitmap;
    lateinit private var countiesApiHandler:CountiesApi;
    lateinit private var product: Product;
    lateinit private var productID: String;
    private var imageIsPicked:Boolean = false;
    lateinit private var productData: MutableMap<String, String>

    lateinit private var productsRecycler:RecyclerView;

    companion object {
        fun newInstance(): AddProductFragment {
            return AddProductFragment()
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_product, container, false);

        getRecycler(container)
        fetchCounties(view)
        fetchProduct(view)

        return view;
    }

    private fun fetchCounties(view:View){
        countiesApiHandler = CountiesApi(context!!,view.newProductCounty);
        countiesApiHandler.submitGetCountiesRequest(Request.Method.POST);
    }

    private fun fetchProduct(view:View){
        val bundle = this.arguments
        if(bundle != null) {
            productID = bundle!!.getString("productID")!!
            product = ProductsRepository.get(productID.toInt())!!
            initializeUIElements(view,"editProduct")
        }
        else{
            initializeUIElements(view,"addProduct")
        }
    }

    private fun initializeUIElements(view:View, accessType:String){
        setUpProductTypeSpinner(view)
        view.buttonSelectProductImage.setOnClickListener{ handleImageSelection() }
        if(accessType == "addProduct"){
            view.buttonAddNewProduct.setOnClickListener{ addNewProduct(view) }
        }
        else{
            view.buttonAddNewProduct.setOnClickListener{ editProduct(view) }
            val progressDialog = ProgressDialog(context)
            displayLoadingMessage(progressDialog)
            Handler().postDelayed({
                loadProduct(view)
                dismissLoadingMessage(progressDialog)
                imageIsPicked = true;
            }, 2000)
        }
    }

    private fun loadProduct(view:View){
        loadImage(view)
        view.newProductPrice.setText(product.price)
        setRadioButton(view)
        setProductType(view)
        view.newProductDetailType.setText(product.detail_type)
        setProductCounty(view)
        view.newProductLocation.setText(product.location)
        view.newProductContact.setText(product.contact)
    }

    private fun loadImage(view:View){
        val productBaseURL = "http://192.168.0.174/Kopg/public/storage/product_images/"
        Picasso.get()
            .load(productBaseURL+product.image_link)
            //.fit()
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(android.R.drawable.stat_notify_error)
            .into(object: com.squareup.picasso.Target{
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    view.newProductImage.setImageBitmap(bitmap)
                    imageBitMap = bitmap!!;
                }
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
            })
    }
    private fun setRadioButton(view: View){
        val quantity1 = view.quantity1
        val quantity2 = view.quantity2
        val quantity3 = view.quantity3
        quantity1.isChecked = false
        quantity2.isChecked = false
        quantity3.isChecked = false

        if(quantity1.text == product.quantity){
            quantity1.isChecked = true;
        }
        if(quantity2.text == product.quantity){
            quantity2.isChecked = true;
        }
        if(quantity3.text == product.quantity){
            quantity3.isChecked = true;
        }
    }
    private fun setProductType(view:View){
        val productType = product.type;
        if(productType == "Suhomesnati proizvodi"){
            view.newProductType.setSelection(0)
        }
        if(productType == "Voće"){
            view.newProductType.setSelection(1)
        }
        if(productType == "Povrće"){
            view.newProductType.setSelection(2)
        }
    }
    private fun setProductCounty(view:View){
        val counties = CountiesRepository.retreiveCounties()
        for(i in 0 until counties.size){
            if(product.county == counties[i]){
                view.newProductCounty.setSelection(i)
            }
        }
    }

    private fun setUpProductTypeSpinner(view:View){
        val productTypes = mutableListOf<String>("Suhomesnati proizvodi","Voće","Povrće")
        val productTypeAdapter = ArrayAdapter<String>(context!!,android.R.layout.simple_list_item_1,productTypes)
        view.newProductType.adapter = productTypeAdapter
    }

    private fun handleImageSelection(){
        if((activity as AppCompatActivity).hasPermissionCompat(externalStoragePermission)){
            Log.d("Tag","ima dozvolu")
            openGallery()
        }
        else{
            (activity as AppCompatActivity).requestPermisionCompat(arrayOf(externalStoragePermission),externalStorageRequestCode)
            Log.d("Tag","nema dozvolu")
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
                    Toast.makeText(context, R.string.permissionNotGranted, Toast.LENGTH_LONG).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK && requestCode == externalStorageRequestCode){
            imageFilePath = data?.data!!;
            try {
                setImageBitMap(imageFilePath)
                newProductImage.setImageBitmap(imageBitMap)
                imageIsPicked = true;
            }
            catch (e: FileNotFoundException){
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setImageBitMap(path:Uri){
        imageInputStream = context!!.contentResolver.openInputStream(path)!!
        imageBitMap = BitmapFactory.decodeStream(imageInputStream)
    }

    private fun imageToString(bitMap: Bitmap):String{
        var outputStream: ByteArrayOutputStream = ByteArrayOutputStream();
        bitMap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        var imageBytes = outputStream.toByteArray();

        var encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        return encodedImage;
    }

    private fun addNewProduct(view: View){
        ProductsRepository.emptyList()
        if(validateInputs(view)){
            fetchProductData(view)
            val fragmentManager = fragmentManager;
            val url = "http://192.168.0.174/Kopg/public/api/list-products";
            val productsApi = ProductsApi(context!!,productsRecycler,fragmentManager!!,"all")
            productsApi.submitAddProductRequest(Request.Method.POST,productData)
        }
        else{
            Toast.makeText(context,"Jedan od unosa je prazan,ispunite ga!",Toast.LENGTH_LONG).show()
        }
    }
    private fun editProduct(view: View){
        ProductsRepository.emptyList()
        if(validateInputs(view)){
            fetchProductData(view)
            val fragmentManager = fragmentManager;
            val url = "http://192.168.0.174/Kopg/public/api/list-products";
            val productsApi = ProductsApi(context!!,productsRecycler,fragmentManager!!,"all")
            productsApi.submitEditProductRequest(Request.Method.POST,productData)
        }
        else{
            Toast.makeText(context,"Jedan od unosa je prazan,ispunite ga!",Toast.LENGTH_LONG).show()
        }
    }

    private fun validateInputs(view:View):Boolean{
        val priceIsNotEmpty = view.newProductPrice.text.toString() != ""
        val detailTypeIsNotEmpty = view.newProductDetailType.text.toString() != ""
        val countyIsNotEmpty = view.newProductCounty.selectedItem.toString() != "Odaberite županiju"
        val placeIsNotEmpty = view.newProductLocation.text.toString() != ""
        val contactIsNotEmpty = view.newProductContact.text.toString() != ""

        return (priceIsNotEmpty&&imageIsPicked&&detailTypeIsNotEmpty&&countyIsNotEmpty&&placeIsNotEmpty&&contactIsNotEmpty);
    }

    private fun fetchProductData(view:View){
        productData = mutableMapOf()
        if(this::productID.isInitialized){
            productData.put("id",productID);
        }
        productData.put("image",imageToString(imageBitMap));
        productData.put("price",view.newProductPrice.text.toString());
        productData.put("quantity",view.findViewById<RadioButton>(view.newProductQuantity.checkedRadioButtonId).text.toString());
        productData.put("type",view.newProductType.selectedItem.toString());
        productData.put("detailType",view.newProductDetailType.text.toString());
        productData.put("county",view.newProductCounty.selectedItem.toString());
        productData.put("location",view.newProductLocation.text.toString());
        productData.put("contact",view.newProductContact.text.toString());
        productData.put("user_id",UserPreferenceManager().retreiveUserID());
        productData.put("user_opg",UserPreferenceManager().retreiveUserOpg());
        productData.put("user",UserPreferenceManager().retreiveName());
    }

    private fun displayLoadingMessage(progressDialog: ProgressDialog){
        progressDialog.setTitle("Učitavanje")
        progressDialog.setMessage("Dohvaćamo proizvod,trenutak molimo.")
        progressDialog.show()
    }
    private fun dismissLoadingMessage(progressDialog: ProgressDialog){
        progressDialog.dismiss()
    }

    private fun getRecycler(container: ViewGroup?){
        val root = layoutInflater.inflate(R.layout.fragment_products,container,false)
        productsRecycler = root.findViewById(R.id.productsDisplay)
    }
}