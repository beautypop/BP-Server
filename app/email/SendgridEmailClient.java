package email;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

import models.Post;
import models.User;
import play.Logger;
import play.Play;

import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;

import common.schedule.JobScheduler;
import common.utils.HtmlUtil;
import common.utils.StringUtil;
import common.utils.UrlUtil;
import controllers.Application;

public class SendgridEmailClient implements TransactionalEmailClient {
    private static final play.api.Logger logger = play.api.Logger.apply(SendgridEmailClient.class);
    
    public static final String SENDGRID_MAIL_FROM_NAME = 
            Play.application().configuration().getString("sendgrid.mail.from.name");
    
    public static final String SENDGRID_MAIL_FROM_ADDRESS = 
            Play.application().configuration().getString("sendgrid.mail.from.address");
    
    public static final String SENDGRID_AUTHEN_USERNAME = 
            Play.application().configuration().getString("sendgrid.authen.username");
    
    public static final String SENDGRID_AUTHEN_PASSWORD = 
            Play.application().configuration().getString("sendgrid.authen.password");
    
    public static final String WELCOME_IMAGE_URL = Application.APPLICATION_BASE_URL + "/image/static/welcome.jpg";
    
    private SendGrid sendgrid;
    
	private static SendgridEmailClient client = new SendgridEmailClient();
	
	public static SendgridEmailClient getInstance(){
		return client;
	}
	
	private SendgridEmailClient() {
	    sendgrid = new SendGrid(SENDGRID_AUTHEN_USERNAME, SENDGRID_AUTHEN_PASSWORD);
	}
	
	public void sendMailAsync(final String to, final String from, final String fromName, final String subject, final String body) {
	    JobScheduler.getInstance().run(
	            new Runnable() {
		            @Override
		            public void run() {
		                sendMail(to, from, fromName, subject, body);
		            }
		        });
	}
	
	public String sendMail(String to, String subject, String body) {
	    return sendMail(to, SENDGRID_MAIL_FROM_ADDRESS, SENDGRID_MAIL_FROM_NAME, subject, body);
	}
	
	@Override
	public String sendMail(String to, String from, String fromName, String subject, String body) {
	    if (!Application.isProd()) {
	        logger.underlyingLogger().info("[email="+to+"][isProd=false] sendMail skipped... body="+body);
	        return "";
	    }
	    
	    SendGrid.Email email = new SendGrid.Email();
	    email.setFrom(from);
	    email.setFromName(fromName);
	    email.setSubject(subject);
	    email.setHtml(body);
	    
	    for (String toEmail : StringUtil.parseValues(to)) {
	        email.addTo(toEmail);
	    }
	    
	    try {
	        SendGrid.Response response = sendgrid.send(email);
	        logger.underlyingLogger().info("[email="+to+"] sendMail response="+response+" body="+body);
	        return response.getMessage();
	    } catch (SendGridException e) {
	        logger.underlyingLogger().error("[email="+to+"] Failed to sendMail body="+body+" error="+e.getMessage(), e);
	        return e.getMessage();
	    }
	}
	
	public void sendMailOnSignup(User target) {
        if (StringUtils.isEmpty(target.email)) {
            logger.underlyingLogger().warn("[recipient="+target.displayName+"] sendMailOnSignup recipient email is null");
            return;
        }
        
        String htmlBody = HtmlUtil.appendImage(WELCOME_IMAGE_URL, 500, 320);
        
        String template = getEmailTemplate(
                "views.html.account.email.sendgrid.welcome_mail",
                "",
                target.displayName,
                "",
                htmlBody);
        
        sendMail(
                getEmailName(target.email, target.displayName), 
                SENDGRID_MAIL_FROM_ADDRESS, 
                SENDGRID_MAIL_FROM_NAME,
                formatSubject(target.displayName+" 歡迎你加入 BeautyPop! Welcome!!"), 
                template);
    }
	
	public void sendMailOnFollow(User actor, User target) {
	    if (StringUtils.isEmpty(target.email)) {
            logger.underlyingLogger().warn("[recipient="+target.displayName+"] sendMailOnFollow recipient email is null");
            return;
        }
	    
		String template = getEmailTemplate(
				"views.html.account.email.sendgrid.follow_mail",
				actor.displayName,
				target.displayName);
		
		sendMail(
		        getEmailName(target.email, target.displayName), 
		        SENDGRID_MAIL_FROM_ADDRESS, 
		        SENDGRID_MAIL_FROM_NAME,
		        formatSubject("有人關注了你"), 
		        template);
	}
	
