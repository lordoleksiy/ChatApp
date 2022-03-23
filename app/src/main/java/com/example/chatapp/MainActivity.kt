package com.example.chatapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    @SuppressLint("ResourceType")
    private val database = Firebase.database
    private val users = database.getReference("users")
    private lateinit var chat:String
    private val waiting = database.getReference("waiting")
    private val chatRef = database.getReference("chats")
    lateinit var auth: FirebaseAuth
    lateinit var name:String
    @SuppressLint("ResourceAsColor", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.icon1svg)

        val usersList = ArrayList<ChatElement>()
        val chatAdapter = ChatAdapter(this, usersList)

        val chatRecyclerView = findViewById<RecyclerView>(R.id.listOfchats)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter

        users.get().addOnSuccessListener {
            it.child(auth.uid.toString()).child("chats").children.forEach { chat ->
                usersList.add(ChatElement(it.child(chat.value.toString()).child("name").value.toString(), chat.key.toString()))
                chatAdapter.notifyDataSetChanged()
            }
        }
    }

    // выдвигаем/задвигам навигатион тул
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            if (findViewById<DrawerLayout>(R.id.drawer).isDrawerOpen(GravityCompat.START))
                findViewById<DrawerLayout>(R.id.drawer).closeDrawer(GravityCompat.START)
            else
                findViewById<DrawerLayout>(R.id.drawer).openDrawer(GravityCompat.START)
            users.child(auth.uid.toString()).child("name").get().addOnSuccessListener {
                findViewById<TextView>(R.id.name).text = it.value.toString()
            }
        }
        return true
    }

    // окошко с выбором новой переписки
    fun showMenu(item: MenuItem) {
        findViewById<ConstraintLayout>(R.id.content).visibility = ConstraintLayout.VISIBLE
        findViewById<RecyclerView>(R.id.listOfchats).visibility = RecyclerView.GONE
    }

    fun showChats(item: MenuItem) {
        findViewById<ConstraintLayout>(R.id.content).visibility = ConstraintLayout.GONE
        findViewById<RecyclerView>(R.id.listOfchats).visibility = RecyclerView.VISIBLE
    }

    // найти человека по интересам
    fun findAlcoPerson(view: View){
        var exists = false
        waiting.get().addOnSuccessListener {
            for (child in it.children){
                if (child.key.toString() == "AlcoPerson" && child.value != auth.uid.toString()){
                    users.child(auth.uid!!).child("chats").child("${auth.uid}-${child.value}").setValue(child.value)
                    users.child(child.value as String).child("chats").child("${auth.uid}-${child.value}").setValue(auth.uid)
                    chatRef.child("${auth.uid}-${child.value}").setValue(1)
                    exists = true
                }
            }
            if (exists)
                waiting.child("AlcoPerson").removeValue()
            else
                waiting.child("AlcoPerson").setValue(auth.uid)
        }
    }
}
