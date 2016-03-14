package controllers;

import static play.data.Form.form;
import handler.FeedHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import common.social.exception.SocialObjectNotCommentableException;
import models.Category;
import models.Collection;
import models.Comment;
import models.Conversation;
import models.Post;
import models.TermsAndConditions;
import models.Post.ConditionType;
import models.Resource;
import models.User;
import play.Play;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import service.SocialRelationHandler;
import viewmodel.CategoryVM;
import viewmodel.CommentVM;
import viewmodel.ConversationVM;
import viewmodel.PostVM;
import viewmodel.PostVMLite;
import viewmodel.ResponseStatusVM;
import viewmodel.UserVM;
import common.model.FeedFilter;
import common.model.FeedFilter.FeedType;
import common.utils.HtmlUtil;
import common.utils.HttpUtil;
import common.utils.ImageFileUtil;
import common.utils.NanoSecondStopWatch;
import controllers.Application.DeviceType;
import domain.DefaultValues;
import domain.SocialObjectType;

public class GuideController extends Controller{
	private static play.api.Logger logger = play.api.Logger.apply(GuideController.class);
	
	@Transactional
    public static Result howToPayment() {
        return ok(views.html.beautypop.guide.how_to_payment.render());
    }
	
	@Transactional
    public static Result howToStaySafe() {
        return ok(views.html.beautypop.guide.how_to_stay_safe.render());
    }
	
	@Transactional
    public static Result newPostCategory() {
        return ok(views.html.beautypop.guide.new_post_category.render());
    }
	
	@Transactional
    public static Result prohibitedItems() {
        return ok(views.html.beautypop.guide.prohibited_items.render());
    }
}