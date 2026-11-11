package com.example.projectuts.ui.add;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectuts.R;
import com.example.projectuts.data.model.Category;
import com.example.projectuts.data.model.Contact;
import com.example.projectuts.data.repository.ContactRepository;
import com.example.projectuts.databinding.ActivityAddContactBinding;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddContactActivity extends AppCompatActivity {

    private ActivityAddContactBinding binding;
    private ContactRepository repository;
    private ValueEventListener categoriesListener;

    private final List<String> categoryNames = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ContactRepository();

        setupSpinner();
        setupListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (categoriesListener != null) {
            repository.removeCategoriesListener(categoriesListener);
        }
    }

    private void setupSpinner() {
        categoryNames.add("Other");
        categoryNames.add("Family");
        categoryNames.add("Friends");
        categoryNames.add("Work");

        spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(spinnerAdapter);

        loadDynamicCategories();
    }

    private void loadDynamicCategories() {
        categoriesListener = repository.getCategoriesRealtime(new ContactRepository.OnCategoriesLoadedListener() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (categories != null && !categories.isEmpty()) {
                    categoryNames.clear();
                    for (Category cat : categories) {
                        if (cat.getName() != null && !cat.getName().isEmpty()) {
                            categoryNames.add(cat.getName());
                        }
                    }
                    spinnerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCancel.setOnClickListener(v -> finish());

        binding.etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    binding.tvErrorName.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    binding.tvErrorPhone.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnSaveContact.setOnClickListener(v -> saveContact());
    }

    private void saveContact() {
        String name = binding.etFullName.getText().toString().trim();
        String phone = binding.etPhoneNumber.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String category = binding.spinnerCategory.getSelectedItem() != null ?
                binding.spinnerCategory.getSelectedItem().toString() : "Other";
        String notes = binding.etNotes.getText().toString().trim();

        boolean isValid = true;

        if (name.isEmpty()) {
            binding.tvErrorName.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.tvErrorName.setVisibility(View.GONE);
        }

        if (phone.isEmpty()) {
            binding.tvErrorPhone.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.tvErrorPhone.setVisibility(View.GONE);
        }

        if (!isValid) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSaveContact.setEnabled(false);
        binding.btnSaveContact.setText("Saving...");

        Contact newContact = new Contact(name, phone, email, address, category, notes);

        repository.addContact(newContact, new ContactRepository.OnContactCreatedListener() {
            @Override
            public void onSuccess(String docId) {
                Toast.makeText(AddContactActivity.this, "Contact saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                binding.btnSaveContact.setEnabled(true);
                binding.btnSaveContact.setText(R.string.btn_save_contact);
                Toast.makeText(AddContactActivity.this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}