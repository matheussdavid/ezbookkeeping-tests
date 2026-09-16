package com.ezbookkeeping.qa.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterResponse {

    private String token;
    private boolean need2FA;
    private UserInfo user;
    private boolean needVerifyEmail;
    private boolean presetCategoriesSaved;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @JsonProperty("need2FA")
    public boolean isNeed2FA() {
        return need2FA;
    }

    public void setNeed2FA(boolean need2FA) {
        this.need2FA = need2FA;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public boolean isNeedVerifyEmail() {
        return needVerifyEmail;
    }

    public void setNeedVerifyEmail(boolean needVerifyEmail) {
        this.needVerifyEmail = needVerifyEmail;
    }

    public boolean isPresetCategoriesSaved() {
        return presetCategoriesSaved;
    }

    public void setPresetCategoriesSaved(boolean presetCategoriesSaved) {
        this.presetCategoriesSaved = presetCategoriesSaved;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserInfo {

        private String username;
        private String email;
        private String nickname;
        private String avatar;
        private String avatarProvider;
        private String defaultAccountId;
        private boolean useLastReconciledTime;
        private int transactionEditScope;
        private String language;

        @JsonProperty("defaultCurrency")
        private String defaultCurrency;

        private int firstDayOfWeek;
        private int fiscalYearStart;
        private int calendarDisplayType;
        private int dateDisplayType;
        private int longDateFormat;
        private int shortDateFormat;
        private int longTimeFormat;
        private int shortTimeFormat;
        private int fiscalYearFormat;
        private int currencyDisplayType;
        private int numeralSystem;
        private int decimalSeparator;
        private int digitGroupingSymbol;
        private int digitGrouping;
        private int coordinateDisplayType;
        private int expenseAmountColor;
        private int incomeAmountColor;
        private boolean emailVerified;

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

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getAvatarProvider() {
            return avatarProvider;
        }

        public void setAvatarProvider(String avatarProvider) {
            this.avatarProvider = avatarProvider;
        }

        public String getDefaultAccountId() {
            return defaultAccountId;
        }

        public void setDefaultAccountId(String defaultAccountId) {
            this.defaultAccountId = defaultAccountId;
        }

        public boolean isUseLastReconciledTime() {
            return useLastReconciledTime;
        }

        public void setUseLastReconciledTime(boolean useLastReconciledTime) {
            this.useLastReconciledTime = useLastReconciledTime;
        }

        public int getTransactionEditScope() {
            return transactionEditScope;
        }

        public void setTransactionEditScope(int transactionEditScope) {
            this.transactionEditScope = transactionEditScope;
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

        public int getFirstDayOfWeek() {
            return firstDayOfWeek;
        }

        public void setFirstDayOfWeek(int firstDayOfWeek) {
            this.firstDayOfWeek = firstDayOfWeek;
        }

        public int getFiscalYearStart() {
            return fiscalYearStart;
        }

        public void setFiscalYearStart(int fiscalYearStart) {
            this.fiscalYearStart = fiscalYearStart;
        }

        public int getCalendarDisplayType() {
            return calendarDisplayType;
        }

        public void setCalendarDisplayType(int calendarDisplayType) {
            this.calendarDisplayType = calendarDisplayType;
        }

        public int getDateDisplayType() {
            return dateDisplayType;
        }

        public void setDateDisplayType(int dateDisplayType) {
            this.dateDisplayType = dateDisplayType;
        }

        public int getLongDateFormat() {
            return longDateFormat;
        }

        public void setLongDateFormat(int longDateFormat) {
            this.longDateFormat = longDateFormat;
        }

        public int getShortDateFormat() {
            return shortDateFormat;
        }

        public void setShortDateFormat(int shortDateFormat) {
            this.shortDateFormat = shortDateFormat;
        }

        public int getLongTimeFormat() {
            return longTimeFormat;
        }

        public void setLongTimeFormat(int longTimeFormat) {
            this.longTimeFormat = longTimeFormat;
        }

        public int getShortTimeFormat() {
            return shortTimeFormat;
        }

        public void setShortTimeFormat(int shortTimeFormat) {
            this.shortTimeFormat = shortTimeFormat;
        }

        public int getFiscalYearFormat() {
            return fiscalYearFormat;
        }

        public void setFiscalYearFormat(int fiscalYearFormat) {
            this.fiscalYearFormat = fiscalYearFormat;
        }

        public int getCurrencyDisplayType() {
            return currencyDisplayType;
        }

        public void setCurrencyDisplayType(int currencyDisplayType) {
            this.currencyDisplayType = currencyDisplayType;
        }

        public int getNumeralSystem() {
            return numeralSystem;
        }

        public void setNumeralSystem(int numeralSystem) {
            this.numeralSystem = numeralSystem;
        }

        public int getDecimalSeparator() {
            return decimalSeparator;
        }

        public void setDecimalSeparator(int decimalSeparator) {
            this.decimalSeparator = decimalSeparator;
        }

        public int getDigitGroupingSymbol() {
            return digitGroupingSymbol;
        }

        public void setDigitGroupingSymbol(int digitGroupingSymbol) {
            this.digitGroupingSymbol = digitGroupingSymbol;
        }

        public int getDigitGrouping() {
            return digitGrouping;
        }

        public void setDigitGrouping(int digitGrouping) {
            this.digitGrouping = digitGrouping;
        }

        public int getCoordinateDisplayType() {
            return coordinateDisplayType;
        }

        public void setCoordinateDisplayType(int coordinateDisplayType) {
            this.coordinateDisplayType = coordinateDisplayType;
        }

        public int getExpenseAmountColor() {
            return expenseAmountColor;
        }

        public void setExpenseAmountColor(int expenseAmountColor) {
            this.expenseAmountColor = expenseAmountColor;
        }

        public int getIncomeAmountColor() {
            return incomeAmountColor;
        }

        public void setIncomeAmountColor(int incomeAmountColor) {
            this.incomeAmountColor = incomeAmountColor;
        }

        public boolean isEmailVerified() {
            return emailVerified;
        }

        public void setEmailVerified(boolean emailVerified) {
            this.emailVerified = emailVerified;
        }
    }
}