$(document).ready(function() {
	beautypop = this;
    
    // validations
    $("#signup-info").validate({
        //debug : true,
        rules : {
            parent_firstname : {
                required : true,
                minlength : 2,
                maxlength : 15
            },
            parent_lastname : {
                required : true,
                minlength : 2,
                maxlength : 15
            },
            parent_displayname : {
                required : true,
                minlength : 2,
                maxlength : 15
            },
            parent_location : {
                required : true
            }
        },
        messages : {
            parent_firstname : {
                required : "請填寫您的名字",
                minlength : "名字最小2個字",
                maxlength : "名字最多15個字"
            },
            parent_lastname : {
                required : "請填寫您的姓氏",
                minlength : "姓氏最小2個字",
                maxlength : "姓氏最多15個字"
            },
            parent_displayname : {
                required : "請填寫您的顯示名稱",
                minlength : "顯示名稱最小2個字",
                maxlength : "顯示名稱最多15個字"
            },
            parent_location : {
                required : "請選擇您的地區"
            }
        },
        errorPlacement: function (error, element) {
            if (element.attr("type") == "radio") {
                error.insertBefore(element);
            } else {
                error.insertAfter(element);
            }
        }
    });
});