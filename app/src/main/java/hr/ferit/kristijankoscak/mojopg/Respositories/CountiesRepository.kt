package hr.ferit.kristijankoscak.mojopg.Respositories

object CountiesRepository {
    private var counties: MutableList<String>
    init {
        counties = mutableListOf()
    }
    fun retreiveCounties(): MutableList<String> {
        return counties
    }
    fun saveCounties(counties:MutableList<String>){
        this.counties = counties
    }
    //fun get(county:String): String? = counties.indexOf() { countyName -> countyName == county }


    fun add(county: String) = counties.add(county)
}