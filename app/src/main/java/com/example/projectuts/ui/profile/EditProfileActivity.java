package com.example.projectuts.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectuts.R;
import com.example.projectuts.data.repository.ContactRepository;
import com.example.projectuts.databinding.ActivityEditProfileBinding;
import com.example.projectuts.utils.AvatarUtils;
import com.google.firebase.database.ValueEventListener;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private ContactRepository repository;
    private ValueEventListener profileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ContactRepository();

        loadCurrentProfile();
        setupListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileListener != null) {
            repository.removeProfileListener(profileListener);
        }
    }

    private void loadCurrentProfile() {
        profileListener = repository.getUserProfile(new ContactRepository.OnProfileLoadedListener() {
            @Override
            public void onSuccess(String name, String email) {
                binding.etDisplayName.setText(name);
                binding.etEmailAddress.setText(email);
                binding.tvProfileAvatar.setText(AvatarUtils.getInitials(name));
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCancel.setOnClickListener(v -> finish());

        binding.etDisplayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    binding.tvErrorName.setVisibility(View.GONE);
                    binding.tvProfileAvatar.setText(AvatarUtils.getInitials(s.toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = binding.etDisplayName.getText().toString().trim();
        String email = binding.etEmailAddress.getText().toString().trim();

        if (name.isEmpty()) {
            binding.tvErrorName.setVisibility(View.VISIBLE);
            return;
        }

        binding.btnSaveProfile.setEnabled(false);
        binding.btnSaveProfile.setText("Saving...");

        repository.saveUserProfile(name, email, new ContactRepository.OnActionCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditProfileActivity.this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                binding.btnSaveProfile.setEnabled(true);
                binding.btnSaveProfile.setText(R.string.btn_save_profile);
                Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}