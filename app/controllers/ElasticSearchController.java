package controllers;

import handler.FeedHandler;
import indexing.PostIndex;
import indexing.UserIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import models.Category;
import models.Post;
import models.User;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse.AnalyzeToken;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;

import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import viewmodel.PostVM;
import viewmodel.PostVMLite;
import viewmodel.SellerVM;

import com.github.cleverage.elasticsearch.IndexClient;
import com.github.cleverage.elasticsearch.IndexQuery;
import com.github.cleverage.elasticsearch.IndexQueryPath;
import com.github.cleverage.elasticsearch.IndexResults;
import com.github.cleverage.elasticsearch.IndexService;
import com.google.inject.Inject;

import common.cache.CategoryCache;
import common.model.FeedFilter.FeedType;
import common.utils.NanoSecondStopWatch;
import common.utils.StringUtil;
import domain.DefaultValues;

public class ElasticSearchController extends Controller {
    private static final play.api.Logger logger = play.api.Logger.apply(UserController.class);
    
    @Inject
    FeedHandler feedHandler;
    
    public static void addPostElasticSearch(Post post) {
		PostIndex postIndex = new PostIndex();
		postIndex.id = post.id+"";
		postIndex.title = post.title;
		postIndex.body = post.body;
		if (post.category.parent != null) {
		    postIndex.catId = post.category.parent.id+"";
		    postIndex.subCatId = post.category.id+"";
		} else {
		    postIndex.catId = post.category.id+"";
            postIndex.subCatId = "";
		}
		postIndex.index();
	}
    
    public static void addUserElasticSearch(User user) {
		UserIndex userIndex = new UserIndex();
		userIndex.id = user.id+"";
		userIndex.displayName = user.displayName;
		userIndex.firstName = user.firstName;
		userIndex.lastName = user.lastName;
		userIndex.index();
	}
    
    public static void removePostElasticSearch(Post post){
    	IndexQuery<PostIndex> indexQuery = PostIndex.find.query();
    	BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
		QueryStringQueryBuilder queryBuilder = QueryBuilders.queryStringQuery(post.id+"").defaultField("id");
		booleanQueryBuilder.must(queryBuilder);
		indexQuery.setBuilder(booleanQueryBuilder);
		IndexResults<PostIndex> results = PostIndex.find.search(indexQuery);
		if (results != null && results.results != null && results.results.size() > 0) {
		    delete(results.results.get(0).getIndexPath(),results.results.get(0).searchHit.id());
		    refresh();
		}
    }
    
    public static void removeUserElasticSearch(User user){
    	IndexQuery<UserIndex> indexQuery = UserIndex.find.query();
    	BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
		QueryStringQueryBuilder queryBuilder = QueryBuilders.queryStringQuery(user.id+"").defaultField("id");
		booleanQueryBuilder.must(queryBuilder);
		indexQuery.setBuilder(booleanQueryBuilder);
		IndexResults<UserIndex> results = UserIndex.find.search(indexQuery);
		if (results != null && results.results != null && results.results.size() > 0) {
		    delete(results.results.get(0).getIndexPath(),results.results.get(0).searchHit.id());
		    refresh();
		}
    }
	
    private List<String> tokenizeSearchKey(String searchKey) {
        searchKey = searchKey.trim();
        return Arrays.asList(searchKey.split(" "));
        
        /*
        List<String> terms = new ArrayList<>();
        try {
            AnalyzeResponse response = IndexClient.client.admin().indices()
                    .prepareAnalyze(searchKey).setTokenizer("smartcn_tokenizer")
                    .execute().get();
            
            //AnalyzeResponse response = IndexClient.client.admin().indices()
            //        .prepareAnalyze(searchKey).setAnalyzer("smartcn")
            //        .execute().get();
            
            List<AnalyzeToken> tokens = response.getTokens();
            for (AnalyzeToken token : tokens) {
                terms.add(token.getTerm());
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.underlyingLogger().error("[searchKey="+searchKey+"] Failed to analyzeSearchKey", e);
        }
        
        logger.underlyingLogger().debug("[searchKey="+searchKey+"][tokens="+StringUtil.collectionToString(terms, ",")+"] analyzeSearchKey");
        return terms;
        */
    }
    
