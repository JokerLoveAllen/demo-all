package com.fenxiang.spark

import java.util.Arrays

import scala.Array._

object Test01 {
  def main(arr:Array[String]){
    //		scala_common()
    //		scala_string(); //scala string
//    		scala_array(); // scala array
    		scala_list();// scala list
    //		scala_set();// scala set
    //		scala_map();// scala map
    //		scala_tuple()
    //		scala_option()
//    scala_iterator()
    val s = "1"
    println("1".eq(s))
//    println(if(1>0) "e" else "a")
//    printStrings("AR","df","uk","rya","DFd","yik","vcxb","as","wer");//多参数数组

  }
  // 可变参数长度的函数
  def printStrings(strs:String*):Unit={
    var i:Int = 0;
    for(str<-strs){
      println("strs["+i+"] = "+str);
      i+=1
    }
  }
  //很多知识点,默认值参数（Python风格）,Int + Double 自动转型,调用可以是名称调用
  def func1(param1 :Int =1,param2 :Double=2.245) :Double ={
    //return 可以忽略
    param1 + param2;
  }
  // 递归斐波那契数列
  def fibonacci(n :Int=10) :BigInt={
    if(n<=2)
      1
    else
      fibonacci(n-1)+fibonacci(n-2) //递归后得到结果直接返回
  }
  //迭代斐波那契数列
  def fibonacci2(n :Int=10) :BigInt={
    var a = 1;
    var b = 1;
    var nz = n;
    while(nz>0){
      a = b;
      b = a+b
      nz -= 1
    }
    b //这里的单个变量是返回结果的
  }
  //如果多参数时,传递给每个调用的干扰(保持一个参数不变,动态的传入改变的参数)
  def loop_() :Unit={
    val loop_fibonacci = fibonacci(_:Int)
    for(i <- 1 to 10){//scala 中没有break,continue等中断操作,可以在循环中return退出方法栈来中断循环(鸡肋)
      println("loop_fibonacci("+i+"): "+loop_fibonacci(i))
    }
  }
  //嵌套函数
  def nesting_func_decorators(i:Int =2) :Int={
    def nesting_func(x :Int,y:Int) :Int={
      if (x<1)
        y
      else
        nesting_func(x-1,y*x)
    }
    nesting_func(i,1)
  }
  //测试匿名函数
  def test_anonymous_func():Unit={
    var anonymous_func1 = (x :Int,y :Int)=>{(x+y)+(x-y)+(x*y)+(x/y)}
    var anonymous_func2 = (x :Int)=>{x%2}
    //柯里华函数每一个括号只能有一个参数
    def anonymous_currying_func1(s1:String)=(s2:String)=> {s1 + s2}//柯里华函数写法1
    def anonymous_currying_func2(s1:Int)(s2:Int)(s3:Int)= {s1 - s2  - s3} //柯里华函数写法2
    var p = anonymous_func1(2,5)+3 //out 17
    var q = anonymous_func2(10021)+10020 // out 10021
    var m = anonymous_currying_func1(" devil ")(" may ")
    var n = anonymous_currying_func2(1008888)(1008888)(666666)
    println(p,q,m,n,"user.dir:"+()->System.getProperty("user.dir"))

  }
  //常规测试
  def scala_common(): Unit ={
    var myVar :String = "abcdef";//定义类型为String, 定义为可变
    val myVar1 :Int = 1234; //定义类型为Int 定义为不可变
    myVar = "我是伦琦深";
    var myVar2 = "ALLEN IS HEADSOME" //scala自己推敲数据类型
    //myVar1 = 34567; //不可变,编译不被通过
    val expressStr = """
		scala 中没有任何一个选择是完美的?!
		scala是运行在JVM之上的脚本语言,兼容脚本语言胶水的特征,可以使用scala完成对一些java功模的快速开发
	"""
    println(myVar,myVar1,myVar2,expressStr)
    val bc = new BaseClass(10086,"i am joker");
    bc.move(10089," love allen")
    val myVar3 = bc.xxx + bc.xx
    println(myVar3)

    val ab = new AdvanceBean(10010,"Joker And",3.1415926);//new 关键字出事化一个实例
    ab.move(9990,"Allen",0.0000000535);
    printStrings("AR","df","uk","rya","DFd","yik","vcxb","as","wer");//多参数数组
    println("func1() have param"+func1(param2=10000.001,param1=263765));//指定参数名称的形式调用，顺序可以打乱
    println("func1() no param"+func1());//默认值参数（Python风格）可以不加参数
    println("fibonacci()recursive :"+fibonacci(5)); //斐波那契递归形式
    println("fibonacci2() iterator :"+fibonacci(5)); //斐波那契迭代形式
    println("nesting_func_decorators()嵌套函数: " +nesting_func_decorators(5)); //测试嵌套函数
    test_anonymous_func();//测试匿名函数；
    println("test closure func():"+ closure(1000000)+"  "+closure(100086)) //测试闭包
  }

