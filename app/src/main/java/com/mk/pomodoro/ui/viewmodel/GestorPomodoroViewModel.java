package com.mk.pomodoro.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GestorPomodoroViewModel extends ViewModel {
    private final MutableLiveData<Integer> tiempoTrabajo = new MutableLiveData<>();
    private final MutableLiveData<Integer> tiempoDescanso = new MutableLiveData<>();
    private final MutableLiveData<Integer> opcionSeleccionada = new MutableLiveData<>();
    private final MutableLiveData<Boolean> configurationChanged = new MutableLiveData<>(false);
    private final MutableLiveData<String> estadoTemporizador = new MutableLiveData<>();
    private final MutableLiveData<Boolean> temporizadorTerminado = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> temporizadorIniciado = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> temaCambiado = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> mostrarInfoPersonalizado = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> mostrarSnackbar = new MutableLiveData<>(false);

    public void setTiempoTrabajo(int tiempo) {
        tiempoTrabajo.setValue(tiempo);
    }

    public LiveData<Integer> getTiempoTrabajo() {
        return tiempoTrabajo;
    }

    public void setTiempoDescanso(int tiempo) {
        tiempoDescanso.setValue(tiempo);
    }

    public LiveData<Integer> getTiempoDescanso() {
        return tiempoDescanso;
    }

    public void setOpcionSeleccionada(int opcion) {
        opcionSeleccionada.setValue(opcion);
    }

    public LiveData<Integer> getOpcionSeleccionada() {
        return opcionSeleccionada;
    }

    public LiveData<Boolean> isConfigurationChanged() {
        return configurationChanged;
    }

    public void setConfigurationChanged(boolean configurationChanged) {
        this.configurationChanged.setValue(configurationChanged);
    }

    public void setEstadoTemporizador(String estado) {
        estadoTemporizador.setValue(estado);
    }

    public LiveData<String> getEstadoTemporizador() {
        return estadoTemporizador;
    }

    public void setTemporizadorTerminado(Boolean terminado) {
        temporizadorTerminado.setValue(terminado);
    }

    public LiveData<Boolean> getTemporizadorTerminado() {
        return temporizadorTerminado;
    }

    public void setTemporizadorIniciado(Boolean iniciado) {
        temporizadorIniciado.setValue(iniciado);
    }

    public LiveData<Boolean> getTemporizadorIniciado() {
        return temporizadorIniciado;
    }

    public LiveData<Boolean> getTemaCambiado() {
        return temaCambiado;
    }

    public void setTemaCambiado(boolean cambiado) {
        temaCambiado.setValue(cambiado);
    }

    public void setMostrarInfoPersonalizado(boolean mostrar) {
        mostrarInfoPersonalizado.setValue(mostrar);
    }

    public LiveData<Boolean> getMostrarInfoPersonalizado() {
        return mostrarInfoPersonalizado;
    }

    public void setMostrarSnackbar(boolean mostrar) {
        mostrarSnackbar.setValue(mostrar);
    }

    public LiveData<Boolean> getMostrarSnackbar() {
        return mostrarSnackbar;
    }
}
