package com.mantum.component.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.util.SparseArray;
import android.view.ViewGroup;

import com.mantum.component.Mantum;

import java.util.List;

public class TabAdapter extends FragmentStatePagerAdapter {

    private final int count;

    private final Context context;

    private final List<Mantum.Fragment> tabs;

    private final SparseArray<Mantum.Fragment> references = new SparseArray<>();

    public TabAdapter(@NonNull Context context, @NonNull FragmentManager fm, @NonNull List<Mantum.Fragment> tabs) {
        super(fm);
        this.context = context;
        this.tabs = tabs;
        this.count = tabs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.get(position).getTitle(context);
    }


    @Override
    public Fragment getItem(int position) {
        return tabs.get(position);
    }

    @Override
    public int getCount() {
        return count;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Mantum.Fragment fragment = (Mantum.Fragment) super.instantiateItem(container, position);
        references.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        references.remove(position);
    }

    @Nullable
    public Mantum.Fragment getFragment(int position) {
        return references.get(position);
    }

    @Nullable
    public Mantum.Fragment getFragment(@NonNull String key) {
        for (int i = 0; i < getCount(); i++) {
            Mantum.Fragment tab = references.get(i);
            if (tab != null && key.equals(references.get(i).getKey())) {
                return references.get(i);
            }
        }
        return null;
    }
}