
var ga_send_pv = function(page_code) {
    if (window.location.host.indexOf('localhost') === 0 || window.location.host.indexOf('192.168.1') === 0) {
        return;
    }
    
    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

    ga('create', 'UA-76753221-2', 'auto');
    ga('send', 'pageview');
    console.log('ga_send_pv - '+window.location.host);
}