package org.delcom.app.utils;

public class ConstUtil {
    public static final String KEY_AUTH_TOKEN = "AUTH_TOKEN";
    public static final String KEY_USER_ID = "USER_ID";

    // --- Authentication ---
    public static final String TEMPLATE_PAGES_AUTH_LOGIN = "pages/auth/login";
    public static final String TEMPLATE_PAGES_AUTH_REGISTER = "pages/auth/register";

    // --- Dashboard / Home ---
    // Sesuai screenshot: src/main/resources/templates/pages/home.html
    public static final String TEMPLATE_PAGES_HOME = "pages/home";

    // --- Books ---
    // Sesuai screenshot: src/main/resources/templates/pages/books/detail.html
    public static final String TEMPLATE_PAGES_BOOKS_DETAIL = "pages/books/detail";
    
    // --- Borrowings ---
    // Sesuai screenshot: src/main/resources/templates/pages/borrowings/list.html & detail.html
    public static final String TEMPLATE_PAGES_BORROWINGS_LIST = "pages/borrowings/list";
    public static final String TEMPLATE_PAGES_BORROWINGS_DETAIL = "pages/borrowings/detail";
}