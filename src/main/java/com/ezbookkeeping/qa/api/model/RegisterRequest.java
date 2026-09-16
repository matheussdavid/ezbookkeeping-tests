package com.ezbookkeeping.qa.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterRequest {

    private String username;
    private String nickname;
    private String email;
    private String password;
    private String language;
    private String defaultCurrency;
    private int firstDayOfWeek;
    private List<CategoryDraft> categories = new ArrayList<>();

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String nickname, String email, String password,
                           String language, String defaultCurrency, int firstDayOfWeek) {
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.language = language;
        this.defaultCurrency = defaultCurrency;
        this.firstDayOfWeek = firstDayOfWeek;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getLanguage() {
        return language;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public int getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    public List<CategoryDraft> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDraft> categories) {
        this.categories = categories;
    }
}