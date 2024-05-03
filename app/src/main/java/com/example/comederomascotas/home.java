package com.example.comederomascotas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class home extends AppCompatActivity {

    ImageButton btn_exit;
    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        btn_exit = findViewById(R.id.btn_cerrar);

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                finish();
                startActivity(new Intent(home.this, login.class));
            }
        });
    }

    public void interfazInformacion(View view) {
        Intent intent = new Intent(view.getContext(), informacion.class);
        view.getContext().startActivity(intent);
    }

    public void interfazHome(View view) {
        Intent intent = new Intent(view.getContext(), home.class);
        view.getContext().startActivity(intent);
    }

    public void interfazRegistroMascotas(View view) {
        Intent intent = new Intent(view.getContext(), registroMascotas.class);
        view.getContext().startActivity(intent);
    }

    public void interfazPendientes(View view) {
        Intent intent = new Intent(view.getContext(), pendientes.class);
        view.getContext().startActivity(intent);
    }

    public void interfazReistroM(View view) {
        Intent intent = new Intent(view.getContext(), registroMascotas.class);
        view.getContext().startActivity(intent);
    }
}