package com.example.comederomascotas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class pendientes extends AppCompatActivity {

    FirebaseFirestore db;
    LinearLayout contenedorRegistros;
    FirebaseAuth mAuth;
    EditText editTextText;
    ImageButton buttonBuscar, btn_exit;
    FirebaseFirestore mFirestore;

    private static final int EDITAR_REGISTRO_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pendientes);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        contenedorRegistros = findViewById(R.id.contenedorRegistros);
        editTextText = findViewById(R.id.editTextText);
        buttonBuscar = findViewById(R.id.button2);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        btn_exit = findViewById(R.id.btn_cerrar);

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                finish();
                startActivity(new Intent(pendientes.this, login.class));
            }
        });

        buttonBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarMascotas();
            }
        });

        obtenerRegistrosMascotas();
    }

    private void obtenerRegistrosMascotas() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            db.collection("mascotas")
                    .whereEqualTo("userId", userId)
                    .orderBy("horaInicio") // Ordenar los documentos por el campo "horaInicio"
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                contenedorRegistros.removeAllViews();

                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                    String nombreMascota = document.getString("nombre");
                                    String horaInicio = document.getString("horaInicio");
                                    String edad = document.getString("edad");
                                    String tamaño = document.getString("tamaño");
                                    String comida = document.getString("comida");
                                    String idCollarStr = document.getString("idCollarStr");

                                    View registroView = crearVistaRegistroMascota(nombreMascota, horaInicio, edad, tamaño, comida);
                                    if (registroView != null) {
                                        contenedorRegistros.addView(registroView);

                                        ImageButton btnEliminar = registroView.findViewById(R.id.eliminarRegistro);
                                        btnEliminar.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                eliminarRegistro(document.getId());
                                            }
                                        });

                                        ImageButton btnEditar = registroView.findViewById(R.id.editar);
                                        btnEditar.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                editarRegistro(nombreMascota, horaInicio, edad, tamaño, comida, document.getId(), idCollarStr);
                                            }
                                        });
                                    } else {
                                        Log.e("pendientes", "Error al crear vista del registro de mascota");
                                    }
                                }
                            } else {
                                Log.d("pendientes", "Error al obtener registros de mascotas: ", task.getException());
                            }
                        }
                    });
        } else {
            Log.e("pendientes", "Usuario no autenticado");
        }
    }

    private void eliminarRegistro(String idRegistro) {
        db.collection("mascotas").document(idRegistro)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("pendientes", "Registro eliminado correctamente");
                        obtenerRegistrosMascotas();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("pendientes", "Error al eliminar el registro", e);
                    }
                });
    }

    private View crearVistaRegistroMascota(String nombreMascota, String horaInicio, String edad, String tamaño, String comida) {
        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            View registroView = inflater.inflate(R.layout.pendiente_perro_layout, contenedorRegistros, false);

            TextView textViewNombre = registroView.findViewById(R.id.nombreMascota);
            TextView textViewHorario = registroView.findViewById(R.id.horario);
            TextView textViewEdad = registroView.findViewById(R.id.edadM);
            TextView textViewTamaño = registroView.findViewById(R.id.tamañoM);
            TextView textViewComida = registroView.findViewById(R.id.comida);

            textViewNombre.setText(nombreMascota);
            textViewHorario.setText(horaInicio);
            textViewEdad.setText(edad);
            textViewTamaño.setText(tamaño);
            textViewComida.setText(comida);

            return registroView;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void buscarMascotas() {
        String textoBusqueda = editTextText.getText().toString().trim();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            db.collection("mascotas")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("nombre", textoBusqueda)
                    .orderBy("horaInicio")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                contenedorRegistros.removeAllViews();

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String nombreMascota = document.getString("nombre");
                                    String horaInicio = document.getString("horaInicio");
                                    String edad = document.getString("edad");
                                    String tamaño = document.getString("tamaño");
                                    String comida = document.getString("comida");

                                    View registroView = crearVistaRegistroMascota(nombreMascota, horaInicio, edad, tamaño, comida);
                                    if (registroView != null) {
                                        contenedorRegistros.addView(registroView);
                                    } else {
                                        Log.e("pendientes", "Error al crear vista del registro de mascota");
                                    }
                                }
                            } else {
                                Log.d("pendientes", "Error al obtener registros de mascotas: ", task.getException());
                            }
                        }
                    });
        } else {
            Log.e("pendientes", "Usuario no autenticado");
        }
    }

    public void editarRegistro(String nombreMascota, String horaInicio, String edad, String tamaño, String comida, String registroId, String idCollarStr) {
        Intent intent = new Intent(this, editarRegistro.class);
        intent.putExtra("nombreMascota", nombreMascota);
        intent.putExtra("horaInicio", horaInicio);
        intent.putExtra("edad", edad);
        intent.putExtra("tamaño", tamaño);
        intent.putExtra("comida", comida);
        intent.putExtra("registroId", registroId);
        intent.putExtra("idCollarStr", idCollarStr);
        startActivityForResult(intent, EDITAR_REGISTRO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDITAR_REGISTRO_REQUEST) {
            if (resultCode == RESULT_OK) {
                obtenerRegistrosMascotas();
            }
        }
    }

    public void interfazHome(View view) {
        Intent intent = new Intent(view.getContext(), home.class);
        view.getContext().startActivity(intent);
    }

    public void interfazInformacion(View view) {
        Intent intent = new Intent(view.getContext(), informacion.class);
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

    public void interfazLogin(View view) {
        Intent intent = new Intent(view.getContext(), login.class);
        view.getContext().startActivity(intent);
    }
}
