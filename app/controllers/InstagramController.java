package controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.Category;
import models.InstagramImportedImage;
import models.Post;
import models.PostToMark;
import models.User;
import models.Country.CountryCode;
import models.Post.ConditionType;

import org.apache.commons.lang.StringUtils;
import org.jinstagram.Instagram;
import org.jinstagram.auth.InstagramAuthService;
import org.jinstagram.auth.model.Token;
import org.jinstagram.auth.model.Verifier;
import org.jinstagram.auth.oauth.InstagramService;
import org.jinstagram.entity.common.ImageData;
import org.jinstagram.entity.common.Images;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;

import common.utils.ImageFileUtil;
import common.utils.NanoSecondStopWatch;
import controllers.Application.DeviceType;
import domain.SocialObjectType;
import insta.importer.dto.MediaDto;
import play.Play;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import service.SocialRelationHandler;
import viewmodel.ResponseStatusVM;

public class InstagramController extends Controller {
    private static final play.api.Logger logger = play.api.Logger.apply(InstagramController.class);

    private static String clientId = Play.application().configuration().getString("instagram.clientId");
    private static String clientSecret = Play.application().configuration().getString("instagram.clientSecret");
    private static String redirectUrl = Play.application().configuration().getString("instagram.redirectUrl");
	
	static InstagramService service = new InstagramAuthService()
	.apiKey(clientId)
	.apiSecret(clientSecret)
	.callback(redirectUrl)
	.build();
    
	@Transactional
    public Result index() {
    	return redirect("assets/insta/importer/main.html");
    }   
    
    public static Result loginInstagram() {
		String authorizationUrl = service.getAuthorizationUrl();
		return redirect(authorizationUrl);
	}
   
    public static Result generateAccessToken(String code){
		Verifier verifier = new Verifier(code);
		try {
		    Token token = service.getAccessToken(verifier);
    		session().put("accessToken", token.getToken());
    		session().put("clientSecret", token.getSecret());
    		return ok();
		} catch (Exception e) {
		}
		return redirect("assets/insta/importer/main.html");
	}
    
    @Transactional
    public static Result getMedia() {
        final User localUser = Application.getLocalUser(session());
        if (!localUser.isLoggedIn()) {
            logger.underlyingLogger().error(String.format("[u=%d] User not logged in", localUser.id));
            return notFound();
        }
        
		Token token = new Token(session().get("accessToken"), session().get("clientSecret"));
		
		List<MediaDto> mediaList = new ArrayList<MediaDto>();
		
		Instagram instagram = new Instagram(token);
		
		MediaFeed mediaInfo = new MediaFeed();
		try {
			mediaInfo = instagram.getRecentMediaFeed("self");
		} catch (InstagramException e) {
			e.printStackTrace();
		}
		
		List<MediaFeedData> mediaFeeds = mediaInfo.getData();
		for (MediaFeedData mediaData : mediaFeeds) {
			MediaDto dto = new MediaDto();
			
		    Images images = mediaData.getImages();
		    dto.setMediaId(mediaData.getId());
		    boolean imported = InstagramImportedImage.isImported(localUser, mediaData.getId());
		    dto.setIsImported(imported);
		    
		    ImageData lowResolutionImg = images.getLowResolution();
		    dto.setImageUrl(lowResolutionImg.getImageUrl());
		    dto.setCaption(mediaData.getCaption().getText());
		    mediaList.add(dto);
		    
		}
		return ok(Json.toJson(mediaList));
	}
    
