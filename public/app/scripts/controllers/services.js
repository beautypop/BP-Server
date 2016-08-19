'use strict';

var beautypop = angular.module('beautypop');

beautypop.run(function($rootScope, $window) {
    $rootScope.doBack = function() {
    	if ($window.history.length > 1) {
    		$window.history.back();		
    	} else {
    		$window.close();
    	}
    };
});

beautypop.service('categoryService',function($resource){
	this.getCategories = $resource(
            '/api/get-categories',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get' ,isArray:true}
            }
    );

	this.getThemeCategories = $resource(
            '/api/get-theme-categories',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get' ,isArray:true}
            }
    );

	this.getTrendCategories = $resource(
            '/api/get-trend-categories',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get' ,isArray:true}
            }
    );

	this.getCustomCategories = $resource(
            '/api/get-custom-categories',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get' ,isArray:true}
            }
    );

    this.getCategoryPopularFeed = $resource(
            '/api/get-category-popular-feed/:id/:conditionType/:offset',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get' ,isArray:true, params:{id:'@id',conditionType: '@conditionType',offset: '@offset'}}
            }
    );
    
    this.getCategoryNewestFeed = $resource(
            '/api/get-category-newest-feed/:id/:conditionType/:offset',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get' ,isArray:true, params:{id:'@id',conditionType: '@conditionType',offset: '@offset'}}
            }
    );
    
    this.getCategoryPriceLowHighFeed = $resource(
            '/api/get-category-price-low-high-feed/:id/:conditionType/:offset',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get' ,isArray:true, params:{id:'@id',conditionType: '@conditionType',offset: '@offset'}}
            }
    );
    
    this.getCategoryPriceHighLowFeed = $resource(
            '/api/get-category-price-high-low-feed/:id/:conditionType/:offset',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get' ,isArray:true, params:{id:'@id',conditionType: '@conditionType',offset: '@offset'}}
            }
    );
    
});

beautypop.service('postService',function($resource){
    this.getPostInfo = $resource(
            '/api/get-post/:id',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get', params:{id:'@id'}}
            }
    );
    
    this.getHomeExploreFeed = $resource(
            '/api/get-home-explore-feed/:offset',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get',isArray:true, params:{offset: '@offset'}}
            }
    );
    
    this.getHomeFollowingFeed = $resource(
            '/api/get-home-following-feed/:offset',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get',isArray:true, params:{offset: '@offset'}}
            }
    );
    
    this.allComments = $resource(
            '/api/get-comments/:id/:offset',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get',isArray:true, params:{id:'@id',offset: '@offset'}}
            }
    );
});

beautypop.service('userService',function($resource){
    this.getRecommendedSellersFeed = $resource(
            '/api/get-recommended-sellers-feed/:offset',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get',isArray:true, params:{offset: '@offset'}}
            }
    );
    
	this.getUserLikedFeed = $resource(
			'/api/get-user-liked-feed/:id/:offset',
			{alt:'json',callback:'JSON_CALLBACK'},
			{
				get: {method:'get', isArray:true, params:{id:'@id',offset: '@offset'}}
			}
	);
	
	this.getUserPostedFeed = $resource(
	        '/api/get-user-posted-feed/:id/:offset',
	        {alt:'json',callback:'JSON_CALLBACK'},
	        {
	            get: {method:'get', isArray:true, params:{id:'@id',offset: '@offset'}}
	        }
	);
        
    this.getUserCollections = $resource(
            '/api/get-user-collections/:id',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get', isArray:true}
            }
    );
});

beautypop.service('followService',function($resource){
    this.followUser = $resource(
            '/api/follow-user/:id',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get', params:{id:'@id'}}
            }
    );
    
    this.unFollowUser = $resource(
            '/api/unfollow-user/:id',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get', params:{id:'@id'}}
            }
    );
    
    this.userfollowers = $resource(
    		 '/api/get-followers/:id/:offset',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get', isArray:true, params:{id:'@id',offset: '@offset'}}
            }
    );
    this.userfollowings = $resource(
    		'/api/get-followings/:id/:offset',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get', isArray:true, params:{id:'@id',offset: '@offset'}}
            }
    );
    
});

beautypop.service('likeService',function($resource){
    this.likePost = $resource(
            '/api/like-post/:id',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get', params:{id:'@id'}}
            }
    );
    
    this.unLikePost = $resource(
            '/api/unlike-post/:id',
            {alt:'json',callback:'JSON_CALLBACK'},
            {
                get: {method:'get', params:{id:'@id'}}
            }
    );
});

beautypop.service('profilePhotoModal',function( $modal){
    
    this.OpenModal = function(arg, successCallback) {
        this.instance = $modal.open(arg);
        this.onSuccess = successCallback;
    }
    
    this.CloseModal = function() {
        this.instance.dismiss('close');
        this.onSuccess();
    }
});
