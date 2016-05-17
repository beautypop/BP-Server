package viewmodel;

import models.Review;
import models.User;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReviewVM {
	@JsonProperty("id") private Long id;
	@JsonProperty("userId") private Long userId;
	@JsonProperty("userName") private String userName;
	@JsonProperty("postId") private Long postId;
	@JsonProperty("postImageId") private Long postImageId;
	@JsonProperty("reviewDate") public Long reviewDate;
	@JsonProperty("review") private String review;
	@JsonProperty("score") private Double score;

	public ReviewVM(Review review, boolean isBuyer) {
		this.id = review.id;
		this.postId = review.conversationOrder.conversation.post.id;
		this.postImageId = review.conversationOrder.conversation.post.getImage();
		
		if (isBuyer) {
		    this.userId = review.buyer.id;
	        this.userName = review.buyer.displayName;
		    this.reviewDate = review.buyerReviewDate.getTime();
            this.review = review.buyerReview;
            this.score = review.buyerScore;
        } else {
            this.userId = review.seller.id;
            this.userName = review.seller.displayName;
            this.reviewDate = review.sellerReviewDate.getTime();
            this.score = review.sellerScore;
            this.review = review.sellerReview;
        }
	}

	public ReviewVM(Review review, User localUser) {
		this.id = review.id;
		this.userId = localUser.id;
		this.userName = localUser.displayName;
		this.postId = review.conversationOrder.conversation.post.id;
		this.postImageId = review.conversationOrder.conversation.post.getImage();
		
		if (localUser.getId() == review.buyer.getId()) {
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
