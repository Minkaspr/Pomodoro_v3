package com.mk.pomodoro.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mk.pomodoro.ui.AjustesFragment;
import com.mk.pomodoro.ui.InicioFragment;
import com.mk.pomodoro.ui.RendimientoFragment;

public class AdaptadorDeVista extends FragmentStateAdapter {

    public AdaptadorDeVista(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new InicioFragment();
        } else if (position == 1) {
            return new RendimientoFragment();
        } else if (position == 2) {
            return new AjustesFragment();
        } else {
            return new Fragment(); // Devuelve un fragmento vacío
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
