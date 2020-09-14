
package hr.ferit.kristijankoscak.mojopg.Fragments

import android.app.ProgressDialog
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import hr.ferit.kristijankoscak.mojopg.API.ProductsApi
import hr.ferit.kristijankoscak.mojopg.Model.Product
import hr.ferit.kristijankoscak.mojopg.Model.User
import hr.ferit.kristijankoscak.mojopg.R
import hr.ferit.kristijankoscak.mojopg.Respositories.ProductsRepository
import hr.ferit.kristijankoscak.mojopg.Respositories.UsersRepository
import java.util.*


class GeoProductsFragment : Fragment() , OnMapReadyCallback {

    private lateinit var locationManager: LocationManager
    private lateinit var map:GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var geocoder: Geocoder
    private lateinit var progressDialog:ProgressDialog;


    companion object {
        fun newInstance(): GeoProductsFragment {
            return GeoProductsFragment()
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
    Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_geo_products, container, false);

        fetchProducts(layoutInflater.inflate(R.layout.fragment_products,container,false))
        initializeUIElemenets(view)
        setUpUI()

        return view;
    }
    private fun initializeUIElemenets(view:View){
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        locationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        geocoder = Geocoder(context, Locale.getDefault())
    }
    private fun setUpUI(){
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        displayLoadingMessage()
        map = googleMap
        Handler().postDelayed({
            Log.d("geoproducts",ProductsRepository.getSize().toString())
            AddProductsLocation(ProductsRepository.retreiveProducts())
            dismissLoadingMessage()
            loadToCountry()
        }, 2000)
    }

    private fun displayProductOnMap(myPosition:LatLng,product:Product){
        val marker = map.addMarker(MarkerOptions().position(myPosition).title(product.user).snippet(product.detail_type + " - " + product.price + " "+product.quantity))
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.uiSettings.isZoomControlsEnabled = true
        //map.moveCamera(CameraUpdateFactory.newLatLng(myPosition))
        map.setOnMarkerClickListener { marker -> handleNavigationToUserProfile(marker.title); true }

    }

    private fun handleNavigationToUserProfile(userName:String){
        val users = UsersRepository.retreiveUsers()
        for(user in users){
            if(user.name == userName){
                navigateToUserProfile(user.id)
            }
        }
    }
    private fun navigateToUserProfile(userID:String){
        val userFragment = UserFragment();
        val bundle = Bundle()
        bundle.putString("userID",userID)
        userFragment.arguments = bundle;
        val fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, userFragment)
        fragmentTransaction.addToBackStack("fragment")
        fragmentTransaction.commit()
    }

    private fun AddProductsLocation(products:MutableList<Product>){

        for(product in products){
            val user = UsersRepository.get(product.user_id.toInt())
            displayProductOnMap(getLatLngFromAddress(user!!),product)
        }
    }
    private fun getLatLngFromAddress(user: User):LatLng{
        var addressList = geocoder.getFromLocationName(user.address,5)
        var location = addressList.get(0)
        return LatLng(location.latitude,location.longitude)
    }
    private fun fetchProducts(view:View){
        val fragmentManager = fragmentManager;
        val productsApi = ProductsApi(context!!,view.findViewById(R.id.productsDisplay),fragmentManager!!,"all")
        productsApi.submitGetProductsRequest(Request.Method.POST)
    }

    private fun loadToCountry(){
        val locations = geocoder.getFromLocationName("Hrvatska",1)
        val location = locations.get(0)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude+2.0),6.5f))
    }

    private fun displayLoadingMessage(){
        progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Učitavanje")
        progressDialog.setMessage("Dohvaćamo i postavljamo proizvode na kartu!")
        progressDialog.show()
    }
    private fun dismissLoadingMessage(){
        progressDialog.dismiss()
    }
}