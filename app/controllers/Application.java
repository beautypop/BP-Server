package controllers;

import handler.FeedHandler;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

import models.Country;
import models.FeaturedItem;
import models.Activity;
import models.Activity.ActivityType;
import models.FeaturedItem.ItemType;
import models.GameBadge.BadgeType;
import models.GameBadgeAwarded;
import models.Location;
import models.Resource;
import models.SecurityRole;
import models.SystemInfo;
import models.TermsAndConditions;
import models.User;
import models.UserInfo;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.Routes;
import play.data.DynamicForm;
import play.data.Form;
import play.data.validation.ValidationError;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.Session;
import play.mvc.Result;
import providers.MyLoginUsernamePasswordAuthUser;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthProvider.MyLogin;
import providers.MyUsernamePasswordAuthProvider.MySignup;
import viewmodel.CountryVM;
import viewmodel.FeaturedItemVM;
import viewmodel.LocationVM;
import viewmodel.UserVM;
import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.google.common.html.HtmlEscapers;

import common.cache.CalcServer;
import common.cache.CountryCache;
import common.cache.FeaturedItemCache;
import common.cache.LocationCache;
import common.utils.HtmlUtil;
import common.utils.UserAgentUtil;
import common.utils.ValidationUtil;
import email.SendgridEmailClient;

public class Application extends Controller {
    private static final play.api.Logger logger = play.api.Logger.apply(Application.class);

    public static final String APPLICATION_ENV = 
            Play.application().configuration().getString("application.env", "dev");
    
    public static final boolean LOGIN_BYPASS_ALL = 
            Play.application().configuration().getBoolean("login.bypass.all", false);
    
    public static final String APPLICATION_NAME_DISPLAY = 
            Play.application().configuration().getString("application.name.display");
    
    public static final String APPLICATION_BASE_URL = 
            Play.application().configuration().getString("application.baseUrl");
    
    public static final String APPLICATION_ANDROID_URL = 
            Play.application().configuration().getString("application.androidUrl");
    
    public static final String APPLICATION_IOS_URL = 
            Play.application().configuration().getString("application.iosUrl");
    
    public static final long FACEBOOK_APP_ID = 
            Play.application().configuration().getLong("facebook.app.id", 0L);
    
    public static final int SIGNUP_DAILY_THRESHOLD = 
            Play.application().configuration().getInt("signup.daily.threshold", 1000);
    
    public static final int SIGNUP_DAILY_LIMIT = 
            Play.application().configuration().getInt("signup.daily.limit", 1000);
    
    public static final String SECRET_KEY_TOKEN = "TheBestSecretkey";
    
    public static final String APP_USER_KEY = "key";
    public static final String SIGNUP_EMAIL = "signup_email";
    public static final String SESSION_PROMOCODE = "PROMO_CODE";
    public static final String FLASH_MESSAGE_KEY = "message";
    public static final String FLASH_ERROR_KEY = "error";

    @Inject
    CalcServer calcServer;
    
    @Inject
    FeedHandler feedHandler;
    
    public static enum DeviceType {
        NA,
        ANDROID,
        IOS,
        WEB,
        WAP
    }
    
    public static boolean isProd() {
        return "prod".equalsIgnoreCase(APPLICATION_ENV);
    }
    
    public static DeviceType parseDeviceType(String deviceType) {
        try {
            return Enum.valueOf(DeviceType.class, deviceType);
        } catch (Exception e) {
            return DeviceType.NA;
        }
    }
    
    public static String generateHeaderMeta(String title, String description, String image) {
        if (StringUtils.isEmpty(description)) {
            description = "Sell Your Style";
        }
        
        title = HtmlEscapers.htmlEscaper().escape(title);
        description = HtmlEscapers.htmlEscaper().escape(description);
        String metaTags =
                "<title>"+title+"</title>"+
                "<meta name='description' content='"+description+"' />"+
                "<meta name='theme-color' content='#FF2742'>" +
                "<meta name='apple-mobile-web-app-status-bar-style' content='#FF2742'>" +
                "<meta property='og:title' content='"+title+"' />"+
                "<meta property='og:description' content='"+description+"' />"+
                "<meta property='og:image' itemprop='image' content='"+HtmlUtil.fullUrl(image)+"' />"+
                "<meta property='og:type' content='website' />"+
                "<meta property='og:site_name' content='"+APPLICATION_BASE_URL+"' />"+
                "<meta property='fb:app_id' content='"+FACEBOOK_APP_ID+"' />";
        return metaTags;
    }
    
