package common.utils;

import java.util.regex.Pattern;

public class ValidationUtil {
    
    public static final int USER_DISPLAYNAME_MIN_CHAR = 2;
    public static final int USER_DISPLAYNAME_MAX_CHAR = 30;
    public static final int USER_NAME_MAX_CHAR = 20;
    
    private static final String EMAIL_FORMAT_REGEX =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String USER_DISPLAYNAME_FORMAT_REGEX =
            "^[_A-Za-z0-9]+([\\._A-Za-z0-9]+)*[_A-Za-z0-9]$";
    private static final String USER_NAME_FORMAT_REGEX =
            "^[_\\p{L}0-9]+([\\._\\p{L}0-9]+)*$";      // \p{L} matches letter in any language

    public static boolean isEmailValid(String email) {
        if (StringUtil.hasWhitespace(email)) {
            return false;
        }
        return Pattern.matches(EMAIL_FORMAT_REGEX, email);
    }
    
    public static boolean isValidDisplayName(String displayName) {
        // char 2-30
        if (displayName.length() < USER_DISPLAYNAME_MIN_CHAR || displayName.length() > USER_DISPLAYNAME_MAX_CHAR) {
            return false;
        }
        
        // whitespace
        if (StringUtil.hasWhitespace(displayName)) {
            return false;
        }
        
        // .. in a row
        if (displayName.contains("..")) {
            return false;
        }
        
        return Pattern.matches(USER_DISPLAYNAME_FORMAT_REGEX, displayName);   
    }
}
