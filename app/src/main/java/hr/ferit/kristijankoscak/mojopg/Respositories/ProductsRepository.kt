package hr.ferit.kristijankoscak.mojopg.Respositories

import hr.ferit.kristijankoscak.mojopg.Model.Product

object ProductsRepository {
    var products: MutableList<Product>
    init {
        products = mutableListOf()
    }
    fun retreiveProducts(): MutableList<Product> {
        return products
    }
    fun saveProducts(products:MutableList<Product>){
        ProductsRepository.products = products
    }
    fun remove(id: Int) = products.removeAll{ product -> product.id == id }
    fun get(id: Int): Product? = products.find { product -> product.id == id }
    fun add(product: Product) = products.add(product)
    fun getSize():Int {
        return products.size;
    }
    fun emptyList(){
        products.clear()
    }
}