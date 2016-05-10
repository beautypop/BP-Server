package viewmodel;

import models.Review;
import models.User;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReviewVM {
	@JsonProperty("id") private Long id;
	@JsonProperty("userId") private Long userId;
	@JsonProperty("userName") private String userName;
	@JsonProperty("postId") private Long postId;
	@JsonProperty("reviewDate") public Long reviewDate;
	@JsonProperty("review") private String review;
	@JsonProperty("score") private Double score;

	public ReviewVM(Review review, User localUser, boolean buyer) {
		User otherUser = review.conversationOrder.conversation.otherUser(localUser);
		this.id = review.id;
		this.userId = otherUser.id;
		this.userName = otherUser.displayName;
		this.postId = review.conversationOrder.conversation.post.id;
		
		if (buyer) {
		    this.reviewDate = review.buyerReviewDate.getTime();
            this.review = review.buyerReview;
            this.score = review.buyerScore;
		} else {
		    this.reviewDate = review.sellerReviewDate.getTime();
            this.review = review.sellerReview;
            this.score = review.sellerScore;
		}
	}

	public ReviewVM(Review review, User localUser) {
		this.id = review.id;
		this.userId = localUser.id;
		this.userName = localUser.displayName;
		this.postId = review.conversationOrder.conversation.post.id;
		
		if (localUser.getId() == review.conversationOrder.user1.getId()) {
		    this.reviewDate = review.buyerReviewDate.getTime();
			this.review = review.buyerReview;
			this.score = review.buyerScore;
		} else {
		    this.reviewDate = review.sellerReviewDate.getTime();
			this.score = review.sellerScore;
			this.review = review.sellerReview;
		}
	}
}
