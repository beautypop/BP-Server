'use strict';

var beautypop = angular.module('beautypop');

beautypop.controller('HomeController', 
		function($scope, $translate, $location, $route, $window, categoryService, postService, userService, followService, $rootScope, ngDialog, userInfo, $anchorScroll, usSpinnerService, featuredItems) {
	
	writeMetaCanonical($location.absUrl());
	
	// agent
	$scope.showAppDownloadTips = showAppDownloadTips();
	$scope.appDownloadUrl = getAppDownloadUrl();

	usSpinnerService.spin('loading...');

	$scope.featuredItems = featuredItems;
	$scope.userInfo = userInfo;
	
	// home tabs
	$scope.homeExplore = true;
	$scope.homeSeller = false;
	$scope.homeFollowing = false;
	$scope.productsLength = DefaultValues.MAX_SELLER_PRODUCTS_FOR_FEED;
	
	$scope.forwardLink = function(featuredItem){
		//console.log(featuredItem);
		var url;
		if(angular.equals(featuredItem.destinationType,"CATEGORY")){
			url = "category/"+featuredItem.destinationObjId+"/popular";
		}
		if(angular.equals(featuredItem.destinationType,"POST")){
			url = "product/"+featuredItem.destinationObjId;
		}
		if(angular.equals(featuredItem.destinationType,"USER")){
			url = "seller/"+featuredItem.destinationObjId;
		}
		window.location.href=url;
	}
	
	$scope.getHomeExploreProducts = function () {
		$scope.products = postService.getHomeExploreFeed.get({offset:0});
		$scope.loadMore = true;
		
		// tabs
		$scope.homeExplore = true;
		$scope.homeSeller = false;
		$scope.homeFollowing = false;
	};

	$scope.getHomeFollowingProducts = function () {
		$scope.products = postService.getHomeFollowingFeed.get({offset:0});
		$scope.loadMore = true;
		
		// tabs
		$scope.homeExplore = false;
		$scope.homeSeller = false;
		$scope.homeFollowing = true;
	};
	
	$scope.getRecommendedSellers = function () {
		$scope.sellers = userService.getRecommendedSellersFeed.get({offset:0});
		$scope.loadMore = true;
		
		// tabs
		$scope.homeExplore = false;
		$scope.homeSeller = true;
		$scope.homeFollowing = false;
	};

	$scope.categories = categoryService.getCategories.get();
	$scope.getHomeExploreProducts();
	
	var loadingMore = false;
	$scope.loadMoreProducts = function () {
		if(!$scope.homeExplore && !$scope.homeFollowing){
			return;
		}
		if($scope.products.length > 0 && $scope.loadMore && !loadingMore){
			var len = $scope.products.length;
			var off = $scope.products[len-1].offset;
			loadingMore = true;
			if($scope.homeExplore){
				postService.getHomeExploreFeed.get({offset:off}, function(data){
					if(data.length == 0) {
						$scope.loadMore = false;
					}
					angular.forEach(data, function(value, key) {
						if(loadingMore) {
							$scope.products.push(value);
						}
					});
					loadingMore = false;
				});
			}
			if($scope.homeFollowing){
				postService.getHomeFollowingFeed.get({offset:off}, function(data){
					if(data.length == 0) {
						$scope.loadMore = false;
					}
					angular.forEach(data, function(value, key) {
						if(loadingMore)
							$scope.products.push(value);
					});
					loadingMore = false;
				});
			}
		}		
	}
	
	$scope.loadMoreSellers = function () {
		if(!$scope.homeSeller){
			return;
		}
		if($scope.sellers.length > 0 && $scope.loadMore && !loadingMore){
			var len = $scope.sellers.length;
			var off = $scope.sellers[len-1].offset;
			loadingMore = true;
			userService.getRecommendedSellersFeed.get({offset:off}, function(data){
				if(data.length == 0) {
					$scope.loadMore = false;
				}
				angular.forEach(data, function(value, key) {
					if(loadingMore) {
						$scope.sellers.push(value);
					}
				});
				loadingMore = false;
			});
		}		
	}
	
	$scope.onFollowUser = function(user) {
		if ($scope.userInfo.isLoggedIn) {
			if ($scope.userInfo.newUser) {
				$window.location.href ='/home';
			} else {
				followService.followUser.get({id:user.id});
				user.isFollowing = !user.isFollowing;
				user.numFollowings++;
			}
		} else {
			$window.location.href ='/login';
		}
	}
	
	$scope.onUnFollowUser = function(user) {
		followService.unFollowUser.get({id:user.id});
		user.isFollowing = !user.isFollowing;
		user.numFollowings--;
	}
	
	// UI helper
	$(window).scroll(function(e){
		$scope.position = window.pageYOffset;
		if($scope.position > 750) {
			$("#back-to-top").show();
		} else {
			$("#back-to-top").hide();
		}
	});
	
	$scope.gotoTop = function(){
		$location.hash('');
		$anchorScroll();
	};
});

