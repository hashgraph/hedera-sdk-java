package com.hedera.android_example.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SectionsPagerAdapter extends FragmentStateAdapter {
    public SectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PrivateKeyFragment();
            case 1:
                return new AccountBalanceFragment();
            case 2:
                return new CryptoTransferFragment();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