	@Transactional
	public Result searchPosts(String searchKey, Long catId, Integer offset){
	    NanoSecondStopWatch sw = new NanoSecondStopWatch();
	    
	    final User localUser = Application.getLocalUser(session());
	    if (localUser == null) {
            return notFound();
        }
        
		int fromCount = offset * DefaultValues.FEED_INFINITE_SCROLL_COUNT;
		IndexQuery<PostIndex> indexQuery = PostIndex.find.query();
		
		if (catId > 0) {
            Category category = CategoryCache.getCategory(catId);
            Category subCategory = null;
            
            // it is a subcategory
            if (category.parent != null) {
                subCategory = category;
                category = category.parent;
            }
            
            BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
            List<String> searches = tokenizeSearchKey(searchKey);
            for (String searchWord : searches) {
            	QueryStringQueryBuilder queryBuilder = QueryBuilders.queryStringQuery(searchWord.trim());
            	booleanQueryBuilder.must(queryBuilder);
            }
            
            if (subCategory != null) {
            	QueryStringQueryBuilder queryBuilderSubCat = QueryBuilders.queryStringQuery(catId.toString()).defaultField("subCatId");
                booleanQueryBuilder.must(queryBuilderSubCat);
            } else {
                QueryStringQueryBuilder queryBuilderCat = QueryBuilders.queryStringQuery(catId.toString()).defaultField("catId");
                booleanQueryBuilder.must(queryBuilderCat);
            }
            
            //booleanQueryBuilder.minimumShouldMatch("1");
            
            indexQuery.setBuilder(booleanQueryBuilder).from(fromCount).size(DefaultValues.FEED_INFINITE_SCROLL_COUNT);
		} else {
            BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
            List<String> searches = tokenizeSearchKey(searchKey);
            for (String searchWord : searches) {
            	QueryStringQueryBuilder queryBuilder = QueryBuilders.queryStringQuery(searchWord.trim());
            	booleanQueryBuilder.must(queryBuilder);
            }
            indexQuery.setBuilder(booleanQueryBuilder).from(fromCount).size(DefaultValues.FEED_INFINITE_SCROLL_COUNT);
		}
		
		IndexResults<PostIndex> results = PostIndex.find.search(indexQuery);
		if (results.results.size() == 0) {
		    sw.stop();
            if (logger.underlyingLogger().isDebugEnabled()) {
                logger.underlyingLogger().debug("[u="+localUser.getId()+"][searchkey="+searchKey+"][catId="+catId+"][posts.size=0] searchPosts(). Took "+sw.getElapsedMS()+"ms");
            }
			return ok(Json.toJson(new ArrayList<Long>()));
		}
		
		List<Long> postIds = new ArrayList<Long>();
		for (int i=0; i<results.results.size(); i++) {
			postIds.add(Long.parseLong(results.results.get(i).id));
		}
		
		List<PostVM> posts = getPostInfos(postIds, localUser);
		
		sw.stop();
        if (logger.underlyingLogger().isDebugEnabled()) {
            logger.underlyingLogger().debug("[u="+localUser.getId()+"][searchkey="+searchKey+"][catId="+catId+"][posts.size="+posts.size()+"] searchPosts(). Took "+sw.getElapsedMS()+"ms");
        }
		return ok(Json.toJson(posts));
	}
	
	public List<PostVM> getPostInfos(List<Long> ids, User localUser) {
        final List<Post> posts = Post.getPosts(ids);
        List<PostVM> vms = new ArrayList<>();
        for (Post post : posts) {
            PostVM vm = new PostVM(post, localUser);
            vms.add(vm);
        }
        return vms;
    }
	