	public void sendMailOnComment(User actor, User target, String product, String comment) {
	    if (StringUtils.isEmpty(product) || StringUtils.isEmpty(comment)) {
            return;
        }
	    
        if (StringUtils.isEmpty(target.email)) {
            logger.underlyingLogger().warn("[recipient="+target.displayName+"] sendMailOnComment recipient email is null");
            return;
        }
        
        String template = getEmailTemplate(
                "views.html.account.email.sendgrid.comment_mail",
                actor.displayName,
                target.displayName,
                product,
                comment);
        
        sendMail(
                getEmailName(target.email, target.displayName), 
                SENDGRID_MAIL_FROM_ADDRESS, 
                actor.displayName+" - "+SENDGRID_MAIL_FROM_NAME, 
                formatSubject("你的商品 "+product+" 有新留言"), 
                template);
    }
	
	public void sendMailOnConversation(User actor, User target, String product, String message) {
	    if (StringUtils.isEmpty(product) || StringUtils.isEmpty(message)) {
            return;
        }
	    
        if (StringUtils.isEmpty(target.email)) {
            logger.underlyingLogger().warn("[recipient="+target.displayName+"] sendMailOnConversation recipient email is null");
            return;
        }
        
        String template = getEmailTemplate(
                "views.html.account.email.sendgrid.conversation_mail",
                actor.displayName,
                target.displayName,
                product,
                message);
        
        sendMail(
                getEmailName(target.email, target.displayName), 
                SENDGRID_MAIL_FROM_ADDRESS, 
                actor.displayName+" - "+SENDGRID_MAIL_FROM_NAME, 
                formatSubject("你的商品 "+product+" 有新訊息"), 
                template);
    }
	
	public void sendMailOnPost(Post post) {
	    User target = post.owner;
        if (StringUtils.isEmpty(target.email)) {
            logger.underlyingLogger().warn("[recipient="+target.displayName+"] sendMailOnPost recipient email is null");
            return;
        }
        
        String htmlBody = 
                HtmlUtil.appendImage(UrlUtil.POST_IMAGE_BY_ID_URL+post.getImage(), 150, 150)+HtmlUtil.appendBr()+ 
                post.title+HtmlUtil.appendBr()+HtmlUtil.appendBr()+ 
                "價格: $" + post.price.longValue();
        
        String template = getEmailTemplate(
                "views.html.account.email.sendgrid.post_mail",
                post.owner.displayName,
                target.displayName,
                post.title,
                htmlBody);
        
        sendMail(
                getEmailName(target.email, target.displayName),
                SENDGRID_MAIL_FROM_ADDRESS, 
                SENDGRID_MAIL_FROM_NAME, 
                formatSubject("成功刊登商品 - "+post.title), 
                template);
    }
	
	public void sendMailOnHelloMessage(String senderName, String senderEmail, String message) {
        if (StringUtils.isEmpty(senderName) || StringUtils.isEmpty(senderEmail) || StringUtils.isEmpty(message)) {
            return;
        }
        
        String template = getEmailTemplate(
                "views.html.account.email.sendgrid.hello_message_mail",
                senderName,
                senderEmail,
                "",
                message);
        
        sendMail(
                SENDGRID_MAIL_FROM_ADDRESS,     // to
                SENDGRID_MAIL_FROM_ADDRESS,     // from
                SENDGRID_MAIL_FROM_NAME, 
                formatSubject("Hello message from - "+senderName), 
                template);
    }
	
	protected String getEmailTemplate(final String template, final String actor, final String target) {
	    return getEmailTemplate(template, actor, target, "", "");
	}
	
	protected String getEmailTemplate(final String template, final String actor, final String target, final String title) {
        return getEmailTemplate(template, actor, target, title, "");
    }
	
	protected String getEmailTemplate(final String template, 
	        final String actor, final String target, final String title, final String body) {
	    
		Class<?> cls = null;
		String ret = null;
		try {
			cls = Class.forName(template);
		} catch (ClassNotFoundException e) {
			Logger.warn("Template: '"+ template + "' was not found!");
		}
		
		if (cls != null) {
			Method htmlRender = null;
			try {
				htmlRender = cls.getMethod("render",String.class,String.class,String.class,String.class);
				ret = htmlRender.invoke(null, actor, target, title, body).toString();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	protected String formatSubject(String subject) {
	    return subject;
	}
	
	/**
	 * https://github.com/joscha/play-easymail/blob/master/code/app/com/feth/play/module/mail/Mailer.java
	 * 
	 * @param email
	 * @param name
	 * @return
	 */
	public static String getEmailName(final String email, final String name) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("email must not be null");
        }
        final StringBuilder sb = new StringBuilder();
        final boolean hasName = name != null && !name.trim().isEmpty();
        if (hasName) {
            sb.append("\"");
            sb.append(name);
            sb.append("\" <");
        }

        sb.append(email);

        if (hasName) {
            sb.append(">");
        }

        return sb.toString();
    }
}
