package hr.ferit.kristijankoscak.mojopg.Fragments.InteractionListener

interface ProductInteractionListener {
    fun onEdit(id:Int)
    fun onRemove(id: Int)
    fun onShowDetails(id: Int)
    fun onReport(id:Int)
}
