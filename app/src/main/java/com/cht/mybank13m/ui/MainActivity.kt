package com.cht.mybank13m.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cht.mybank13m.R
import com.cht.mybank13m.data.model.Account
import com.cht.mybank13m.databinding.ActivityMainBinding
import com.cht.mybank13m.databinding.DialogAddBinding
import com.cht.mybank13m.ui.viewModel.AccountViewModel
import com.cht.mybank13m.ui.adapter.AccountsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AccountsAdapter
    private val viewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initAdapter()
        subscribeToLiveData()

        binding.btnAdd.setOnClickListener {
            showAddDialog()
        }
    }

    private fun subscribeToLiveData() {
        viewModel.accounts.observe(this) {
            adapter.submitList(it)
        }
    }

    private fun showAddDialog(){
        val binding = DialogAddBinding.inflate(LayoutInflater.from(this))
        with(binding){
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Добавление нового счета")
                .setView(binding.root)
                .setPositiveButton("Добавить") { _,_ ->
                    val account = Account (
                        name = etName.text.toString(),
                        balance = etBalance.text.toString().toInt(),
                        currency = etCurrency.text.toString()
                    )
                    viewModel.addAccount(account)
                }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAccounts()
    }

    private fun initAdapter() = with(binding) {
        adapter = AccountsAdapter(
            onEdit = {
                showEditDialog(it)
            },
            onSwitchToggle = { id, isChecked ->
                viewModel.updateAccountPartially(id, isChecked)
            },
            onDelete = {
                showDeleteDialog(it)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.adapter = adapter
    }

    private fun showDeleteDialog(id: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_sure))
            .setMessage(getString(R.string.delete_with_id, id))
            .setPositiveButton(getString(R.string.delete)) {_,_ ->
                viewModel.deleteAccount(id)
            }
            .setNegativeButton(getString(R.string.cancel)) { _,_ ->
            }.show()
    }

    private fun showEditDialog(account: Account) {
        val binding = DialogAddBinding.inflate(LayoutInflater.from(this))
        with(binding){

            account.run {

                etName.setText(name)
                etBalance.setText(balance.toString())
                etCurrency.setText(currency)

                AlertDialog.Builder(this@MainActivity)
                    .setTitle(getText(R.string.edit_title))
                    .setView(binding.root)
                    .setPositiveButton("Изменить") { _,_ ->

                        val updatedAccount = account.copy (
                            name = etName.text.toString(),
                            balance = etBalance.text.toString().toInt(),
                            currency = etCurrency.text.toString()
                        )

                        viewModel.updateAccountFully(updatedAccount)

                    }.show()
            }
        }
    }

}