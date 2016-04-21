package controllers;

import indexing.PostIndex;
import indexing.UserIndex;

import java.util.ArrayList;
import java.util.List;

import models.Post;
import models.User;

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
import viewmodel.UserVM;

import com.github.cleverage.elasticsearch.IndexClient;
import com.github.cleverage.elasticsearch.IndexQuery;
import com.github.cleverage.elasticsearch.IndexQueryPath;
import com.github.cleverage.elasticsearch.IndexResults;
import com.github.cleverage.elasticsearch.IndexService;

import domain.DefaultValues;

public class ElasticSearchController extends Controller {
	
	public static final int FEED_RETRIEVAL_COUNT = DefaultValues.FEED_INFINITE_SCROLL_COUNT;
	
    public static void addPostElasticSearch(Long id, String title, String body, Long catId){
		PostIndex postIndex = new PostIndex();
		postIndex.id = id+"";
		postIndex.title = title;
		postIndex.body = body;
		postIndex.catId = catId+"";
		postIndex.index();
	}
    
    public static void addUserElasticSearch(Long id, String displayName, String firstName, String lastName){
		UserIndex userIndex = new UserIndex();
		userIndex.id = id+"";
		userIndex.displayName = displayName;
		userIndex.firstName = firstName;
		userIndex.lastName = lastName;
		userIndex.index();
	}
    
    public static void removePostElasticSearch(Long id){
    	IndexQuery<PostIndex> indexQuery = PostIndex.find.query();
    	BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
		QueryStringQueryBuilder queryBuilder = QueryBuilders.queryStringQuery(id+"").defaultField("id");
		booleanQueryBuilder.must(queryBuilder);
		indexQuery.setBuilder(booleanQueryBuilder);
		IndexResults<PostIndex> results = PostIndex.find.search(indexQuery);
		delete(results.results.get(0).getIndexPath(),results.results.get(0).searchHit.id());
		refresh();
    }
    
    public static void removeUserElasticSearch(Long id){
    	IndexQuery<UserIndex> indexQuery = UserIndex.find.query();
    	BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
		QueryStringQueryBuilder queryBuilder = QueryBuilders.queryStringQuery(id+"").defaultField("id");
		booleanQueryBuilder.must(queryBuilder);
		indexQuery.setBuilder(booleanQueryBuilder);
		IndexResults<UserIndex> results = UserIndex.find.search(indexQuery);
		delete(results.results.get(0).getIndexPath(),results.results.get(0).searchHit.id());
		refresh();
    }
	
	@Transactional
	public static Result elasticSearchPosts(String searchKey, String catId, Integer offset){
		int fromCount = offset * FEED_RETRIEVAL_COUNT;
		IndexQuery<PostIndex> indexQuery = PostIndex.find.query();
		
		if (!catId.equals("-1")) {
			BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
			QueryStringQueryBuilder queryBuilderTitle = QueryBuilders.queryStringQuery(searchKey).defaultField("title");
			booleanQueryBuilder.should(queryBuilderTitle);
			
			QueryStringQueryBuilder queryBuilderBody = QueryBuilders.queryStringQuery(searchKey).defaultField("body");
			booleanQueryBuilder.should(queryBuilderBody);
			
			QueryStringQueryBuilder queryBuilderCat = QueryBuilders.queryStringQuery(catId).defaultField("catId");
			booleanQueryBuilder.must(queryBuilderCat);
			
			booleanQueryBuilder.minimumShouldMatch("1");
			
			indexQuery.setBuilder(booleanQueryBuilder).from(fromCount).size(FEED_RETRIEVAL_COUNT);
		} else {
			indexQuery.setBuilder(QueryBuilders.queryStringQuery(searchKey)).from(fromCount).size(FEED_RETRIEVAL_COUNT);
		}
		
		IndexResults<PostIndex> results = PostIndex.find.search(indexQuery);

		if (results.results.size() == 0) {
			return notFound();
		}
		
		List<Long> postIds = new ArrayList<Long>();
		for (int i=0; i<results.results.size(); i++) {
			postIds.add(Long.parseLong(results.results.get(i).id));
		}
		
		return getPostInfos(postIds);
	}
	
	public static Result getPostInfos(List<Long> ids) {
        final User localUser = Application.getLocalUser(session());
        final List<Post> posts = Post.getPosts(ids);
        
        if (localUser == null || posts == null) {
            return notFound();
        }
        
        List<PostVM> vms = new ArrayList<PostVM>();
        for (Post post : posts) {
            PostVM vm = new PostVM(post, localUser);
            vms.add(vm);
        }
        
        return ok(Json.toJson(vms));
    }
	
	@Transactional
	public static Result elasticSearchUsers(String searchKey, Integer offset){
		int fromCount = offset * FEED_RETRIEVAL_COUNT;
		IndexQuery<UserIndex> indexQuery = UserIndex.find.query();
		indexQuery.setBuilder(QueryBuilders.queryStringQuery(searchKey)).from(fromCount).size(FEED_RETRIEVAL_COUNT);
		IndexResults<UserIndex> results = UserIndex.find.search(indexQuery);
		if(results.results.size() == 0){
			return notFound();
		}
		
		List<Long> userIds = new ArrayList<Long>();
        for (int i=0; i<results.results.size(); i++) {
            userIds.add(Long.parseLong(results.results.get(i).id));
        }
		
		return getUserInfos(userIds);
	}
	
	public static Result getUserInfos(List<Long> ids) {
        final User localUser = Application.getLocalUser(session());
        final List<User> users = User.getUsers(ids);
        
        if (localUser == null || users == null) {
            return notFound();
        }
        
        List<UserVM> vms = new ArrayList<UserVM>();
        for (User user : users) {
        	UserVM vm = new UserVM(user, localUser);
        	vms.add(vm);
		}
        
        return ok(Json.toJson(vms));
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

        //System.out.println("ElasticSearch : Delete " + deleteResponse.toString());
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