beautypop.controller('CategoryPageController', 
		function($scope, $location, $translate, $route, $rootScope, ngDialog, $routeParams, subCategories, category, products, userInfo, categoryService, $anchorScroll, usSpinnerService) {
	
	writeMetaCanonical($location.absUrl());
	
	/*
	writeMetaTitleDescription(
			category.name, 
			category.description, 
			formatToExternalUrl(category.icon));
	*/
	
	// agent
	$scope.showAppDownloadTips = showAppDownloadTips();
	$scope.appDownloadUrl = getAppDownloadUrl();

	usSpinnerService.spin('loading...');
	
	$scope.userInfo = userInfo;
	$scope.products = products;
	$scope.cat = category;
	$scope.subcats = subCategories;
	
	//we are routing this from scala file so we can't able to get $routeParams , so this is just a workaround
	var url = $location.absUrl();
	var values= url.split("/");
	$scope.catType = values[values.length-1];

	if($scope.catType == 'popular')
		$scope.loadMore = true;
	if($scope.catType == 'newest')
		$scope.loadMore = true;
	if($scope.catType == 'high2low')
		$scope.loadMore = true;
	if($scope.catType == 'low2high')
		$scope.loadMore = true;
	
	$scope.gotoTop=function(){
		$location.hash('');
		$anchorScroll();
	};
	
	var catId = $scope.cat.id;
	var loadingMore = false;
	$scope.loadMoreProducts = function () {
		if($scope.products.length > 0 && $scope.loadMore && !loadingMore){
			var len = $scope.products.length;
			var off = $scope.products[len-1].offset;

			if($scope.catType == 'popular'){
				loadingMore = true;
				categoryService.getCategoryPopularFeed.get({id:catId , conditionType:"ALL", offset:off}, function(data){
					if (data.length == 0) {
						$scope.loadMore = false;
					}
					angular.forEach(data, function(value, key) {
						if (loadingMore) {
							$scope.products.push(value);
						}
					});
					loadingMore = false;
				});
			}

			if($scope.catType == 'newest'){
				loadingMore = true;
				categoryService.getCategoryNewestFeed.get({id:catId , conditionType:"ALL", offset:off}, function(data){
					if (data.length == 0) {
						$scope.loadMore = false;
					}
					angular.forEach(data, function(value, key) {
						if (loadingMore) {
							$scope.products.push(value);
						}
					});
					loadingMore = false;
				});
			}
			
			if($scope.catType == 'high2low'){
				loadingMore = true;
				categoryService.getCategoryPriceHighLowFeed.get({id:catId , conditionType:"ALL", offset:off}, function(data){
					if (data.length == 0) {
						$scope.loadMore = false;
					}
					angular.forEach(data, function(value, key) {
						if (loadingMore) {
							$scope.products.push(value);
						}
					});
					loadingMore = false;
				});
			}
			
			if($scope.catType == 'low2high'){
				loadingMore = true;
				categoryService.getCategoryPriceLowHighFeed.get({id:catId , conditionType:"ALL", offset:off}, function(data){
					if (data.length == 0) {
						$scope.loadMore = false;
					}
					angular.forEach(data, function(value, key) {
						if (loadingMore) {
							$scope.products.push(value);
						}
					});
					loadingMore = false;
				});
			}
		}		
	}
	
	// UI helper
	$(window).scroll(function(e){
		$scope.position = window.pageYOffset;
		if($scope.position > 750) {
			$("#back-to-top").show();
		} else {
			$("#back-to-top").hide();
		}
	});
	
	$scope.gotoTop=function(){
		$location.hash('');
		$anchorScroll();
	};
});

