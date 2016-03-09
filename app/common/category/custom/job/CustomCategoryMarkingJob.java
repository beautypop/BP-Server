package common.category.custom.job;

import java.util.List;

import models.Category;
import models.Post;

public interface CustomCategoryMarkingJob {
	
    public void execute(Post post, Category customCategory, List<String> customData);
    
}
