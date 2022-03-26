package com.example.chatapp

import android.annotation.SuppressLint
import android.content.Intent
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
    private val cities = arrayOf("Київ", "Одеса", "Дніпро", "Чернігів", "Харків", "Житомир", "Полтава",
        "Херсон"  ,"Київська область"  ,"Запоріжжя"  ,"Луганськ"  ,"Донецьк"  ,"Вінниця"  ,"Крим наш"
        ,"Миколаїв"  ,"Кіровоград"  ,"Суми"  ,"Львів"  ,"Черкаси"  ,"Хмельницький"  ,"Волинь"  ,"Рівне",
        "Івано-Франківськ"  ,"Тернопіль"  ,"Десь в Карпатах"  ,"Чернівці", "Севастополь")
    private val activities = arrayOf("Спорт", "Алкоголь", "Музика", "Інтелектуальні ігри", "Наука",
    "Big black cocks", "Я гуцул, який любить випасати худобу", "Я черепашка", "Драти москаля")
    @SuppressLint("ResourceType")
    private val database = Firebase.database
    private val users = database.getReference("users")
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



        val arrayAdapter1 = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cities)
        val arrayAdapter2 = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, activities)
        arrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinnerCity).adapter = arrayAdapter1
        findViewById<Spinner>(R.id.spinnerActivity).adapter = arrayAdapter2

        updateChats()
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
    fun showMenu(item: MenuItem? = null) {
        findViewById<ConstraintLayout>(R.id.content).visibility = ConstraintLayout.VISIBLE
        findViewById<RecyclerView>(R.id.listOfchats).visibility = RecyclerView.GONE
    }

    fun showChats(item: MenuItem? = null) {
        findViewById<ConstraintLayout>(R.id.content).visibility = ConstraintLayout.GONE
        findViewById<RecyclerView>(R.id.listOfchats).visibility = RecyclerView.VISIBLE
    }


    // найти человека по интересам
    fun findAlcoPerson(view: View) {
        var exists = false
        val city = findViewById<Spinner>(R.id.spinnerCity).selectedItem.toString()
        val activity = findViewById<Spinner>(R.id.spinnerActivity).selectedItem.toString()
        waiting.get().addOnSuccessListener {
            for (child in it.children) {
                if ((child.child("city").value == city && child.child("activity").value == activity && child.key != auth.uid.toString())) {
                    users.child(auth.uid!!).child("chats").child("${auth.uid}-${child.key}")
                        .setValue(child.key)
                    users.child(child.key as String).child("chats")
                        .child("${auth.uid}-${child.key}").setValue(auth.uid)
                    chatRef.child("${auth.uid}-${child.key}").setValue(1)
                    waiting.child(child.key.toString()).removeValue()
                    exists = true
                    updateChats()
                    showChats()
                }
            }
            if (!exists) {
                waiting.child(auth.uid.toString()).child("city").setValue(city)
                waiting.child(auth.uid.toString()).child("activity").setValue(activity)
            }
        }
    }
    fun getHelp(item:MenuItem){
        startActivity(Intent(this, Help::class.java))
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateChats(){
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
}