  // scala String类型的操作
  def scala_string():Unit={
    var len = "joker love allen".concat("abcdef").length();
    var name = "西门吹雪" ; var height = 2.3f
    var outLen = printf("\"joker love allen\".concat\"abcdef\"length() is %d",len)//格式化输出

    println(s"hello,${1+1}")//s 插值器${}内可以是代码段和变量
    println(f"name $name%s height is $height%2.2f tall")//f 插值器
  }

  /**
   *scala中map容器
   * Map(映射)是一种可迭代的键值对（key/value）结构。
   * 所有的值都可以通过键来获取。
   * Map 中的键都是唯一的。
   * Map 也叫哈希表（Hash tables）。
   * Map 有两种类型，可变与不可变，区别在于可变对象可以修改它，而不可变对象不可以。
   * 默认情况下 Scala 使用不可变 Map。
   * 如果你需要使用可变集合，你需要显式的引入 import scala.collection.mutable.Map 类
   * 在 Scala 中 你可以同时使用可变与不可变 Map，不可变的直接使用 Map，可变的使用 mutable.Map
   */
  def scala_map(): Unit = {
    import scala.collection.mutable.Map//使map引用可变(不加不可变[scala.collection.immutable.Map])
    val map1:Map[Int,Int]=Map(1->11,2->22,3->33)
    val map2 = Map(1->111,2->222,3->333)
    println("map1的键",map1.keys)//键
    println("map1的值",map1.values)//值
    println("map1是否为空",map1.isEmpty)//为空?!
    map1 ++= List(5->55,6->66)//追加集合?!
    println("追加集合:",map1)//追加集合
    map1.put(7,77) //put操作
    map2(3)=3333;map2(5)=5555//赋|修改值
    println("put,MAP()= 赋值操作",map1,map2)
    println("get()根据key读取元素,没有则默认值替换",map1.get(99).getOrElse(9990999))//根据key读取元素,没有则默认值替换
    println("retain 只保留规则内的元素:",Map(1->11,2->22,3->33).retain((k,v)=> k==1))//只保留规则内的元素
    println("按照(k,v)过滤",map1.filter({case(k,v)=> k%2==1;v>50}))//过滤({case()=>})形式
    //使用 ++ 运算符或Map.++()方法来连接两个Map，Map合并时会移除重复的key
    println("使用 ++ 运算符或Map.++()方法来连接两个Map",map1 ++ map2,map2.++(map1))

    //循环输出 key 和 value
    map2.keys.foreach{i=> print("key="+i);println(" value="+map2(i))}

    println(" Map 中是否存在指定的 Key",map1.contains(3))// Map中是否存在指定的 Key
    println("返回一个新的 Map,移除key为elem1...",map1-(1,3))//返回一个新的 Map, 移除 key 为 elem1, elem2 或其他 elems
    println("返回指定key的值[2种]",map1(3),map1.get(3))//返回指定key的值[2种]
    println("清空 Map","def clear(): Unit")//清空 Map
  }