    public static Result getStaticImage(String path) {
        response().setHeader("Cache-Control", "max-age=604800");
        File file = Resource.getStorageStaticImage(path);
        if (file != null) {
            return ok(file);
        }
        return notFound();
    }
    
    @Transactional
    public Result index() {
        return hello();
    }   
    
    //
    // Entry points
    //
    
    public static Result hello() {
        return ok(views.html.beautypop.web.hello.render());
    }
    
    public static Result photoguide() {
        return ok(views.html.beautypop.web.photoguide.render());
    }
    
    @Transactional
    public Result home() {
        final User user = getLocalUser(session());
        if (user.id == -1) {
            return ok(views.html.beautypop.web.home.render(Json.stringify(Json.toJson(new UserVM(user))), Json.stringify(Json.toJson(getFeaturedItemVMs("HOME_SLIDER")))));
        }
        
        if (!User.isLoggedIn(user)){
            return login();
        } else if (user.userInfo == null) {
            if (user.fbLogin) {
                return ok(views.html.signup_info_fb.render(user));
            }
            return ok(views.html.signup_info.render(user));
        }
        
        if (user.isNewUser()) {
            initNewUser();
        }

        return home(user);
    }

    public Result home(User user) {
        return ok(views.html.beautypop.web.home.render(Json.stringify(Json.toJson(new UserVM(user))), Json.stringify(Json.toJson(getFeaturedItemVMs("HOME_SLIDER")))));
    }
    
    @Transactional
    public static Result signup() {
        final User localUser = getLocalUser(session());
        if (User.isLoggedIn(localUser)) {
            return redirect("/home");
        }
        
        return ok(views.html.signup.render(MyUsernamePasswordAuthProvider.SIGNUP_FORM));
    }
    
    @Transactional
    public static Result getUserTargetProfile() {
        final User localUser = getLocalUser(session());
        if (localUser.isLoggedIn() && localUser.userInfo != null) {
            //TargetProfile targetProfile = TargetProfile.fromUser(localUser);
            return ok();
        }
        return ok();        
    }
    
    public static boolean isOverDailySignupThreshold() {
        return User.getTodaySignupCount() >= SIGNUP_DAILY_THRESHOLD;
    }
    
    public static boolean isOverDailySignupLimit() {
        return User.getTodaySignupCount() >= SIGNUP_DAILY_LIMIT;
    }

    @Transactional
    public static Result signupWithPromoCode(String promoCode) {
        // put into http session
        session().put(SESSION_PROMOCODE, promoCode);

        return signup();
    }
    
    @Transactional
    public static Result detailsForPromoCode(String promoCode) {
        // put into http session
        session().put(SESSION_PROMOCODE, promoCode);

        return redirect("/home#!/promo-code-page/"+promoCode);
    }

    @Transactional
    public static Result saveSignupInfoFb() {
        return doSaveSignupInfo(true);
    }
    
    @Transactional
    public static Result saveSignupInfo() {
        return doSaveSignupInfo(false);
    }
    
    @Transactional
    public static Result doSaveSignupInfo(boolean fb) {
        final User localUser = getLocalUser(session());
        
        // UserInfo
        DynamicForm form = DynamicForm.form().bindFromRequest();
        String displayName = form.get("parent_displayname").trim();
        //Location parentLocation = Location.getLocationById(Integer.valueOf(form.get("parent_location")));

        if (!ValidationUtil.isValidDisplayName(displayName)) {
            return handleSaveSignupInfoError("顯示名稱只可輸入 英文字母(a-z) 數字(0-9) 和 符號(_)(.) , 不可以 (.) 結尾, 不可有空格, 不可有2個或以上相連符號(.)", fb);
        }
        if (User.isDisplayNameExists(displayName)) {
            return handleSaveSignupInfoError("\""+displayName+"\" 已被選用。請選擇另一個顯示名稱重試", fb);
        }
        //if (parentLocation == null) {
        //    return handleSaveSignupInfoError("請填寫您的地區", fb);
        //}
        
        localUser.displayName = displayName.toLowerCase();
        localUser.name = new String(localUser.firstName+" "+localUser.lastName).trim();
        localUser.promoCode = localUser.generatePromoCode();
        
        UserInfo userInfo = new UserInfo();
        //userInfo.location = parentLocation;
        localUser.userInfo = userInfo;
        localUser.userInfo.save();
        
        logger.underlyingLogger().info("[u="+localUser.id+"][name="+localUser.displayName+"] doSaveSignupInfo");
        return redirect("/home");
    }
    