    @Transactional
	public static Result newProductWithForm() {
		DynamicForm dynamicForm = DynamicForm.form().bindFromRequest();
		String catId = dynamicForm.get("catId");
		String themeId = dynamicForm.get("themeId");
		String trendId = dynamicForm.get("trendId");
	    String title = dynamicForm.get("title");
	    String body = dynamicForm.get("body");
	    String price = dynamicForm.get("price");
	    String conditionType = dynamicForm.get("conditionType");
	    String originalPrice = dynamicForm.get("originalPrice");
	    Boolean freeDelivery = Boolean.valueOf(dynamicForm.get("freeDelivery"));
	    String countryCode = dynamicForm.get("countryCode");
	    String hashtags = dynamicForm.get("hashtags");
	    String deviceType = dynamicForm.get("deviceType");
	    String images = dynamicForm.get("images");
	    String mediaId = dynamicForm.get("mediaId");
	    
	    if (StringUtils.isEmpty(originalPrice)) {
	        originalPrice = "-1";
        }
	    
	    if (StringUtils.isEmpty(countryCode)) {
            countryCode = CountryCode.NA.name();
        }
	    
	    if (StringUtils.isEmpty(deviceType)) {
	        deviceType = DeviceType.WEB.name();
        }
	    
	    try {
	        return newProduct(
	                title, body, Long.parseLong(catId), Double.parseDouble(price), Post.parseConditionType(conditionType), images, 
	                Double.parseDouble(originalPrice), freeDelivery, Post.parseCountryCode(countryCode), hashtags, Application.parseDeviceType(deviceType), mediaId,  Long.parseLong(themeId),  Long.parseLong(trendId));    
	    } catch (Exception e) {
	        return badRequest();
	    }
	}

	private static Result newProduct(
	        String title, String body, Long catId, Double price, ConditionType conditionType, String images, 
	        Double originalPrice, Boolean freeDelivery, CountryCode countryCode, String hashtags, DeviceType deviceType, String mediaId, Long themeId, Long trendId) {
	    
	    NanoSecondStopWatch sw = new NanoSecondStopWatch();
	    
		final User localUser = Application.getLocalUser(session());
		if (!localUser.isLoggedIn()) {
			logger.underlyingLogger().error(String.format("[u=%d] User not logged in", localUser.id));
			return notFound();
		}
		
		Category category = Category.findById(catId);
        if (category == null) {
            logger.underlyingLogger().debug("[u="+localUser.getId()+"][catId="+catId+"] createProduct() Invalid catId");
            return badRequest("Failed to create product. Invalid catId="+catId);
        }
        Category theme = Category.findById(themeId);
        Category trend = Category.findById(trendId);
        
		try {
			Post newPost = localUser.createProduct(
			        title, body, category, price, conditionType, 
			        originalPrice, freeDelivery, countryCode, deviceType, theme, trend);
			if (newPost == null) {
			    logger.underlyingLogger().debug("[u="+localUser.getId()+"][catId="+catId+"][title="+title+"][body="+body+"][price="+price+"][conditionType="+conditionType+"] createProduct() Invalid catId");
                return badRequest("Failed to create product. Invalid parameters.");
			}
			
			File fileTo =  ImageFileUtil.copyImageFileFromUrl(title.replaceAll(" ", "_").toLowerCase()+".png", images);
			newPost.addPostPhoto(fileTo);
			
			ProductController.addHashtagsToPost(hashtags, newPost);
			SocialRelationHandler.recordNewPost(newPost, localUser);
			ResponseStatusVM response = new ResponseStatusVM(SocialObjectType.POST, newPost.id, localUser.id, true);
			
			// To be marked by PostMarker 
			PostToMark mark = new PostToMark(newPost.id);
			mark.save();
			
			sw.stop();
	        if (logger.underlyingLogger().isDebugEnabled()) {
	            logger.underlyingLogger().debug("[u="+localUser.getId()+"][p="+newPost.id+"] createProduct(). Took "+sw.getElapsedMS()+"ms");
	        }
	        saveInstagramImportedImage(mediaId);
			return ok(Json.toJson(response));
		} catch (IOException e) {
			logger.underlyingLogger().error("Error in createProduct", e);
		}
		
		return badRequest();
	}
	
	public static void saveInstagramImportedImage(String mediaId){
	    InstagramImportedImage insta = new InstagramImportedImage();
		insta.user = Application.getLocalUser(session());
		insta.mediaId = mediaId;
		insta.save();
	}
}
