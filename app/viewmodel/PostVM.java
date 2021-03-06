package viewmodel;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import domain.DefaultValues;
import models.Comment;
import models.Post;
import models.User;

public class PostVM extends PostVMLite {
	@JsonProperty("ownerNumProducts") public Long ownerNumProducts;
	@JsonProperty("ownerNumFollowers") public Long ownerNumFollowers;
	@JsonProperty("ownerLastLogin") public Long ownerLastLogin;
	@JsonProperty("body") public String body;
	@JsonProperty("categoryName") public String categoryName;
	@JsonProperty("categoryIcon") public String categoryIcon;
	@JsonProperty("categoryType") public String categoryType;
    @JsonProperty("subCategoryName") public String subCategoryName;
    @JsonProperty("subCategoryIcon") public String subCategoryIcon;
    @JsonProperty("subCategoryType") public String subCategoryType;
    @JsonProperty("latestComments") public List<CommentVM> latestComments;
    
	@JsonProperty("isOwner") public boolean isOwner = false;
	@JsonProperty("isFollowingOwner") public boolean isFollowingOwner = false;
    
	@JsonProperty("deviceType") public String deviceType;
	
    public PostVM(Post post, User user) {
    	super(post, user);
    	
        this.ownerNumProducts = post.owner.numProducts;
        this.ownerNumFollowers = post.owner.numFollowers;
        this.ownerLastLogin = 
                post.owner.lastLogin == null? 
                        post.getUpdatedDate().getTime() : post.owner.lastLogin.getTime();
        this.body = post.body;
        
        if (post.category.parent == null) {
            this.categoryId = post.category.id;
            this.categoryName = post.category.name;
            this.categoryIcon = post.category.icon;
            this.categoryType = post.category.categoryType.toString();
            this.subCategoryId = -1L;
            this.subCategoryName = "";
            this.subCategoryIcon = "";
            this.subCategoryType = "";
        } else {
            this.categoryId = post.category.parent.id;
            this.categoryName = post.category.parent.name;
            this.categoryIcon = post.category.parent.icon;
            this.categoryType = post.category.parent.categoryType.toString();
            this.subCategoryId = post.category.id;
            this.subCategoryName = post.category.name;
            this.subCategoryIcon = post.category.icon;
            this.subCategoryType = post.category.categoryType.toString();
        }
        
        this.latestComments = new ArrayList<>();
        for (Comment comment : post.getLatestComments(DefaultValues.LATEST_COMMENTS_COUNT)) {
        	this.latestComments.add(new CommentVM(comment, user));
        }
        
        this.isOwner = (post.owner.id == user.id);
        this.isFollowingOwner = user.isFollowing(post.owner);
        
        this.deviceType = post.deviceType == null? "" : post.deviceType.name();
    }

    public Long getOwnerNumProducts() {
        return ownerNumProducts;
    }

    public void setOwnerNumProducts(Long ownerNumProducts) {
        this.ownerNumProducts = ownerNumProducts;
    }
    
    public Long getOwnerNumFollowers() {
        return ownerNumFollowers;
    }

    public void setOwnerNumFollowers(Long ownerNumFollowers) {
        this.ownerNumFollowers = ownerNumFollowers;
    }
    
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    
    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getSubCategoryIcon() {
        return subCategoryIcon;
    }

    public void setSubCategoryIcon(String subCategoryIcon) {
        this.subCategoryIcon = subCategoryIcon;
    }

    public String getSubCategoryType() {
        return subCategoryType;
    }

    public void setSubCategoryType(String subCategoryType) {
        this.subCategoryType = subCategoryType;
    }
    
    public List<CommentVM> getLatestComments() {
		return latestComments;
	}

	public void setLatestComments(List<CommentVM> latestComments) {
		this.latestComments = latestComments;
	}

	public boolean isOwner() {
        return isOwner;
    }

    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }
    
    public boolean isFollowingOwner() {
        return isFollowingOwner;
    }

    public void setIsFollowingOwner(boolean isFollowingOwner) {
        this.isFollowingOwner = isFollowingOwner;
    }
}