    private static Result handleSaveSignupInfoError(String error, boolean fb) {
        final User localUser = getLocalUser(session());
        flash(FLASH_ERROR_KEY, error);
        return fb? badRequest(views.html.signup_info_fb.render(localUser)):
            badRequest(views.html.signup_info.render(localUser));
    }

    public static User getLocalUser(final Session session) {
        // request from mobile 
        String userKey = getMobileUserKey(request());
        if(userKey != null){
            User localUser = null;
            String decryptedValue = null;
            try {
                Key dkey = generateKey();
                Cipher c = Cipher.getInstance("AES");
                c.init(Cipher.DECRYPT_MODE, dkey);
                byte[] decodedValue = new BASE64Decoder().decodeBuffer(userKey);
                byte[] decValue = c.doFinal(decodedValue);
                decryptedValue = new String(decValue);
                //logger.underlyingLogger().debug("getLocalUser from mobile - " + userKey + " => " + decryptedValue);
                localUser = getMobileLocalUser(decryptedValue);
                return localUser;
            } catch(Exception e) { 
                logger.underlyingLogger().error("Failed to getLocalUser from mobile - " + userKey + " => " + decryptedValue, e);
                return null;
            }
        }

        // request from web
        final AuthUser currentAuthUser = PlayAuthenticate.getUser(session);
        if (currentAuthUser == null) {
            return User.noLoginUser();
        }
        final User localUser = User.findByAuthUserIdentity(currentAuthUser);
        if (localUser == null) {
            return User.noLoginUser();
        }
        
        //DateTime exp = new DateTime(currentAuthUser.expires());
        //logger.underlyingLogger().debug("User ["+localUser.getId()+"|"+localUser.getDisplayName()+"] will expire in "+exp.toString());
        
        return localUser;
    }
    
    public static User getLocalUser(final String session) {
        final AuthUser currentAuthUser = PlayAuthenticate.getUser(session);
        if (currentAuthUser == null) {
            return User.noLoginUser();
        }
        final User localUser = User.findByAuthUserIdentity(currentAuthUser);
        if (localUser == null) {
            return User.noLoginUser();
        }
        return localUser;
    }
    
    public static User getMobileLocalUser(final String decryptedValue) {
        final AuthUser currentAuthUser = PlayAuthenticate.getUser(decryptedValue);
        
        if (currentAuthUser == null) {
            return User.noLoginUser();
        }
        final User localUser = User.findByAuthUserIdentity(currentAuthUser);
        if (localUser == null) {
            return User.noLoginUser();
        }
        return localUser;
    }
    
    public static String getMobileUserKey(final play.mvc.Http.Request r) {
        return getMobileUserKey(r, APP_USER_KEY);
    }
    
    public static String getMobileUserKey(final play.mvc.Http.Request r, final Object key) {
        final String[] m = r.queryString().get(key);
        if(m != null && m.length > 0) {
            try {
                return URLDecoder.decode(m[0], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.underlyingLogger().error("Error in getMobileUserKey", e);
            }
        }
        return null;
    }
    
    public static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(SECRET_KEY_TOKEN.getBytes(), "AES");
        return key;
    }
    
    public static Long getLocalUserId() {
        User user = null;
        try {
            user = getLocalUser(session());
        } catch (Exception e) {
            // ignore
        }

        if (user != null) {
            return user.id;
        }
        return User.NO_LOGIN_ID;
    }

    @Restrict(@Group(SecurityRole.USER))
    public static Result restricted() {
        final User localUser = getLocalUser(session());
        return ok(views.html.restricted.render(localUser));
    }

    @Restrict(@Group(SecurityRole.USER))
    public static Result profile() {
        final User localUser = getLocalUser(session());
        String metaTags = generateHeaderMeta(localUser.getDisplayName(), "", "/image/get-profile-image-by-id/"+localUser.getId());
        return ok(views.html.beautypop.web.profile.render(
                Json.stringify(Json.toJson(new UserVM(localUser))), 
                Json.stringify(Json.toJson(new UserVM(localUser))), 
                metaTags));
    }
    
