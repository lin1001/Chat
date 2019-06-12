<%@ page contentType="text/html;charset=UTF-8" language="java"
	pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<html>
<head>
<title>WebChat | 聊天</title>
<jsp:include page="include/commonfile.jsp" />
<script src="${ctx}/static/plugins/sockjs/sockjs.js"></script>
<script src="https://cdn.bootcss.com/jquery/3.4.0/jquery.min.js"></script>
</head>
<body>
	<header class="am-topbar admin-header">
		<div class="am-topbar-brand">
			<i class="am-icon-weixin"></i> <strong>WebChat</strong> <small>网页聊天室</small>
		</div>
		<button
			class="am-topbar-btn am-topbar-toggle am-btn am-btn-sm am-btn-success am-show-sm-only"
			data-am-collapse="{target: '#topbar-collapse'}">
			<span class="am-sr-only">导航切换</span> <span class="am-icon-bars"></span>
		</button>
		<div class="am-collapse am-topbar-collapse" id="topbar-collapse">

			<ul
				class="am-nav am-nav-pills am-topbar-nav am-topbar-right admin-header-list">
				<li class="am-dropdown" data-am-dropdown><a
					class="am-dropdown-toggle" data-am-dropdown-toggle
					href="javascript:;"> ${userid} <span class="am-icon-caret-down"></span>
				</a>
					<ul class="am-dropdown-content">
						<li onclick="team()"><a><span class="am-icon-male"></span>发起组队</a></li>
						<li><a href="${ctx}/${userid}"><span class="am-icon-user"></span>
								资料</a></li>
						<li><a href="${ctx}/${userid}/config"><span
								class="am-icon-cog"></span> 设置</a></li>
						<li><a href="${ctx}/${userid}/log"><span
								class="am-icon-inbox"></span> 系统日志</a></li>
						<li><a href="${ctx}/room/quit"><span
								class="am-icon-power-off"></span> 退出</a></li>
					</ul></li>
			</ul>
			<ul
				class="am-nav am-nav-pills am-topbar-nav am-topbar-right admin-header-list">
				<li class="am-dropdown" data-am-dropdown><a
					class="am-dropdown-toggle" id="news"> 消息 </a>
					<ul class="am-dropdown-content" id="news-ul">

					</ul></li>
			</ul>
		</div>
	</header>
	<div class="am-cf admin-main">

		<!-- content start -->
		<div class="admin-content"">
			<div class="" style="width: 80%;float:left; ">
				<!-- 聊天区 -->
				<div class="am-scrollable-vertical" id="chat-view"
					style="height:70%;">
					<ul class="am-comments-list am-comments-list-flip" id="chat">
					</ul>
				</div>
				<!-- 输入区 -->
				<div class="am-form-group am-form">
					<textarea class="" id="message" name="message" rows="5"
						placeholder="输入你想发送的信息..."></textarea>
				</div>
				<!-- 按钮区 -->
				<div class="am-btn-group am-btn-group-xs" style="float:right;">
					<button class="am-btn am-btn-default" type="button"
						onclick="sendMessage()">
						<span class="am-icon-commenting"></span> 发送
					</button>
				</div>
			</div>
			<!-- 列表区 -->
			<div class="am-panel am-panel-default"
				style="float:right;width: 20%; height:100%;" id="list_div">
				<div class="am-panel-hd">
					<h3 class="am-panel-title">
						在线列表 [<span id="onlinenum"></span>]
					</h3>
				</div>

				<ul class="am-list am-list-static am-list-striped" id="list">
				</ul>
			</div>

			<div id="team_div" style="float:right;width: 20%;height:35%;">

			</div>
		</div>
		<!-- content end -->
	</div>
	<a href="#" class="am-show-sm-only admin-menu"
		data-am-offcanvas="{target: '#admin-offcanvas'}"> <span
		class="am-icon-btn am-icon-th-list"></span>
	</a>
	<jsp:include page="include/footer.jsp" />

	<script>
		$(function() {
			context.init({
				preventDoubleContext : false
			});
			context.settings({
				compress : true
			});
			context.attach('#chat-view', [
				{
					header : '操作菜单',
				},
				{
					text : '清理',
					action : clearConsole
				},
				{
					divider : true
				},
				{
					text : '选项',
					subMenu : [
						{
							header : '连接选项'
						},
						{
							text : '检查',
							action : checkConnection
						},
						{
							text : '连接',
							action : getConnection
						},
						{
							text : '断开',
							action : closeConnection
						}
					]
				},
				{
					text : '销毁菜单',
					action : function(e) {
						e.preventDefault();
						context.destroy('#chat-view');
					}
				}
			]);
		});
		if ("${message}") {
			layer.msg('${message}', {
				offset : 0
			});
		}
		if ("${error}") {
			layer.msg('${error}', {
				offset : 0,
				shift : 6
			});
		}
	
		var wsServer = null;
		var ws = null;
		wsServer = "ws://" + location.host + "${pageContext.request.contextPath}" + "/chatServer/${roomId}";
		ws = new WebSocket(wsServer); //创建WebSocket对象
		ws.onopen = function(evt) {
			layer.msg("已经建立连接", {
				offset : 0
			});
		};
		ws.onmessage = function(evt) {
			analysisMessage(evt.data); //解析后台传回的消息,并予以展示
		};
		ws.onerror = function(evt) {
			layer.msg("产生异常", {
				offset : 0
			});
		};
		ws.onclose = function(evt) {
			layer.msg("已经关闭连接", {
				offset : 0
			});
		};
	
		/**
		 * 连接
		 */
		function getConnection() {
			if (ws == null) {
				ws = new WebSocket(wsServer); //创建WebSocket对象
				ws.onopen = function(evt) {
					layer.msg("成功建立连接!", {
						offset : 0
					});
				};
				ws.onmessage = function(evt) {
					analysisMessage(evt.data); //解析后台传回的消息,并予以展示
				};
				ws.onerror = function(evt) {
					layer.msg("产生异常", {
						offset : 0
					});
				};
				ws.onclose = function(evt) {
					layer.msg("已经关闭连接", {
						offset : 0
					});
				};
			} else {
				layer.msg("连接已存在!", {
					offset : 0,
					shift : 6
				});
			}
		}
	
		/**
		 * 关闭连接
		 */
		function closeConnection() {
			if (ws != null) {
				ws.close();
				ws = null;
				$("#list").html(""); //清空在线列表
				layer.msg("已经关闭连接", {
					offset : 0
				});
			} else {
				layer.msg("未开启连接", {
					offset : 0,
					shift : 6
				});
			}
		}
	
		/**
		 * 检查连接
		 */
		function checkConnection() {
			if (ws != null) {
				layer.msg(ws.readyState == 0 ? "连接异常" : "连接正常", {
					offset : 0
				});
			} else {
				layer.msg("连接未开启!", {
					offset : 0,
					shift : 6
				});
			}
		}
	
		/**
		 * 发送信息给后台
		 */
		function sendMessage() {
			if (ws == null) {
				layer.msg("连接未开启!", {
					offset : 0,
					shift : 6
				});
				return;
			}
			var message = $("#message").val();
			if (message == null || message == "") {
				layer.msg("请输入内容", {
					offset : 1,
					shift : 6
				});
				return;
			}
	
			ws.send(JSON.stringify({
				message : {
					content : message,
					from : '${userid}',
					time : getDateFull()
				},
				type : "message"
			}));
		}
	
		/**
		 * 解析后台传来的消息
		 * "massage" : {
		 *              "from" : "xxx",
		 *              "content" : "xxx",
		 *              "time" : "xxxx.xx.xx"
		 *          },
		 * "type" : {notice|message|news},
		 * "list" : {[xx],[xx],[xx]}
		 * "roomId":{"xxx"},
		 * "teamId":{"xxx"},
		 */
		function analysisMessage(message) {
			message = JSON.parse(message);
			if (message.set != null) {
				for (var mes in message.set) {
					showChat(JSON.parse(message.set[mes]).message);
				}
			} else {
				if (message.type == "message") { //会话消息
					showChat(message.message);
				}else
				if (message.type == "notice") { //提示消息
					showNotice(message.message);
				}else
				if (message.type == "news") { //提示消息
					showNews(message);
				}else
				if(message.type == "team"){
					showTeam(message);
				}else
					if(message.type == "talk"){
						showTeamRoom(message.message);
					}
				if (message.list != null && message.list != undefined) { //在线列表
					showOnline(message.list);
				}
			}
		}
	
		/**
		 * 展示提示信息
		 */
		function showNotice(notice) {
			$("#chat").append("<div><p class=\"am-text-success\" style=\"text-align:center\"><span class=\"am-icon-bell\"></span> " + notice + "</p></div>");
			var chat = $("#chat-view");
			chat.scrollTop(chat[0].scrollHeight); //让聊天区始终滚动到最下面
		}
	
		/**
		 * 通知消息
		 */
		var timerArr;
		function showNews(news) {
			$("#news").text("新消息");
			jQuery(function($) {
				timerArr = $.blinkTitle.show();
			});
			$("#news-ul").append("<li onclick=\"deleteLi(this)\"><a target=\"_blank\" href=\"${ctx}/talk/" + news.roomId + "\">" + JSON.parse(news.message).from + "</a></li>");
	
		}
		/**
		 * 开启组队
		 *
		 *"massage" : {
		 *              "from" : "xxx",
		 *              "content" : "xxx",
		 *              "time" : "xxxx.xx.xx",
		 *				"team_type":"x",
		 *				"amount":"xxx"
		 *          },
		 *"state":"x"
		 */
		function showTeam(team) {
			//console.log(team);
			//console.log(team.message);
			var message = JSON.parse(team.message);
			var from = message.from=='${userid}'?"您":message.from;
			var state = team.state==1?" disabled=\"disabled\">已加入":" >加入"
			//if(message.team_type=="1"){
				$('#team_div').html("<form class=\"am-form\" action=\"${ctx}/active/join\" method=\"post\" onsubmit=\"return subJoin();\" id=\"join_form\"><fieldset><legend>"+from+message.content+"</legend><div class= \"am-form-groupam-form-icon\" style=\"text-align:center;\"> <p class=\"am-text-xxxl\"><i class=\"am-icon-male\"></i>"+message.amount+"</p></div><div class=\"\" style=\"text-align:center;\"><input type=\"hidden\" name=\"teamId\" value=\""+team.teamId+"\"/> <input type=\"hidden\" name=\"team_type\" value=\""+message.team_type+"\"/> <button class=\"am-btn am-btn-primary am-btn-xl am-round\" "+state+"</button></div></fieldset></form>");
				$("#list_div").height("50%");
			//}else{
				//$('#team_div').html('2');
			//}
		}
			function subJoin() {  
				console.log($('#join_form').serialize());
				$.ajax({  
	                type: "post",  
	                url:"${ctx}/active/join",  
	                data:$('#join_form').serialize(), 
	                async: true,  
	                error: function(request) {  
	                      
	                },  
	                success: function(data) {  
	                      console.log(data);
	                }  
	            });
				return false;
	    }
		function showTeamRoom(mes) {
			
			var message = JSON.parse(mes);
			console.log(message);
			if(message.type=="1"){
				$('#team_div').html("<form class=\"am-form\"  action=\"${ctx}/"+message.url+"\" target=\"_blank\" method=\"post\" id=\"teamRoom_form\"><fieldset><legend></legend><div class= \"am-form-groupam-form-icon\" style=\"text-align:center;\"> <p class=\"am-text-xxxl\">"+message.content+"</p></div><div class=\"\" style=\"text-align:center;\"><button class=\"am-btn am-btn-primary am-btn-xl am-round\"  onclick=\"subTeamRoom("+message.url+");\">确认</button></div></fieldset></form>");
			}else{
				$('#team_div').html("<form class=\"am-form\"  onsubmit=\"return subTeamRoom();\" id=\"teamRoom_form\"><fieldset><legend></legend><div class= \"am-form-groupam-form-icon\" style=\"text-align:center;\"> <p class=\"am-text-xxxl\">"+message.content+"</p></div><div class=\"\" style=\"text-align:center;\"><button class=\"am-btn am-btn-primary am-btn-xl am-round\">确认</button></div></fieldset></form>");
			}
			$("#list_div").height("50%");
		}
		function subTeamRoom(){
			console.log("teamRoom_success");
			$('#team_div').html("");
			$("#list_div").height("100%");
			return true;
		}
		/**
		 * 删除通知
		 */
		function deleteLi(obj) {
			var $object = $(obj);
			var index = $object.index();
			var li_list = document.getElementById('news-ul').querySelectorAll('li');
			li_list[index].remove();
			$.blinkTitle.clear(timerArr);
		}
	
		/**
		 * 展示会话信息
		 */
		function showChat(message) {
			var isSef = '${userid}' == message.from ? "am-comment-flip am-comment-primary" : "am-comment-success"; //如果是自己则显示在右边,他人信息显示在左边
			var html = "<li class=\"am-comment " + isSef + " \"><a href=\"#link-to-user-home\"><img width=\"48\" height=\"48\" class=\"am-comment-avatar\" alt=\"\" src=\"${ctx}/" + message.from + "/head\"></a><div class=\"am-comment-main\">\n" +
				"<header class=\"am-comment-hd\"><div class=\"am-comment-meta\">   <a class=\"am-comment-author\" href=\"#link-to-user\">" + message.from + "</a> 发表于<time> " + message.time + "</time></div></header><div class=\"am-comment-bd\"> <p>" + message.content + "</p></div></div></li>";
			$("#chat").append(html);
			$("#message").val(""); //清空输入区
			var chat = $("#chat-view");
			chat.scrollTop(chat[0].scrollHeight); //让聊天区始终滚动到最下面
		}
	
		/**
		 * 展示在线列表
		 */
		function showOnline(list) {
			$("#list").html(""); //清空在线列表
			$.each(list, function(index, item) { //添加私聊按钮
				var li = "<li>" + item + "</li>";
				if ('${userid}' != item) { //排除自己
					li = "<li>" + item + " <button type=\"button\" class=\"am-btn am-btn-xs am-btn-primary am-round\" onclick=\"talk('" + item + "');\"><span class=\"am-icon-phone\"><span> 私聊</button></li>";
				}
				$("#list").append(li);
			});
			$("#onlinenum").text($("#list li").length); //获取在线人数
		}
	
	
	
	
		function talk(user) {
			//$("#chat").append("1" + user);
			window.open("${ctx}/room/talk?talkId=" + user);
		}
		/**
		 * 清空聊天区
		 */
		function clearConsole() {
			$("#chat").html("");
		}
	
		function appendZero(s) {
			return ("00" + s).substr((s + "").length);
		} //补0函数
	
		function getDateFull() {
			var date = new Date();
			var currentdate = date.getFullYear() + "-" + appendZero(date.getMonth() + 1) + "-" + appendZero(date.getDate()) + " " + appendZero(date.getHours()) + ":" + appendZero(date.getMinutes()) + ":" + appendZero(date.getSeconds());
			return currentdate;
		}
		//发起组队
		function team(){
			
			$("#team_div").html("<form class=\"am-form\" action=\"${ctx}/active/team\" method=\"post\"onsubmit=\"return sub();\" id=\"team_form\"><fieldset><legend>发起组队</legend><div class=\"am-form-group am-form-icon\"><i class=\"am-icon-male\"></i> <input type=\"amount\" name=\"amount\"class=\"am-form-field\" placeholder=\"人数\"></div><div class=\"am-form-group\"><label class=\"am-radio-inline\"> <input type=\"radio\"value=\"1\" name=\"team_type\"> 抢占</label> <label class=\"am-radio-inline\"> <input type=\"radio\"value=\"2\" name=\"team_type\"> 随机</label></div><div class=\"am-form-group\"><label for=\"doc-select-1\">时限</label> <select id=\"doc-select-1\"name=\"time\"><option value=\"30\">30s</option><option value=\"60\">60s</option><option value=\"120\">120s</option></select> <span class=\"am-form-caret\"></span></div><p><button  class=\"am-btn am-btn-default\" name=\"roomId\"value=\"${roomId}\">开启</button></p></fieldset></form>");
			$("#list_div").height("50%")
		}
		//不跳转
		function sub() {  
			var roomId = '${roomId}';
			console.log($('#team_form').serialize())
			$.ajax({  
                type: "post",  
                url:"${ctx}/active/team",  
                data:$('#team_form').serialize()+'&'+$.param({roomId:roomId}),// 你的formid  
                async: true,  
                error: function(request) {  
                      
                },  
                success: function(data) {  
                      console.log(data);
                }  
            });
			return false;
    }              
		//消息闪烁
		(function($) {
			$.extend({
				/** 
				* 调用方法： var timerArr = $.blinkTitle.show(); 
				* $.blinkTitle.clear(timerArr); 
				*/
				blinkTitle : {
					show : function() { //有新消息时在title处闪烁提示 
						var step = 0,
							_title = document.title;
						//14, 144, 210
						
						var color = $("#news").css("color");
						var timer = setInterval(function() {
							step++;
							if (step == 3) {
								step = 1
							}
	
							if (step == 1) {
								$("#news").css("color", "#eeeeee");
							}
	
							if (step == 2) {
								$("#news").css("color", "#666666");
							}
	
						}, 500);
						return [ timer, _title ];
					},
					/** 
					* @param timerArr[0], timer标记 
					* @param timerArr[1], 初始的title文本内容 
					*/
					clear : function(timerArr) { //去除闪烁提示，恢复初始title文本 
						if (timerArr) {
							clearInterval(timerArr[0]);
							$("#news").text('消息');
							$("#news").css("color", "#666666");
						}
	
					}
				}
			});
		})(jQuery);
	</script>
</body>
</html>
