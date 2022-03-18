package com.example.chatapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Register : AppCompatActivity() {
    private val database = Firebase.database
    private val myRef = database.getReference("users")
    lateinit var launcher: ActivityResultLauncher<Intent>
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = Firebase.auth
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try{
                val account = task.getResult(ApiException::class.java)
                if (account != null){
                    authWithGoogle(account.idToken!!)
                }
            }
            catch (e: ApiException){
                Log.e("Ебать", "Ошибка вылетела из направления А в направление Б")
                Toast.makeText(this, "Дебил, интернет включи", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.button).setOnClickListener{
            signInWithGoogle()
        }
        checkAuth()
    }

    private fun getClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)
    }
    private fun signInWithGoogle(){
        val signInClient = getClient()
        launcher.launch(signInClient.signInIntent)
    }

    private fun authWithGoogle(idToken:String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful)
                checkData()
            else
                Toast.makeText(this, "Unfortunately, the error occurs", Toast.LENGTH_SHORT).show()

        }
    }
    private fun checkAuth(){
        if (auth.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    @SuppressLint("SetTextI18n")
    fun checkData(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ooops, I don't know what is your name")
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.HORIZONTAL
        val text1 = TextView(this)
        text1.text = "      Name: "
        val editText = EditText(this)
        layout.addView(text1)
        layout.addView(editText)
        builder.setView(layout)
        builder.setPositiveButton("Ok"){_, _ ->
            if (editText.text.toString().isEmpty()){
                Toast.makeText(this, "Сука заполни Имя", Toast.LENGTH_SHORT).show()
            }
            else{
                myRef.child(auth.uid!!.toString()).child("name").setValue(editText.text.toString())
                checkAuth()
            }
        }
        builder.show()
    }
}