    @Transactional
    public static Result login() {
        return ok(views.html.login.render(MyUsernamePasswordAuthProvider.LOGIN_FORM, isOverDailySignupThreshold()));
    }
    
    @Transactional
    public Result doLogin() {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM.bindFromRequest();
        if (filledForm.hasErrors()) {
            // User did not fill everything properly
            flash(FLASH_ERROR_KEY, "登入電郵或密碼錯誤");
            return badRequest(views.html.login.render(filledForm, isOverDailySignupThreshold()));
        } else {
            // Everything was filled
            Result r = UsernamePasswordAuthProvider.handleLogin(ctx());
            final User localUser = getLocalUser(session());
            if (User.isLoggedIn(localUser)) {
                calcServer.buildQueuesForUser(localUser);
                logger.underlyingLogger().info("[u="+localUser.id+"] [name="+localUser.displayName+"] Native login");
            }
            return r;
        }
    }
    
    @Transactional
    public static Result doLoginMobile() throws AuthException {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM.bindFromRequest();
        if (filledForm.hasErrors()) {
            // User did not fill everything properly
            return badRequest();
        } else {
            // Everything was filled
            Result r = PlayAuthenticate.handleAuthenticationByProvider(ctx(),
                     com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.Case.LOGIN,
                     new MyUsernamePasswordAuthProvider(Play.application()));
            
            // check redirect result and flash for errors
            String error = ctx().flash().get(controllers.Application.FLASH_ERROR_KEY);
            if (!StringUtils.isEmpty(error)) {
                return badRequest(error);
            }
            
            // case where user not verify email yet
            // for all cases, see MyUsernamePasswordAuthProvider.loginUser() and UsernamePasswordAuthProvider.authenticate()
            MyLogin login = filledForm.get();
            UsernamePasswordAuthUser authUser = new MyLoginUsernamePasswordAuthUser(login.getPassword(), login.getEmail());
            final User user = User.findByUsernamePasswordIdentity(authUser);
            if (user != null && !user.emailValidated) {
                return badRequest("電郵尚未認證，請登入電郵並按認證連結 - "+user.email);
            }
            
            // null:null
            String providerKey = session().get(PlayAuthenticate.PROVIDER_KEY);
            String userKey = session().get(PlayAuthenticate.USER_KEY);
            if (StringUtils.isEmpty(providerKey) || "null".equals(providerKey.trim()) || 
                    StringUtils.isEmpty(userKey) || "null".equals(userKey.trim())) {
                return badRequest("沒有此用戶，請確認電郵或密碼無誤");
            }
            
            String encryptedValue = null;
            String plainData = session().get(PlayAuthenticate.PROVIDER_KEY) +
                    PlayAuthenticate.USER_ENCRYPTED_KEY_SEPARATOR +
                    session().get(PlayAuthenticate.USER_KEY);
            try { 
                Key key = generateKey();
                Cipher c = Cipher.getInstance("AES");
                c.init(Cipher.ENCRYPT_MODE, key);
                byte[] encVal = c.doFinal(plainData.getBytes());
                encryptedValue = new BASE64Encoder().encode(encVal);
            } catch(Exception e) { 
                return badRequest();
            }

            encryptedValue = encryptedValue.replace("+", "%2b");
            logger.underlyingLogger().info("[u="+user.id+"] [name="+user.displayName+"] Native mobile login - encryptedValue="+encryptedValue);
            return ok(encryptedValue);
        }
    }

    @Transactional
    public static Result doLoginPopup() {
        DynamicForm form = DynamicForm.form().bindFromRequest();
        String redirectURL = form.get("rurl");
        session().put(PlayAuthenticate.ORIGINAL_URL, redirectURL);
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM.bindFromRequest();
        if (filledForm.hasErrors()) {
            // User did not fill everything properly
            flash(FLASH_ERROR_KEY, "登入電郵或密碼錯誤");
            return badRequest(views.html.login.render(filledForm, isOverDailySignupThreshold()));
        } else {
            // Everything was filled
            return UsernamePasswordAuthProvider.handleLogin(ctx());
        }
    }
    
