package controllers;

import handler.FeedHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import models.Category;
import models.User;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import viewmodel.CategoryVM;
import viewmodel.PostVMLite;
import viewmodel.UserVM;
import common.cache.CategoryCache;
import common.model.FeedFilter;
import common.model.FeedFilter.FeedType;

public class CategoryController extends Controller{
	private static play.api.Logger logger = play.api.Logger.apply(CategoryController.class);
	
	@Inject
    FeedHandler feedHandler;
    
	@Transactional 
    public Result getTrendPreviewFeeds(){
	    final User localUser = Application.getLocalUser(session());
        Map<Long, List<PostVMLite>> vms = feedHandler.getTrendPreviewFeeds(localUser, FeedType.CATEGORY_POPULAR);
        return ok(Json.toJson(vms));
    }
	
	@Transactional 
	public Result getCategoryPopularFeed(Long id, String conditionType, Long offset){
		final User localUser = Application.getLocalUser(session());
		FeedFilter.ConditionType condition = FeedFilter.ConditionType.ALL;
		try {
		    condition = FeedFilter.ConditionType.valueOf(conditionType);
		} catch (Exception e) {
		    ;
		}
		List<PostVMLite> vms = null;
		if (FeedFilter.ConditionType.ALL.equals(condition)) {
		    vms = feedHandler.getFeedPosts(id, offset, localUser, FeedType.CATEGORY_POPULAR);
		} else if (FeedFilter.ConditionType.NEW.equals(condition)) {
		    vms = feedHandler.getFeedPosts(id, offset, localUser, FeedType.CATEGORY_POPULAR_NEW);
		} else if (FeedFilter.ConditionType.USED.equals(condition)) {
		    vms = feedHandler.getFeedPosts(id, offset, localUser, FeedType.CATEGORY_POPULAR_USED);
		}
		return ok(Json.toJson(vms));
	}

	@Transactional 
	public Result getCategoryNewestFeed(Long id, String conditionType, Long offset){
		final User localUser = Application.getLocalUser(session());
		List<PostVMLite> vms = feedHandler.getFeedPosts(id, offset, localUser, FeedType.CATEGORY_NEWEST);
		return ok(Json.toJson(vms));
	}
	
	@Transactional 
	public Result getCategoryPriceLowHighFeed(Long id, String conditionType, Long offset){
		final User localUser = Application.getLocalUser(session());
		List<PostVMLite> vms = feedHandler.getFeedPosts(id, offset, localUser, FeedType.CATEGORY_PRICE_LOW_HIGH);
		return ok(Json.toJson(vms));
	}
	
	@Transactional 
	public Result getCategoryPriceHighLowFeed(Long id, String conditionType, Long offset) {
		final User localUser = Application.getLocalUser(session());
		List<PostVMLite> vms = feedHandler.getFeedPosts(id, offset, localUser, FeedType.CATEGORY_PRICE_HIGH_LOW);
		return ok(Json.toJson(vms));
	}
	
	@Transactional
    public static Result getAllCategories(){
        List<CategoryVM> categories = new ArrayList<CategoryVM>();
        for (Category category : CategoryCache.getCategories()) {
            CategoryVM vm = new CategoryVM(category);
            categories.add(vm);
        }
        for (Category category : CategoryCache.getThemeCategories()) {
            CategoryVM vm = new CategoryVM(category);
            categories.add(vm);
        }
        for (Category category : CategoryCache.getTrendCategories()) {
            CategoryVM vm = new CategoryVM(category);
            categories.add(vm);
        }
        return ok(Json.toJson(categories));
    }

	@Transactional
    public static Result getCategories(){
        List<CategoryVM> categories = new ArrayList<CategoryVM>();
        for (Category category : CategoryCache.getCategories()) {
            CategoryVM vm = new CategoryVM(category);
            categories.add(vm);
        }
        return ok(Json.toJson(categories));
    }
    
	@Transactional
    public static Result getThemeCategories(){
        List<CategoryVM> categories = new ArrayList<CategoryVM>();
        for (Category category : CategoryCache.getThemeCategories()) {
            CategoryVM vm = new CategoryVM(category);
            categories.add(vm);
        }
        return ok(Json.toJson(categories));
    }

	@Transactional
	public static Result getTrendCategories(){
	    List<CategoryVM> categories = new ArrayList<CategoryVM>();
	    for (Category category : CategoryCache.getTrendCategories()) {
	        CategoryVM vm = new CategoryVM(category);
	        categories.add(vm);
	    }
	    return ok(Json.toJson(categories));
	}
	
	@Transactional
    public static Result getCustomCategories(){
        List<CategoryVM> categories = new ArrayList<CategoryVM>();
        for (Category category : CategoryCache.getCustomCategories()) {
            CategoryVM vm = new CategoryVM(category);
            categories.add(vm);
        }
        return ok(Json.toJson(categories));
    }
	
	@Transactional
    public static Result getSubCategories(Long id){
        return ok(Json.toJson(getSubCategoryVMs(id)));
    }
	
	public static List<CategoryVM> getSubCategoryVMs(Long id) {
	    List<CategoryVM> vms = new ArrayList<CategoryVM>();
	    List<Category> subCategories = Category.getSubCategories(id);
	    if (subCategories != null) {
            for(Category category : subCategories){
                CategoryVM vm = new CategoryVM(category);
                vms.add(vm);
            }
	    }
        return vms;
	}
    
    @Transactional
    public static Result getCategory(Long id){
        Category category = Category.findById(id);
        if (category == null) {
            logger.underlyingLogger().warn(String.format("[cat=%d] Category not found", id));
            return notFound();
        }
        
        CategoryVM categoryVM = new CategoryVM(category);
        return ok(Json.toJson(categoryVM));
    }
    
    @Transactional
    public Result viewCategory(Long id, String catagoryFilter){
        User localUser = Application.getLocalUser(session());
        Category category = Category.findById(id);
        if (category == null) {
            logger.underlyingLogger().warn(String.format("[category=%d][u=%d] Category not found", id, localUser.id));
            return Application.pathNotFound();
        }
        
        List<CategoryVM> subCategoryVMs = getSubCategoryVMs(id);
        
        CategoryVM categoryVM = new CategoryVM(category);
        List<PostVMLite> postVMs = new ArrayList<>();
        
        switch(catagoryFilter){
        case "popular":
            postVMs = feedHandler.getFeedPosts(id, 0L, localUser, FeedType.CATEGORY_POPULAR);
            break;
        case "newest":
            postVMs = feedHandler.getFeedPosts(id, 0L, localUser, FeedType.CATEGORY_NEWEST);
            break;
        case "high2low":
            postVMs = feedHandler.getFeedPosts(id, 0L, localUser, FeedType.CATEGORY_PRICE_HIGH_LOW);
            break;
        case "low2high":
            postVMs = feedHandler.getFeedPosts(id, 0L, localUser, FeedType.CATEGORY_PRICE_LOW_HIGH);
            break;
        }
        
        String metaTags = Application.generateHeaderMeta(category.name, category.description, category.icon);
        return ok(views.html.beautypop.web.category.render(
                Json.stringify(Json.toJson(subCategoryVMs)),
                Json.stringify(Json.toJson(categoryVM)),
                Json.stringify(Json.toJson(postVMs)), 
                Json.stringify(Json.toJson(new UserVM(localUser))),
                metaTags));
    }
}