	@Transactional
	public Result searchUsers(String searchKey, Integer offset){
	    NanoSecondStopWatch sw = new NanoSecondStopWatch();
        
	    final User localUser = Application.getLocalUser(session());
	    if (localUser == null) {
            return notFound();
        }
	    
		int fromCount = offset * DefaultValues.DEFAULT_INFINITE_SCROLL_COUNT;
		IndexQuery<UserIndex> indexQuery = UserIndex.find.query();
		indexQuery.setBuilder(QueryBuilders.queryStringQuery(searchKey)).from(fromCount).size(DefaultValues.DEFAULT_INFINITE_SCROLL_COUNT);
		IndexResults<UserIndex> results = UserIndex.find.search(indexQuery);
		if (results.results.size() == 0) {
		    sw.stop();
	        if (logger.underlyingLogger().isDebugEnabled()) {
	            logger.underlyingLogger().debug("[u="+localUser.getId()+"][searchkey="+searchKey+"][users.size=0] searchUsers(). Took "+sw.getElapsedMS()+"ms");
	        }
	        return ok(Json.toJson(new ArrayList<Long>()));
		}
		
		List<Long> userIds = new ArrayList<Long>();
        for (int i=0; i<results.results.size(); i++) {
            userIds.add(Long.parseLong(results.results.get(i).id));
        }
		
		List<SellerVM> users = getUserInfos(userIds, localUser);
        
        sw.stop();
        if (logger.underlyingLogger().isDebugEnabled()) {
            logger.underlyingLogger().debug("[u="+localUser.getId()+"][searchkey="+searchKey+"][users.size="+users.size()+"] searchUsers(). Took "+sw.getElapsedMS()+"ms");
        }
        return ok(Json.toJson(users));
	}
	
	public List<SellerVM> getUserInfos(List<Long> ids, User localUser) {
        final List<User> users = User.getUsers(ids);
        List<SellerVM> vms = new ArrayList<>();
        for (User user : users) {
            if (user.newUser || !user.active || user.deleted) {
                continue;
            }
            
            // get first batch of seller products
            List<PostVMLite> posts = feedHandler.getFeedPosts(
                    user.id, 0L, localUser, FeedType.USER_POSTED, 
                    DefaultValues.MAX_SELLER_PRODUCTS_FOR_FEED);
            if (posts.size() > DefaultValues.MAX_SELLER_PRODUCTS_FOR_FEED) {
                posts = posts.subList(0, DefaultValues.MAX_SELLER_PRODUCTS_FOR_FEED);
            }
            
            SellerVM vm = new SellerVM(user, localUser, posts);
            vms.add(vm);
        }
        return vms;
    }
	
	/**
     * Clean full index
     */
	public static void cleanIndex() {

        String[] indexNames = IndexClient.config.indexNames;
        for (String indexName : indexNames) {
            cleanIndex(indexName);
        }
    }

	/**
     * Clean an index
     * @param indexName
     */
    public static void cleanIndex(String indexName) {

        if (IndexService.existsIndex(indexName)) {
            IndexService.deleteIndex(indexName);
        }
        IndexService.createIndex(indexName);
        IndexService.prepareIndex(indexName);
    }
    
    /**
     * Refresh full index
     */
    public static void refresh() {
        String[] indexNames = IndexClient.config.indexNames;
        for (String indexName : indexNames) {
            refresh(indexName);
        }
    }

    /**
     * Refresh an index
     * @param indexName
     */
    private static void refresh(String indexName) {
        IndexClient.client.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
    }
    
    /**
     * Delete element in index
     * @param indexPath
     * 
     */
    public static void delete(IndexQueryPath indexPath, String id) {
    	DeleteResponse deleteResponse = getDeleteRequestBuilder(indexPath, id)
                .execute()
                .actionGet();
    }
    
    /**
     * Create a DeleteRequestBuilder
     * @param indexPath
     * @param id
     * @return
     */
    public static DeleteRequestBuilder getDeleteRequestBuilder(IndexQueryPath indexPath, String id) {
        return IndexClient.client.prepareDelete(indexPath.index, indexPath.type, id);
    }
}
