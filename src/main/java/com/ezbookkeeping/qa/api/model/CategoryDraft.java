package com.ezbookkeeping.qa.api.model;

import java.util.ArrayList;
import java.util.List;

public class CategoryDraft {

    private String name;
    private int type;
    private String icon;
    private String color;
    private List<CategoryDraft> subCategories = new ArrayList<>();

    public CategoryDraft() {
    }

    public CategoryDraft(String name, int type, String icon, String color, List<CategoryDraft> subCategories) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.subCategories = subCategories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<CategoryDraft> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<CategoryDraft> subCategories) {
        this.subCategories = subCategories;
    }
}