package com.mk.pomodoro.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mk.pomodoro.ui.FiltroAyerFragment;
import com.mk.pomodoro.ui.FiltroHoyFragment;
import com.mk.pomodoro.ui.FiltroMesFragment;
import com.mk.pomodoro.ui.FiltroSemanaFragment;

public class GestorFiltrosAdapter extends FragmentStateAdapter {

    public GestorFiltrosAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }
    /*public GestorFiltrosAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }*/

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FiltroHoyFragment();
            case 1:
                return new FiltroAyerFragment();
            case 2:
                return new FiltroSemanaFragment();
            case 3:
                return new FiltroMesFragment();
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
