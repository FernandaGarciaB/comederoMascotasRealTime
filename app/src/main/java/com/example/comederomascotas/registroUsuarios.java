package com.example.comederomascotas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class registroUsuarios extends AppCompatActivity {

    Button btn_register;
    EditText name, email, password;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    private FirebaseFirestore mfirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_usuarios);
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        name = findViewById(R.id.nombre);
        email = findViewById(R.id.correo);
        password = findViewById(R.id.contraseña);
        btn_register = findViewById(R.id.btn_registro);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameUser = name.getText().toString().trim();
                String emailUser = email.getText().toString().trim();
                String passUser = password.getText().toString().trim();

                if (nameUser.isEmpty() || emailUser.isEmpty() || passUser.isEmpty()){
                    Toast.makeText(registroUsuarios.this, "Complete los datos", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(nameUser, emailUser,passUser);
                }
            }
        });
    }

    private void registerUser(String nameUser, String emailUser, String passUser) {

        mAuth.createUserWithEmailAndPassword(emailUser, passUser)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String id = mAuth.getCurrentUser().getUid();
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", id);
                            map.put("name", nameUser);
                            map.put("email", emailUser);
                            map.put("password", passUser);

                            mFirestore.collection("Users").document(id).set(map)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            finish();
                                            startActivity(new Intent(registroUsuarios.this,home.class));
                                            Toast.makeText(registroUsuarios.this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(registroUsuarios.this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(registroUsuarios.this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void interfazLogin(View view) {
        Intent intent = new Intent(view.getContext(), login.class);
        view.getContext().startActivity(intent);
    }
}