beautypop.controller('ProductPageController', 
		function($scope, $location, $translate, $route, $rootScope, $http, $window, likeService, userService, product, userInfo, suggestedPost) {

	writeMetaCanonical($location.absUrl());
	/*
	writeMetaTitleDescription(
			product.title, 
			product.body, 
			formatToExternalUrl("/image/get-post-image-by-id/"+product.images[0]));
	*/
	
	$scope.product = product;
	$scope.userInfo = userInfo;
	$scope.suggestedPost = suggestedPost;

	$scope.iosAppDownloadUrl = getIOSAppDownloadUrl();
	$scope.androidAppDownloadUrl = getAndroidAppDownloadUrl();
	
	$scope.like_Unlike = function(id) {
		if ($scope.userInfo.isLoggedIn) {
			if ($scope.userInfo.newUser) {
				$window.location.href ='/home';
			} else {
				if($scope.product.isLiked){
					likeService.unLikePost.get({id:id});
					$scope.product.isLiked = !$scope.product.isLiked;
					$scope.product.numLikes--;
				}else{
					likeService.likePost.get({id:id});
					$scope.product.isLiked = !$scope.product.isLiked;
					$scope.product.numLikes++;
				}
			}
		} else {
			$window.location.href ='/login';
		}
	}

	$scope.openPopup = function(){
		$('#popupBtn').click();
	}
	
});

beautypop.controller('StoryPageController', 
		function($scope, $location, $translate, $route, $rootScope, $http, $window, likeService, userService, story, userInfo) {

	writeMetaCanonical($location.absUrl());
	/*
	writeMetaTitleDescription(
			story.title, 
			story.body, 
			formatToExternalUrl("/image/get-post-image-by-id/"+story.images[0]));
	*/
	
	$scope.story = story;
	$scope.userInfo = userInfo;

	$scope.like_Unlike = function(id) {
		if ($scope.userInfo.isLoggedIn) {
			if ($scope.userInfo.newUser) {
				$window.location.href ='/home';
			} else {
				if($scope.story.isLiked){
					likeService.unLikePost.get({id:id});
					$scope.story.isLiked = !$scope.story.isLiked;
					$scope.story.numLikes--;
				}else{
					likeService.likePost.get({id:id});
					$scope.story.isLiked = !$scope.story.isLiked;
					$scope.story.numLikes++;
				}
			}
		} else {
			$window.location.href ='/login';
		}
	}

	$scope.openPopup = function(){
		$('#popupBtn').click();
	}
	
});

beautypop.controller('CommentOnProductController', 
		function($scope, $location, $translate, $route, $http) {

	writeMetaCanonical($location.absUrl());
	
	$scope.formData = {};
	$scope.comArray=[];
	$scope.submit = function() {
		var newCommentVM = {
				"postId" : $scope.product.id,
				"body" : $scope.formData.comment,
		};

		usSpinnerService.spin('loading...');
		$http.post('/comment/new', newCommentVM) 
		.success(function(response) {
			console.log(response);
			$scope.comArray.push({
				comment:$scope.formData.comment,
				name: $scope.userInfo.displayName,
				id: $scope.userInfo.id

			});
			$scope.formData.comment="";
			usSpinnerService.stop('loading...');
		});
	}
});


