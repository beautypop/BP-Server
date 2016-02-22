package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.db.jpa.JPA;
import play.db.jpa.Transactional;

/**
 * 
 */
@Entity
public class SystemInfo {
    private static final play.api.Logger logger = play.api.Logger.apply(SystemInfo.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    
    @Temporal(TemporalType.TIMESTAMP)
    public Date serverStartTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    public Date serverRunTime;
    
	public String androidVersion = "1";
	
	public String iosVersion = "1";
	
	@Column(nullable = false, columnDefinition = "TINYINT(1)")
	public boolean versionUpdated = false;
	
	public Long beautypopAdmin = 1L;
	
	public Long beautypopSeller = 1L;
	
	public Long beautypopCustomerCare = 1L;
	
	private static SystemInfo systemInfo;
	
	private static User BP_ADMIN;
	private static User BP_SELLER;
	private static User BP_CUSTOMER_CARE;
	
	public User getBeautyPopAdmin() {
	    if (BP_ADMIN == null) {
	        BP_ADMIN = User.findById(beautypopAdmin);
	    }
	    return BP_ADMIN;
	}
	
    public User getBeautyPopSeller() {
        if (BP_SELLER == null) {
            BP_SELLER = User.findById(beautypopSeller);
        }
        return BP_SELLER;
    }

    public User getBeautyPopCustomerCare() {
        if (BP_CUSTOMER_CARE == null) {
            BP_CUSTOMER_CARE = User.findById(beautypopCustomerCare);
        }
        return BP_CUSTOMER_CARE;
    }
	   
	public SystemInfo() {
	}

	public static void checkVersionUpdated() {
	    // nullify systemInfo and refresh in next call
        if (systemInfo != null) {
            synchronized(systemInfo) {
                if (isVersionUpdated()) {
                    systemInfo.versionUpdated = false;
                    systemInfo.save();
                    systemInfo = null;
                    logger.underlyingLogger().debug("refreshVersion versionUpdate");
                }
            }
        }    
	}
	
	public static SystemInfo getInfo() {
	    if (systemInfo != null) {
	        return systemInfo;
	    }
	    
        try {
            Query q = JPA.em().createQuery("SELECT s FROM SystemInfo s");
            q.setMaxResults(1);
            
            if (q.getMaxResults() > 1) {
                logger.underlyingLogger().error(q.getMaxResults()+" SystemInfo exists!!");
            }
            
            systemInfo = (SystemInfo) q.getSingleResult();
            logger.underlyingLogger().debug("getInfo() \n"+systemInfo.toString());
            return systemInfo;
        } catch (NoResultException e) {
            return null;
        }
    }
	
	public static Boolean isVersionUpdated() {
        try {
            Query q = JPA.em().createQuery("SELECT s FROM SystemInfo s order by id desc");
            q.setMaxResults(1);
            
            if (q.getMaxResults() > 1) {
                logger.underlyingLogger().error(q.getMaxResults()+" SystemInfo exists!!");
            }
            
            systemInfo = (SystemInfo) q.getSingleResult();
            return systemInfo.versionUpdated;
        } catch (NoResultException e) {
            return null;
        }
    }
	
	@Transactional
	public static void recordServerStartTime() {
	    SystemInfo info = getInfo();
	    if (info != null) {
	        info.serverStartTime = new Date();
	        info.save();
	    }
	}
	
	@Transactional
	public static void recordServerRunTime() {
        SystemInfo info = getInfo();
        if (info != null) {
            info.serverRunTime = new Date();
            info.save();
        }
    }
	
	@Transactional
    public void save() {
        JPA.em().persist(this);
        JPA.em().flush();     
    }
      
    @Transactional
    public void delete() {
        JPA.em().remove(this);
    }
    
    @Transactional
    public void merge() {
        JPA.em().merge(this);
    }
    
    @Transactional
    public void refresh() {
        JPA.em().refresh(this);
    }
    
    @Override
    public String toString() {
        return "serverStartTime="+serverStartTime+"\n"+
                "serverRunTime="+serverRunTime+"\n"+
                "androidVersion="+androidVersion+"\n"+
                "iosVersion="+iosVersion+"\n"+
                "versionUpdated="+versionUpdated+"\n"+
                "beautypopAdmin="+beautypopAdmin+"\n"+
                "beautypopSeller="+beautypopSeller+"\n"+
                "beautypopCustomerCare="+beautypopCustomerCare;
    }
}
