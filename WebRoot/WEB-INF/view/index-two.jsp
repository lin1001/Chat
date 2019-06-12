<%@ page contentType="text/html;charset=UTF-8" language="java"
	pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<%String path = request.getContextPath();%>
<html>
<head>
<title>WebChat | 聊天</title>
<jsp:include page="include/commonfile.jsp" />
<script src="${ctx}/static/plugins/sockjs/sockjs.js"></script>
<script src="${ctx}/static/source/IMJ2V2/js/echo.min.js"></script>
<link rel="stylesheet"
	href="${ctx}/static/source/IMJ2V2/css/amazeui.min.css">
<link rel="stylesheet" href="${ctx}/static/source/IMJ2V2/css/style.css">
<link rel="stylesheet" href="${ctx}/static/plugins/custom.css">
</head>
<body>
	<jsp:include page="include/header.jsp" />
	
		<div class="am-container m-list" id="chat-view"
			style="height: 85%;">

			<article>
				<section class="m-case-list">
					<ul class="am-avg-sm-1 am-avg-md-2 am-avg-lg-3 am-thumbnails"
						id="room">
					</ul>
				</section>
			</article>
		</div>

	<a href="#" class="am-show-sm-only admin-menu"
		data-am-offcanvas="{target: '#admin-offcanvas'}"> <span
		class="am-icon-btn am-icon-th-list"></span>
	</a>
	<jsp:include page="include/footer.jsp" />

	<script>
	//图片懒加载
	echo.init({
	    offset: 100,
	    throttle: 250,
	    unload: false,
	    callback: function (element, op) {
	      console.log(element, 'has been', op + 'ed')
	    }
	  });
    if("${message}"){
        layer.msg('${message}', {
            offset: 0
        });
    }
    if("${error}"){
        layer.msg('${error}', {
            offset: 0,
            shift: 6
        });
    }

  
    $.ajax({
        type : "get",
        async : true,
        url : "${ctx}/room/list",
        dataType : "json",     
        success : function(data) {
        	for(var i=0;i<data.length;i++){
        		var html = "<li><div class=\"am-panel am-panel-default\"><figure class=\"effect-lily\"><img src=\"${ctx}/static/source/img/loading.gif\"data-echo=\"${ctx}/static/photo/"+data[i]+".jpg\"class=\"am-img-responsive\"><figcaption><h3>"+data[i]+"</h3><a href=\"${ctx}/room/get?roomId="+data[i]+"\">View more</a></figcaption></figure></div></li>";
        		$("#room").append(html);
        		console.log(data[i]);
        	}
            
            layer.msg(data);
        },
        error : function(errorMsg) {

        }
    });

    
	

   
</script>
</body>
</html>
