package hr.ferit.kristijankoscak.mojopg.API

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import hr.ferit.kristijankoscak.mojopg.R

import hr.ferit.kristijankoscak.mojopg.Respositories.CountiesRepository
import org.json.JSONArray
import org.json.JSONObject

class CountiesApi(context: Context, spinner: Spinner): ApiHandler(context){

    private var getCountiesApi = context.resources.getString(R.string.getCountiesUrl);

    private var context: Context;
    private var spinner: Spinner
    private lateinit var countiesAdapter: ArrayAdapter<String>
    private var countiesArray: MutableList<String>

    init {
        this.context = context;
        this.spinner = spinner;
        countiesArray = mutableListOf()
    }

    fun submitGetCountiesRequest(method: Int){
        if(CountiesRepository.retreiveCounties().size == 0){
            val request = StringRequest(
                method, getCountiesApi,
                Response.Listener { response ->
                    Log.d("counties",response.toString())
                    lateinit var jsonObject: JSONObject;
                    lateinit var jsonArray: JSONArray;
                    if(response is String){
                        countiesArray.add("Odaberite županiju")
                        jsonObject = JSONObject(response)
                        jsonArray = jsonObject.getJSONArray("counties")
                        for(i in 0 until jsonArray.length()){
                            countiesArray.add(jsonArray.getJSONObject(i).get("name").toString())
                        }
                        CountiesRepository.saveCounties(countiesArray)
                        setUpCountiesSpinner()
                        countiesAdapter.notifyDataSetChanged()
                    }
                },
                Response.ErrorListener { error->
                    Toast.makeText(this.context, error.message, Toast.LENGTH_LONG).show();
                }
            )
            queue.add(request)
        }
        else{
            setUpCountiesSpinner()
        }

    };
    private fun setUpCountiesSpinner(){
        countiesAdapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, CountiesRepository.retreiveCounties())
        spinner.adapter = countiesAdapter
    }
}