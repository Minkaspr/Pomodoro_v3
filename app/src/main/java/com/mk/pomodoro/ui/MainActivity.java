package com.mk.pomodoro.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mk.pomodoro.R;
import com.mk.pomodoro.ui.adapter.GestorPaginasAdapter;
import com.mk.pomodoro.ui.viewmodel.GestorPomodoroViewModel;
import com.mk.pomodoro.util.PomodoroAppDB;

public class MainActivity extends AppCompatActivity {

    private static final String ELEMENTO_SELECCIONADO = "elemento_seleccionado";
    private static final String ID_CANAL_SERVICIO_TEMPORIZADOR = "CanalServicioTemporizador";
    private static final String ACCION_TEMPORIZADOR_TERMINADO = "com.mk.pomodoro.ACCION_TEMPORIZADOR_TERMINADO";
    //private static final int CODIGO_SOLICITUD_NOTIFICACIONES = 1;
    private PomodoroAppDB pomodoroDB;

    private int idElementoSeleccionado;
    private CharSequence nombreCanal;
    private String descripcionCanal;

    private ViewPager2 pagerVista;
    private BottomNavigationView navegacionInferior;
    private GestorPomodoroViewModel gestorPomodoro;

    private final BroadcastReceiver temporizadorTerminadoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACCION_TEMPORIZADOR_TERMINADO.equals(intent.getAction())) {
                mostrarNotificacionFinalizada();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aplicarConfiguraciones();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        configurarInsets(findViewById(R.id.main));
        pomodoroDB = new PomodoroAppDB(this);

        // Registrar el BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(temporizadorTerminadoReceiver, new IntentFilter(ACCION_TEMPORIZADOR_TERMINADO));

        pagerVista = findViewById(R.id.pager_vista);
        navegacionInferior = findViewById(R.id.navegacion_inferior);
        gestorPomodoro = new ViewModelProvider(this).get(GestorPomodoroViewModel.class);
        nombreCanal = getString(R.string.canal_not_nombre);
        descripcionCanal = getString(R.string.canal_not_descripcion);

        pagerVista.setAdapter(new GestorPaginasAdapter(this));
        pagerVista.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                navegacionInferior.getMenu().getItem(position).setChecked(true);
            }
        });

        // Recuperar el estado guardado del fragmento seleccionado
        seleccionarFragmentoGuardado(savedInstanceState);

        navegacionInferior.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navegacion_inicio) {
                pagerVista.setCurrentItem(0);
                return true;
            } else if (item.getItemId() == R.id.navegacion_rendimiento) {
                pagerVista.setCurrentItem(1);
                return true;
            } else if (item.getItemId() == R.id.navegacion_ajustes) {
                pagerVista.setCurrentItem(2);
                return true;
            }
            return false;
        });

        gestorPomodoro.getEstadoTemporizador().observe(this, estadoTemporizador -> {
            if (gestorPomodoro.getTemporizadorTerminado().getValue() != null && gestorPomodoro.getTemporizadorTerminado().getValue()) {
                mostrarNotificacion();
            }
        });

        gestorPomodoro.getTemporizadorTerminado().observe(this, temporizadorTerminado -> {
            if (temporizadorTerminado != null && temporizadorTerminado) {
                mostrarNotificacion();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mostrarNotificacion();
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this.getApplicationContext());
        managerCompat.cancel(1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Anular el registro del BroadcastReceiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(temporizadorTerminadoReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Guardar el estado del fragmento seleccionado
        outState.putInt(ELEMENTO_SELECCIONADO, idElementoSeleccionado);
        super.onSaveInstanceState(outState);
    }

    /**
     * Configura los insets de la vista dada para ajustar el padding según las barras del sistema.
     *
     * @param view La vista a la que se aplicarán los insets
     */
    private void configurarInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, navigationBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void seleccionarFragmentoGuardado(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            idElementoSeleccionado = savedInstanceState.getInt(ELEMENTO_SELECCIONADO, 0);
            MenuItem elementoSeleccionadoMenu = navegacionInferior.getMenu().findItem(idElementoSeleccionado);
            if (elementoSeleccionadoMenu != null) {
                pagerVista.setCurrentItem(elementoSeleccionadoMenu.getOrder());
            }
        } else {
            // Si no hay un estado guardado, selecciona el primer fragmento
            pagerVista.setCurrentItem(0);
        }
    }

    private void aplicarConfiguraciones() {
        SharedPreferences sharedPreferences = getSharedPreferences("minka", MODE_PRIVATE);
        int tema = sharedPreferences.getInt("tema", 0);
        switch (tema) {
            case 0: // Sistema
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1: // Claro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2: // Oscuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }

    private void mostrarNotificacion() {
        String estadoTemporizador = gestorPomodoro.getEstadoTemporizador().getValue();
        Boolean temporizadorTerminado = gestorPomodoro.getTemporizadorTerminado().getValue();
        Boolean temporizadorIniciado = gestorPomodoro.getTemporizadorIniciado().getValue();

        String titulo;
        String texto;
        if (Boolean.TRUE.equals(temporizadorTerminado)) {
            titulo = "Tiempo de " + estadoTemporizador + " completado";
            texto = "El tiempo de " + estadoTemporizador + " ha terminado.";
            gestorPomodoro.setTemporizadorTerminado(false);
            mostrarNuevaNotificacion(titulo, texto);
        }
        if (Boolean.TRUE.equals(temporizadorIniciado)) {
            titulo = "Tiempo de " + estadoTemporizador + " iniciado";
            texto = "El temporizador está funcionando en segundo plano.";
            mostrarNuevaNotificacion(titulo, texto);
        }
    }


    private void mostrarNuevaNotificacion(String title, String text) {

        Intent openAppIntent = new Intent(this, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getApplicationContext(), MainActivity.ID_CANAL_SERVICIO_TEMPORIZADOR)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel canal = new NotificationChannel(MainActivity.ID_CANAL_SERVICIO_TEMPORIZADOR, nombreCanal, importancia);
            canal.setDescription(descripcionCanal);
            // Registrar el canal en el sistema
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this.getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!managerCompat.areNotificationsEnabled()) {
                Intent notificationSettingsIntent  = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(notificationSettingsIntent );
                return;
            }
        } else if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        managerCompat.notify(1, builder.build());
    }

    private void mostrarNotificacionFinalizada() {
        String estadoTemporizador = gestorPomodoro.getEstadoTemporizador().getValue();

        String title = "Tiempo de " + estadoTemporizador + " completado";
        String text = "El tiempo de " + estadoTemporizador + " ha terminado.";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel canal = new NotificationChannel(MainActivity.ID_CANAL_SERVICIO_TEMPORIZADOR, nombreCanal, importancia);
            canal.setDescription(descripcionCanal);
            // Registrar el canal en el sistema
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }

        Intent openAppIntent = new Intent(this, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.ID_CANAL_SERVICIO_TEMPORIZADOR)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationManager.areNotificationsEnabled()) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
                return;
            }
        } else if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManager.notify(1, builder.build());
    }
    // ---
    /*
    private void mostrarNuevaNotificacion(String titulo, String texto) {
        // Intent para abrir la aplicación al tocar la notificación
        Intent abrirAppIntent = new Intent(this, MainActivity.class);
        abrirAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent intencionPendiente = PendingIntent.getActivity(this, 0, abrirAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Construcción de la notificación
        NotificationCompat.Builder constructorNotificacion = new NotificationCompat.Builder(this.getApplicationContext(), MainActivity.ID_CANAL_SERVICIO_TEMPORIZADOR)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(intencionPendiente)
                .setAutoCancel(true);

        crearCanalNotificaciones();

        // Comprobar permisos y configuración de notificaciones (Android Marshmallow y superiores)
        if (!comprobarPermisosNotificaciones()) {
            return; // Si no hay permisos, no se muestra la notificación
        }

        // Construir y mostrar la notificación si se tienen los permisos necesarios
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this.getApplicationContext());
        managerCompat.notify(1, constructorNotificacion.build());
    }



    private void mostrarNotificacionFinalizada() {
        String estadoTemporizador = gestorPomodoro.getEstadoTemporizador().getValue();

        String titulo = "Tiempo de " + estadoTemporizador + " completado";
        String texto = "El tiempo de " + estadoTemporizador + " ha terminado.";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence nombreCanal = getString(R.string.canal_not_nombre); // El nombre visible para el usuario del canal.
            String descripcionCanal = getString(R.string.canal_not_descripcion); // La descripción visible para el usuario del canal.
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel canal = new NotificationChannel(MainActivity.ID_CANAL_SERVICIO_TEMPORIZADOR, nombreCanal, importancia);
            canal.setDescription(descripcionCanal);
            // Registrar el canal en el sistema
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }

        Intent abrirAppIntent = new Intent(this, MainActivity.class);
        abrirAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intencionPendiente = PendingIntent.getActivity(this, 0, abrirAppIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder constructorNotificacion = new NotificationCompat.Builder(this, MainActivity.ID_CANAL_SERVICIO_TEMPORIZADOR)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(intencionPendiente)
                .setAutoCancel(true);

        NotificationManagerCompat managerNotificaciones = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!managerNotificaciones.areNotificationsEnabled()) {
                Intent configuracionNotificacionesIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(configuracionNotificacionesIntent);
                return;
            }
        } else if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        managerNotificaciones.notify(1, constructorNotificacion.build());
    }

    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel canal = new NotificationChannel(MainActivity.ID_CANAL_SERVICIO_TEMPORIZADOR, nombreCanal, importancia);
            canal.setDescription(descripcionCanal);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }
    }

    private boolean comprobarPermisosNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this.getApplicationContext());
            if (!managerCompat.areNotificationsEnabled()) {
                // Notificar al usuario para que habilite las notificaciones
                solicitarActivacionNotificaciones();
                return false;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Comprobar si el usuario ha rechazado la solicitud anteriormente
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.POST_NOTIFICATIONS)) {
                    // El usuario ha rechazado la solicitud de permiso anteriormente
                    // Notificar al usuario para que habilite las notificaciones
                    solicitarActivacionNotificaciones();
                    return false;
                }
                // Si no se ha rechazado antes, solicitar el permiso
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, CODIGO_SOLICITUD_NOTIFICACIONES);
                return false;
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_SOLICITUD_NOTIFICACIONES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El permiso fue concedido
                mostrarNuevaNotificacion("Título", "Texto");
            } else {
                // El permiso fue rechazado
                solicitarActivacionNotificaciones();
            }
        }
    }

    private void solicitarActivacionNotificaciones() {
        // Crear un diálogo o una notificación para pedir al usuario que active las notificaciones
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Activar Notificaciones")
                .setMessage("Esta aplicación necesita que habilites las notificaciones.")
                .setPositiveButton("Configuración", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Crear y mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    */
}