
app.controller('homeController',function($scope,$http,$state,$window,$cookies,notificationService){
	/*for login babybox */
	$scope.onLogin = function(data){
		 $http({method:'POST',url:'/login/mobile', data: data})
		 .success(function(response){
			 $state.go("connect");
		 }).error(function(data){
			 alert("Invalid email or password");
		});
	}
});

app.controller('connectController',function($scope,$http,$state,$location,$window){
	$scope.connectToInstagram = function(){
		$window.location.href = '/loginInstagram'; //connect to instagram
	}
});

app.controller('importController',function($scope,$http,$state,$location,$rootScope){
	$scope.selectedImages = [];
	$scope.mediaData=[];
	var selectedImageCount = 0;
	
	var url = $location.absUrl(); // Returns full URL
	var code = (url.split("?"))[1]; // Get Code sent by instagram
	
	// Send code to get Instagram api token
	$http.get('/connect/'+ code,{}).then(function(res){
		
		$http.get('/import',{}).then(function(res){ //Import images from instagram
			$scope.mediaData=res.data;
		});
		
	});
	
	// Get Instagram Photos
	$scope.importPhoto = function() {
		var cardsData = document.getElementsByClassName("card");
		for(var i=0; i<cardsData.length; i++){
			for(var j=0; j<cardsData[i].children[2].classList.length; j++){
				if(cardsData[i].children[2].classList[j] == 'selected'){
					var temp = cardsData[i].children[1].style.backgroundImage
					var imageJson = {
							"imageUrl": temp.substring(temp.indexOf('"') + 1, temp.lastIndexOf('"')),
							"caption": cardsData[i].attributes[2].nodeValue
					};
					$scope.selectedImages.push(imageJson);
				}
			}
		}
		$rootScope.importedImages = $scope.selectedImages;
		$state.go("addDetails");
	};

	// Select Image to Import
	$scope.selectImages = function() {  
		if($(event.currentTarget).find('div.checkbox').hasClass('selected')){
			$(event.currentTarget).find('div.checkbox').removeClass('selected');
			$(event.currentTarget).find('div.overlay').css('display', 'none');
			$(event.currentTarget).find('div.checkbox').css("background", "url(stylesheets/images/unselected.png) center no-repeat");
			selectedImageCount--;
		}else{
			$(event.currentTarget).find('div.checkbox').addClass('selected');
			$(event.currentTarget).find('div.overlay').css('display', 'block');
			$(event.currentTarget).find('div.checkbox').css("background", "url(stylesheets/images/selected.png) center no-repeat");
			selectedImageCount++;
		}
		
		$scope.validateImport();

	};
	
	$scope.validateImport = function(){
		if(selectedImageCount > 0){
			$("#select-view a").removeClass('disabled');
			var importLabel = "";
			if(selectedImageCount == 1){
				importLabel = "Import 1 photo";
			}else{
				importLabel = "Import "+selectedImageCount+" photos";
			}
			$("#select-view a").text(importLabel);
		}else{
			$("#select-view a").addClass('disabled');
			$("#select-view a").text("Pick some photos");
		}
		
		if(selectedImageCount == $(".checkbox").length){
			$('.ui-checkbox').css("background", "url(stylesheets/images/selected.png) center no-repeat");
		}else{
			$('.ui-checkbox').css("background", "url(stylesheets/images/unselected.png) center no-repeat");
		}
	}
	
	// Select all images to import
	$scope.onSelectAll = function() {
		if($(event.currentTarget).hasClass('selected')){
			$(".checkbox").removeClass("selected");
			$('.checkbox').css("background", "url(stylesheets/images/unselected.png) center no-repeat");
			$('.overlay').css('display', 'none');
			$(event.currentTarget).css("background", "url(stylesheets/images/unselected.png) center no-repeat");
			$(event.currentTarget).removeClass("selected");
			selectedImageCount = 0;
		}else{
			$(".checkbox").addClass("selected");
			$('.checkbox').css("background", "url(stylesheets/images/selected.png) center no-repeat");
			$('.overlay').css('display', 'block');
			$(event.currentTarget).css("background", "url(stylesheets/images/selected.png) center no-repeat");
			$(event.currentTarget).addClass("selected");
			selectedImageCount = $(".checkbox").length;
		}
		
		$scope.validateImport();
	};

});

app.controller('detailsController',function($scope,$http,$state,$location,$window,$rootScope,$upload,$timeout,notificationService){
	var imagesData = $rootScope.importedImages;
	var detailsData = [];
	
	//create json to post product for babybox 
	for(var i=0; i<imagesData.length; i++){
		var temp = {
				"title" : "",
				"body" : imagesData[i].caption,
				"catId" : "",
				"price" : "",
				"originalPrice" : "",
				"freeDelivery" : "",
				"conditionType" : "",
				"countryCode": "",
				"hashtags": "",
				"deviceType": "",
				"images" : imagesData[i].imageUrl
		}
		detailsData.push(temp);
	}
	$scope.listingData = detailsData;
	
	// create label
	var count  = $rootScope.importedImages.length;
	if(count == 1){
		$scope.imagesCount = $rootScope.importedImages.length+" Listing";
	}else{
		$scope.imagesCount = $rootScope.importedImages.length+" Listings";
	}
	
	//Post photos to babybox
	$scope.postPhotos = function(data){
		$scope.sendImages(0);
	};
	
	$scope.sendImages = function(count){
		if(count != $scope.listingData.length){
			var postData = $scope.listingData[count];
			var file = [{}];	
			
			$upload.upload({
				url : 'instagram/product/new',
				data : postData,
				file : file,
				method : 'post',
				fileFormDataName: 'photo'
			}).success(function(data) {
				$scope.sendImages(count+1);
			}).error(function(data){
			});
		}else{
			$state.go("connect");
			notificationService.success("Posted to Babybox successfully");
		}
	} 
	
	
});

app.controller('signupController',function($scope,$http,$state,$location,$window,$files){
	
	//Register on babybox
	$scope.onSignUp = function(data){
		$http({method:'POST',url:'instagram/signup', data: data})
		 .success(function(response){
			 notificationService.success("Registered successfully");	
			 $state.go("/");
		 }).error(function(data){
			 notificationService.error("Error occured while registering.");
		});
	}
	
});
