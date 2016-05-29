package common.utils;

import controllers.Application;
import viewmodel.CategoryVM;
import viewmodel.PostVMLite;
import viewmodel.UserVMLite;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtil {
    public static final String POST_IMAGE_BY_ID_URL = Application.APPLICATION_BASE_URL + "/image/get-post-image-by-id/";
    
    public static final String ENCODE_CHARSET_UTF8 = "UTF-8";

    private static final String SELLER_URL = Application.APPLICATION_BASE_URL + "/seller/%d";
    private static final String PRODUCT_URL = Application.APPLICATION_BASE_URL + "/product/%d";
    private static final String CATEGORY_URL = Application.APPLICATION_BASE_URL + "/category/%d";

    //private static final String APPS_DOWNLOAD_URL = Application.APPLICATION_BASE_URL + "/apps";
    private static final String APPS_DOWNLOAD_URL = "https://play.google.com/store/apps/details?id=com.beautypop.app";
    private static final String REFERRAL_URL = Application.APPLICATION_BASE_URL + "/signup-code/%s";

    private static String SELLER_URL_REGEX = ".*/seller/(\\d+)";
    private static String PRODUCT_URL_REGEX = ".*/product/(\\d+)";
    private static String CATEGORY_URL_REGEX = ".*/category/(\\d+)";

    private static String VALID_SELLER_URL_REGEX = "[A-Za-z0-9._-]";

    private static String[] HTTP_PREFIXES = {
            "http://www.",
            "https://www.",
            "http://",
            "https://",
            "www."
    };

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, ENCODE_CHARSET_UTF8);
        } catch (Exception e) {
        }
        return value;
    }

    public static String getFullUrl(String url) {
        if (!url.startsWith("http")) {      // !url.startsWith(Application.APPLICATION_BASE_URL)) {
            url = Application.APPLICATION_BASE_URL + url;
        }
        return url;
    }

    public static String createSellerUrl(UserVMLite user) {
        return Application.APPLICATION_BASE_URL + "/" + user.displayName.toLowerCase();
    }

    public static String createProductUrl(PostVMLite post) {
        return String.format(PRODUCT_URL, post.id);
    }

    public static String createCategoryUrl(CategoryVM category) {
        return String.format(CATEGORY_URL, category.id);
    }

    public static String createAppsDownloadUrl() {
        return APPS_DOWNLOAD_URL;
    }

    public static long parseSellerUrlId(String url) {
        return parseUrlMatcher(SELLER_URL_REGEX, url);
    }

    public static long parseProductUrlId(String url) {
        return parseUrlMatcher(PRODUCT_URL_REGEX, url);
    }

    public static long parseCategoryUrlId(String url) {
        return parseUrlMatcher(CATEGORY_URL_REGEX, url);
    }

    public static String createShortSellerUrl(UserVMLite user) {
        //return AppController.getInstance().getString(R.string.seller_url) + ": " + stripHttpPrefix(createSellerUrl(user));
        return stripHttpPrefix(createSellerUrl(user));
    }

    public static String stripHttpPrefix(String url) {
        for (String prefix : HTTP_PREFIXES) {
            if (url.startsWith(prefix)) {
                return url.replace(prefix, "");
            }
        }
        return url;
    }

    /**
     * Group always starts at 1. Group 0 is whole string.
     *
     * @param regex
     * @param url
     * @param pos
     * @return
     */
    private static long parseUrlMatcherAtPosition(String regex, String url, int pos) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(url);
        if (m.find()) {
            return Long.parseLong(m.group(pos));
        }
        return -1;
    }

    private static long parseUrlMatcher(String regex, String url) {
        return parseUrlMatcherAtPosition(regex, url, 1);
    }
}