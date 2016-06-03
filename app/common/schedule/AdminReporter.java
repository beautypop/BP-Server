package common.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import common.utils.DateTimeUtil;
import common.utils.HtmlUtil;
import common.utils.StringUtil;
import common.utils.UrlUtil;
import email.SendgridEmailClient;
import models.Post;
import models.User;
import play.Play;
import play.mvc.Controller;
import viewmodel.PostVMLite;
import viewmodel.UserVMLite;

public class AdminReporter extends Controller {
    private static final play.api.Logger logger = play.api.Logger.apply(AdminReporter.class);

    public static final int ADMIN_REPORTS_RUN_DAYS_AGO = Play.application().configuration().getInt("admin.reports.run.days.ago");
    public static final String ADMIN_REPORTS_SUBJECT = Play.application().configuration().getString("admin.reports.subject");
    public static final String ADMIN_REPORTS_RECIPIENTS = Play.application().configuration().getString("admin.reports.recipients");
    public static final String ADMIN_REPORTS_SKIP_USER_EMAIL_PREFIXES = Play.application().configuration().getString("admin.reports.skip.user.email.prefixes");
    
    private static List<String> skipUserEmailPrefixes = StringUtil.parseValues(ADMIN_REPORTS_SKIP_USER_EMAIL_PREFIXES);
    
    public static void runReports() {
        runNewUsersTodayReport();
        runLoginUsersTodayReport();
        runNewProductsTodayReport();
    }
    
    private static String printHeader(String title) {
        String header = "";
        header += "-----" + HtmlUtil.appendBr();
        header += title;
        header += "-----" + HtmlUtil.appendBr();
        header += HtmlUtil.appendBr();
        header += HtmlUtil.appendBr();
        return header;
    }
    
    private static void runNewUsersTodayReport() {
        String subject = ADMIN_REPORTS_SUBJECT + DateTimeUtil.toNamePart(new Date()) + " NEW USERS";
        String header = "";
        String body = "";
        
        List<UserVMLite> users = getNewUsersToday();
        
        int total = 0;
        for (UserVMLite user : users) {
            if (!StringUtils.isEmpty(user.email) && 
                    StringUtil.startsWithPrefixes(user.email, skipUserEmailPrefixes)) {
                continue;
            }
            
            total++;
            
            String t = HtmlUtil.convertNewlineToHtml(user.shortInfo()) + HtmlUtil.appendBr();
            t += UrlUtil.createSellerUrl(user) + HtmlUtil.appendBr();
            t += HtmlUtil.appendBr();
            body += t + HtmlUtil.appendBr();
        }
        logger.underlyingLogger().info(String.format("runNewUsersTodayReport users=%d", users.size()));    

        // header
        header += "NEW USERS=" + total + HtmlUtil.appendBr();
        
        body = printHeader(header) + HtmlUtil.appendP(body);
        String response = SendgridEmailClient.getInstance().sendMail(ADMIN_REPORTS_RECIPIENTS, subject, body);
        logger.underlyingLogger().info("send mail response: "+response);
    }
    
