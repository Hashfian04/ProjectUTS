package com.example.projectuts.ui.edit;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.projectuts.R;
import com.example.projectuts.data.model.Category;
import com.example.projectuts.data.model.Contact;
import com.example.projectuts.data.repository.ContactRepository;
import com.example.projectuts.databinding.ActivityEditContactBinding;
import com.example.projectuts.utils.AvatarUtils;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EditContactActivity extends AppCompatActivity {

    private ActivityEditContactBinding binding;
    private ContactRepository repository;
    private ValueEventListener categoriesListener;

    private final List<String> categoryNames = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    private String contactId;
    private Contact existingContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ContactRepository();

        contactId = getIntent().getStringExtra("CONTACT_ID");
        existingContact = (Contact) getIntent().getSerializableExtra("CONTACT_DATA");

        setupSpinner();
        setupListeners();

        if (existingContact != null) {
            populateData(existingContact);
        } else if (contactId != null) {
            loadContactFromFirestore();
        }
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

                    if (existingContact != null && existingContact.getCategory() != null) {
                        selectCategoryInSpinner(existingContact.getCategory());
                    }
                }
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    private void selectCategoryInSpinner(String category) {
        for (int i = 0; i < categoryNames.size(); i++) {
            if (categoryNames.get(i).equalsIgnoreCase(category)) {
                binding.spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void loadContactFromFirestore() {
        repository.getContactById(contactId, new ContactRepository.OnContactLoadedListener() {
            @Override
            public void onSuccess(Contact contact) {
                if (contact != null) {
                    existingContact = contact;
                    populateData(contact);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EditContactActivity.this, "Failed to load contact", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateData(Contact contact) {
        binding.etFullName.setText(contact.getName());
        binding.etPhoneNumber.setText(contact.getPhone());
        binding.etEmail.setText(contact.getEmail() != null ? contact.getEmail() : "");
        binding.etAddress.setText(contact.getAddress() != null ? contact.getAddress() : "");
        binding.etNotes.setText(contact.getNotes() != null ? contact.getNotes() : "");

        String category = contact.getCategory() != null ? contact.getCategory() : "Other";
        selectCategoryInSpinner(category);

        String initials = AvatarUtils.getInitials(contact.getName());
        binding.tvEditAvatar.setText(initials);

        AvatarUtils.AvatarTheme theme = AvatarUtils.getAvatarTheme(contact.getName());
        binding.tvEditAvatar.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, theme.bgResId)
        ));
        binding.tvEditAvatar.setTextColor(ContextCompat.getColor(this, theme.textResId));
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
                    String initials = AvatarUtils.getInitials(s.toString());
                    binding.tvEditAvatar.setText(initials);
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

        binding.btnSaveContact.setOnClickListener(v -> updateContact());
    }

    private void updateContact() {
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

        Contact updatedContact = new Contact(contactId, name, phone, email, address, category, notes);

        repository.updateContact(updatedContact, new ContactRepository.OnActionCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditContactActivity.this, "Contact updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                binding.btnSaveContact.setEnabled(true);
                binding.btnSaveContact.setText(R.string.btn_save_changes);
                Toast.makeText(EditContactActivity.this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}