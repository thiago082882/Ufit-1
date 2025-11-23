package com.app.ufit.viewmodels.register

import android.app.Application
import android.content.ContentValues
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.app.ufit.data.SharedPref
import com.app.ufit.models.ResponseHttp
import com.app.ufit.models.User
import com.app.ufit.provider.UsersProvider
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class RegisterInfoViewModel @Inject constructor(
    private val usersProvider: UsersProvider,
    application: Application
) : AndroidViewModel(application) {

    val success = MutableLiveData<Boolean>()
    val load = MutableLiveData<Boolean>()

    fun registerUser(user: User) {
        load.postValue(true)

        usersProvider.register(user)?.enqueue(object : Callback<ResponseHttp> {
            override fun onResponse(
                call: Call<ResponseHttp>,
                response: Response<ResponseHttp>
            ) {
                login(user.email, user.password)
                success.postValue(true)
                load.postValue(false)

                val msg = response.body()?.message ?: "Registro realizado com sucesso."

                Toast.makeText(
                    getApplication(),
                    msg,
                    Toast.LENGTH_LONG
                ).show()

                Log.d(ContentValues.TAG, "Response: $response")
                Log.d(ContentValues.TAG, "Body: ${response.body()}")
            }

            override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                load.postValue(false)
                val error = "Ocorreu um erro: ${t.message ?: "desconhecido"}"

                Log.d(ContentValues.TAG, error)
                Toast.makeText(
                    getApplication(),
                    error,
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }


    fun login(email: String, password: String) {
        usersProvider.login(email, password)?.enqueue(object : Callback<ResponseHttp> {
            override fun onResponse(call: Call<ResponseHttp>, response: Response<ResponseHttp>) {
                Log.d("Main", "Response : ${response.body()}")

                if (response.body()?.isSuccess == true) {
                    saveUserInSession(response.body()?.data.toString())
                }
            }

            override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                Log.d("Main", "Houve um Erro ${t.message}")
                Toast.makeText(getApplication(), "Houve um Erro ${t.message}", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    fun saveUserInSession(data: String) {
        val sharedPref = SharedPref(getApplication())
        val gson = Gson()
        val user = gson.fromJson(data, User::class.java)
        sharedPref.save("user", user)
    }
}