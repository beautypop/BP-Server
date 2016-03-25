
app.controller('homeController',function($scope,$http,$state,$cookies){
	/*for login beautypop */
	$scope.onLogin = function(data){
		 $http({method:'POST',url:'/login/mobile', data: data})
		 .success(function(response){
			 $cookies.put("accessToken", response);
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

app.controller('importController',function($scope,$http,$state,$location,$rootScope,notificationService){
	$scope.selectedImages = [];
	$scope.mediaData=[];
	var selectedImageCount = 0;
	$scope.maxiumumImageCount = 20;
	
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
							"caption": cardsData[i].attributes[2].nodeValue,
							"mediaId": cardsData[i].attributes[3].nodeValue
					};
					$scope.selectedImages.push(imageJson);
				}
			}
		}
		$rootScope.importedImages = $scope.selectedImages;
		$state.go("addDetails");
	};

	// Select Image to Import
	$scope.selectImages = function(isImported) {
		if(!isImported){
			if($(event.currentTarget).find('div.checkbox').hasClass('selected')){
				$(event.currentTarget).find('div.checkbox').removeClass('selected');
				$(event.currentTarget).find('div.overlay').css('display', 'none');
				$(event.currentTarget).find('div.checkbox').css("background", "url(images/unselected.png) center no-repeat");
				selectedImageCount--;
			}else{
				if(selectedImageCount < $scope.maxiumumImageCount){
					$(event.currentTarget).find('div.checkbox').addClass('selected');
					$(event.currentTarget).find('div.overlay').css('display', 'block');
					$(event.currentTarget).find('div.checkbox').css("background", "url(images/selected.png) center no-repeat");
					selectedImageCount++;
				}else{
					notificationService.error("Maximum "+$scope.maxiumumImageCount+" Photos Allowed");
				}
			}
			
			$scope.validateImport();
		}
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
		
		var totalCheckbox = 0;
		$('.checkbox').each(function(index,el){
			if($(el).next().attr("ng-hide") == "true"){
				totalCheckbox++;
			}
		});
		
		if(selectedImageCount == totalCheckbox || selectedImageCount == $scope.maxiumumImageCount){
			$('.ui-checkbox').css("background", "url(images/selected.png) center no-repeat");
		}else{
			$('.ui-checkbox').css("background", "url(images/unselected.png) center no-repeat");
		}
	}
	
	// Select all images to import
	$scope.onSelectAll = function() {
		if($(event.currentTarget).hasClass('selected')){
			$(".checkbox").removeClass("selected");
			$('.checkbox').css("background", "url(images/unselected.png) center no-repeat");
			$('.overlay').css('display', 'none');
			$(event.currentTarget).css("background", "url(images/unselected.png) center no-repeat");
			$(event.currentTarget).removeClass("selected");
			selectedImageCount = 0;
		}else{
			$('.checkbox').each(function(index,el){
			    if(index < $scope.maxiumumImageCount){
					if($(el).next().attr("ng-hide") == "true"){
				    	$(el).addClass("selected");
					    $(el).css("background", "url(images/selected.png) center no-repeat");
					    $(el).parent().find('.overlay').css('display', 'block');
					    selectedImageCount = index+1;
					} 
			    }
			});
			
			$(event.currentTarget).css("background", "url(images/selected.png) center no-repeat");
			$(event.currentTarget).addClass("selected");
		}
		
		$scope.validateImport();
	};

});

app.controller('detailsController',function($scope,$http,$state,$upload,notificationService,$cookies,$rootScope){
	var imagesData = $rootScope.importedImages;
	var detailsData = [];
	$scope.hidePromotedSellerFields = true;
	
	//get countries
	$http.get('/get-countries',{}).then(function(res){
		$scope.countriesData=res.data;
	});
	
	//get categories
	$http.get('/get-categories',{}).then(function(res){
		$scope.categoriesData=res.data;
	});
	
	//get user info to verify promoted seller or verified seller
	$http.get('/get-user-info?key='+$cookies.get("accessToken"),{}).then(function(res){
		if(res.promotedSeller || res.verifiedSeller){
			$scope.hidePromotedSellerFields = false;
		}
	});
	
	//create json to post product for beautypop 
	for(var i=0; i<imagesData.length; i++){
		var temp = {
				"title" : "",
				"body" : imagesData[i].caption,
				"catId" : "",
				"price" : "0",
				"originalPrice" : "0",
				"freeDelivery" : "",
				"conditionType" : "",
				"countryCode": "",
				"deviceType": "",
				"images" : imagesData[i].imageUrl,
				"mediaId" : imagesData[i].mediaId
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
	
	//Post photos to beautypop
	$scope.errorMessageHide = true;
	$scope.postPhotos = function(data){
		if($scope.myform.$valid){
			$scope.sendImages(0);
		}else{
			$scope.errorMessageHide = false;
		}
	};
	
	$scope.sendImages = function(count){
		if(count != $scope.listingData.length){
			var postData = $scope.listingData[count];
			if(postData.originalPrice == null){
				postData.originalPrice = 0;
			}
			var file = [{}];	
			
			$upload.upload({
				url : '/instagram/product/new',
				data : postData,
				file : file,
				method : 'post',
				fileFormDataName: 'photo'
			}).success(function(data) {
				$scope.sendImages(count+1);
			}).error(function(data){
				notificationService.error("Failed to import from Instagram. Please make sure all fields are filled out correctly. Please try again later.");
			});
		}else{
			$state.go("complete");
			notificationService.success("Posted to beautypop successfully");
		}
	} 
	
	//apply to all fields
	$scope.onApplyAll = function(value, selectedCheckbox, selectedComponent){
		if($(event.currentTarget).hasClass('selected')){
			$("."+selectedCheckbox).removeClass("selected");
			$("."+selectedCheckbox).css("background", "url(images/unselected.png) center no-repeat");
		}else{
			$("."+selectedCheckbox).addClass("selected");
			$("."+selectedCheckbox).css("background", "url(images/selected.png) center no-repeat");
			for(var i=0; i<$scope.listingData.length; i++){
				if(selectedComponent in $scope.listingData[i]){
					$scope.listingData[i][selectedComponent] = value;
				}
			}
		}
	}
	
	$scope.makeAllSame = function(value, selectedComponent){
		if($(event.currentTarget).next().find(".ui-checkbox").hasClass('selected')){
			for(var i=0; i<$scope.listingData.length; i++){
				if(selectedComponent in $scope.listingData[i]){
					$scope.listingData[i][selectedComponent] = value;
				}
			}
		}
	}
});

app.controller('completeController',function($scope,$http,$state){
	
});