beautypop.controller('ProfilePageController', 
		function($scope, $location, $translate, $route, $rootScope, $filter, $window, $anchorScroll, profileUser, userService, userInfo, followService, ngDialog, profilePhotoModal) {

	writeMetaCanonical($location.absUrl());
	//writeMetaTitleDescription(profileUser.displayName, "看看 beautypop 商店");

	$scope.toggleLang = function () {
		$translate.use() === 'zh'? $translate.use('en') : $translate.use('zh');
		location.reload();
	};
	 
	$scope.active = true;
	$scope.userInfo = userInfo;
	$scope.user = profileUser;
	$scope.sellerUrl = stripHttpPrefix(formatToExternalUrl(profileUser.displayName));

	$scope.products = userService.getUserPostedFeed.get({id:profileUser.id, offset:0});
	
	$scope.onFollowUser = function() {
		if ($scope.userInfo.isLoggedIn) {
			if ($scope.userInfo.newUser) {
				$window.location.href ='/home';
			} else {
				followService.followUser.get({id:profileUser.id});
				$scope.user.isFollowing = !$scope.user.isFollowing;
				$scope.user.numFollowings++;
			}
		} else {
			$window.location.href ='/login';
		}
	}
	
	$scope.onUnFollowUser = function() {
		followService.unFollowUser.get({id:profileUser.id});
		$scope.user.isFollowing = !$scope.user.isFollowing;
		$scope.user.numFollowings--;
	}
	
	$scope.userProducts = function() {
		$scope.active = true;
		$scope.loadMore = true;
		$scope.products = userService.getUserPostedFeed.get({id:profileUser.id, offset:0});
	}
	
	$scope.likedProducts = function() {
		$scope.active = false;
		$scope.loadMore = true;
		$scope.products=userService.getUserLikedFeed.get({id:profileUser.id, offset:0});
	}
	
	var loadingMore = false;
	$scope.loadMore = true;
	$scope.loadMoreProducts = function () {
		if($scope.products.length > 0 && $scope.loadMore && !loadingMore){
			var len = $scope.products.length;
			var off = $scope.products[len-1].offset;
			if($scope.active){
				loadingMore = true;
				userService.getUserPostedFeed.get({id:profileUser.id, offset:off}, function(data){
					if (data.length == 0) {
						$scope.loadMore = false;
					}
					angular.forEach(data, function(value, key) {
						if (loadingMore) {
							$scope.products.push(value);
						}
					});
					loadingMore = false;
				});
			}
			if(!$scope.active){
				loadingMore = true;
				userService.getUserLikedFeed.get({id:profileUser.id, offset:off}, function(data){
					if (data.length == 0) {
						$scope.loadMore = false;
					}
					angular.forEach(data, function(value, key) {
						if (loadingMore) {
							$scope.products.push(value);
						}
					});
					loadingMore = false;
				});
			}
		}		
	}
	
	$scope.openUploadPhotoModal = function(flag) {
		if($scope.user.id == $scope.userInfo.id) {
			if (flag == 'profile') {
				PhotoModalController.url = '../image/upload-profile-photo';
			} else if (flag == 'cover') {
				PhotoModalController.url = '../image/upload-cover-photo';
			}
			
			profilePhotoModal.OpenModal({
				templateUrl: 'upload-photo-modal',
				controller: PhotoModalController
			},function() {
				 $scope.date = new Date();
				 var date1= $filter('date')($scope.date, 'HH:mm:ss')
				 if (flag == 'profile') {
					 $(".avatar").append($('.avatar').css({'background-image': "url(/image/get-thumbnail-profile-image-by-id/"+$scope.user.id+"?q="+date1+")"}));
				 } else if (flag == 'cover') {
					 // refresh cover image
				 }
			});
			
			PhotoModalController.isProfileOn = true;
		}
	}
	
	// UI helper
	$(window).scroll(function(e){
		$scope.position = window.pageYOffset;
		if($scope.position > 750) {
			$("#back-to-top").show();
		} else {
			$("#back-to-top").hide();
		}
	});
	
	$scope.gotoTop=function(){
		$location.hash('');
		$anchorScroll();
	};
});


