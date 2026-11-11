package com.example.projectuts.ui.detail;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.projectuts.R;
import com.example.projectuts.data.model.Contact;
import com.example.projectuts.data.repository.ContactRepository;
import com.example.projectuts.databinding.ActivityDetailBinding;
import com.example.projectuts.ui.edit.EditContactActivity;
import com.example.projectuts.utils.AvatarUtils;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private ContactRepository repository;
    private String contactId;
    private Contact currentContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ContactRepository();

        contactId = getIntent().getStringExtra("CONTACT_ID");
        Contact passedContact = (Contact) getIntent().getSerializableExtra("CONTACT_DATA");
        if (passedContact != null) {
            currentContact = passedContact;
            displayContact(passedContact);
        }

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (contactId != null && !contactId.isEmpty()) {
            loadContactDetails();
        }
    }

    private void loadContactDetails() {
        repository.getContactById(contactId, new ContactRepository.OnContactLoadedListener() {
            @Override
            public void onSuccess(Contact contact) {
                if (contact != null) {
                    currentContact = contact;
                    displayContact(contact);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(DetailActivity.this, "Failed to load details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayContact(Contact contact) {
        binding.tvDetailName.setText(contact.getName());
        binding.tvDetailPhone.setText(contact.getPhone() != null && !contact.getPhone().isEmpty() ? contact.getPhone() : "-");

        // Email
        if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            binding.tvDetailEmail.setText(contact.getEmail());
            binding.rowEmail.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetailEmail.setText("-");
        }

        // Address
        if (contact.getAddress() != null && !contact.getAddress().isEmpty()) {
            binding.tvDetailAddress.setText(contact.getAddress());
            binding.rowAddress.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetailAddress.setText("-");
        }

        // Category Badge
        String category = contact.getCategory() != null ? contact.getCategory() : "Other";
        binding.tvDetailCategoryBadge.setText(category);

        AvatarUtils.AvatarTheme badgeTheme = AvatarUtils.getCategoryBadgeTheme(category);
        binding.tvDetailCategoryBadge.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, badgeTheme.bgResId)
        ));
        binding.tvDetailCategoryBadge.setTextColor(ContextCompat.getColor(this, badgeTheme.textResId));

        // Initial Avatar
        String initials = AvatarUtils.getInitials(contact.getName());
        binding.tvDetailAvatar.setText(initials);

        AvatarUtils.AvatarTheme avatarTheme = AvatarUtils.getAvatarTheme(contact.getName());
        binding.tvDetailAvatar.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, avatarTheme.bgResId)
        ));
        binding.tvDetailAvatar.setTextColor(ContextCompat.getColor(this, avatarTheme.textResId));

        // Notes
        if (contact.getNotes() != null && !contact.getNotes().isEmpty()) {
            binding.tvDetailNotes.setText(contact.getNotes());
            binding.cardNotes.setVisibility(View.VISIBLE);
        } else {
            binding.cardNotes.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        // Quick Actions - Implicit Intents
        binding.btnQuickCall.setOnClickListener(v -> makePhoneCall());
        binding.btnQuickWhatsApp.setOnClickListener(v -> openWhatsApp());
        binding.btnQuickEmail.setOnClickListener(v -> sendEmail());
        binding.rowAddress.setOnClickListener(v -> openGoogleMaps());

        // Edit Explicit Intent
        binding.btnEditContact.setOnClickListener(v -> {
            if (currentContact != null) {
                Intent intent = new Intent(DetailActivity.this, EditContactActivity.class);
                intent.putExtra("CONTACT_ID", currentContact.getId());
                intent.putExtra("CONTACT_DATA", currentContact);
                startActivity(intent);
            }
        });

        // Delete Dialog
        binding.btnDeleteContact.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void makePhoneCall() {
        if (currentContact == null || currentContact.getPhone() == null || currentContact.getPhone().isEmpty()) {
            Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + currentContact.getPhone()));
        startImplicitIntent(intent, "No phone app available");
    }

    private void openWhatsApp() {
        if (currentContact == null || currentContact.getPhone() == null || currentContact.getPhone().isEmpty()) {
            Toast.makeText(this, "No phone number for WhatsApp", Toast.LENGTH_SHORT).show();
            return;
        }
        String phone = currentContact.getPhone().replaceAll("[^0-9+]", "");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + phone));
        startImplicitIntent(intent, "WhatsApp application not installed");
    }

    private void sendEmail() {
        if (currentContact == null || currentContact.getEmail() == null || currentContact.getEmail().isEmpty()) {
            Toast.makeText(this, "No email address available", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + currentContact.getEmail()));
        startImplicitIntent(intent, "No email app available");
    }

    private void openGoogleMaps() {
        if (currentContact == null || currentContact.getAddress() == null || currentContact.getAddress().isEmpty()) {
            return;
        }
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(currentContact.getAddress()));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        startImplicitIntent(mapIntent, "No maps application available");
    }

    private void startImplicitIntent(Intent intent, String fallbackMessage) {
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, fallbackMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        if (currentContact == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_confirmation, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvMsg = dialogView.findViewById(R.id.tvDialogDeleteMsg);
        View btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        View btnDeleteConfirm = dialogView.findViewById(R.id.btnDialogDeleteConfirm);

        String msgText = getString(R.string.dialog_delete_msg, currentContact.getName());
        tvMsg.setText(msgText);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDeleteConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            repository.deleteContact(currentContact.getId(), new ContactRepository.OnActionCompleteListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(DetailActivity.this, "Contact deleted", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(DetailActivity.this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}