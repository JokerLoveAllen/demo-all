function exit(){
	var index = parent.layer.getFrameIndex(window.name); // 获取窗口索引
	parent.layer.close(index);
}

function update() {
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
			parent.destoryAndLoad();
			parent.layer.alert(data.msg);
			var index = parent.layer.getFrameIndex(window.name);
			parent.layer.close(index);
		}
	});
}