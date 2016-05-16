jQuery(function ($) {
    $("#telefone").mask("(999) 9999-9999", {placeholder: "(999) 9999-9999"});
});

$(document).ready(function () {
    $("#adicionaNovoUsuario").click(function () {
        $("p").show();
    });
    $("#CancelarAdicionarNovoUsuario").click(function () {
        $("p").show();
    });
});
