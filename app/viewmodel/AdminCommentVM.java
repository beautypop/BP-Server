package viewmodel;

import com.fasterxml.jackson.annotation.JsonProperty;

import models.Comment;
import models.Post;

public class AdminCommentVM {
	@JsonProperty("id") public Long id;
	@JsonProperty("createdDate") public Long createdDate;
	@JsonProperty("ownerId") public Long ownerId;
	@JsonProperty("ownerName") public String ownerName;
	@JsonProperty("body") public String body;
	
	@JsonProperty("postId") public Long postId;
    @JsonProperty("postImage") public Long postImage;
    @JsonProperty("postTitle") public String postTitle;
    @JsonProperty("postPrice") public Long postPrice;
    @JsonProperty("postSold") public boolean postSold;

    @JsonProperty("deviceType") public String deviceType;
    
    public AdminCommentVM(Comment comment, Post post) {
        this.id = comment.id;
        this.ownerId = comment.owner.id;
        this.ownerName = comment.owner.displayName;
        this.createdDate = comment.getCreatedDate().getTime();
        this.body = comment.body;
        
        this.postId = post.id;
        this.postTitle = post.title;
        this.postPrice = post.price.longValue();
        this.postSold = post.sold;
        
        Long[] images = post.getImages();
        if (images != null && images.length > 0) {
            this.postImage = images[0];
        }
        
        this.deviceType = comment.deviceType == null? "" : comment.deviceType.name();
    }
}