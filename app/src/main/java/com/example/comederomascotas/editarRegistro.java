package com.example.comederomascotas;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class editarRegistro extends AppCompatActivity {

    EditText editTextNombre, editTextHoraInicio, editTextIdC;
    RadioButton radioButtonPequeño, radioButtonMediano, radioButtonGrande;
    RadioButton radioButtonCachorro, radioButtonAdulto, radioButtonVejez;
    DatabaseReference dbRef;
    String registroId;
    ImageButton btn_exit;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_registro);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        btn_exit = findViewById(R.id.btn_cerrar);

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                finish();
                startActivity(new Intent(editarRegistro.this, login.class));
            }
        });

        // Obtener los datos del registro seleccionado de los extras del intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String nombreMascota = extras.getString("nombreMascota");
            String horaInicio = extras.getString("horaInicio");
            String idCollarStr = extras.getString("idCollarStr");
            registroId = extras.getString("registroId");

            // Configurar EditText con los datos del registro
            editTextNombre = findViewById(R.id.editTextNombre);
            editTextNombre.setText(nombreMascota);

            editTextHoraInicio = findViewById(R.id.editTextHoraInicio);
            editTextHoraInicio.setText(horaInicio);

            editTextIdC = findViewById(R.id.editTextIdC);
            editTextIdC.setText(idCollarStr);

            // Recuperar los RadioButtons de tamaño y edad
            radioButtonPequeño = findViewById(R.id.radioButton4);
            radioButtonMediano = findViewById(R.id.radioButton5);
            radioButtonGrande = findViewById(R.id.radioButton6);
            radioButtonCachorro = findViewById(R.id.radioButton7);
            radioButtonAdulto = findViewById(R.id.radioButton8);
            radioButtonVejez = findViewById(R.id.radioButton9);

            // Recuperar los valores de los RadioButtons del intent y establecerlos
            String tamaño = extras.getString("tamaño");
            String edad = extras.getString("edad");
            establecerValoresRadio(tamaño, edad);
        }

        // Mostrar el selector de hora al hacer clic en el EditText de hora de inicio
        editTextHoraInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarSelectorHoraInicio();
            }
        });
    }

    // Método para establecer los valores de los RadioButtons según los datos del registro seleccionado
    private void establecerValoresRadio(String tamaño, String edad) {
        switch (tamaño) {
            case "Pequeño":
                radioButtonPequeño.setChecked(true);
                break;
            case "Mediano":
                radioButtonMediano.setChecked(true);
                break;
            case "Grande":
                radioButtonGrande.setChecked(true);
                break;
        }

        switch (edad) {
            case "Cachorro":
                radioButtonCachorro.setChecked(true);
                break;
            case "Adulto":
                radioButtonAdulto.setChecked(true);
                break;
            case "Vejez":
                radioButtonVejez.setChecked(true);
                break;
        }
    }

    public void guardarCambios(View view) {
        // Obtener los nuevos datos del registro editado
        String nuevoNombre = editTextNombre.getText().toString();
        String nuevaHoraInicio = editTextHoraInicio.getText().toString();
        String nuevoId = editTextIdC.getText().toString();

        // Obtener los nuevos valores de los RadioButtons de tamaño y edad
        String nuevoTamaño = obtenerValorRadio();
        String nuevaEdad = obtenerValorRadioEdad();

        // Actualizar el registro en la base de datos
        Map<String, Object> datosActualizados = new HashMap<>();
        datosActualizados.put("nombre", nuevoNombre);
        datosActualizados.put("horaInicio", nuevaHoraInicio);
        datosActualizados.put("idCollarStr", nuevoId);
        datosActualizados.put("tamaño", nuevoTamaño);
        datosActualizados.put("edad", nuevaEdad);

        // Actualizar también los valores de los ramos de comida
        String[] comidaYPasos = obtenerComidaYPasosPorEdadYTamaño(nuevaEdad, nuevoTamaño);
        String comida = comidaYPasos[0];
        String pasos = comidaYPasos[1];
        datosActualizados.put("comida", comida);
        datosActualizados.put("pasos", pasos);

        dbRef.child("mascotas").child(registroId)
                .updateChildren(datosActualizados)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(editarRegistro.this, "Registro actualizado correctamente", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(editarRegistro.this, "Error al actualizar el registro", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Método para obtener el valor del RadioButton de tamaño seleccionado
    private String obtenerValorRadio() {
        if (radioButtonPequeño.isChecked()) {
            return "Pequeño";
        } else if (radioButtonMediano.isChecked()) {
            return "Mediano";
        } else if (radioButtonGrande.isChecked()) {
            return "Grande";
        }
        return "";
    }

    // Método para obtener el valor del RadioButton de edad seleccionado
    private String obtenerValorRadioEdad() {
        if (radioButtonCachorro.isChecked()) {
            return "Cachorro";
        } else if (radioButtonAdulto.isChecked()) {
            return "Adulto";
        } else if (radioButtonVejez.isChecked()) {
            return "Vejez";
        }
        return "";
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
                        editTextHoraInicio.setText(horaSeleccionada);
                    }
                }, hora, minuto, true);
        timePickerDialog.show();
    }

    public void interfazHome(View view) {
        Intent intent = new Intent(view.getContext(), home.class);
        view.getContext().startActivity(intent);
    }

    public void interfazPendientes(View view) {
        Intent intent = new Intent(view.getContext(), pendientes.class);
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
}
