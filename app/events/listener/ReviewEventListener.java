package events.listener;

import notification.PushNotificationSender;
import models.Activity;
import models.Review;
import models.User;
import models.Activity.ActivityType;
import events.map.ReviewEvent;

import com.google.common.eventbus.Subscribe;

import common.thread.TransactionalRunnableTask;
import common.utils.StringUtil;

public class ReviewEventListener extends EventListener {
    private static final play.api.Logger logger = play.api.Logger.apply(ReviewEventListener.class);
    
	@Subscribe
    public void recordReviewEvent(ReviewEvent map){
	    try {
    		final User localUser = (User) map.get("user");
    		final Review review = (Review) map.get("review");

    		final boolean isBuyer = review.buyer != null && review.buyer.id == localUser.id;
    		
    		// activity / notif to seller
    		final Long postId = review.conversationOrder.conversation.post.id;
    		final Long postImageId = review.conversationOrder.conversation.post.getImage();
            executeAsync(
                    new TransactionalRunnableTask() {
                        @Override
                        public void execute() {
                            
                            if (isBuyer) {
                                // activity
                                Activity activity = new Activity(
                                        ActivityType.BUYER_REVIEW, 
                                        review.seller.id,
                                        false, 
                                        review.buyer.id,
                                        review.buyer.id,
                                        review.buyer.displayName,
                                        postId,
                                        postImageId,
                                        StringUtil.shortMessage(review.buyerReview));
                                activity.ensureUniqueAndCreate();
                                
                                // Push notification
                                PushNotificationSender.sendNewBuyerReviewNotification(
                                        review.seller.id, 
                                        review.buyer.displayName,
                                        review.buyerReview);
                            } else {
                                // activity
                                Activity activity = new Activity(
                                        ActivityType.SELLER_REVIEW, 
                                        review.buyer.id,
                                        false, 
                                        review.seller.id,
                                        review.seller.id,
                                        review.seller.displayName,
                                        postId,
                                        postImageId,
                                        StringUtil.shortMessage(review.sellerReview));
                                activity.ensureUniqueAndCreate();
                                
                                // Push notification
                                PushNotificationSender.sendNewSellerReviewNotification(
                                        review.buyer.id, 
                                        review.seller.displayName,
                                        review.sellerReview);
                            }
                        }
                    });
    	} catch(Exception e) {
            logger.underlyingLogger().error(e.getMessage(), e);
        }
    }
}
