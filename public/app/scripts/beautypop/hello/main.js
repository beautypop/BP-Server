$(document).ready(function() {

    $('#feed-list').find('a').click(function (e) {
        e.preventDefault();
        dataLayer.push({
            'event': 'marketplace-link',
            'item-url' : $(this).attr('href'),
            'playlist' : $(this).parents('.playlist-container').attr('data-list-name'),
        })
        document.location.href = $(this).attr('href');
    });

    $('.top-nav li').localScroll();

    $('.top-nav').mobileMenu({
    	defaultText: 'Navigation',
    	className: 'select-menu',
    	subMenuDash: '&ndash;'
    });

    $('#main-slider').flexslider({
        animation: "fade",
        slideshowSpeed: 3500
    });

    $('.flexslider').flexslider({
        animation: "slide",
        controlNav: "thumbnails"
    });

    $('.top-nav').onePageNav();

    $(window).scroll( function() {
        var value = $(this).scrollTop();
        if ( value > 350 )
            $(".top-nav li").css("padding", "20px 15px 0px");
        else
            $(".top-nav li").css("padding", "30px 15px 10px");
    });
    if (window.outerWidth > 768) {
        $(window).scroll( function() {
            var value = $(this).scrollTop();
            if ( value > 350 )
                $(".logo h1").css("margin", "0px 0 0 0");
            else
                $(".logo h1").css("margin", "10px 0 0 0");
        });
    }
    $("a.sellform").click(function(e){
        e.preventDefault();
        $('#sellform').slideToggle();
    });

    $('#sellform form').submit(function(e){
        e.preventDefault();
        var data = $(this).serializeArray();
        data.push({name: "nospamplease", value: "sure"});

        var error = false;

        $('#sellform input').removeClass('noContent');

        $.each(data, function(k,v){
            if(v.value.length < 2) {
                $('#sellform').find("input[name='"+v.name+"']").addClass('noContent');
                console.log(v.name);
                error = true;
            }
        });

        if(error) {
            return false;
        }

        $.post('order.html', $.param(data), function(data){
            if(!data) {
                $('#sellform .row').slideToggle(function(){
                    $('#sellform .info').html("<p>Your message was succesfully sent!</p>")
                        .slideToggle(function(){
                            $(this).find('p').fadeIn();
                        });
                });
            } else {
                $('#sellform .row').slideToggle(function(){
                    $('#sellform .info').html("<p>Your message could not be delivered.</p>")
                        .slideToggle(function(){
                            $(this)
                                .find('p')
                                .fadeIn()
                                .css('color', 'red');
                        });
                });
            }

            setTimeout(function(){
                //$('.info').fadeOut();
                $('#sellform').slideToggle(function(){
                    $('#sellform .info').hide();
                    $('#sellform .row').show();
                });

                $('#sellform input').val("");
            }, 5000);
        });
    });
    
    $('#marketplace a').click(function(evt) {
    	window.open(this.href, '_blank');
    	//window.location = this.href;
    });
});

