package com.example.projectuts.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projectuts.R;
import com.example.projectuts.adapter.ContactAdapter;
import com.example.projectuts.data.model.Contact;
import com.example.projectuts.data.repository.ContactRepository;
import com.example.projectuts.databinding.ActivityMainBinding;
import com.example.projectuts.ui.add.AddContactActivity;
import com.example.projectuts.ui.category.CategoryActivity;
import com.example.projectuts.ui.detail.DetailActivity;
import com.example.projectuts.ui.profile.ProfileActivity;
import com.example.projectuts.utils.AvatarUtils;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ContactRepository repository;
    private ContactAdapter adapter;
    private ValueEventListener realtimeListener;
    private ValueEventListener profileListener;

    private TextView selectedChip;
    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ContactRepository();

        setupRecyclerView();
        setupSearch();
        setupCategoryChips();
        setupClickListeners();
        setupBottomNav();
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenContacts();
        listenProfile();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (realtimeListener != null) {
            repository.removeRealtimeListener(realtimeListener);
            realtimeListener = null;
        }
        if (profileListener != null) {
            repository.removeProfileListener(profileListener);
            profileListener = null;
        }
    }

    private void setupRecyclerView() {
        adapter = new ContactAdapter(contact -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("CONTACT_ID", contact.getId());
            intent.putExtra("CONTACT_DATA", contact);
            startActivity(intent);
        });

        binding.rvContacts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvContacts.setAdapter(adapter);
    }

    private void listenContacts() {
        realtimeListener = repository.getContactsRealtime(new ContactRepository.OnContactsLoadedListener() {
            @Override
            public void onSuccess(List<Contact> contacts) {
                adapter.setContacts(contacts);
                updateUIState(contacts.size());
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, "Error loading contacts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenProfile() {
        profileListener = repository.getUserProfile(new ContactRepository.OnProfileLoadedListener() {
            @Override
            public void onSuccess(String name, String email) {
                binding.tvGreeting.setText(getString(R.string.greeting_user, name));
                binding.tvUserAvatar.setText(AvatarUtils.getInitials(name));
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    private void updateUIState(int totalCount) {
        binding.tvTotalCountNum.setText(String.valueOf(totalCount));

        if (adapter.getItemCount() == 0) {
            binding.rvContacts.setVisibility(View.GONE);
            binding.contactsHeaderLayout.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.rvContacts.setVisibility(View.VISIBLE);
            binding.contactsHeaderLayout.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterQuery(s.toString());
                updateEmptyStateVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateEmptyStateVisibility() {
        if (adapter.getItemCount() == 0) {
            binding.rvContacts.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.rvContacts.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void setupCategoryChips() {
        selectedChip = binding.chipAll;

        binding.chipAll.setOnClickListener(v -> selectChip(binding.chipAll, "All"));
        binding.chipFamily.setOnClickListener(v -> selectChip(binding.chipFamily, "Family"));
        binding.chipFriends.setOnClickListener(v -> selectChip(binding.chipFriends, "Friends"));
        binding.chipWork.setOnClickListener(v -> selectChip(binding.chipWork, "Work"));
        binding.chipOther.setOnClickListener(v -> selectChip(binding.chipOther, "Other"));
    }

    private void selectChip(TextView chipView, String category) {
        if (selectedChip != null) {
            selectedChip.setBackgroundResource(R.drawable.bg_rounded_chip_unselected);
            selectedChip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }

        chipView.setBackgroundResource(R.drawable.bg_rounded_chip_selected);
        chipView.setTextColor(ContextCompat.getColor(this, R.color.white));

        selectedChip = chipView;
        selectedCategory = category;

        adapter.filterCategory(selectedCategory);
        updateEmptyStateVisibility();
    }

    private void setupClickListeners() {
        View.OnClickListener openAddContact = v -> {
            Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
            startActivity(intent);
        };

        binding.fabAddContact.setOnClickListener(openAddContact);
        binding.btnAddFirstContact.setOnClickListener(openAddContact);
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_contacts);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_categories) {
                startActivity(new Intent(MainActivity.this, CategoryActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return id == R.id.nav_contacts;
        });
    }
}