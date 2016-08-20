package viewmodel;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import common.utils.DateTimeUtil;
import models.Country;
import models.Country.CountryCode;
import models.Post;
import models.User;

public class PostVMLite {
	@JsonProperty("id") public Long id;
	@JsonProperty("createdDate") public Long createdDate;
    @JsonProperty("updatedDate") public Long updatedDate;
	@JsonProperty("ownerId") public Long ownerId;
    @JsonProperty("ownerName") public String ownerName;

	@JsonProperty("title") public String title;
	@JsonProperty("price") public double price;
	@JsonProperty("sold") public boolean sold;
	@JsonProperty("conditionType") public String conditionType;
	@JsonProperty("images") public Long[] images;
	@JsonProperty("hasImage") public boolean hasImage = false;
	
	@JsonProperty("categoryId") public Long categoryId;
	@JsonProperty("subCategoryId") public Long subCategoryId;
	@JsonProperty("themeId") public Long themeId;
    @JsonProperty("trendId") public Long trendId;
    
	@JsonProperty("numLikes") public int numLikes;
	@JsonProperty("numConversations") public int numConversations;
	@JsonProperty("numBuys") public int numBuys;
	@JsonProperty("numComments") public int numComments;
	@JsonProperty("numViews") public int numViews;
	@JsonProperty("isLiked") public boolean isLiked = false;
    
	// for feed
	@JsonProperty("offset") public Long offset;
	
	// seller fields
	@JsonProperty("originalPrice") public double originalPrice;
	@JsonProperty("freeDelivery") public boolean freeDelivery;
	@JsonProperty("countryCode") public String countryCode;
	@JsonProperty("countryIcon") public String countryIcon;
	
	// admin fields
	@JsonProperty("baseScore") public Long baseScore = 0L;
	@JsonProperty("timeScore") public Double timeScore = 0D;

	public PostVMLite(Post post) {
	    this(post, null);
	}
	
    public PostVMLite(Post post, User user) {
        this.id = post.id;
        this.createdDate = post.getCreatedDate().getTime();
        this.updatedDate = post.getUpdatedDate().getTime();
        this.ownerId = post.owner.id;
        this.ownerName = post.owner.displayName;
        this.title = post.title;
        this.price = post.price;
        this.sold = post.sold;
        if (post.conditionType != null) {
            this.conditionType = post.conditionType.toString();
        }
        
        Long[] images = post.getImages();
        if (images != null && images.length > 0) {
            this.hasImage = true;
            this.images = images;
        }
        
        if (post.category.parent == null) {
            this.categoryId = post.category.id;
            this.subCategoryId = -1L;
        } else {
            this.categoryId = post.category.parent.id;
            this.subCategoryId = post.category.id;
        }

        if (post.theme != null) {
            this.themeId = post.theme.id;
        }
        
        if (post.trend != null) {
            this.trendId = post.trend.id;
        }
        
        this.numLikes = post.numLikes;
        this.numConversations = post.numConversations;
        this.numBuys = post.numBuys;
        this.numComments = post.numComments;
        this.numViews = post.numViews;
        
        if (user != null) {
            this.isLiked = post.isLikedBy(user);
            
            if (user.isSuperAdmin()) {
                this.baseScore = post.baseScore;
                this.timeScore = post.timeScore;
            }
        }
        
        this.originalPrice = post.originalPrice;
        this.freeDelivery = post.freeDelivery;
        this.countryCode = post.countryCode.name();
        
        if (post.countryCode != null && post.countryCode != CountryCode.NA) {
            Country country = Country.getCountry(post.countryCode);
            if (country != null) {
                this.countryIcon = country.getIcon();
            }
        }
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public Long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Long updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean getSold() {
        return sold;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }

    public int getNumConversations() {
        return numConversations;
    }

    public void setNumConversations(int numConversations) {
        this.numConversations = numConversations;
    }

    public int getNumBuys() {
        return numBuys;
    }

    public void setNumBuys(int numBuys) {
        this.numBuys = numBuys;
    }

    public int getNumComments() {
        return numComments;
    }

    public void setNumComments(int numComments) {
        this.numComments = numComments;
    }

    public String getConditionType() {
        return conditionType;
    }
    
    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public Long[] getImages() {
		return images;
	}
	
    public void setImages(Long[] images) {
		this.images = images;
	}
	
	public boolean getHasImage() {
		return hasImage;
	}
	
	public void setHasImage(boolean hasImage) {
		this.hasImage = hasImage;
	}
	
	public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public Long getThemeId() {
        return themeId;
    }

    public void setThemeId(Long themeId) {
        this.themeId = themeId;
    }
    
    public Long getTrendId() {
        return trendId;
    }

    public void setTrendId(Long trendId) {
        this.trendId = trendId;
    }
    
    public int getNumViews() {
        return numViews;
    }

    public void setNumViews(int numViews) {
        this.numViews = numViews;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(int numLikes) {
        this.numLikes = numLikes;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public boolean getFreeDelivery() {
        return freeDelivery;
    }

    public void setFreeDelivery(boolean freeDelivery) {
        this.freeDelivery = freeDelivery;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    public String getCountryIcon() {
        return countryIcon;
    }

    public void setCountryIcon(String countryIcon) {
        this.countryIcon = countryIcon;
    }
    
    public String shortInfo() {
        return title + "\n" +
                "$" + price + "\n" +
                "id=" + id + "\n" +
                "seller=" + ownerName + "\n" +
                "listed on=" + DateTimeUtil.toString(new Date(createdDate));
    }
}