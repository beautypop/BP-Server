package domain;

import common.social.exception.SocialObjectNotPostableException;
import models.SocialObject;
import models.User;

public interface Postable {
	public abstract SocialObject onPost(User user, String title, String body) throws SocialObjectNotPostableException;
}
