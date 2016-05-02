package viewmodel;

import models.Conversation;
import models.Review;
import models.User;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReviewVM {
	private static final play.api.Logger logger = play.api.Logger.apply(ReviewVM.class);

	@JsonProperty("id") private Long id;
	@JsonProperty("userId") private Long userId;
	@JsonProperty("userName") private String userName;
	@JsonProperty("postId") private Long postId;
	@JsonProperty("review") private String review;
	@JsonProperty("score") private Double score;

	public ReviewVM(Review review, User localUser, boolean forSeller) {
		User otherUser = review.conversationOrder.conversation.otherUser(localUser);
		this.id = review.id;
		this.userId = otherUser.id;
		this.userName = otherUser.displayName;
		this.postId = review.conversationOrder.conversation.post.id;
		if(forSeller){
			this.score = review.sellerScore;
			this.review = review.sellerReview;
		} else {
			this.score = review.buyerScore;
			this.review = review.buyerReview;
		}
	}

	public ReviewVM(Review review, User localUser) {
		this.id = review.id;
		this.userId = localUser.id;
		this.userName = localUser.displayName;
		this.postId = review.conversationOrder.conversation.post.id;
		if(localUser.getId() == review.conversationOrder.user1.getId()){
			this.review = review.buyerReview;
			this.score = review.buyerScore;
		} else {
			this.score = review.sellerScore;
			this.review = review.sellerReview;
		}
			
	}

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}
}
