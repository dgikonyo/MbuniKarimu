package com.gikonyo.mbunikarimu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText loginEmail,loginPass;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //inflate toolbar
        Toolbar toolbar=findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        loginBtn =findViewById(R.id.loginBtn);
        loginPass=findViewById(R.id.login_password);
        loginEmail=findViewById(R.id.login_email);

        //initialize the firebase authentication instance
        mAuth= FirebaseAuth.getInstance();
        //initialize the database reference where you have the child node users
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        //set onClickListener on the loginButton
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this,"Processing",Toast.LENGTH_LONG).show();
                //get the email and password entered by the user
                String email=loginEmail.getText().toString().trim();
                String password=loginPass.getText().toString().trim();

                if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)){
                    /*
                    * create a firebase authentication instance and use it to call the method signInWithEmailAndPassword()
                    method for passing the email and password you got from the views
                    * Further call the addOnCompleteListener() method to handle the Authentication result
                    * */
                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //we check if user exists in our database
                                checkUserExistence();
                            }else{
                                Toast.makeText(LoginActivity.this,"Couldn't login, worng email or password"
                                        ,Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    //if all fields for login were not filled show a message
                    Toast.makeText(LoginActivity.this,"Complete all fields",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
        //check if user exists
    public void checkUserExistence() {
        //check existence of a user using the user_id in the users database reference
        final String user_id=mAuth.getCurrentUser().getUid();
            //call the method addValueEventListener to check if the userid supplied exists in the database
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.hasChild(user_id)){
                        //if user exists, send them to the main page
                        Intent mainPage=new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(mainPage);
                    }else{
                        Toast.makeText(LoginActivity.this,"You are not registered",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            })
    }

}