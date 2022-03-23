package com.example.chatapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Chat : AppCompatActivity() {
    private val database = Firebase.database
    private val chatRef = database.getReference("chats")
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<ChatMessage>
    lateinit var auth: FirebaseAuth
    private lateinit var chat: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)


        auth = Firebase.auth
        chat = intent.extras?.get("chat").toString()
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        val chatRecyclerView = findViewById<RecyclerView>(R.id.MessageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        chatRef.child(chat).child("messages").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (postSnapshot in snapshot.children){
                    postSnapshot.getValue(ChatMessage::class.java)?.let { (messageList).add(it) }
                }
                messageAdapter.notifyDataSetChanged()
                chatRecyclerView.scrollToPosition(messageList.size-1)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            startActivity(Intent(this, MainActivity::class.java))
        }
        return true
    }

    @SuppressLint("SimpleDateFormat")
    fun sendMessage(view: View){ // отправка сообщения в базу
        val text = findViewById<EditText>(R.id.editText).text.toString()
        if (text.isNotEmpty()){
            chatRef.child(chat).child("messages").push().setValue(ChatMessage(text, auth.uid,SimpleDateFormat("HH:mm").format(Date())))
            findViewById<EditText>(R.id.editText).setText("")
        }
    }
}