  /**
   * scala中set和list的关系类比java中list、set关系
   * 比 Java多了一些交、补、差、并、等运算？！
   * Scala Set(集合)是没有重复的对象集合，所有的元素都是唯一的。
   * Scala 集合分为可变的和不可变的集合。
   * 默认情况下，Scala 使用的是不可变集合，
z   * 如果你想使用可变集合，需要引用 scala.collection.mutable.Set 包。
   */
  def scala_set(): Unit ={
    //   对不可变Set进行操作(add/remove)，会产生一个新的set，原来的set并没有改变
    //      val set1:Set[Int] = Set(1,3,5,6,8,9)//不可变集合
    //      val set2:Set[Int] = Set(2,4,7,6,8,9)
    import scala.collection.mutable.Set //变为可变集合
    val set1:Set[Int] = Set(1,3,5,6,8,9)//不可变集合
    val set2:Set[Int] = Set(2,4,7,6,8,9)
    set1.add(10);//添加元素
    set2+=(11)//set1+(x,y,z,...)为集合添加新元素，x并创建一个新的集合，除非元素已存在
    set1.remove(9);//移除元素
    set2-=(9)//或者set1-(x,y,z,...)移除集合中的元素，并创建一个新的集合
    println("进行删除添加元素后:",set1,set2)
    println("可变集合:",set1.toSet.getClass.getName)//可变集合
//    println(" ++ 运算符号 合并两个集合",set1++set2)//++运算符号合 并两个集合
//    println(" .++() 方法 合并两个集合",set1.++(set2))//.++()方法 合并两个集合
    println("取交集[3种]:",set1.&(set2),set1.intersect(set2),set1 & set2)//取交集[3种]
    println("取差集[3种]:",set1 &~ set2,set2.&~(set1),set2.diff(set1))//返回两个集合的差集[3种]
    println("返回不可变集合中数字元素的积::",set1.product)//返回不可变集合中数字元素的积
    println("返回不可变集合元素的数量",set2.size)//返回不可变集合元素的数量
    println("把不可变集合拆分为两个容器，第一个由前 n 个元素组成，第二个由剩下的元素组成",set2.splitAt(3))//把不可变集合拆分为两个容器，第一个由前 n 个元素组成，第二个由剩下的元素组成
    println("如果集合中含有子集返回 true，否则返回false",set1.subsetOf(set2))//如果集合中含有子集返回 true，否则返回false
  }

  //scala 数组的操作
  def scala_array() :Unit={
    val arr1 : Array[Object] = new Array[Object](3); //定长数组
    arr1(0) = "devil"; arr1(1) = " may "; arr1(100/50) = "cry"; //访问数组为[变量](指针) 注意为小括号
    val arr2 = Array(213.23d,3124.677d,8657.123d,23456.415d,87988.231d,87988.2134d);//填充数据的数组

    Arrays.sort(arr2); //排序
    var sum = 0d;     //累加和
    for( i <- 0 to arr2.length-1){
      sum += arr2(i);
    }
    println(Arrays.toString(arr1),Arrays.toString(arr2),arr2(arr2.length -1),sum);//打印数组

    //合并数组
    for( i <- concat(range(1,21,2),range(2,22,3))){
      print(i+ " ");
    }
    println();
  }

