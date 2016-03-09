package common.category.custom.job;

import java.util.Date;
import java.util.List;

import common.cache.CalcServer;
import common.utils.DateTimeUtil;
import models.Category;
import models.Post;

public class NewTodayMarkingJob implements CustomCategoryMarkingJob {
	private static play.api.Logger logger = play.api.Logger.apply(NewTodayMarkingJob.class);
	
	public void execute(Post post, Category customCategory, List<String> customData) {
		if (DateTimeUtil.withinADay(post.getCreatedDate().getTime(), new Date().getTime())) {
		    logger.underlyingLogger().info("Mark post id="+post.id);
			CalcServer.instance().addToCategoryQueues(post, customCategory);
		}
	}
}
