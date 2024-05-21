package com.example.comederomascotas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class registroMascotas extends AppCompatActivity {

    EditText nombreMascota, horaIntervalo, horaInicio, idCollar, cantidadRegistros;
    Button btnRegistrar, btnCancelar;
    DatabaseReference dbRef;
    ImageButton btn_exit;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_mascotas);

        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        nombreMascota = findViewById(R.id.nombreMascota);
        horaIntervalo = findViewById(R.id.horaIntervalo);
        btnRegistrar = findViewById(R.id.registrar);
        btnCancelar = findViewById(R.id.cancelar);
        horaInicio = findViewById(R.id.horaDeInicio);
        idCollar = findViewById(R.id.idC);
        cantidadRegistros = findViewById(R.id.cantidadR);
        btn_exit = findViewById(R.id.btn_cerrar);

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                finish();
                startActivity(new Intent(registroMascotas.this, login.class));
            }
        });

        horaInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarSelectorHoraInicio();
            }
        });
    }

    public void mostrarSelectorHoraInicio() {
        final Calendar c = Calendar.getInstance();
        int hora = c.get(Calendar.HOUR_OF_DAY);
        int minuto = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String horaSeleccionada = String.format("%02d:%02d", hourOfDay, minute);
                        horaInicio.setText(horaSeleccionada);
                    }
                }, hora, minuto, true);
        timePickerDialog.show();
    }

    private void agregarRegistro(Map<String, Object> mascota) {
        dbRef.child("mascotas").push().setValue(mascota)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(registroMascotas.this, "Mascota registrada con éxito", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(registroMascotas.this, "Error al registrar la mascota", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String obtenerTipoComida(String horaInicioStr) {
        String[] horaInicioParts = horaInicioStr.split(":");
        int horaInicio = Integer.parseInt(horaInicioParts[0]);

        if (horaInicio >= 7 && horaInicio < 12) {
            return "Desayuno";
        } else if (horaInicio >= 12 && horaInicio < 17) {
            return "Comida";
        } else {
            return "Cena";
        }
    }

    public void registrarMascota(View view) {
        String nombre = nombreMascota.getText().toString().trim();
        String intervaloHora = horaIntervalo.getText().toString().trim();
        String horaInicioStr = horaInicio.getText().toString().trim();
        String idCollarStr = idCollar.getText().toString().trim();
        String cantidadRegistrosStr = cantidadRegistros.getText().toString().trim();

        String tamaño = obtenerTamañoSeleccionado();
        String edad = obtenerEdadSeleccionada();
        String[] comidaYPasos = obtenerComidaYPasosPorEdadYTamaño(edad, tamaño);
        String comida = comidaYPasos[0];
        String pasos = comidaYPasos[1];

        if (idCollarStr.length() != 8) {
            Toast.makeText(this, "El ID del collar debe tener 8 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nombre.isEmpty() || intervaloHora.isEmpty() || horaInicioStr.isEmpty() || tamaño.isEmpty() || edad.isEmpty() || comida.isEmpty() || idCollarStr.isEmpty() || cantidadRegistrosStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int registrosAutomaticos = Integer.parseInt(cantidadRegistrosStr);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            Map<String, Object> mascotaManual = new HashMap<>();
            mascotaManual.put("nombre", nombre);
            mascotaManual.put("intervaloHora", intervaloHora);
            mascotaManual.put("horaInicio", horaInicioStr);
            mascotaManual.put("tamaño", tamaño);
            mascotaManual.put("edad", edad);
            mascotaManual.put("comida", comida);
            mascotaManual.put("idCollarStr", idCollarStr);
            mascotaManual.put("cantidadRegistrosStr", cantidadRegistrosStr);

            agregarRegistro(mascotaManual);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaInicioStr.split(":")[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(horaInicioStr.split(":")[1]));
            int intervalo = Integer.parseInt(intervaloHora);

            for (int i = 0; i < registrosAutomaticos; i++) {
                Map<String, Object> mascotaAutomatica = new HashMap<>();
                mascotaAutomatica.putAll(mascotaManual);

                if (i > 0) {
                    calendar.add(Calendar.HOUR_OF_DAY, intervalo);
                    mascotaAutomatica.put("horaInicio", String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
                }

                mascotaAutomatica.put("userId", userId);
                mascotaAutomatica.put("pasos", pasos);
                mascotaAutomatica.put("tipoComida", obtenerTipoComida(mascotaAutomatica.get("horaInicio").toString()));
                agregarRegistro(mascotaAutomatica);
            }

            startActivity(new Intent(registroMascotas.this, pendientes.class));
            finish();
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    private String[] obtenerComidaYPasosPorEdadYTamaño(String edad, String tamaño) {
        String[] comidaYPasos = new String[2];
        int comidaGramos = 0;
        int pasos = 0;

        if (tamaño.equals("Pequeño")) {
            if (edad.equals("Cachorro")) {
                comidaGramos = 100;
                pasos = 300;
            } else if (edad.equals("Adulto") || edad.equals("Vejez")) {
                comidaGramos = 150;
                pasos = 450;
            }
        } else if (tamaño.equals("Mediano")) {
            if (edad.equals("Cachorro")) {
                comidaGramos = 300;
                pasos = 900;
            } else if (edad.equals("Adulto") || edad.equals("Vejez")) {
                comidaGramos = 400;
                pasos = 1200;
            }
        } else if (tamaño.equals("Grande")) {
            if (edad.equals("Cachorro")) {
                comidaGramos = 500;
                pasos = 1500;
            } else if (edad.equals("Adulto") || edad.equals("Vejez")) {
                comidaGramos = 800;
                pasos = 2400;
            }
        }

        comidaYPasos[0] = Integer.toString(comidaGramos) + "g";
        comidaYPasos[1] = Integer.toString(pasos);

        return comidaYPasos;
    }

    private String obtenerTamañoSeleccionado() {
        RadioGroup radioGroupTamaño = findViewById(R.id.radioGroupTamaño);
        int selectedTamañoRadioButtonId = radioGroupTamaño.getCheckedRadioButtonId();
        if (selectedTamañoRadioButtonId != -1) {
            RadioButton radioButton = findViewById(selectedTamañoRadioButtonId);
            return radioButton.getText().toString();
        }
        return "";
    }

    private String obtenerEdadSeleccionada() {
        RadioGroup radioGroupEdad = findViewById(R.id.radioGroupEdad);
        int selectedEdadRadioButtonId = radioGroupEdad.getCheckedRadioButtonId();
        if (selectedEdadRadioButtonId != -1) {
            RadioButton radioButton = findViewById(selectedEdadRadioButtonId);
            return radioButton.getText().toString();
        }
        return "";
    }

    public void cancelarRegistro(View view) {
        startActivity(new Intent(registroMascotas.this, pendientes.class));
    }
}
