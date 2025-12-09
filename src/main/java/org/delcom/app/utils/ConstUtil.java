package org.delcom.app.utils;

public class ConstUtil {
    // === Session Keys ===
    public static final String KEY_AUTH_TOKEN = "AUTH_TOKEN";
    public static final String KEY_USER_ID = "USER_ID";

    // === Authentication Templates ===
    public static final String TEMPLATE_PAGES_AUTH_LOGIN = "pages/auth/login";
    public static final String TEMPLATE_PAGES_AUTH_REGISTER = "pages/auth/register";

    // === Dashboard / Home Templates ===
    public static final String TEMPLATE_PAGES_HOME = "pages/home";

    // === Books Templates ===
    public static final String TEMPLATE_PAGES_BOOKS_DETAIL = "pages/books/detail";

    // === Books Modals ===
    public static final String TEMPLATE_MODALS_BOOKS_ADD = "modals/books/add";
    public static final String TEMPLATE_MODALS_BOOKS_EDIT = "modals/books/edit";
    public static final String TEMPLATE_MODALS_BOOKS_DELETE = "modals/books/delete";
    public static final String TEMPLATE_MODALS_BOOKS_EDIT_COVER = "modals/books/edit-cover";

    // === Layouts ===
    public static final String TEMPLATE_LAYOUTS_BASE = "layouts/base";

    // === Borrowings Templates (Future Development) ===
    public static final String TEMPLATE_PAGES_BORROWINGS_LIST = "pages/borrowings/list";
    public static final String TEMPLATE_PAGES_BORROWINGS_DETAIL = "pages/borrowings/detail";

    // === File Storage ===
    public static final String UPLOAD_DIR = "uploads/";
    public static final String BOOK_COVER_DIR = "uploads/books/covers/";
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // === Date Format ===
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DATETIME_FORMAT = "dd MMMM yyyy HH:mm";
    public static final String TIME_FORMAT = "HH:mm";
}