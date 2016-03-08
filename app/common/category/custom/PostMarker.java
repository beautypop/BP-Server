package common.category.custom;

import models.Category;
import models.PostToMark;
import models.Hashtag;
import models.Post;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PostMarker {
    private static final play.api.Logger logger = play.api.Logger.apply(PostMarker.class);
    
	public static void markPosts(){
		try {
			//classloadar to get all loaded classes.
			ClassLoader classLoader = PostToMark.class.getClassLoader();
			Class[] paramString = new Class[2];	
			paramString[0] = Post.class;
			paramString[1] = Hashtag.class;

			for(PostToMark postToMark : PostToMark.getAllPostsToMark()){
			    Post post = Post.findById(postToMark.postId);
				for(Category customCategory : Category.getCustomCategories()){
					try {
						//customJob name must be qualified name in DB, for eg: common.Category.NewThisWeekHashtagMarkingJob
						Class cls = classLoader.loadClass(customCategory.customJob);
						Object job = cls.newInstance();
						Method method = cls.getDeclaredMethod("execute", paramString);
						method.invoke(job, new Object[] { post, customCategory });
					} catch (InvocationTargetException e) {
					    logger.underlyingLogger().error(customCategory.customJob+" failed to execute... \n"+e.getMessage(), e.getTargetException());
					}
				}
				postToMark.delete();
			}
		} catch (Exception e) {
			logger.underlyingLogger().error("Error in markPosts()", e);
		}
	}				
}
