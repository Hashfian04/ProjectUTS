package com.example.projectuts.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectuts.R;
import com.example.projectuts.data.model.Contact;
import com.example.projectuts.data.repository.ContactRepository;
import com.example.projectuts.databinding.ActivityProfileBinding;
import com.example.projectuts.ui.category.CategoryActivity;
import com.example.projectuts.ui.home.MainActivity;
import com.example.projectuts.utils.AvatarUtils;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private ContactRepository repository;
    private ValueEventListener contactsListener;
    private ValueEventListener profileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ContactRepository();

        setupListeners();
        setupBottomNav();
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenStats();
        listenProfileData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (contactsListener != null) {
            repository.removeRealtimeListener(contactsListener);
            contactsListener = null;
        }
        if (profileListener != null) {
            repository.removeProfileListener(profileListener);
            profileListener = null;
        }
    }

    private void listenProfileData() {
        profileListener = repository.getUserProfile(new ContactRepository.OnProfileLoadedListener() {
            @Override
            public void onSuccess(String name, String email) {
                binding.tvProfileName.setText(name);
                binding.tvInfoDisplayName.setText(name);
                binding.tvInfoEmailAddress.setText(email);
                binding.tvProfileAvatar.setText(AvatarUtils.getInitials(name));
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ProfileActivity.this, "Error loading profile data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenStats() {
        contactsListener = repository.getContactsRealtime(new ContactRepository.OnContactsLoadedListener() {
            @Override
            public void onSuccess(List<Contact> contacts) {
                int totalContacts = contacts.size();
                Set<String> categories = new HashSet<>();
                for (Contact c : contacts) {
                    if (c.getCategory() != null && !c.getCategory().isEmpty()) {
                        categories.add(c.getCategory().toLowerCase());
                    }
                }
                int catCount = Math.max(categories.size(), 4);
                binding.tvProfileStats.setText(getString(R.string.profile_stats_fmt, totalContacts, catCount));
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ProfileActivity.this, "Error loading stats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        View.OnClickListener openEditProfile = v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        };

        binding.btnEditProfileHeader.setOnClickListener(openEditProfile);
        binding.cardProfileBanner.setOnClickListener(openEditProfile);
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_profile);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_contacts) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_categories) {
                startActivity(new Intent(ProfileActivity.this, CategoryActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return id == R.id.nav_profile;
        });
    }
}