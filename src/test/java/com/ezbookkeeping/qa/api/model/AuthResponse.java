package com.ezbookkeeping.qa.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {

    private String token;
    private boolean need2FA;

    @JsonProperty("need2FA")
    public boolean isNeed2FA() {
        return need2FA;
    }

    public void setNeed2FA(boolean need2FA) {
        this.need2FA = need2FA;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserInfo {
        private long uid;
        private String username;
        private String email;
        private String nickname;
        private String language;

        @JsonProperty("defaultCurrency")
        private String defaultCurrency;

        public long getUid() {
            return uid;
        }

        public void setUid(long uid) {
            this.uid = uid;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getDefaultCurrency() {
            return defaultCurrency;
        }

        public void setDefaultCurrency(String defaultCurrency) {
            this.defaultCurrency = defaultCurrency;
        }
    }
}
