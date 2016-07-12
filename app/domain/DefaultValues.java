package domain;

public class DefaultValues {
    public static final String DELIMITER_COMMA = ",";
    
	public static final int DEFAULT_INFINITE_SCROLL_COUNT = 20;
	public static final int FEED_INFINITE_SCROLL_COUNT = 40;
	
	public static final int MAX_POST_IMAGES = 4;
	public static final int MAX_MESSAGE_IMAGES = 1;
	public static final int MAX_TREND_PRODUCTS_FOR_FEED = 10;
	public static final int MAX_SELLER_PRODUCTS_FOR_FEED = 4;
	public static final int MIN_RECOMMENDED_SELLER_PRODUCTS = 4;
	
    public static final int POST_PREVIEW_CHARS = 300;
    public static final int COMMENT_PREVIEW_CHARS = 200;
    public static final int DEFAULT_PREVIEW_CHARS = 200;
    
    public static final int LATEST_COMMENTS_COUNT = 3;
    
    public static final int SHORT_MESSAGE_COUNT = 50;
    
    public static final int MAX_CONVERSATIONS_COUNT = 1000;
    
    public static final int ACTIVITY_NEW_COMMENT_MAX_FAN_OUT = 100;
    public static final int MAX_ACTIVITIES_COUNT = 100;
    
    public static final int MAX_ITEMS_COUNT = 1000;
    
    public static final String GOOGLEMAP_PREFIX = "http://maps.google.com.hk/maps?q=";

    static {
        init();
    }
    
    private static void init() {
        
    }
}
