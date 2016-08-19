package viewmodel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import controllers.CategoryController;
import models.Category;

public class CategoryVM {
	@JsonProperty("id") public Long id;
	@JsonProperty("icon") public String icon;
	@JsonProperty("thumbnail") public String thumbnail;
	@JsonProperty("name") public String name;
	@JsonProperty("description") public String description;
	@JsonProperty("categoryType") public String categoryType;
	@JsonProperty("seq") public int seq;
	@JsonProperty("featured") public boolean featured;
	@JsonProperty("parentId") public Long parentId;
	@JsonProperty("subCategories") public List<CategoryVM> subCategories;

    public CategoryVM(Category category) {
    	this.id = category.id;
    	this.icon = category.icon;
    	this.thumbnail = category.thumbnail;
    	this.name = category.name;
    	this.description = category.description;
    	this.categoryType = category.categoryType.name();
    	this.seq = category.seq;
    	this.featured = category.featured;
    	this.parentId = category.parent == null? -1L : category.parent.id;
    	this.subCategories = CategoryController.getSubCategoryVMs(id);
    }
}
