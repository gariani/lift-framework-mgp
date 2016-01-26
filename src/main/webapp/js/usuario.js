angular.module("usuario", [])
.controller("config", function($scope){

    $scope.login = false;

    $scope.onLogin = function(){
        console.log("teste");
    };
});