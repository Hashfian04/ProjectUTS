package com.example.projectuts.data.repository;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.projectuts.data.model.Category;
import com.example.projectuts.data.model.Contact;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactRepository {

    private static final String TAG = "ContactRepository";
    private static final String DATABASE_URL = "https://project-uts-40978-default-rtdb.asia-southeast1.firebasedatabase.app";
    private final DatabaseReference contactsRef;
    private final DatabaseReference profileRef;
    private final DatabaseReference categoriesRef;

    public interface OnContactsLoadedListener {
        void onSuccess(List<Contact> contacts);
        void onError(Exception e);
    }

    public interface OnCategoriesLoadedListener {
        void onSuccess(List<Category> categories);
        void onError(Exception e);
    }

    public interface OnContactLoadedListener {
        void onSuccess(Contact contact);
        void onError(Exception e);
    }

    public interface OnProfileLoadedListener {
        void onSuccess(String name, String email);
        void onError(Exception e);
    }

    public interface OnActionCompleteListener {
        void onSuccess();
        void onError(Exception e);
    }

    public interface OnContactCreatedListener {
        void onSuccess(String docId);
        void onError(Exception e);
    }

    public ContactRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance(DATABASE_URL);
        contactsRef = database.getReference("contacts");
        profileRef = database.getReference("user_profile");
        categoriesRef = database.getReference("categories");
    }

    public ValueEventListener getContactsRealtime(final OnContactsLoadedListener listener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Contact> contacts = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        Contact c = child.getValue(Contact.class);
                        if (c != null) {
                            c.setId(child.getKey());
                            contacts.add(c);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing contact child: " + child.getKey(), e);
                    }
                }
                contacts.sort((c1, c2) -> {
                    String n1 = c1.getName() != null ? c1.getName() : "";
                    String n2 = c2.getName() != null ? c2.getName() : "";
                    return n1.compareToIgnoreCase(n2);
                });

                Log.d(TAG, "Loaded " + contacts.size() + " contacts from Realtime Database");
                listener.onSuccess(contacts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage(), error.toException());
                listener.onError(error.toException());
            }
        };

        contactsRef.addValueEventListener(valueEventListener);
        return valueEventListener;
    }

    public void removeRealtimeListener(ValueEventListener listener) {
        if (listener != null) {
            contactsRef.removeEventListener(listener);
        }
    }

    public ValueEventListener getCategoriesRealtime(final OnCategoriesLoadedListener listener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    seedDefaultCategories();
                    return;
                }
                List<Category> categories = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        Category cat = child.getValue(Category.class);
                        if (cat != null) {
                            cat.setId(child.getKey());
                            categories.add(cat);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing category: " + child.getKey(), e);
                    }
                }
                listener.onSuccess(categories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        };

        categoriesRef.addValueEventListener(valueEventListener);
        return valueEventListener;
    }

    public void removeCategoriesListener(ValueEventListener listener) {
        if (listener != null) {
            categoriesRef.removeEventListener(listener);
        }
    }

    private void seedDefaultCategories() {
        String[] defaults = new String[]{"Family", "Friends", "Work", "Other"};
        for (String name : defaults) {
            DatabaseReference ref = categoriesRef.push();
            Map<String, Object> map = new HashMap<>();
            map.put("id", ref.getKey());
            map.put("name", name);
            ref.setValue(map);
        }
    }

    public void addCategory(String categoryName, final OnActionCompleteListener listener) {
        DatabaseReference ref = categoriesRef.push();
        String id = ref.getKey();
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", categoryName);

        ref.setValue(map)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void updateCategory(String categoryId, String newName, final OnActionCompleteListener listener) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", newName);

        categoriesRef.child(categoryId).updateChildren(map)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void deleteCategory(String categoryId, final OnActionCompleteListener listener) {
        categoriesRef.child(categoryId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public ValueEventListener getUserProfile(final OnProfileLoadedListener listener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = "Alex Taylor";
                String email = "alex.taylor@example.com";
                if (snapshot.exists()) {
                    if (snapshot.child("name").getValue(String.class) != null) {
                        name = snapshot.child("name").getValue(String.class);
                    }
                    if (snapshot.child("email").getValue(String.class) != null) {
                        email = snapshot.child("email").getValue(String.class);
                    }
                }
                listener.onSuccess(name, email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        };

        profileRef.addValueEventListener(valueEventListener);
        return valueEventListener;
    }

    public void removeProfileListener(ValueEventListener listener) {
        if (listener != null) {
            profileRef.removeEventListener(listener);
        }
    }

    public void saveUserProfile(String name, String email, final OnActionCompleteListener listener) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name != null ? name : "Alex Taylor");
        data.put("email", email != null ? email : "alex.taylor@example.com");

        profileRef.updateChildren(data)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void getContactById(String id, final OnContactLoadedListener listener) {
        if (id == null || id.isEmpty()) {
            listener.onSuccess(null);
            return;
        }

        contactsRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Contact c = snapshot.getValue(Contact.class);
                    if (c != null) {
                        c.setId(snapshot.getKey());
                    }
                    listener.onSuccess(c);
                } else {
                    listener.onSuccess(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading contact by id: " + id, error.toException());
                listener.onError(error.toException());
            }
        });
    }

    public void addContact(Contact contact, final OnContactCreatedListener listener) {
        DatabaseReference newRef = contactsRef.push();
        String docId = newRef.getKey();
        if (docId == null) {
            docId = String.valueOf(System.currentTimeMillis());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", docId);
        data.put("name", contact.getName() != null ? contact.getName() : "");
        data.put("phone", contact.getPhone() != null ? contact.getPhone() : "");
        data.put("email", contact.getEmail() != null ? contact.getEmail() : "");
        data.put("address", contact.getAddress() != null ? contact.getAddress() : "");
        data.put("category", contact.getCategory() != null ? contact.getCategory() : "Other");
        data.put("notes", contact.getNotes() != null ? contact.getNotes() : "");

        final String finalDocId = docId;
        newRef.setValue(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Contact added with ID: " + finalDocId);
                    listener.onSuccess(finalDocId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding contact", e);
                    listener.onError(e);
                });
    }

    public void updateContact(Contact contact, final OnActionCompleteListener listener) {
        if (contact.getId() == null || contact.getId().isEmpty()) {
            listener.onError(new IllegalArgumentException("Contact ID cannot be empty for update"));
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", contact.getId());
        data.put("name", contact.getName() != null ? contact.getName() : "");
        data.put("phone", contact.getPhone() != null ? contact.getPhone() : "");
        data.put("email", contact.getEmail() != null ? contact.getEmail() : "");
        data.put("address", contact.getAddress() != null ? contact.getAddress() : "");
        data.put("category", contact.getCategory() != null ? contact.getCategory() : "Other");
        data.put("notes", contact.getNotes() != null ? contact.getNotes() : "");

        contactsRef.child(contact.getId()).updateChildren(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Contact updated ID: " + contact.getId());
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating contact ID: " + contact.getId(), e);
                    listener.onError(e);
                });
    }

    public void deleteContact(String id, final OnActionCompleteListener listener) {
        if (id == null || id.isEmpty()) {
            listener.onSuccess();
            return;
        }

        contactsRef.child(id).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Contact deleted ID: " + id);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting contact ID: " + id, e);
                    listener.onError(e);
                });
    }
}