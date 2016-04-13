package controllers;

import indexing.PostIndex;
import indexing.UserIndex;

import java.util.ArrayList;
import java.util.List;

import models.Post;
import models.User;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;

import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import viewmodel.PostVM;
import viewmodel.UserVM;

import com.github.cleverage.elasticsearch.IndexQuery;
import com.github.cleverage.elasticsearch.IndexResults;

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
    
    public static void addUserElasticSearch(Long id, String fname, String lname, String email){
		UserIndex userIndex = new UserIndex();
		userIndex.id = id+"";
		userIndex.fname = fname;
		userIndex.lname = lname;
		userIndex.email = email;
		userIndex.index();
	}
	
	@Transactional
	public static Result elasticSearchPost(String searchKey, String catId, Integer offset){
		int fromCount = offset * FEED_RETRIEVAL_COUNT;
		IndexQuery<PostIndex> indexQuery = PostIndex.find.query();
		
		if(!catId.equals("0")){
			BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
			QueryStringQueryBuilder queryBuilderTitle = QueryBuilders.queryStringQuery(searchKey).defaultField("title");
			booleanQueryBuilder.should(queryBuilderTitle);
			
			QueryStringQueryBuilder queryBuilderBody = QueryBuilders.queryStringQuery(searchKey).defaultField("body");
			booleanQueryBuilder.should(queryBuilderBody);
			
			QueryStringQueryBuilder queryBuilderCat = QueryBuilders.queryStringQuery(catId).defaultField("catId");
			booleanQueryBuilder.must(queryBuilderCat);
			
			booleanQueryBuilder.minimumShouldMatch("1");
			
			indexQuery.setBuilder(booleanQueryBuilder).from(fromCount).size(FEED_RETRIEVAL_COUNT);
		}else{
			indexQuery.setBuilder(QueryBuilders.queryStringQuery(searchKey)).from(fromCount).size(FEED_RETRIEVAL_COUNT);
		}
		
		IndexResults<PostIndex> results = PostIndex.find.search(indexQuery);

		if(results.results.size() == 0){
			return notFound();
		}
		List<Long> postIds = new ArrayList<Long>();
		for(int i=0; i<results.results.size(); i++){
			postIds.add(Long.parseLong(results.results.get(i).id));
		}
		
		return getProductInfo(postIds);
	}
	
	public static Result getProductInfo(List<Long> id) {
		List<PostVM> post = null;
		if(id.size() > 0){
			post = getPostInfoVM(id);
		}	
		if (post == null) {
			return notFound();
		}
		return ok(Json.toJson(post));
	}
	
	public static List<PostVM> getPostInfoVM(List<Long> ids) {
		User localUser = Application.getLocalUser(session());
		List<Post> posts = Post.findByIdList(ids);
		
		if (posts == null) {
			return null;
		}
		//onView(post, localUser);
		List<PostVM> postVm = new ArrayList<PostVM>();
		for (Post post : posts) {
			PostVM vm = new PostVM(post, localUser);
			postVm.add(vm);
		}
		
		return postVm;
	}
	
	@Transactional
	public static Result elasticSearchUser(String searchKey, Integer offset){
		int fromCount = offset * FEED_RETRIEVAL_COUNT;
		IndexQuery<UserIndex> indexQuery = UserIndex.find.query();
		indexQuery.setBuilder(QueryBuilders.queryStringQuery(searchKey)).from(fromCount).size(FEED_RETRIEVAL_COUNT);
		IndexResults<UserIndex> results = UserIndex.find.search(indexQuery);
		if(results.results.size() == 0){
			return notFound();
		}
		Long[] userIds = new Long[results.results.size()];
		for(int i=0; i<results.results.size(); i++){
			userIds[i] = Long.parseLong(results.results.get(i).id);
		}
		return getUserInfoById(userIds);
	}
	
	public static Result getUserInfoById(Long[] ids) {
        final User localUser = Application.getLocalUser(session());
        final List<User> users = User.findByIdList(ids);
        
        if (localUser == null || users == null) {
            return notFound();
        }
        List<UserVM> userVm = new ArrayList<UserVM>();
        for (User user : users) {
        	UserVM vm = new UserVM(user, localUser);
        	userVm.add(vm);
		}
        
        return ok(Json.toJson(userVm));
    }
}
