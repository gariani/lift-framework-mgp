angular.module('usuario', ['ngRoute','Mensagem'])
.controller('config', function($scope, mensagem) {

    $scope.login = false;

    $scope.onClick = function (){
        mensagem.getResultado().then(function(msg){
            $scope.resultado = msg;
        });
    };
  });