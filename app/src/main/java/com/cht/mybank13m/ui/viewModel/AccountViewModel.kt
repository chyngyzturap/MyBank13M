package com.cht.mybank13m.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cht.mybank13m.data.model.Account
import com.cht.mybank13m.data.model.AccountState
import com.cht.mybank13m.data.network.AccountsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountsApi: AccountsApi
) : ViewModel() {

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    fun loadAccounts() {
        accountsApi.getAccounts().handleAccountResponse(onSuccess = { _accounts.value = it })
    }

    fun addAccount(account: Account) {
        accountsApi.addAccount(account).handleAccountResponse()
    }

    fun updateAccountFully(updatedAccount: Account) {
        updatedAccount.id?.let { accountsApi.updateAccountFully(it, updatedAccount).handleAccountResponse() }
    }

    fun updateAccountPartially(id: String, isChecked: Boolean) {
        accountsApi.updateAccountPartially(id, AccountState(isChecked)).handleAccountResponse()
    }

    fun deleteAccount(id: String) { accountsApi.deleteAccount(id).handleAccountResponse() }

    private fun <T> Call<T>?.handleAccountResponse(
        onSuccess: (T) -> Unit = { loadAccounts() },
        onError: (String) -> Unit = {}
    ) {
        this?.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val result = response.body()
                if (result != null && response.isSuccessful) {
                    onSuccess(result)
                } else {
                    val errorText =
                        "${response.code()}: ${response.errorBody()}, ${response.message()}"
                    onError(errorText)
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                onError(t.message.toString())
            }

        }

        )
    }


}