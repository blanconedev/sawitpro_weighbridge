package com.blanccone.sawitproweighbridge.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.core.util.Utils
import com.blanccone.sawitproweighbridge.databinding.ActivityHomeBinding
import com.blanccone.sawitproweighbridge.ui.HomeMenuAdapter
import com.blanccone.sawitproweighbridge.ui.viewmodel.WeighmentViewModel
import com.blanccone.sawitproweighbridge.util.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : CoreActivity<ActivityHomeBinding>() {

    private val viewModel: WeighmentViewModel by viewModels()

    private val homeMenuAdapter by lazy { HomeMenuAdapter() }

    private val tickets = arrayListOf<Ticket>()

    @Inject
    internal lateinit var firebaseDb: DatabaseReference

    override fun inflateLayout(inflater: LayoutInflater): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Weighment Home"
        setMenu()
        setEvent()
        setObserves()
        viewModel.gettickets()
    }

    private fun setObserves() {
        viewModel.tickets.observe(this) {
            setView(it)
        }
    }

    private fun fetchFromFirebase() {
        firebaseDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tickets.clear()
                for (ticketSnapshot in snapshot.children) {
                    val ticket = ticketSnapshot.getValue(Ticket::class.java)
                    ticket?.let {
                        if (ticket.status == "Done") {
                            tickets.add(it)
                        }
                    }
                }
                setView(tickets)
            }

            override fun onCancelled(error: DatabaseError) {
                if (!Utils.isConnected(this@HomeActivity)) {
                    fetchFromLocal()
                }
            }
        })
    }

    private fun fetchFromLocal() {
        viewModel.gettickets()
    }

    private fun setView(tickets: List<Ticket>) {
        val inboundTickets = tickets.filter { ticket -> ticket.status == "Inbound" }
        val outboundTickets = tickets.filter { ticket -> ticket.status == "Outbound" }
        with(binding) {
            tvFirstWeight.text = inboundTickets.size.toString()
            tvSecondWeight.text = outboundTickets.size.toString()
        }
    }

    private fun setMenu() {
        binding.rvMenuUtama.adapter = homeMenuAdapter.apply {
            submitData(Const.homeMenu())
        }
    }

    private fun setEvent() {
        homeMenuAdapter.setOnItemClickListener {
            if (it == "tickets123") {
                ListTicketActivity.newInstance(this)
            } else {
                ListWeighmentResultActivity.newInstance(this)
            }
        }
    }

    companion object {

        fun newInstance(context: Context) {
            val intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)
        }
    }
}