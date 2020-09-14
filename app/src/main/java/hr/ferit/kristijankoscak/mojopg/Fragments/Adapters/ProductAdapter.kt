package hr.ferit.kristijankoscak.mojopg.Fragments.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import hr.ferit.kristijankoscak.mojopg.Fragments.InteractionListener.ProductInteractionListener
import hr.ferit.kristijankoscak.mojopg.UserPreferenceManager
import hr.ferit.kristijankoscak.mojopg.Model.Product
import hr.ferit.kristijankoscak.mojopg.R
import kotlinx.android.synthetic.main.item_product.view.*

class ProductAdapter(products: MutableList<Product>, productType:String ,productListener: ProductInteractionListener): RecyclerView.Adapter<ProductHolder>() {

    private val products: MutableList<Product>
    private val productListener: ProductInteractionListener;
    private val productType:String;

    init {

        this.products = mutableListOf()
        this.products.clear()
        this.products.addAll(products)
        this.productListener = productListener
        this.productType = productType
    }
    /*fun refreshData(products: MutableList<Product>) {
        this.products.clear()
        this.products.addAll(products)
        this.notifyDataSetChanged()
    }*/
    override fun getItemCount(): Int = products.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
        val productView = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        val productHolder = ProductHolder(productView)
        return productHolder
    }
    override fun onBindViewHolder(holder: ProductHolder, position: Int) {
        val product = products[position]
        holder.bind(product,productListener)
    }
}
class ProductHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val productImageUrl = "http://192.168.0.174/Kopg/public/storage/product_images/"

    fun bind(product: Product, productListener: ProductInteractionListener) {
        Log.d("user",UserPreferenceManager().retreiveEmail())
        itemView.itemProductType.text = product.type
        itemView.itemProductDetailType.text = product.detail_type
        Picasso.get()
            .load(productImageUrl+product.image_link)
            .fit()
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(android.R.drawable.stat_notify_error)
            .into(itemView.itemProductImage)
        itemView.itemProductInfo.setOnClickListener{productListener.onShowDetails(product.id);true;}
        itemView.itemProductReport.setOnClickListener{productListener.onReport(product.id);true;}
        itemView.itemProductDelete.setOnClickListener {productListener.onRemove(product.id);true;}
        itemView.itemProductEdit.setOnClickListener {productListener.onEdit(product.id);true;}

        toggleButtons(itemView,product.user_id)
        toggleReportButton(itemView)

    }

    fun toggleReportButton(itemView: View){
        Log.d("user",UserPreferenceManager().retreiveEmail())
        if(UserPreferenceManager().retreiveUserID() == ""){
            itemView.itemProductReport.visibility = View.INVISIBLE;
        }
        else{
            itemView.itemProductReport.visibility = View.VISIBLE;
        }
    }
    fun toggleButtons(itemView:View,productAuthorID:String){
        if(UserPreferenceManager().retreiveUserID() != productAuthorID){
            itemView.itemProductInfo.visibility = View.VISIBLE
            itemView.itemProductReport.visibility = View.VISIBLE
            itemView.itemProductEdit.visibility = View.INVISIBLE
            itemView.itemProductDelete.visibility = View.INVISIBLE
        }
        else{
            itemView.itemProductInfo.visibility = View.INVISIBLE
            itemView.itemProductReport.visibility = View.INVISIBLE
            itemView.itemProductEdit.visibility = View.VISIBLE
            itemView.itemProductDelete.visibility = View.VISIBLE
        }
    }

}