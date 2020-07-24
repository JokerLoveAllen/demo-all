
var prefix = "/permission";
$(function() {
	doInit();
	load();
});

function load() {
	$('#exampleTable')
			.bootstrapTable(
					{
						method : 'get', // 服务器数据的请求方式 get or post
						url : prefix + "/dataList", // 服务器数据的加载地址
						// showRefresh : true,
						// showToggle : true,
						iconSize : 'outline',
						toolbar : '#exampleToolbar',
						pageList : [10,20,30],
						striped : true, // 设置为true会有隔行变色效果
						dataType : "json", // 服务器返回的数据类型
						pagination : true, // 设置为true会在底部显示分页条
						// queryParamsType : "limit",
						// //设置为limit则会发送符合RESTFull格式的参数
						singleSelect : false, // 设置为true将禁止多选
						// contentType : "application/x-www-form-urlencoded",
						// //发送到服务器的数据编码类型
						pageSize : 30, // 如果设置了分页，每页数据条数
						pageNumber : 1, // 如果设置了分布，首页页码
						//search : true, // 是否显示搜索框
						showColumns : false, // 是否显示内容下拉框（选择显示的列）
						sidePagination : "server", // 设置在哪里进行分页，可选值为"client" 或者 "server"
						responseHandler: function (res) {//bootstarp table返回数据格式必须是  [rows : type(list), total:type(int)]
							if(res.status===200)
								return {rows:res.data.rows,total:res.data.total};
							return {rows:[],total:0};
						},
						queryParams : function(params) {
							var p = {
								//说明：传入后台的参数包括offset开始索引，limit步长，sort排序列，order：desc或者,以及所有列的键值对
								limit: params.limit,
								offset: params.offset,
								userGroup: $.trim($("#userGroup").val())
							};
							return p;
						},
						// //请求服务器数据时，你可以通过重写参数的方式添加一些额外的参数，例如 toolbar 中的参数 如果
						// queryParamsType = 'limit' ,返回参数必须包含
						// limit, offset, search, sort, order 否则, 需要包含:
						// pageSize, pageNumber, searchText, sortName,
						// sortOrder.
						// 返回false将会终止请求
						columns : [
															{
									field : 'id',
									title : '序号',
									align : 'center'
								},
																{
									field : 'userid',
									title : '用户id',
									align : 'center',
									width : 120,
									formatter : bootstarpTableFormatFunc
								},
																		{
									field : 'userGroup',
									title : '用户组',
									align : 'center',
									width : 120,
									formatter: bootstarpTableFormatFunc
								},
																{
									field : 'total',
									title : '关键词数量',
									width: 120,
									align : 'center',
									formatter : bootstarpTableFormatFunc
								},
																	{
									field : 'exportLimit',
									title : '最大导出数量',
									width: 120,
									align : 'center',
									formatter : bootstarpTableFormatFunc
								},
																{
									field : 'validateTimeStr',
									title : '用户到期时间',
									width: 160,
									align : 'center',
									formatter : bootstarpTableFormatFunc
								},
																{
									field : 'gmtCreateStr',
									title : '创建时间',
									width: 160,
									align : 'center',
									formatter : bootstarpTableFormatFunc
								},
																{
									field : 'gmtModifiedStr',
									title : '修改时间',
									width : 160,
									align : 'center',
									formatter : bootstarpTableFormatFunc
								},
																{
									title : '操作',
									field : 'cid',
									align : 'center',
									width : 250,
									formatter : function(value, row, index) {
										var e = '<a class="" href="#" mce_href="#" title="修改" onclick="modify(\''
												+ row.id
												+ '\')">修改&nbsp&nbsp</a> ';
										var d = '<a class="" href="javascript:void(0)" title="删除"  mce_href="#" onclick="del(\''
												+ row.id
												+ '\')">删除</a> ';
										return e + d;
									}
								} ]
					});
//	bootStrap 隐藏列
	$('#exampleTable').bootstrapTable('hideColumn',"id");

}
function bootstarpTableFormatFunc(value,row,index) {
	if(value==null||value.length===0){
		return '-';
	}
	return value;
}
function reLoad() {
	// $("#exampleTable").bootstrapTable('destroy');
	// load();
	$("#exampleTable").bootstrapTable('refresh');
}

function doInit(){
  	//toggle 选择行 颜色
	$('#exampleTable').on('click', 'tbody tr', function(event) {
		$(this).addClass('highlight').siblings().removeClass('highlight');
	});
}

//添加页面
function add() {
	layer.open({
		type : 2,
		title : '增加',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '800px', '520px' ],
		content : prefix + '/add' // iframe的url
	});
}

//修改页面
function modify(id) {
	layer.open({
		type : 2,
		title : '修改',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '800px', '460px' ],
		content : prefix + '/update/' + id // iframe的url
	});
}
//删除事件
function del(id) {
	layer.confirm("确认要删除选中的条数据吗?", {
		btn : [ '确定', '取消' ]
		// 按钮
	}, function() {
		$.ajax({
			url: prefix + '/delete/' + id,
			type: 'post',
			dataType: "json",
			success : function(r) {
				if(r.status===200){
					layer.msg("删除成功")
					reLoad();
				} else {
					layer.msg(r.msg);
				}
			}
		});
	}, function() {

	});
}


/*清除输入框*/
function batchClear(){
	$('input[name=txtGroup]').val("");
	$('select[name=clearGroup]').prop('selectedIndex',0);
}
