package common.category.custom;

import models.Category;
import models.PostToMark;
import models.Post;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.utils.StringUtil;

public class PostMarker {
    private static final play.api.Logger logger = play.api.Logger.apply(PostMarker.class);
    
    private static ClassLoader classLoader;
    private static Class[] paramString; 

    private static Map<Long, Object> customCategoryToJobMap = new HashMap<>();
    private static Map<Long, Method> customCategoryToJobMethodMap = new HashMap<>();
    
    static {
        classLoader = PostMarker.class.getClassLoader();
        paramString = new Class[3]; 
        paramString[0] = Post.class;
        paramString[1] = Category.class;
        paramString[2] = List.class;
        
        for (Category customCategory : Category.getCustomCategories()) {
            try {
                // customJob name must be qualified name in DB
                // eg: common.category.custom.job.PriceMarkingJob
                Class cls = classLoader.loadClass(customCategory.customJob);
                Object job = cls.newInstance();
                if (job != null) {
                    customCategoryToJobMap.put(customCategory.id, job);
                    Method method = cls.getDeclaredMethod("execute", paramString);
                    if (method != null) {
                        customCategoryToJobMethodMap.put(customCategory.id, method);
                    }    
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException e) {
                logger.underlyingLogger().error(customCategory.customJob+" failed to get method... \n"+e.getMessage(), e);
            }
        }
    }
    
    public static void markPosts() {
        for (PostToMark postToMark : PostToMark.getAllPostsToMark()) {
            Post post = Post.findById(postToMark.postId);
            if (post != null) {
                markPost(post);
            }
            postToMark.delete();
        }
    }
    
	public static void markPost(Post post) {
		try {
			for (Category customCategory : Category.getCustomCategories()) {
			    Object job = customCategoryToJobMap.get(customCategory.id);
			    if (job != null) {
				    Method method = customCategoryToJobMethodMap.get(customCategory.id);
				    if (method != null) {
				        List<String> customData = StringUtil.parseValues(customCategory.customJobData);
    					try {
    						method.invoke(job, new Object[] { post, customCategory, customData });
    					} catch (InvocationTargetException e) {
    					    logger.underlyingLogger().error(customCategory.customJob+" failed to execute... \n"+e.getMessage(), e.getTargetException());
    					}
				    }
			    }
			}
		} catch (Exception e) {
			logger.underlyingLogger().error("Error in markPosts()", e);
		}
	}				
}
