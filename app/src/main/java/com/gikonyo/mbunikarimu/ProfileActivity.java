package com.gikonyo.mbunikarimu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;

public class ProfileActivity extends AppCompatActivity {

    //create instances of the views
    private EditText profUserName;
    private ImageButton imageButton;
    private Button doneBtn;

    //declare an instance of the firebase authentication
    private FirebaseAuth mAuth;
    //we now create an instance of the DatabaseReference where we will be storing the user photo and the username
    private DatabaseReference mDatabaseUser;
    //declare an instance of the StorageReference where we will upload the photo
    private StorageReference mStorageRef;
    //declare an instance of the URI to get an image from our mobile device
    private Uri profileImageUri=null;

    /*
    * we want to get and set the image hence we will start an implict intent using the method startActivityForResult() which requires two params-the intent
    * and the request code
    *
    * Now we declare our requestCode
    * */

    private final static int GALLERY_REQ=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //inflate the toolbar
        Toolbar toolbar=findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        //initialize the instances of the views
        profUserName=findViewById(R.id.profUserName);
        imageButton=findViewById(R.id.imagebutton);
        doneBtn=findViewById(R.id.doneBtn);

        //initialize the instances of the Firebase authentication
        mAuth=FirebaseAuth.getInstance();
        // we want to set the profile for specific , hence get the userID of the current user and assign it to a string variable
       final String userID=mAuth.getCurrentUser().getUid();
       //initialize the database reference where you have your registered users and get the specific user reference using the userID
        mDatabaseUser= FirebaseDatabase.getInstance().getReference().child("User").child(userID);

        //initialize the firebase storage reference where you will store the profile photo images
        mStorageRef= FirebaseStorage.getInstance().getReference().child("profile_images");

        //now we set an OnClickListener on the image button so as to allow users to pick their profile photo from their gallery
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create an implicit intent for getting the images
                Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
                //set the type to images only
                galleryIntent.setType("images/*");
                //now we need to see results so we use the startActivityForResult method and pass the params:request code and the intent
                startActivityForResult(galleryIntent,GALLERY_REQ);
            }
        });
        //when we click on an image, we want to view the name and the profile photo , then later save this on a database reference for a specific user
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get the custom display name entered by the user
                final String name=profUserName.getText().toString().trim();

                //validate to ensure that the profile photo and the name are not null
                if(!TextUtils.isEmpty(name)&&profileImageUri!=null){
                    /*
                    * create a storage reference node, inside the profile_image storage reference where we will save the profile image
                    * */
                    StorageReference profileImagePath=mStorageRef.child("profile_images").child(profileImageUri.getLastPathSegment());
                    /*
                    * call the putFile() method passing the profile image that was set by the user set on the  storage reference where you are uploading the image
                    *
                    * after this, call addOnSuccessListener() on the reference to listen if the upload task was successful, and get a snapshot of the task
                    * */
                    profileImagePath.putFile(profileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload of the profile image was successful, get the download URL
                            if (taskSnapshot.getMetadata() != null) {
                                if(taskSnapshot.getMetadata().getReference()!=null){
                                    //get the download URL from the phone's storage by using the methods:getStorage() and getDownloadUrl()
                                    Task<Uri> result=taskSnapshot.getStorage().getDownloadUrl();

                                    //now we call the onSuccessListener to determine if we got the download Url
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            //convert the URI to a string upon success
                                            final String profileImage=uri.toString();

                                            //call the method push() to add values  on the database reference of a specific user
                                            mDatabaseUser.push();

                                            //call addValueEventListener() to publish the additions in the database reference of a specific user
                                            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    //add the profile photo and displayName for the current user
                                                    mDatabaseUser.child("displayName").setValue(name);
                                                    mDatabaseUser.child("profile_photo").setValue(profileImage).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                //show a toast to indicate it was updated
                                                                Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                                                                //now launch the LoginActiity
                                                                Intent login =new Intent(ProfileActivity.this,LoginActivity.class);
                                                                startActivity(login);
                                                            }
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });

    }
    @Override
    //override this method to get the profile image set in the image button view
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==GALLERY_REQ&&resultCode==RESULT_OK){
            //get the image selected by the user
            profileImageUri=data.getData();
            //set in the image button view
            imageButton.setImageURI(profileImageUri);

        }
    }
}