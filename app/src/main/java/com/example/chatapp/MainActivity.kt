package com.example.chatapp

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    @SuppressLint("ResourceType")
    private val database = Firebase.database
    private val users = database.getReference("users")
    private val chatRef = database.getReference("chats")
    private val waiting = database.getReference("waiting")
    private lateinit var chat:String
    lateinit var auth: FirebaseAuth
    lateinit var name:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.icon1svg)
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

    fun changeView(){
        val cLayout = findViewById<ConstraintLayout>(R.id.content)
        val chatLayout = findViewById<ConstraintLayout>(R.id.chat)
        if (cLayout.isVisible){
            cLayout.visibility = ConstraintLayout.GONE
            chatLayout.visibility = ConstraintLayout.VISIBLE
        }
        else{
            cLayout.visibility = ConstraintLayout.VISIBLE
            chatLayout.visibility = ConstraintLayout.GONE
        }
    }

    // окошко с выбором новой переписки
    fun showContent(item: MenuItem) {
        findViewById<ConstraintLayout>(R.id.content).visibility = ConstraintLayout.VISIBLE
        findViewById<ConstraintLayout>(R.id.chat).visibility = ConstraintLayout.GONE
        findViewById<LinearLayout>(R.id.chats).visibility = LinearLayout.GONE
    }

    // окошко где переписка
    fun showChat(){
        findViewById<ConstraintLayout>(R.id.content).visibility = ConstraintLayout.GONE
        findViewById<ConstraintLayout>(R.id.chat).visibility = ConstraintLayout.VISIBLE
        findViewById<LinearLayout>(R.id.chats).visibility = LinearLayout.GONE
        onChangeListener(chatRef.child(chat))
    }

    // показать окошко, где размещены все чаты
    @SuppressLint("CutPasteId", "ResourceAsColor")
    fun showChats(item: MenuItem) {
        findViewById<ConstraintLayout>(R.id.content).visibility = ConstraintLayout.GONE
        findViewById<LinearLayout>(R.id.chats).visibility = LinearLayout.VISIBLE
        findViewById<ConstraintLayout>(R.id.chat).visibility = ConstraintLayout.GONE
        val chatsLayout = findViewById<LinearLayout>(R.id.chats)
        users.get().addOnSuccessListener {
            it.child(auth.uid.toString()).child("chats").children.forEach { chat ->
                val textView = TextView(this)
//                textView.width = ViewGroup.LayoutParams.MATCH_PARENT
                textView.text = it.child(chat.value.toString()).child("name").value as CharSequence?
                textView.textSize = 24f
                textView.height = 150
                textView.setBackgroundColor(R.color.black)
                textView.setOnClickListener {
                    this.chat = chat.key.toString()
                    showChat()
                }
                if (chatsLayout.childCount < it.child(auth.uid.toString()).child("chats").childrenCount)
                    chatsLayout.addView(textView)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(view: View){ // отправка сообщения в базу
        val text = findViewById<EditText>(R.id.editText).text.toString()
        if (text.isNotEmpty()){
            val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:MM:ss"))
            val list = mapOf("from" to auth.uid.toString(), "message" to text)
            chatRef.child(chat).child(time).updateChildren(list)
            findViewById<EditText>(R.id.editText).setText("")
        }
    }

    //  слушаем изминения в базе
    private fun onChangeListener(dRef: DatabaseReference){
        dRef.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                users.get().addOnSuccessListener {
                    if (snapshot.child("from").value == auth.uid.toString())
                        findViewById<TextView>(R.id.textChatView).append("    ${it.child(auth.uid.toString()).child("name").value}: ${snapshot.child("message").value}\n\n")
                    else
                        findViewById<TextView>(R.id.textChatView).append("    ${it.child(it.child(auth.uid.toString()).child("chats").child(chat).value.toString()).child("name").value}: ${snapshot.child("message").value}\n\n")
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })

    }
}
