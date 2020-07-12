package com.gikonyo.mbunikarimu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button registerBtn;
    private EditText emailField,usernameField,passwordField;
    private TextView loginTxtView;

    //declare instance of firebase auth
    private FirebaseAuth mAuth;
    //declare instance of Firebase Database
    private FirebaseDatabase database;

    /*
    * create an instance of a database reference
    * a database reference is a node in the db that will identify a specific user*/

    private DatabaseReference userDetailsReference;

    public RegisterActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //inflate the toolbar
        Toolbar toolbar=findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        //initialize the views
        loginTxtView=findViewById(R.id.loginLink);
        registerBtn=findViewById(R.id.btn_register);
        emailField=findViewById(R.id.emailField);
        usernameField=findViewById(R.id.usernameField);
        passwordField=findViewById(R.id.passwordField);

        //initialize an instance of the Firebase Authentication by calling the getInstance() method
        mAuth=FirebaseAuth.getInstance();
        //initialize an instance of the Firebase Database  by calling the get instance method
        database=FirebaseDatabase.getInstance();

        /*initiialize an instance of the Firebase Database reference  by calling the database instance, getting the reference
        *  using the getReference() method on the database,
        *  and creating a new child node, in our case 'Users' where we will store the details of the registered users
        * */
        userDetailsReference=database.getReference().child("Users");
        /*
        * For those Users who have already registered, we want to redirect them directly to the login page
        * To implement this we use setOnClickListener() on the textView object of redirecting the user to LoginActivity
        * We implement an intent to do this
        * */
        loginTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(loginIntent);
            }
        });
        /*
        * we set an OnClickListener() on the register button to get the contents of the email, username and password after they've
        * been entered in the edittexts
        *
        * After this they'll be redirected to a profileActivity that will enable the users to set a custom displayName and their
        * profile image
        * */
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RegisterActivity.this,"LOADING",Toast.LENGTH_LONG).show();
                //we now get the username, email and password
                final String username=usernameField.getText().toString().trim();
                final String email=emailField.getText().toString().trim();
                final String password=passwordField.getText().toString().trim();

                //now we validate to check if the details have been entered in the email and username fields if yes the user is registered
                if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(username)&&!TextUtils.isEmpty(password)){
                    /*
                    create a new createAccount() that takes in an email address and password, validates them and creates a
                    new user with the [createUserWithEmailAndPassword] using the instance of the Firebase Authentication(mAuth)
                    that we created and add addOnCompleteListener
                    * */
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //so here, we will register the users according to their respective id's
                            //declare a string variable to get the value of a currently registered user
                            String user_id=mAuth.getCurrentUser().getUid();
                            //create a child node database reference to attach the user_id to the users node
                            DatabaseReference current_user_db=userDetailsReference.child(user_id);
                            //set the username and image on the users' unique_path(current_users_db)
                            current_user_db.child("Username").setValue(username);
                            current_user_db.child("Image").setValue("Default");
                            //return a messsage to user about the registration success
                            Toast.makeText(RegisterActivity.this,"Registration Successful",Toast.LENGTH_SHORT).show();
                            //we now create the ProfileActivity and redirect the users to this activity after registration
                            Intent profIntent=new Intent(RegisterActivity.this,ProfileActivity.class);
                            profIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(profIntent);
                        }
                    });
                }else{
                    Toast.makeText(RegisterActivity.this,"Complete all fields",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}