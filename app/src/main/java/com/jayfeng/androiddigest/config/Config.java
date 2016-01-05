package com.jayfeng.androiddigest.config;

public class Config {

    /*
     * ========================================================================
     * App Config
     * ========================================================================
     */

    public static final int PAGE_SIZE = 15;
    public static final int PAGE_START = 1;

    public static final String DIGEST_TYPE_TEXT = "text";
    public static final String DIGEST_TYPE_HTML = "html";


    public static final String BLOG_TYPE_HEADER = "header";
    public static final String BLOG_TYPE_DIR = "dir";
    public static final String BLOG_TYPE_HTML = "html";

    public static final String OFFLINE_TYPE_DIR = "dir";
    public static final String OFFLINE_TYPE_HTML = "html";

    public static final String TOOL_CATEGORY_COMPOMENT = "compoment";
    public static final String TOOL_CATEGORY_LIBRARY = "library";
    public static final String TOOL_CATEGORY_TOOL = "tool";
    public static final String TOOL_CATEGORY_CODE = "code";
    public static final String TOOL_CATEGORY_DESIGN = "design";
    public static final String TOOL_CATEGORY_PROJECT = "project";

    /*
     * ========================================================================
     * Web Config
     * ========================================================================
     */

    public static String sDomain = "http://www.yy317.com/";

    public static String getDigestListUrl(int page, int size) {
        return sDomain + "android/digest/list.php?page=" + page + "&size=" + size;
    }

    public static String getDigestDetailUrl(int id) {
        return sDomain + "android/digest/detail.php?id=" + id;
    }

    public static String getSearchDigestListUrl(String key, int page, int size) {
        return sDomain + "android/digest/search.php?key=" + key + "&page=" + page + "&size=" + size;
    }

    public static String getSearchToolListUrl(String key, int page, int size) {
        return sDomain + "android/tool/search.php?key=" + key + "&page=" + page + "&size=" + size;
    }

    public static String getAddReviewDigestUrl() {
        return sDomain + "android/digest/review_post.php";
    }

    public static String getBlogListUrl() {
        return sDomain + "android/blog/list.json";
    }

    public static String getToolListUrl(String type) {
        return sDomain + "android/tool/list.php?type=" + type;
    }

    public static String getCheckUpdateUrl() {
        return sDomain + "android/latest.json";
    }

    public static String getReviewDigestListUrl(int page, int size) {
        return sDomain + "android/digest/list.php?table=android_digest_review&page=" + page + "&size=" + size;
    }

    public static String getOfflineListUrl() {
        return sDomain + "android/offline/list.json";
    }
}
