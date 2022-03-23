package com.example.chatapp

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(val context: Context, val messageList: ArrayList<ChatElement>): RecyclerView.Adapter<ChatAdapter.ChatHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.chat, parent, false)
        return ChatHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        val currentMessage = messageList[position]
        val viewHolder = holder as ChatHolder
        holder.name.text = currentMessage.name
        holder.body.setOnClickListener {
            val intent = Intent(context, Chat::class.java)
            intent.putExtra("chat", currentMessage.chatName)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class ChatHolder(item: View): RecyclerView.ViewHolder(item) {
        var body = item.findViewById<ConstraintLayout>(R.id.chatElementBody)
        var name = item.findViewById<TextView>(R.id.textName)
    }
}