package com.example.projectuts.ui.category;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projectuts.R;
import com.example.projectuts.adapter.CategoryAdapter;
import com.example.projectuts.data.model.Category;
import com.example.projectuts.data.model.Contact;
import com.example.projectuts.data.repository.ContactRepository;
import com.example.projectuts.databinding.ActivityCategoryBinding;
import com.example.projectuts.ui.home.MainActivity;
import com.example.projectuts.ui.profile.ProfileActivity;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryActivity extends AppCompatActivity {

    private ActivityCategoryBinding binding;
    private ContactRepository repository;
    private CategoryAdapter adapter;
    private ValueEventListener categoriesListener;
    private ValueEventListener contactsListener;

    private List<Category> currentCategories = new ArrayList<>();
    private List<Contact> currentContacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ContactRepository();

        setupRecyclerView();
        setupBottomNav();
        setupAddCategoryButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenRealtimeData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (categoriesListener != null) {
            repository.removeCategoriesListener(categoriesListener);
            categoriesListener = null;
        }
        if (contactsListener != null) {
            repository.removeRealtimeListener(contactsListener);
            contactsListener = null;
        }
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdapter(new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(Category category) {
                showEditCategoryDialog(category);
            }

            @Override
            public void onDelete(Category category) {
                showDeleteCategoryDialog(category);
            }
        });

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCategories.setAdapter(adapter);
    }

    private void listenRealtimeData() {
        categoriesListener = repository.getCategoriesRealtime(new ContactRepository.OnCategoriesLoadedListener() {
            @Override
            public void onSuccess(List<Category> categories) {
                currentCategories = categories;
                updateCategoryCounts();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CategoryActivity.this, "Failed to load categories: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        contactsListener = repository.getContactsRealtime(new ContactRepository.OnContactsLoadedListener() {
            @Override
            public void onSuccess(List<Contact> contacts) {
                currentContacts = contacts;
                updateCategoryCounts();
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }

    private void updateCategoryCounts() {
        if (currentCategories == null) return;

        Map<String, Integer> counts = new HashMap<>();
        for (Contact c : currentContacts) {
            String cat = c.getCategory() != null ? c.getCategory().toLowerCase() : "other";
            Integer current = counts.get(cat);
            counts.put(cat, current != null ? current + 1 : 1);
        }

        for (Category cat : currentCategories) {
            String catKey = cat.getName() != null ? cat.getName().toLowerCase() : "other";
            Integer countVal = counts.get(catKey);
            cat.setCount(countVal != null ? countVal : 0);
        }

        adapter.setCategories(currentCategories);
        binding.tvCatCountStats.setText(getString(R.string.categories_count_fmt, currentCategories.size()));
        binding.tvContactCountStats.setText(getString(R.string.contacts_count_fmt, currentContacts.size()));
    }

    private void setupAddCategoryButton() {
        binding.btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void showAddCategoryDialog() {
        final EditText input = new EditText(this);
        input.setHint("Category name");
        input.setPadding(40, 24, 40, 24);

        new AlertDialog.Builder(this)
                .setTitle("Add New Category")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String newCatName = input.getText().toString().trim();
                    if (!newCatName.isEmpty()) {
                        repository.addCategory(newCatName, new ContactRepository.OnActionCompleteListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(CategoryActivity.this, "Category added to Firebase", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(CategoryActivity.this, "Error adding category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditCategoryDialog(Category category) {
        final EditText input = new EditText(this);
        input.setText(category.getName());
        input.setPadding(40, 24, 40, 24);

        new AlertDialog.Builder(this)
                .setTitle("Edit Category")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String updatedName = input.getText().toString().trim();
                    if (!updatedName.isEmpty() && category.getId() != null) {
                        repository.updateCategory(category.getId(), updatedName, new ContactRepository.OnActionCompleteListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(CategoryActivity.this, "Category updated", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(CategoryActivity.this, "Error updating category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteCategoryDialog(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category?")
                .setMessage("Are you sure you want to delete category '" + category.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (category.getId() != null) {
                        repository.deleteCategory(category.getId(), new ContactRepository.OnActionCompleteListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(CategoryActivity.this, "Category deleted", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(CategoryActivity.this, "Error deleting category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_categories);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_contacts) {
                startActivity(new Intent(CategoryActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(CategoryActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return id == R.id.nav_categories;
        });
    }
}