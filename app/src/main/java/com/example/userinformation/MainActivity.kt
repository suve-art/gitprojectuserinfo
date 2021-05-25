package com.example.userinformation

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinsql.DatabaseHandler
import com.example.kotlinsql.MyListAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*

class EmpModelClass (var userId: Int, val userName:String , val userEmail: String)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button2.setOnClickListener {
            var id = u_id.text.toString().toInt()
            var name = u_name.text.toString()
            var email = u_email.text.toString()
            database.child(id.toString()).setValue(user(name,email))
            saveRecord()
        }
    }
    var database = FirebaseDatabase.getInstance().reference
    fun saveRecord(){
        val id = u_id.text.toString()
        val name = u_name.text.toString()
        val email = u_email.text.toString()
        val databaseHandler: DatabaseHandler = DatabaseHandler(this)
        if(id.trim()!="" && name.trim()!="" && email.trim()!=""){
            val status = databaseHandler.addEmployee(EmpModelClass(Integer.parseInt(id),name, email))
            if(status > -1){
                Toast.makeText(applicationContext,"record save",Toast.LENGTH_LONG).show()
                u_id.text.clear()
                u_name.text.clear()
                u_email.text.clear()
            }
        }else{
            Toast.makeText(applicationContext,"id or name or email cannot be blank",Toast.LENGTH_LONG).show()
        }
    }

    fun viewRecord(view: View){

        if(isNetworkAvailbale()) {
            var getdata = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                @RequiresApi(Build.VERSION_CODES.KITKAT)
                override fun onDataChange(p0: DataSnapshot) {
                    var sb = StringBuilder()



                    val nameArray: MutableList<String> = ArrayList()

                    var idArray :MutableList<String> = ArrayList()
                    var emailArray: MutableList<String>  = ArrayList()
                    for(i in p0.children){


                        var id = i.key
                        var names = i.child("name").getValue();
                        var emails = i.child("email").getValue();

                        nameArray.add(names.toString());
                        idArray.add(id.toString())
                        emailArray.add(emails.toString());


                    }
                    val myListAdapter = MyListAdapter(
                        this@MainActivity, idArray,
                        nameArray, emailArray
                    )
                    listView.adapter = myListAdapter
                    Toast.makeText(applicationContext, sb.toString(),Toast.LENGTH_LONG).show()
//

                }
            }
            database.addValueEventListener(getdata)
            database.addListenerForSingleValueEvent(getdata)
        } else {
            Toast.makeText(this, "Network connection is not available", Toast.LENGTH_SHORT).show()
            val databaseHandler: DatabaseHandler = DatabaseHandler(this)

            val emp: List<EmpModelClass> = databaseHandler.viewEmployee()
            val empArrayId: MutableList<String> = ArrayList()
            val empArrayName : MutableList<String> = ArrayList()
            val empArrayEmail : MutableList<String> = ArrayList()
            var index = 0
            for (e in emp) {
                empArrayId.add(e.userId.toString())
                empArrayName.add(e.userName)
                empArrayEmail.add(e.userEmail)
                index++
            }

            val myListAdapter = MyListAdapter(this, empArrayId, empArrayName, empArrayEmail)
            listView.adapter = myListAdapter

        }
    }
    fun  isNetworkAvailbale():Boolean{
        val conManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val internetInfo =conManager.activeNetworkInfo
        return internetInfo!=null && internetInfo.isConnected
    }
}
