package com.example.projectuts.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectuts.R;
import com.example.projectuts.data.model.Category;
import com.example.projectuts.utils.AvatarUtils;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
    }

    private List<Category> categoryList = new ArrayList<>();
    private final OnCategoryActionListener actionListener;

    public CategoryAdapter(OnCategoryActionListener listener) {
        this.actionListener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categoryList = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.bind(category, actionListener);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivCategoryIcon;
        private final TextView tvCategoryName;
        private final TextView tvCategoryCount;
        private final ImageView btnEditCategory;
        private final ImageView btnDeleteCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryCount = itemView.findViewById(R.id.tvCategoryCount);
            btnEditCategory = itemView.findViewById(R.id.btnEditCategory);
            btnDeleteCategory = itemView.findViewById(R.id.btnDeleteCategory);
        }

        public void bind(final Category category, final OnCategoryActionListener listener) {
            tvCategoryName.setText(category.getName());
            tvCategoryCount.setText(category.getCount() + " contacts");

            int iconRes = AvatarUtils.getCategoryIconRes(category.getName());
            ivCategoryIcon.setImageResource(iconRes);

            AvatarUtils.AvatarTheme theme = AvatarUtils.getCategoryBadgeTheme(category.getName());
            ivCategoryIcon.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.getContext(), theme.bgResId)
            ));
            ivCategoryIcon.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.getContext(), theme.textResId)
            ));

            btnEditCategory.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(category);
            });

            btnDeleteCategory.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(category);
            });
        }
    }
}