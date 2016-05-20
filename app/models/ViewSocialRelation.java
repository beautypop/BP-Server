package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import play.db.jpa.JPA;
import domain.DefaultValues;
import domain.SocialObjectType;

@Entity
public class ViewSocialRelation extends SocialRelation {
    private static final play.api.Logger logger = play.api.Logger.apply(ViewSocialRelation.class);
    
	public ViewSocialRelation(){}
	
	public ViewSocialRelation(Long id, SocialObject actor, Integer weight, SocialObject target) {
		super(id, actor, weight, target);
	}
	
	public ViewSocialRelation(SocialObject actor, SocialObject target) {
		super(actor, target);
	}
	
	@Override
	public SocialRelation.Action getAction() {
		return Action.VIEW;
	}
	
	public static Long getUserViewsCount(Long id) {
        Query q = JPA.em().createQuery(
                "Select count(sr) from ViewSocialRelation sr where actor = ?1 and actorType = ?2 and targetType = ?3");
        q.setParameter(1, id);
        q.setParameter(2, SocialObjectType.USER);
        q.setParameter(3, SocialObjectType.POST);
        try {
            Long count = (Long)q.getSingleResult();
            return count;
        } catch (NoResultException e) {
            return 0L;
        }
    }
	
	public static List<ViewSocialRelation> getUserViews(Long id, Long offset) {
        Query q = JPA.em().createQuery(
                "Select sr from ViewSocialRelation sr where actor = ?1 and actorType = ?2 and targetType = ?3 order by CREATED_DATE desc");
        q.setParameter(1, id);
        q.setParameter(2, SocialObjectType.USER);
        q.setParameter(3, SocialObjectType.POST);
        try {
            q.setFirstResult((int) (offset * DefaultValues.DEFAULT_INFINITE_SCROLL_COUNT));
            q.setMaxResults(DefaultValues.DEFAULT_INFINITE_SCROLL_COUNT);
            return (List<ViewSocialRelation>) q.getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }
}