    @Transactional
    public Result initNewUser() {
        final User localUser = getLocalUser(session());
        if (!User.isLoggedIn(localUser)) {
            logger.underlyingLogger().error(String.format("[u=%d] User not logged in", localUser.id));
            return notFound();
        }
        
        localUser.setNewUser(false);
        
        //String promoCode = session().get(SESSION_PROMOCODE);
        //GameAccountReferral.processAnyReferral(promoCode, user);

        //GameAccount.setPointsForSignUp(user);

        /*
        if (user.hasCompleteInfo()) {
            GameBadgeAwarded.recordGameBadge(user, BadgeType.PROFILE_INFO);
        }
        */
                
        // CalcServer
        calcServer.clearUserQueues(localUser);
        
        // ES
        ElasticSearchController.addUserElasticSearch(localUser);

        // activity
        User beautypopUser = SystemInfo.getInfo().getBeautyPopCustomerCare();
        Activity activity = new Activity(
                ActivityType.TIPS_NEW_USER, 
                localUser.id,
                beautypopUser.id,
                beautypopUser.id,
                "");
        activity.save();
        
        // Sendgrid
        SendgridEmailClient.getInstance().sendMailOnSignup(localUser);
        
        return ok(Json.toJson(new UserVM(localUser)));
    }
    
    public static Result jsRoutes() {
        return ok(Routes.javascriptRouter("jsRoutes", 
                controllers.routes.javascript.Signup.forgotPassword())).as("text/javascript");
    }

    @Transactional
    public static Result doSignup() {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        Form<MySignup> filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM.bindFromRequest();
        
        if (!filledForm.hasErrors() && filledForm.get() != null) {
            String email = filledForm.get().email;
            if (email != null) {
                final User existingUser = User.findByEmail(email);
                if (existingUser != null && existingUser.emailValidated) {
                    List<ValidationError> errors = new ArrayList<>();
                    errors.add(new ValidationError(Signup.EMAIL_EXISTS_ERROR_KEY, Signup.EMAIL_EXISTS_ERROR_MESSAGE));
                    filledForm.errors().put(Signup.EMAIL_EXISTS_ERROR_KEY, errors);
                    logger.underlyingLogger().info("[email="+email+"] already registered");
                }
            }
        }
        
        if (filledForm.hasErrors()) {
            String errorRequired = Messages.get("error.required") + " - ";
            String errorRequiredFields = "";
            String errorOther = "";
            for (Entry<String, List<ValidationError>> errorEntry : filledForm.errors().entrySet()) {
                List<ValidationError> errors = errorEntry.getValue();
                for (ValidationError error : errors) {
                    if ("error.required".equalsIgnoreCase(error.message())) {
                        if ("lname".equalsIgnoreCase(error.key())) {
                            errorRequiredFields += "'姓' ";
                        } else if ("fname".equalsIgnoreCase(error.key())) {
                            errorRequiredFields += "'名' ";
                        } else if ("email".equalsIgnoreCase(error.key())) {
                            errorRequiredFields += "'電郵' ";
                        } else if ("password".equalsIgnoreCase(error.key())) {
                            errorRequiredFields += "'密碼' ";
                        } else if ("repeatPassword".equalsIgnoreCase(error.key())) {
                            errorRequiredFields += "'重複密碼' ";
                        } else {
                            errorRequiredFields += error.key() + " ";
                        }
                    } if ("error.minLength".equalsIgnoreCase(error.message()) ||
                            "error.maxLength".equalsIgnoreCase(error.message())) {
                        if (!errorOther.isEmpty()) {
                            break;
                        }
                        if ("password".equalsIgnoreCase(error.key()) ||
                                "repeatPassword".equalsIgnoreCase(error.key())) {
                            errorOther += "密碼" + String.format(Messages.get(error.message()), error.arguments().get(0));
                        } else {
                            errorOther += error.key() + String.format(Messages.get(error.message()), error.arguments().get(0));
                        }
                    } else {
                        if (!errorOther.isEmpty()) {
                            break;
                        }
                        errorOther += Messages.get(error.message());      // + " - " + error.key();
                    }
                }
            }
            
            if (!errorRequiredFields.isEmpty()) {
                flash().put(controllers.Application.FLASH_ERROR_KEY, errorRequired + errorRequiredFields);
            } else if (!errorOther.isEmpty()) {
                flash().put(controllers.Application.FLASH_ERROR_KEY, errorOther);
            } else {
                flash().put(controllers.Application.FLASH_ERROR_KEY, Messages.get("error.invalid"));
            }
            return badRequest(views.html.signup.render(filledForm));
        } else {
            // Everything was filled
            String email = filledForm.get().email;
            session().put(SIGNUP_EMAIL, email);

            // native signup with promoCode
            String promoCode = session().get(SESSION_PROMOCODE);
            if (promoCode != null) {
               // GameAccountReferral.addNonValidatedReferral(promoCode, email);
            }

            logger.underlyingLogger().info("STS [email="+email+"] Native signup submitted");
            return UsernamePasswordAuthProvider.handleSignup(ctx());
        }
    }
    
