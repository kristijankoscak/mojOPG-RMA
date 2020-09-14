package hr.ferit.kristijankoscak.mojopg.API

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

open class ApiHandler {
    var queue: RequestQueue;

    constructor(context: Context){
        queue = Volley.newRequestQueue(context);
    }
}