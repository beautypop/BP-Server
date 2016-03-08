package common.category.custom;

import models.Hashtag;
import models.Post;

public interface CustomCategoryMarkingJob {
	
    public void execute(Post post, Hashtag hashtag);
    
}
