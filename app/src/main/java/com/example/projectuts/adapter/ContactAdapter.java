package com.example.projectuts.adapter;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectuts.R;
import com.example.projectuts.data.model.Contact;
import com.example.projectuts.utils.AvatarUtils;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    private List<Contact> originalList = new ArrayList<>();
    private List<Contact> filteredList = new ArrayList<>();
    private final OnContactClickListener clickListener;

    private String currentQuery = "";
    private String currentCategory = "All";

    public ContactAdapter(OnContactClickListener listener) {
        this.clickListener = listener;
    }

    public void setContacts(List<Contact> contacts) {
        this.originalList = contacts != null ? new ArrayList<>(contacts) : new ArrayList<>();
        applyFilter();
    }

    public void filterQuery(String query) {
        this.currentQuery = query != null ? query.trim() : "";
        applyFilter();
    }

    public void filterCategory(String category) {
        this.currentCategory = category != null ? category.trim() : "All";
        applyFilter();
    }

    private void applyFilter() {
        filteredList.clear();
        for (Contact contact : originalList) {
            boolean matchesCategory = currentCategory.equalsIgnoreCase("All")
                    || (contact.getCategory() != null && contact.getCategory().equalsIgnoreCase(currentCategory));

            boolean matchesQuery = currentQuery.isEmpty()
                    || (contact.getName() != null && contact.getName().toLowerCase().contains(currentQuery.toLowerCase()))
                    || (contact.getPhone() != null && contact.getPhone().contains(currentQuery));

            if (matchesCategory && matchesQuery) {
                filteredList.add(contact);
            }
        }
        notifyDataSetChanged();
    }

    public int getFilteredCount() {
        return filteredList.size();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = filteredList.get(position);
        holder.bind(contact, clickListener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvAvatar;
        private final TextView tvContactName;
        private final TextView tvContactPhone;
        private final TextView tvCategoryBadge;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvContactPhone = itemView.findViewById(R.id.tvContactPhone);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
        }

        public void bind(final Contact contact, final OnContactClickListener listener) {
            tvContactName.setText(contact.getName());
            tvContactPhone.setText(contact.getPhone() != null ? contact.getPhone() : "");

            // Category badge
            String category = contact.getCategory() != null ? contact.getCategory() : "Other";
            tvCategoryBadge.setText(category);

            AvatarUtils.AvatarTheme badgeTheme = AvatarUtils.getCategoryBadgeTheme(category);
            tvCategoryBadge.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.getContext(), badgeTheme.bgResId)
            ));
            tvCategoryBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), badgeTheme.textResId));

            // Initial Avatar
            String initials = AvatarUtils.getInitials(contact.getName());
            tvAvatar.setText(initials);

            AvatarUtils.AvatarTheme avatarTheme = AvatarUtils.getAvatarTheme(contact.getName());
            tvAvatar.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.getContext(), avatarTheme.bgResId)
            ));
            tvAvatar.setTextColor(ContextCompat.getColor(itemView.getContext(), avatarTheme.textResId));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContactClick(contact);
                }
            });
        }
    }
}