//TODO: I dont like way i am defining PhotoModalController
var PhotoModalController = function( $scope, $http, $timeout, $upload, profilePhotoModal, usSpinnerService) {
	$scope.fileReaderSupported = window.FileReader != null;
	$scope.uploadRightAway = true;

	$scope.hasUploader = function(index) {
		return $scope.upload[index] != null;
	};
	
	$scope.abort = function(index) {
		$scope.upload[index].abort(); 
		$scope.upload[index] = null;
	};
	
	$scope.close = function() {
		profilePhotoModal.CloseModal();
	}
	
	$scope.onFileSelect = function($files) {
		$scope.selectedFiles = [];
		$scope.progress = [];
		if ($scope.upload && $scope.upload.length > 0) {
			for (var i = 0; i < $scope.upload.length; i++) {
				if ($scope.upload[i] != null) {
					$scope.upload[i].abort();
				}
			}
		}
		$scope.upload = [];
		$scope.uploadResult = [];
		$scope.selectedFiles = $files;
		$scope.dataUrls = [];
		for ( var i = 0; i < $files.length; i++) {
			var $file = $files[i];
			
			if (window.FileReader && $file.type.indexOf('image') > -1) {
			  	var fileReader = new FileReader();
		        fileReader.readAsDataURL($files[i]);
		        $scope.setPreview(fileReader, i);
			}
			
			$scope.progress[i] = -1;
			if ($scope.uploadRightAway) {
				$scope.start(i);
			}
		}
	} // End of onSelect
	
	$scope.setPreview = function(fileReader, index) {
	    fileReader.onload = function(e) {
	        $timeout(function() {
	        	$scope.dataUrls[index] = e.target.result;
	        });
	    }
	}

	$scope.start = function(index) {
		$scope.progress[index] = 0;
		usSpinnerService.spin('loading..');
		console.log("PhotoModalController.url==>", PhotoModalController.url, index);
		 $upload.upload({
             url : PhotoModalController.url,
             method: $scope.httpMethod,
             file: $scope.selectedFiles[index],
             fileFormDataName: 'profile-photo'
         }).success(function(data, status, headers, config) {
			usSpinnerService.stop('loading..');
			profilePhotoModal.CloseModal();
		}).error(function(data, status, headers, config) {
			console.log("Error ", config);
            prompt("Error");
        });
	} // End of start
	
}

beautypop.controller('CommentController', 
		function($scope, $location, $route, $translate, $http, $anchorScroll, comments, userInfo, postService) {
	
	writeMetaCanonical($location.absUrl());
	
	$scope.comments = comments;
	$scope.userInfo = userInfo;
	console.log($scope.comments);
	var url = $location.absUrl();
	var values= url.split("/");
	$scope.pid = values[values.length-1];
	$scope.submit = function(commentBody) {
		var newCommentVM = {
				"postId" : $scope.pid,
				"body" : commentBody,
		};
		$http.post('/comment/new', newCommentVM) 
		.success(function(response) {
			$scope.comments.push({
				body:commentBody,
				ownerName: $scope.userInfo.displayName,
				ownerId: $scope.userInfo.id
			});
			commentBody="";
		});
	}
	
	var off = 1;
	var loadingMore = false;
	$scope.loadMore = true;
	$scope.loadMoreComments = function () {
		if($scope.comments.length > 0 && $scope.loadMore && !loadingMore){
			loadingMore = true;
			postService.allComments.get({id:$scope.pid, offset:off}, function(data){
				off++;
				if (data.length == 0) {
					$scope.loadMore = false;
				}
				angular.forEach(data, function(value, key) {	
					if (loadingMore) {
						$scope.comments.push(value);
					}
				});
				loadingMore = false;

			});
		}
	}

	// UI helper
	$(window).scroll(function(e){
		$scope.position = window.pageYOffset;
		if($scope.position > 750) {
			$("#back-to-top").show();
		} else {
			$("#back-to-top").hide();
		}
	});
	
	$scope.gotoTop=function(){
		$location.hash('');
		$anchorScroll();
	};
	
});

