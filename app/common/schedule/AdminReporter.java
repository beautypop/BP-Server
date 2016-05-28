package common.schedule;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import controllers.Application;
import email.SendgridEmailClient;
import models.Post;
import models.User;
import play.Play;
import play.mvc.Controller;
import viewmodel.PostVMLite;
import viewmodel.UserVMLite;

public class AdminReporter extends Controller {
    private static final play.api.Logger logger = play.api.Logger.apply(AdminReporter.class);

    public static final String ADMIN_REPORTS_SUBJECT = Play.application().configuration().getString("admin.reports.subject");
    public static final String ADMIN_REPORTS_RECIPIENTS = Play.application().configuration().getString("admin.reports.recipients");
    public static final String ADMIN_REPORTS_SKIP_USER_EMAIL_PREFIXES = Play.application().configuration().getString("admin.reports.skip.user.email.prefixes");
    
    public static void runReports() {
        String body = "";
        
        List<UserVMLite> users = getNewUsersToday();
        
        logger.underlyingLogger().info(String.format("getNewUsersToday=%d", users.size()));
        
        users = getLoginUsersToday();
        
        logger.underlyingLogger().info(String.format("getLoginUsersToday=%d", users.size()));
        
        List<PostVMLite> posts = getNewProductsToday();
        
        logger.underlyingLogger().info(String.format("getNewProductsToday=%d", posts.size()));
        
        SendgridEmailClient.getInstance().sendMail(ADMIN_REPORTS_RECIPIENTS, ADMIN_REPORTS_SUBJECT, body);
    }
    
    private static List<UserVMLite> getNewUsersToday() {
        final User localUser = Application.getLocalUser(session());
        if (!localUser.isLoggedIn()) {
            logger.underlyingLogger().error(String.format("[u=%d] User not logged in", localUser.id));
            return null;
        }
        
        if (!localUser.isSuperAdmin()) {
            logger.underlyingLogger().error(String.format("[u=%d] User is not super admin. Failed to get new users today !!", localUser.id));
            return null;
        }
        
        DateTime daysBefore = (new DateTime()).minusDays(1);
        List<User> users = User.getUsersBySignup(daysBefore);
        List<UserVMLite> vms = new ArrayList<>();
        for (User user : users) {
            if (user != null) {
                UserVMLite vm = new UserVMLite(user, localUser);
                vms.add(vm);
            }
        }
        return vms;
    }
    
    private static List<UserVMLite> getLoginUsersToday() {
        final User localUser = Application.getLocalUser(session());
        if (!localUser.isLoggedIn()) {
            logger.underlyingLogger().error(String.format("[u=%d] User not logged in", localUser.id));
            return null;
        }
        
        if (!localUser.isSuperAdmin()) {
            logger.underlyingLogger().error(String.format("[u=%d] User is not super admin. Failed to get login users today !!", localUser.id));
            return null;
        }
        
        DateTime daysBefore = (new DateTime()).minusDays(1);
        List<User> users = User.getUsersByLogin(daysBefore);
        List<UserVMLite> vms = new ArrayList<>();
        for (User user : users) {
            if (user != null) {
                UserVMLite vm = new UserVMLite(user, localUser);
                vms.add(vm);
            }
        }
        return vms;
    }
    
    private static List<PostVMLite> getNewProductsToday() {
        final User localUser = Application.getLocalUser(session());
        if (!localUser.isLoggedIn()) {
            logger.underlyingLogger().error(String.format("[u=%d] User not logged in", localUser.id));
            return null;
        }
        
        if (!localUser.isSuperAdmin()) {
            logger.underlyingLogger().error(String.format("[u=%d] User is not super admin. Failed to get new products today !!", localUser.id));
            return null;
        }
        
        DateTime daysBefore = (new DateTime()).minusDays(1);
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