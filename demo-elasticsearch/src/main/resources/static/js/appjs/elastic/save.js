function add() {
	$.ajax({
		cache : true,
		type : "POST",
		url : "/elastic/adate",
		data : $('#signupForm').serialize(),// 你的formid
		async : false,
		error : function(request) {
			parent.layer.alert("Connection error");
		},
		success : function(data) {
			parent.layer.msg(data.msg);
			parent.reLoad();
			var index = parent.layer.getFrameIndex(window.name); // 获取窗口索引
			parent.layer.close(index);
		}
	});
}
function exit(){
	var index = parent.layer.getFrameIndex(window.name); // 获取窗口索引
	parent.layer.close(index);
}