    private static void runLoginUsersTodayReport() {
        String subject = ADMIN_REPORTS_SUBJECT + DateTimeUtil.toNamePart(new Date()) + " LOGIN USERS";
        String header = "";
        String body = "";
        
        List<UserVMLite> users = getLoginUsersToday();
        
        int total = 0, ios = 0, android = 0, buyers = 0, sellers = 0;
        for (UserVMLite user : users) {
            if (!StringUtils.isEmpty(user.email) &&
                    StringUtil.startsWithPrefixes(user.email, skipUserEmailPrefixes)) {
                continue;
            }
            
            // skip new signups
            if (DateTimeUtil.withinADay(user.createdDate, user.lastLogin)) {
                continue;
            }
            
            total++;
            
            // ios / android
            if (user.lastLoginUserAgent != null && 
                    user.lastLoginUserAgent.toLowerCase().startsWith("ios")) {
                ios++;
            } else if (user.lastLoginUserAgent != null && 
                    user.lastLoginUserAgent.toLowerCase().startsWith("android")) {
                android++;
            } 

            // buyer / seller
            if (user.numProducts > 0) {
                sellers++;
            } else {
                buyers++;
            }
            
            String t = HtmlUtil.convertNewlineToHtml(user.shortInfo()) + HtmlUtil.appendBr();
            t += UrlUtil.createSellerUrl(user) + HtmlUtil.appendBr();
            t += HtmlUtil.appendBr();
            body += t + HtmlUtil.appendBr();
        }
        logger.underlyingLogger().info(String.format("runLoginUsersTodayReport users=%d", users.size()));    
        
        // header
        header += "TOTAL LOGIN USERS=" + total + HtmlUtil.appendBr();
        header += "iOS=" + ios + " | Android=" + android + HtmlUtil.appendBr();
        header += "Buyers=" + buyers + " | Sellers=" + sellers + HtmlUtil.appendBr();
        
        body = printHeader(header) + HtmlUtil.appendP(body);
        String response = SendgridEmailClient.getInstance().sendMail(ADMIN_REPORTS_RECIPIENTS, subject, body);
        logger.underlyingLogger().info("send mail response: "+response);
    }
    
    private static void runNewProductsTodayReport() {
        String subject = ADMIN_REPORTS_SUBJECT + DateTimeUtil.toNamePart(new Date()) + " NEW PRODUCTS";
        String header = "";
        String body = "";
        
        List<PostVMLite> posts = getNewProductsToday();
        
        int total = 0;
        for (PostVMLite post : posts) {
            total++;
            String t = HtmlUtil.appendImage(UrlUtil.POST_IMAGE_BY_ID_URL+post.images[0], 150, 150) + HtmlUtil.appendBr();
            t += HtmlUtil.convertNewlineToHtml(post.shortInfo()) + HtmlUtil.appendBr();
            t += UrlUtil.createProductUrl(post) + HtmlUtil.appendBr();
            t += HtmlUtil.appendBr();
            body += t + HtmlUtil.appendBr();
        }
        logger.underlyingLogger().info(String.format("runLoginUsersTodayReport users=%d", posts.size()));    
        
        // header
        header += "NEW PRODUCTS=" + total + HtmlUtil.appendBr();
        
        body = printHeader(header) + HtmlUtil.appendP(body);
        String response = SendgridEmailClient.getInstance().sendMail(ADMIN_REPORTS_RECIPIENTS, subject, body);
        logger.underlyingLogger().info("send mail response: "+response);
    }
    
    private static List<UserVMLite> getNewUsersToday() {
        DateTime daysBefore = (new DateTime()).minusDays(ADMIN_REPORTS_RUN_DAYS_AGO);
        List<User> users = User.getUsersBySignup(daysBefore);
        List<UserVMLite> vms = new ArrayList<>();
        for (User user : users) {
            if (user != null && !user.isNewUser() && !StringUtils.isEmpty(user.displayName)) {
                UserVMLite vm = new UserVMLite(user);
                vms.add(vm);
            }
        }
        return vms;
    }
    
    private static List<UserVMLite> getLoginUsersToday() {
        DateTime daysBefore = (new DateTime()).minusDays(ADMIN_REPORTS_RUN_DAYS_AGO);
        List<User> users = User.getUsersByLogin(daysBefore);
        List<UserVMLite> vms = new ArrayList<>();
        for (User user : users) {
            if (user != null && !user.isNewUser() && !StringUtils.isEmpty(user.displayName)) {
                UserVMLite vm = new UserVMLite(user);
                vms.add(vm);
            }
        }
        return vms;
    }
    
    private static List<PostVMLite> getNewProductsToday() {
        DateTime daysBefore = (new DateTime()).minusDays(ADMIN_REPORTS_RUN_DAYS_AGO);
        List<Post> posts = Post.getPostsByCreatedDate(daysBefore);
        List<PostVMLite> vms = new ArrayList<>();
        for (Post post : posts) {
            if (post != null) {
                PostVMLite vm = new PostVMLite(post);
                vms.add(vm);
            }
        }
        return vms;
    }
}