  /**
   * scala List集合
   * Scala 列表类似于数组，它们所有元素的类型都相同，
   * 但是它们也有所不同：列表是不可变的，值一旦被定义了就不能改变
   */
  def scala_list() :Unit={
    //常规写法
    val lst1: List[Double] = List(123.4,5436.7,31453.6,897342.21)
    val lst2: List[Nothing] = List()
    val lst3: List[List[Int]] = List(List(123,4,53),List(987,678,54),List(978,75,758))
    //使用 :: 和 Nil 构造列表
    val lst4: List[String] = "dabao"::("dubbo"::("double"::Nil))
    val lst5 = 10086 :: (20086 :: (30086 :: (40086 :: Nil)))
    val lst6 = (1 ::(2::(3::Nil)))::(5::(6::(7::Nil)))::(9::(10::(11::(12::Nil))))::Nil
    println(lst1,lst2,lst3,lst4,lst5,lst6)
    //列表基本方法
    println(lst4.head,lst4.tail,lst4.isEmpty)
    //链接列表 :::| .:::() | List.concat() 都可以链接多个链表
    println(lst4 ::: lst1 ::: lst5)
    println( lst4 .::: (lst1) .:::(lst5))
    println(List.concat(lst1,lst4,lst5))
    var lst7 = List.fill(10)("“嘎嘎嘎”") //填充数组
    println(lst7)
    var lst8 = List.tabulate(5)(i => i*i*i*i*i) //通过给定的函数创建列表
    var lst81 = List.tabulate(4,5)(_*_)
    println(lst8,lst81)
    val lst9 :List[Int] = 123::(123::(23::(34::Nil)))//::中缀操作符，将对象作为整体放到前端
    val lst10 :List[String] = "wad"::("fas"::("poo"::Nil))
    println("测试反转",lst9.:::(lst10).reverse) //测试反转
    println("预先添加元素",20085 +: lst9)//预先添加元素
    println("添加表头元素", "表头":: lst9)//添加表头元素
    println("列表开头添加指定列表的元素",lst9 ::: lst10,lst10++lst9)//在列表开头添加指定列表的元素
    println("复制添加元素后列表",lst10+:lst9)//复制添加元素后列表
    println("将列表的所有元素添加到StringBuilder，并指定分隔符",
      lst9.addString(new StringBuilder()," |-| "))//将列表的所有元素添加到StringBuilder，并指定分隔符
    println("使用分隔符将列表所有元素作为字符串显示",lst9.mkString("|-|"))//
    println("通过列表索引获取元素",lst10.apply(2))//通过列表索引获取元素
    println("检测列表中是否包含指定的元素",lst9.contains(Nil))//检测列表中是否包含指定的元素
    val arr:Array[String] = Array();lst10.copyToArray(arr) //将列表的元素复制到数组中
    println("将列表的元素复制到数组中",arr)
    println("列表转换为数组","def toArray: Array[A]")//列表转换为数组
    println("去除列表的重复元素，并返回新列表",lst9.distinct)//去除列表的重复元素，并返回新列表
    println("去除列表的重复元素，并返回新列表",lst9.drop(2))//去除列表的重复元素，并返回新列表
    println("丢弃最后n个元素，并返回新列表",lst9.dropRight(2))//丢弃最后n个元素，并返回新列表
    println("从左向右丢弃元素，直到条件p不成立",lst9.dropWhile(_== lst9.head))//从左向右丢弃元素，直到条件p不成立
    println("检测列表是否以指定序列结尾",lst9.endsWith(lst10))//检测列表是否以指定序列结尾
    println("检测所有的元素是否以制定结尾",lst10.forall(s=>s.endsWith("s")))//检测所有的元素是否以制定结尾
    println("判断是否相等",lst9.equals(lst10))//判断是否相等
    println("判断列表中指定条件的元素是否存在",lst9.exists(p=>p==23))//=exists(_==23) 等于 contains(23) 判断列表中指定条件的元素是否存在
    println("输出符号指定条件的所有元素",lst10.filter(p=>p.length==3))//输出符号指定条件的所有元素
    println("将函数应用到列表的所有元素","def foreach(f: (A) => Unit): Unit")//将函数应用到列表的所有元素
    println("获取列表的第一个元素",lst9.head)//获取列表的第一个元素
    println("从指定位置from开始查找元素第一次出现的位置",lst9.indexOf(43,2))//从指定位置from开始查找元素第一次出现的位置
    println("在指定的位置end开始查找元素最后出现的位置",lst9.lastIndexOf(43,2))//在指定的位置end开始查找元素最后出现的位置
    println("返回所有元素，除了最后一个",lst9.init)//返回所有元素，除了最后一个
    println("计算多个集合的交集",lst9.intersect(lst9))//计算多个集合的交集
    println("检测列表是否为空",lst9.isEmpty)//检测列表是否为空
    println("创建一个新的迭代器来迭代元素",lst9.iterator)//创建一个新的迭代器来迭代元素
    println("返回最后一个元素",lst10.last)//返回最后一个元素
    println("返回所有元素，除了第一个",lst10.tail)//返回所有元素，除了第一个
    println("提取列表的前n个元素",lst10.take(3))//提取列表的前n个元素
    println("提取列表的后n个元素",lst10.takeRight(3))//提取列表的后n个元素
    println("通过给定的方法将所有元素重新计算","def map[B](f: (A) => B): List[B]")//通过给定的方法将所有元素重新计算
    println("查找最小元素,查找最大元素,求和",lst9.min,lst9.max,lst9.sum)//查找最小元素,查找最大元素,求和
    println("列表排序",lst9.sorted)//
    println("检测列表在指定位置是否包含指定序列",lst9.startsWith(lst9))//检测列表在指定位置是否包含指定序列
    println("返回缓冲区，包含了列表的所有元素",lst10.toBuffer)//返回缓冲区，包含了列表的所有元素
    println("List转换为Map|Set|Seq(Unit)|toString","lst10.toMap,lst10.toSeq,lst10.toSet,lst10.toString()")
  }

