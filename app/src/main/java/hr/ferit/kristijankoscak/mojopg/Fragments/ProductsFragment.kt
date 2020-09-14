
package hr.ferit.kristijankoscak.mojopg.Fragments

import android.os.Bundle


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import hr.ferit.kristijankoscak.mojopg.API.CountiesApi
import hr.ferit.kristijankoscak.mojopg.API.ProductsApi
import hr.ferit.kristijankoscak.mojopg.R
import kotlinx.android.synthetic.main.fragment_products.view.*


class ProductsFragment : Fragment() {

    private lateinit var productsRecycler:RecyclerView;
    private lateinit var productsType : String
    private lateinit var countyFilter: Spinner
    private lateinit var typeFilter : Spinner
    private lateinit var countiesApiHandler:CountiesApi
    private lateinit var productsAdapter:ArrayAdapter<String>

    companion object {
        fun newInstance(): ProductsFragment {
            return ProductsFragment()
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_products, container, false);

        initalizeUIElemenets(view)
        loadProducts(view)


        return view;
    }

    private fun loadProducts(view:View){
        val bundle = this.arguments
        if(bundle != null) {
            productsType = bundle.getString("productsType")!!

            if(productsType == "all"){
                submitRequestForProducts()
                loadCounties(view)
                loadTypeMenu(view)
                view.filterSubmitButton.visibility = View.VISIBLE
            }
            if(productsType == "userProducts"){
                submitRequestForProducts()
                view.filterSubmitButton.visibility = View.INVISIBLE
            }
        }
    }

    private fun submitRequestForProducts(){
        val fragmentManager = fragmentManager;
        val productsApi = ProductsApi(context!!,productsRecycler,fragmentManager!!,productsType)
        productsApi.submitGetProductsRequest(Request.Method.POST)

    }

    private fun initalizeUIElemenets(view:View){
        productsRecycler = view.findViewById(R.id.productsDisplay)
        productsRecycler.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL,false)
        productsRecycler.itemAnimator = DefaultItemAnimator()
        productsRecycler.addItemDecoration(DividerItemDecoration(activity, RecyclerView.VERTICAL))

        countyFilter = view.filterCounty
        typeFilter = view.filterType

        view.filterSubmitButton.setOnClickListener { filterProducts(view) }
    }

    private fun filterProducts(view: View){
        val fragmentManager = fragmentManager;
        val productsApi = ProductsApi(context!!,productsRecycler,fragmentManager!!,productsType)
        productsApi.setFilterInputs(countyFilter.selectedItem.toString(),typeFilter.selectedItem.toString())
        productsApi.submitGetFilteredProducts(Request.Method.POST)
    }

    private fun loadCounties(view:View){
        countiesApiHandler = CountiesApi(context!!,countyFilter);
        countiesApiHandler.submitGetCountiesRequest(Request.Method.POST);
    }
    private fun loadTypeMenu(view: View){
        val types = mutableListOf<String>("Suhomesnati proizvodi","Voće","Povrće")
        val productTypeAdapter = ArrayAdapter<String>(context!!,android.R.layout.simple_list_item_1,types)
        typeFilter.adapter = productTypeAdapter
    }
}