beautypop.controller('UserFollowController', 
		function($scope, $translate, $location, $route, $anchorScroll, $http, followers, userInfo, profileUser, followService) {
	
	writeMetaCanonical($location.absUrl());

	$scope.followers = followers;
	$scope.userInfo = userInfo;
	$scope.profileUser = profileUser;
	console.log($scope.followers);
	console.log($scope.userInfo);
	var url = $location.absUrl();
	if (url.indexOf('followers') > -1) {
		$scope.follow = 'followers'
	} else if (url.indexOf('followings') > -1) {
		$scope.follow = 'followings'
	}
	
	//var values = url.split("/");
	//$scope.follow = values[values.length-2];
	if($scope.follow == 'followers' || 
			$scope.follow == 'followings') {
		$scope.loadMore = true;
	}
	
	$scope.onFollowUser = function(user) {
		if(user.id != $scope.userInfo.id){
			followService.followUser.get({id:user.id});
			user.isFollowing = !user.isFollowing;
			user.numFollowings++;
		}
	}
	
	$scope.onUnFollowUser = function(user) {
		followService.unFollowUser.get({id:user.id});
		user.isFollowing = !user.isFollowing;
		user.numFollowings--;
	}

	var off = 1;
	var loadingMore = false;
	$scope.loadMore = true;
	$scope.loadMoreFollowers = function () {
		if($scope.followers.length > 0 && $scope.loadMore && !loadingMore){
			if($scope.follow == 'followers'){
				loadingMore = true;
				followService.userfollowers.get({id:$scope.profileUser.id, offset:off}, function(data){
					off++;
					if (data.length == 0) {
						$scope.loadMore = false;
					}
					console.log(data);
					angular.forEach(data, function(value, key) {
						if (loadingMore) {
							$scope.followers.push(value);
						}
					});
					loadingMore = false;
				});
			}
			if($scope.follow == 'followings'){
				loadingMore = true;
				followService.userfollowings.get({id:$scope.profileUser.id, offset:off}, function(data){
					off++;
					if (data.length == 0) {
						$scope.loadMore = false;
					}
					console.log(data);
					angular.forEach(data, function(value, key) {
						if (loadingMore) {
							$scope.followers.push(value);
						}
					});
					loadingMore = false;
				});
			}	
		}	
	}
	
	// UI helper
	$(window).scroll(function(e){
		$scope.position = window.pageYOffset;
		if($scope.position > 750) {
			$("#back-to-top").show();
		} else {
			$("#back-to-top").hide();
		}
	});
	
	$scope.gotoTop=function(){
		$location.hash('');
		$anchorScroll();
	};
});

/*
beautypop.controller('CreateCollectionController', 
		function($scope, $location, $route, $http, usSpinnerService) {
	
	writeMetaCanonical($location.absUrl());
	
	$scope.formData = {};
	$scope.createCollection = function() {
		console.log($scope.formData);
		usSpinnerService.spin('loading...');
		$http.post('/create-collection', $scope.formData)
		.success(function(data){
			usSpinnerService.stop('loading...');
		});
	}
});
*/

beautypop.controller('CreateProductController',
		function($scope, $location, $http, $upload, $validator, usSpinnerService, userInfo){
	
	writeMetaCanonical($location.absUrl());
	
	$scope.userInfo = userInfo;
	$scope.formData = {};
	$scope.selectedFiles =[];
	$scope.submitBtn = "發出";
	$scope.submit = function() {
		console.log($scope.formData);		
		var newPostVM = {
				"catId" : $scope.formData.category,
				"title" : $scope.formData.name,
				"body" : $scope.formData.body,
				"price" : $scope.formData.price,
		};
		console.log(newPostVM);

		usSpinnerService.spin('loading...');
		$validator.validate($scope, 'formData')
		.success(function () {
			usSpinnerService.spin('載入中...');
			$upload.upload({
				url: '/product/new/form',
				method: 'POST',
				file: $scope.selectedFiles,
				data: newPostVM,
				fileFormDataName: 'photo'
			}).progress(function(evt) {
				$scope.submitBtn = "請稍候...";
				usSpinnerService.stop('載入中...');
			}).success(function(data, status, headers, config) {
				$scope.submitBtn = "完成";
				usSpinnerService.stop('loading...');
			}).error(function(data, status, headers, config) {
				if( status == 505 ) {
					$scope.uniqueName = true;
					usSpinnerService.stop('載入中...');
					$scope.submitBtn = "再試一次";
				}  
			});
		})
		.error(function () {
			prompt("建立社群失敗。請重試");
		});
	}

	$scope.onFileSelect = function($files) {
		$scope.selectedFiles.push($files[0]);
		$scope.formData.photo = 'photo';
	}

});
