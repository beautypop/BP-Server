package events.listener;

import notification.PushNotificationSender;
import models.Message;
import models.User;
import events.map.MessageEvent;

import com.google.common.eventbus.Subscribe;

import common.thread.TransactionalRunnableTask;
import email.SendgridEmailClient;

public class MessageEventListener extends EventListener {
    private static final play.api.Logger logger = play.api.Logger.apply(MessageEventListener.class);
    
	@Subscribe
    public void recordMessageEvent(MessageEvent map){
	    try {
    	    final Message message = (Message) map.get("message");
    	    final User sender = (User) map.get("sender");
    	    final User recipient = (User) map.get("recipient");
    	    final Boolean notify = (Boolean) map.get("notify");
    	    
    	    executeAsync(
                    new TransactionalRunnableTask() {
                        @Override
                        public void execute() {
                            if (notify) {
                                // Sendgrid
                                SendgridEmailClient.getInstance().sendMailOnConversation(
                                        sender, recipient, message.conversation.post.title, message.body);
                            }
                            
                            // Push notification
                            PushNotificationSender.sendNewMessageNotification(
                                    recipient.id, 
                                    sender.displayName,
                                    message.body);
                        }
                    });
    	} catch(Exception e) {
            logger.underlyingLogger().error(e.getMessage(), e);
        }
    }
}	
