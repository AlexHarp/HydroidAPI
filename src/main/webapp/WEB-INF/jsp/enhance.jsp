<!DOCTYPE html>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <title>Hydroid | Document Enhancement</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">
    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>
<body>
<div class="container-fluid">
    <form action="/enhancer" method="post">
        <div class="panel panel-info">
            <div class="panel-heading">
                <h4>Text/Document Enhancement (JSP)</h4>
                <p>Paste or type the text you would like to enhance in the box below</p>
            </div>
            <div class="panel-body">
                <c:if test="${not empty alertCss}">
                    <div class="alert <c:out value="${alertCss}"/>">
                        <c:out value="${alertMessage}"/>
                    </div>
                </c:if>
                <textarea name="content" rows="30" class="form-control"></textarea>
            </div>
            <div class="panel-footer">
                <input class="btn btn-primary" type="submit" value="Submit" />
            </div>
        </div>
    </form>
</div>
</body>
</html>