  /** scala Tuple 元组
   * 与列表一样，元组也是不可变的，但与列表不同的是元组可以包含不同类型的元素。
   * 元组的值是通过将单个的值包含在圆括号中构成的
   */
  def scala_tuple(): Unit ={
    val t1 = (1,3)//创建元组时要定长?!
    val t2 = (3,4,5,6,7,8,9)
    println("交换后的元组:",t1.swap)//两个元素时候可以交换
    t2.productIterator.foreach{i =>println("value = ",i)}//遍历元组
    println("访问元组的方式:",t1._1,t2._6)//访问元组的方式
  }

  /**
   * Scala Option(选项)类型用来表示一个值是可选的（有值或无值)。
   * Option[T] 是一个类型为 T 的可选值的容器：
   * 如果值存在，Option[T] 就是一个Some[T] ，如果不存在，Option[T]就是对象None
   */
  def scala_option(): Unit ={
    val map1 = Map("a"->"allen","b"->"bitch","c"->"color","d"->"delay","e"->"element")
    //Scala使用Option[String]来告诉你：
    //「我会想办法回传一个String，但也可能没有String给你」
    //。map1 里并没有key2这笔数据，get()方法返回None。
    val o1:Option[String] = map1.get("c") // Some[String]
    val o2:Option[String] = map1.get("f")//None
    println("返回Option",o1,o2)
    println(o1.getOrElse(12),o2.getOrElse(888))//返回默认值(如果是None的话)
    println("不是Some返回未定义|是否为空",o1.isDefined,o1.isEmpty)//不是Some返回未定义|是否为空
    println("如果选项包含有值返回选项值，否则返回 null",o2.orNull)//如果选项包含有值返回选项值，否则返回 null
    println("如果一个 Option是None,orElse方法会返回传名参数",o2.orElse(map1.get("e")))//如果一个 Option 是 None ， orElse 方法会返回传名参数的值，否则，就直接返回这个 Option。
    println("获取指定的可选项，以0为起始。即A(x_1, ..., x_k),返回x_(n+1)，0<n<k.",o1.productElement(0))
  }
  /**
   * Scala Iterator（迭代器）不是一个集合，它是一种用于访问集合的方法。
   * 迭代器 it 的两个基本操作是 next 和 hasNext。
   */
  def scala_iterator() :Unit={
    val iter1 = Iterator("123","678","9999","99878")
    //println(iter1.length,iter1.max,iter1.min,iter1.size)
    //以上四种方法都会消费迭代器元素,即调用后迭代器为empty

    //迭代器和py中性质相同,当元素被"消费"后就不存在了.
    while(iter1.hasNext){
      println(iter1.next())
    }
  }

  // 闭包函数::在闭合的范围内。在闭合的范围内。函数引用factor，每次读取其当前值。
  // 如果函数没有外部引用，那么它本身就会被简单地关闭，不需要外部上下文
  var factor:Double = 3.5619988d
  val closure = (x :Int)=>{x*factor}
}
class AdvanceBean(override val x:Int,override val y:String,val z:Double) extends BaseClass(x,y){
  var zz = z; var zzz:Double = z;
  def move(dx:Int,dy:String,dz:Double){
    xx += dx; zz +=dz; this.yy += dy  //子类调用父类的属性
    println(xx,yy,zz,xxx,yyy,zzz) //打印子类和父类的所有属性
  }
}

class BaseClass(val x :Int,val y :String){//construction函数
  var xx :Int = x;var yy :String = y
  var xxx = x;var yyy = y;
  println(xx,xxx,yy)
  def move(dx :Int,dy :String){
    xx += dx; yyy+=dy;
    println(xx,yyy,xxx,yy)
  }
  override def toString():String="ddddddddffffff"
}
