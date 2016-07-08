package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.Query;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

import common.cache.CategoryCache;
import domain.Likeable;
import domain.Postable;
import domain.SocialObjectType;
import play.db.jpa.JPA;

/**
 * insert into category (CREATED_DATE,deleted,name,objectType,system,categoryType,description,icon,seq,owner_id) values (now(),0,'Fashion','CATEGORY',1,'PUBLIC','Fashion','http://www.beautypop.hk/image/static/category/fashion.jpg',1,1);
 */
@Entity
public class Category extends SocialObject implements Likeable, Postable, Comparable<Category> {

    public String icon;

    @Column(length=2000)
    public String description;
    
    @Enumerated(EnumType.STRING)
    public CategoryType categoryType;
    
    @ManyToOne
    public Category parent;
    
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    public boolean featured = false;
    
    public int seq;

    public int minPercentFeedExposure = 0;
    
    public int maxPercentFeedExposure = 100;
    
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JsonIgnore
    public Folder albumPhotoProfile;
    
    @OneToMany(cascade = CascadeType.REMOVE)
    public List<Folder> folders;
    
    public String customJob;
    
    public String customJobData;
    
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    public boolean run = false;
    
    public static enum CategoryType {
        PUBLIC,
        CUSTOM,
        THEME,
        TREND
    }
    
    public Category() {
        this.objectType = SocialObjectType.CATEGORY;
    }
    
    public Category(String name, String description, User owner, String icon, int seq) {
        this(name, description, owner, icon, seq, null);
    }
    
    public Category(String name, String description, User owner, String icon, int seq, Category parent) {
        this(CategoryType.PUBLIC, name, description, owner, icon, seq, parent);
    }

    public Category(CategoryType categoryType, String name, String description, User owner, String icon, int seq, Category parent) {
        this();
        this.name = name;
        this.description = description;
        this.categoryType = categoryType;
        this.owner = owner;
        this.icon = icon;
        this.seq = seq;
        this.parent = parent;
        this.system = true;
    }
    
    public static List<Category> loadCategories() {
        try {
            Query q = JPA.em().createQuery("SELECT c FROM Category c where categoryType = ?1 and parent is null and deleted = 0 order by seq");
            q.setParameter(1, CategoryType.PUBLIC);
            return (List<Category>) q.getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }
    
    public static List<Category> loadSubCategories() {
        try {
            Query q = JPA.em().createQuery("SELECT c FROM Category c where categoryType = ?1 and parent is not null and deleted = 0 order by seq");
            q.setParameter(1, CategoryType.PUBLIC);
            return (List<Category>) q.getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }
    
    public static List<Category> loadCustomCategories() {
        return loadCategories(CategoryType.CUSTOM);
    }
    
    public static List<Category> loadThemeCategories() {
        return loadCategories(CategoryType.THEME);
    }
    
    public static List<Category> loadTrendCategories() {
        return loadCategories(CategoryType.TREND);
    }
    
    public static List<Category> loadCategories(CategoryType categoryType) {
        try {
            Query q = JPA.em().createQuery("SELECT c FROM Category c where categoryType = ?1 and deleted = 0 order by seq");
            q.setParameter(1, categoryType);
            return (List<Category>) q.getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public static Category findById(Long id) {
        Category category = CategoryCache.getCategory(id);
        if (category != null) {
            return category;
        }

        try {
            Query q = JPA.em().createQuery("SELECT c FROM Category c where id = ?1 and deleted = 0");
            q.setParameter(1, id);
            return (Category) q.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public static List<Category> getCategories() {
        return CategoryCache.getCategories();
    }
    
    public static List<Category> getSubCategories(Long categoryId) {
        return CategoryCache.getSubCategories(categoryId);
    }
    
    public static List<Category> getThemeCategories() {
        return CategoryCache.getThemeCategories();
    }
    
    public static List<Category> getTrendCategories() {
        return CategoryCache.getTrendCategories();
    }
    
    public static List<Category> getCustomCategories() {
        return CategoryCache.getCustomCategories();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Category) {
            final Category other = (Category) o;
            return new EqualsBuilder().append(id, other.id).isEquals();
        } 
        return false;
    }
    
    @Override
    public int compareTo(Category o) {
        if (this.system != o.system) {
            return Boolean.compare(this.system, o.system);
        }
        if (this.parent != null && this.parent != o.parent) {
            return this.parent.compareTo(o.parent);
        }
        if (this.categoryType != null && o.categoryType != null && 
                this.categoryType != o.categoryType) {
            return this.categoryType.compareTo(o.categoryType);
        }
        return this.name.compareTo(o.name);
    }
}