    //
    // Mobile
    //

    public static void setMobileUserAgent(User user) {
        if (user.isLoggedIn()) {
            UserAgentUtil userAgentUtil = new UserAgentUtil(request());
            String agentStr = userAgentUtil.getUserAgent();
            if (!StringUtils.isEmpty(agentStr)) {
                user.lastLoginUserAgent = userAgentUtil.getUserAgent().substring(0, Math.min(100, agentStr.length()));
            }
        }
    }
    
    @Transactional
    public static Result apps() {
        return ok(views.html.apps.render(
                APPLICATION_IOS_URL,
                APPLICATION_ANDROID_URL,
                APPLICATION_BASE_URL));
    }
    
    @Transactional
    public static Result getStarted() {
        return ok(views.html.getstarted.render(
                APPLICATION_IOS_URL,
                APPLICATION_ANDROID_URL,
                APPLICATION_BASE_URL));
    }
    
    @Transactional
    public static Result privacy() {
        TermsAndConditions terms = TermsAndConditions.getTermsAndConditions();
        return ok(views.html.privacy.render(terms.privacy));
    }
    
    @Transactional
    public static Result terms() {
        TermsAndConditions terms = TermsAndConditions.getTermsAndConditions();
        return ok(views.html.terms_and_conditions.render(terms.terms));
    }
    
    @Transactional
    public static Result guide() {
        return ok(views.html.guide.render());
    }
    
    public static String formatTimestamp(final long t) {
        return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
    }
    
    //
    // Other APIs
    //
    
    @Transactional
    public static Result getDistricts() {
        List<LocationVM> vms = new ArrayList<>();
        try {
            List<Location> districts = LocationCache.getHongKongDistricts();
            for (Location district : districts) {
                vms.add(new LocationVM(district));
            }
        } catch (Exception e) {
        }
        return ok(Json.toJson(vms));
    }
    
    @Transactional
    public static Result getCountries() {
        List<CountryVM> vms = new ArrayList<>();
        try {
            List<Country> countries = CountryCache.getCountries();
            for (Country country : countries) {
                vms.add(new CountryVM(country));
            }
        } catch (Exception e) {
        }
        return ok(Json.toJson(vms));
    }

    @Transactional
    public static Result getFeaturedItems(String itemType) {
        List<FeaturedItemVM> vms = getFeaturedItemVMs(itemType);
        return ok(Json.toJson(vms));
    }

    public static List<FeaturedItemVM> getFeaturedItemVMs(String itemType) {
        List<FeaturedItemVM> vms = new ArrayList<>();
        try {
            List<FeaturedItem> featuredItems = FeaturedItemCache.getFeaturedItems(ItemType.valueOf(itemType));
            for (FeaturedItem featuredItem : featuredItems) {
                vms.add(new FeaturedItemVM(featuredItem));
            }
        } catch (Exception e) {
        }
        return vms;
    }

    public static Result sendHelloMessage() {
        DynamicForm form = DynamicForm.form().bindFromRequest();
        String senderName = form.get("senderName").trim();
        String senderEmail = form.get("senderEmail").trim();
        String message = form.get("message").trim();
        SendgridEmailClient.getInstance().sendMailOnHelloMessage(senderName, senderEmail, message);
        return ok("success");
    }
    
    //
    // Webmaster
    //
    
    @Transactional
    public static Result googleWebmaster() {
        return ok(views.html.google_webmaster.render());
    }
    
    @Transactional
    public static Result pathNotFound() {
        return redirect("/home");
    }
    
    @Transactional
    public static Result pathNotFound(String path) {
        if (path.contains("/")) {
            return pathNotFound();
        }
        
        logger.underlyingLogger().warn("Path not found - "+path);
        User user = User.findByDisplayName(path);
        if (user != null) {
            logger.underlyingLogger().info(String.format("[u=%d][displayName=%s] Found user to redirect to profile ", user.id, user.displayName));
            return redirect("/seller/"+user.id);
        }
        return pathNotFound();
    }
}
