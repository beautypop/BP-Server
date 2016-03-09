package common.category.custom.job;

import java.util.List;

import common.cache.CalcServer;
import models.Category;
import models.Post;

public class PriceMarkingJob implements CustomCategoryMarkingJob {
	private static play.api.Logger logger = play.api.Logger.apply(PriceMarkingJob.class);
	
	public void execute(Post post, Category customCategory, List<String> customData) {
	    if (customData == null || customData.isEmpty()) {
	        logger.underlyingLogger().info("customData is empty... exit marking for customCategory="+customCategory.id);
	        return;
	    }
	    
	    Long price = Long.valueOf(customData.get(0));
		if (post.price <= price) {
		    logger.underlyingLogger().info("Mark post id="+post.id);
		    CalcServer.instance().addToCategoryQueues(post, customCategory);
		}
	}
}
