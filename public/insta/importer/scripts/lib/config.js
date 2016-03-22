
app.config(function($stateProvider, $urlRouterProvider, $ocLazyLoadProvider){

    $ocLazyLoadProvider.config({
        // Set to true if you want to see what and when is dynamically loaded
        debug: false
    });

$urlRouterProvider.otherwise("/");
    $stateProvider
	    .state('home', {
	        url: "/",
	        templateUrl: "home.html",
	        controller: "homeController",
	        resolve: {
	            loadPlugin: function ($ocLazyLoad) {
	                return $ocLazyLoad.load([
	
	                ]);
	            }
	        }
	
	    })
        .state('connect', {
            url: "/connect",
            templateUrl: "connect.html",
            controller: "connectController",
            resolve: {
                loadPlugin: function ($ocLazyLoad) {
                    return $ocLazyLoad.load([

                    ]);
                }
            }

        }).state('import', {
            url: "/import",
            templateUrl: "import.html",
            controller: "importController",
            resolve: {
                loadPlugin: function ($ocLazyLoad) {
                    return $ocLazyLoad.load([

                    ]);
                }
            }

        }).state('addDetails', {
            url: "/details",
            templateUrl: "addDetails.html",
            controller: "detailsController",
            resolve: {
                loadPlugin: function ($ocLazyLoad) {
                    return $ocLazyLoad.load([

                    ]);
                }
            }

        }).state('complete', {
            url: "/complete",
            templateUrl: "complete.html",
            controller: "completeController",
            resolve: {
                loadPlugin: function ($ocLazyLoad) {
                    return $ocLazyLoad.load([

                    ]);
                }
            }

        });

});
