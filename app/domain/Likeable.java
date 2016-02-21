package domain;

import beautypop.shopping.social.exception.SocialObjectNotLikableException;
import models.User;

public interface Likeable {
	public abstract boolean onLikedBy(User user) throws SocialObjectNotLikableException;
	public abstract boolean onUnlikedBy(User user) throws SocialObjectNotLikableException;
}
