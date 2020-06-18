package com.example.skychat.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.skychat.Fragment.FriendRequestsFragment;
import com.example.skychat.Fragment.FriendsFragment;
import com.example.skychat.Fragment.MessagesFragment;

public class MainViewPagerAdapter extends FragmentPagerAdapter {

    public MainViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return new FriendRequestsFragment();

            case 1:
                return new MessagesFragment();

            case 2:
                return new FriendsFragment();

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "Friend Requets";

            case 1:
                return "Messages";

            case 2:
                return "Friends";

            default:
                return null;
        }
    }
}
