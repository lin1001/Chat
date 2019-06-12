<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%String path = request.getContextPath();%>
<!DOCTYPE html>
<html>
<head>
  <title>WebChat | 登录</title>
  <link href="<%=path%>/static/source/css/login.css" rel='stylesheet' type='text/css' />
  <script src="<%=path%>/static/plugins/jquery/jquery-2.1.4.min.js"></script>
  <script src="<%=path%>/static/plugins/layer/layer.js"></script>
</head>
<body>

<h1 style="color:#CCCCCC">WebChat</h1>
<div class="login-form">
  <div class="head-info">注册<label  class="lbl-4"><a href="<%=path%>/user/login">登录</a></label></div>
   
  <div class="clear"> </div>
  <div class="avtar"><img src="<%=path%>/static/source/img/icon.png" width="80" height="80"  /></div>
  <form id="login-form" action="<%=path%>/user/register" method="post" onsubmit="return checkLoginForm()">
    <div class="key">
      <input type="text" id="username" name="userid" placeholder="请输入账号(长度4-16位，以字符开头)" >
    </div>
    <div class="key">
      <input type="password" id="password" name="password" placeholder="请输入密码">
    </div>
    <div class="key">
      <input type="password" id="password_2" name="password_2" placeholder="请再次输入密码">
    </div>
    <div class="signin">
      <input type="submit" id="submit" value="注册" >
    </div>
  </form>
</div>

<script>
  $(function(){
    <c:if test="${not empty param.timeout}">
      layer.msg('连接超时,请重新登陆!', {
        offset: 0,
        shift: 6
      });
    </c:if>

    if("${error}"){
      $('#submit').attr('value',"${error}").css('background','red');
    }

    if("${message}"){
      layer.msg('${message}', {
        offset: 0,
        shift: 6
      });
    }


    $('#username,#password,#password_2').change(function(){
      $('#submit').attr('value','注册').css('background','#23232e');
    });
  });

  /**
   * check the login form before user login.
   * @returns {boolean}
   */
  function checkLoginForm(){
    var username = $('#username').val();
    var password = $('#password').val();
    var password_2 = $('#password_2').val();
    var reg=/^[a-zA-Z][a-zA-Z0-9]{3,15}$/; 
    if(isNull(username) && isNull(password)){
      $('#submit').attr('value','请输入账号和密码!!!').css('background','red');
      return false;
    }
    if(isNull(username)){
      $('#submit').attr('value','请输入账号!!!').css('background','red');
      return false;
    }
    if(reg.test(username)==false){
       $('#submit').attr('value','用户名格式错误!!!').css('background','red');
   	   return false;
      }
    if(isNull(password)){
      $('#submit').attr('value','请输入密码!!!').css('background','red');
      return false;
    }
    if(password!=password_2){
        $('#submit').attr('value','两次密码不相同!!!').css('background','red');
        return false;
      }
    else{
      $('#submit').attr('value','注册中~');
      return true;
    }
  }

  /**
   * check the param if it's null or '' or undefined
   * @param input
   * @returns {boolean}
   */
  function isNull(input){
    if(input == null || input == '' || input == undefined){
      return true;
    }
    else{
      return false;
    }
  }
</script>
</body>
</html>