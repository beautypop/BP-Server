package models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;
import domain.Creatable;
import domain.Updatable;

@Entity
public class Review extends domain.Entity implements Serializable, Creatable, Updatable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@Required
	@OneToOne
	public ConversationOrder conversationOrder;

	@Required
    @ManyToOne
    public User seller;
	
	public Double sellerScore = 0.0;
	   
	@Column(length=2000)
	public String sellerReview;

	@Temporal(TemporalType.TIMESTAMP)
    public Date sellerReviewDate;

	@Required
	@ManyToOne
    public User buyer;
	
	public Double buyerScore = 0.0;
	
	@Column(length=2000)
	public String buyerReview;
	
	@Temporal(TemporalType.TIMESTAMP)
    public Date buyerReviewDate;
    
	public Review() {
	}
	
	public Review(ConversationOrder conversationOrder) {
		this.conversationOrder = conversationOrder;
		this.buyer = conversationOrder.user1;
		this.seller = conversationOrder.user2;
	}

	@SuppressWarnings("unchecked")
	public static List<Review> getBuyerReviewsFor(Long userId) {
		try {
            Query q = JPA.em().createQuery("SELECT r FROM Review r where seller.id = ?1 and buyerReviewDate is not null");
            q.setParameter(1, userId);
            return q.getResultList();
        } catch (NoResultException nre) {
            return null;
        }
	}
	
	@SuppressWarnings("unchecked")
	public static List<Review> getSellerReviewsFor(Long userId) {
		try {
            Query q = JPA.em().createQuery("SELECT r FROM Review r where buyer.id = ?1 and sellerReviewDate is not null");
            q.setParameter(1, userId);
            return q.getResultList();
        } catch (NoResultException nre) {
            return null;
        }
	}
	
    public static Review findById(Long id) {
        try { 
            Query q = JPA.em().createQuery("SELECT r FROM Review r where id = ?1");
            q.setParameter(1, id);
            return (Review) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

	public static Review getByConversationOrderId(Long conversationOrderId) {
		try { 
            Query q = JPA.em().createQuery("SELECT r FROM Review r where conversationOrder.id = ?1");
            q.setParameter(1, conversationOrderId);
            return (Review) q.getSingleResult();
        } catch (NoResultException e) {
        	return null;
        }
	}
}
