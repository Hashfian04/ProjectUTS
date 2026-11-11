package com.example.projectuts.data.model;

public class Category {
    private String id;
    private String name;
    private int iconResId;
    private int count;

    public Category() {
    }

    public Category(String id, String name, int iconResId, int count) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}