package com.qianlima.demo.mongo.repository;

import com.mongodb.MongoException;
import com.mongodb.client.result.UpdateResult;
import com.qianlima.demo.mongo.SpringBootDemoMongodbApplicationTests;
import com.qianlima.demo.mongo.model.DataModel;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.DocumentCallbackHandler;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/11 14:02
 * @Version 0.0.1
 */
@Slf4j
public class DataModelTest extends SpringBootDemoMongodbApplicationTests {

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private MongoTemplate mongoTemplate;



    @Test
    public void delData(){
        String id = "643172a2-0425-4579-9750-0ee8b75f31a3";
        DataModel dataModel = dataRepository.findById(id).orElse(null);
        log.warn("正常查询{} 返回{}", id, dataModel);
        dataRepository.deleteById(id);
        dataModel = dataRepository.findById(id).orElse(null);
        log.warn("删除后查询{} 返回{}", id, dataModel);
    }

    /**
     * 对应mongo shell -> db.{tableName}.save() 如果存在id则更新否则执行插入
     */
    @Test
    public void updateData(){
        Optional<DataModel> dataRepositoryById = dataRepository.findById("eeb4a521-8b1f-4ec6-ab7e-d2c55821cb82");
        DataModel dataModel = dataRepositoryById.orElse(new DataModel());
        log.error("before: {}",dataModel);
        dataModel.setJob("high level worker");
        dataModel.setFrom("北方大区");
        dataModel.setName("刘武德");
        dataModel = dataRepository.save(dataModel);
        log.error("after: {}", dataModel);
    }

    /**
     * 对应mongo shell -> db.{tableName}.insert() 只更新
     */
    @Test
    public void insertRndData(){
        DataModel dataModel = DataModel.builder()
                .name(rndString(4))
                .id(UUID.randomUUID().toString())
                .job(rndString(6))
                .pwd(rndString(10))
                .from(rndString(4))
                .build();
        log.warn("插入构造的信息为:{}",dataModel);
        DataModel model = dataRepository.insert(dataModel); // eq save()
        log.warn("插入返回为:{}", model.toString());
    }

    /**
     * 使用Query 和 Update 进行更新
     */
    @Test
    public void updateWithQueryUpdateClass(){
        Query query = Query.query(Criteria.where("name").is("刘武德"));
        Update update = new Update();
//        Update.update("from","浙江南通").set("job","扫地僧");
        update.set("from","南通").set("job","扫地僧");
        UpdateResult result = mongoTemplate.updateFirst(query, update, DataModel.class);
        long modifiedCount = result.getModifiedCount();
        log.warn("修改数量:{}",modifiedCount);
        findById("eeb4a521-8b1f-4ec6-ab7e-d2c55821cb82");
    }







    @Test
    public void selectWithQuery(){
        Query query1 = new Query();
        query1.fields().include("expandField");
        query1.addCriteria(Criteria.where("id").is(165850724));
//        log.warn("{}",expandFields);
        mongoTemplate.executeQuery(query1, "notice_info", new DocumentCallbackHandler() {
            @Override
            public void processDocument(Document document) throws MongoException, DataAccessException {
                log.warn("{}",document);
            }
        });
    }









    /**
     * 使用默认的MongoRepository进行查询操作
     * @param id
     */
    public void findById(String id){
        DataModel dataModel = mongoTemplate.findById(id, DataModel.class);
        log.warn("id: {}, data: {}",id,dataModel);
    }
    @Test
    public void findAll(){
        List<DataModel> all = dataRepository.findAll();
        log.warn("查询列表为:{}", all.toString());
    }


    private static Random rnd  = new Random(System.currentTimeMillis());

    private static char[] rndCodeMap = new char[52];
    static {
        int i = 0;
        for(char j ='a';j<='z'; j++){
            rndCodeMap[i++] = j;
        }
        for(char j ='A';j<='Z'; j++){
            rndCodeMap[i++] = j;
        }
    }
    private String rndString(int len){
        String res = "";
        while(len-->0){
            res += rndCodeMap[rnd.nextInt(rndCodeMap.length)];
        }
        return res;
    }


    /**************************聚合查询,聚合查询,聚合查询,聚合查询,聚合查询,聚合查询*********************************/
    String SERACH_COLLECTION = "sdasd";
    /***
     *  聚合查询 demo
     *  db.user_behavior_info.aggregate([
     *      {"$match" :{uid:154623,oTime:{$gt:"2019-12-12 01", $lt:"2019-12-30 23"}}},
     *      {"$group" : {_id:"$date", count:{$sum:1}}},
     *      {"$sort" : {_id:-1}},
     *      {"$project" : {}},
     *      {"$limit" : 200 },
     * ]);
     */
    @Test
    public void test2(){
        /**
         * data schema:
         * {
         * 	"_id" : ObjectId("5e0ab043e6006d8db26acb61"),
         * 	"uid" : 154623,
         * 	"oid" : 28,
         * 	"cid" : "163288778",
         * 	"oTime" : "2019-12-12 09",
         * 	"ctx" : "",
         * 	"ctxId" : "b7896362-b047-4ab7-a05a-b45c10a8d0b2",
         * 	"cType" : 0,
         * 	"deed" : 0,
         * 	"level" : 60,
         * 	"offset" : 0,
         * 	"pageNo" : 0,
         * 	"pageSize" : 0,
         * 	"refer" : "",
         * 	"source" : "gw",
         * 	"date" : "2019-12-12",
         * 	"_class" : "com.qianlima.mongoservice.entity.mongcitd.UserBehaviorInfo"
         * }
         */
        Criteria commonCriteria = Criteria
                .where("uid").is(154623)
                .and("oTime").gt("2019-12-12 08").lte("2019-12-15 10");

        //1活跃天数
//        Query dayQuery = new Query(commonCriteria);
//        dayQuery.fields().include("date");
//        Iterable<String> distinct = mongoTemplate.getCollection(SERACH_COLLECTION).distinct("date", dayQuery.getQueryObject(),String.class);
       //等价：
        /**
          db.user_behavior_info.aggregate([
                     {"$match" :{uid:154623,oTime:{$gt:"2019-12-12 01", $lt:"2019-12-30 23"}}},
                     {"$group" : {_id:"$date", count:{$sum:1}}},
                     {"$project" : {count : 1}} // 1 打开,0 关闭
                ]);
         */
        Aggregation dayAggregation=Aggregation.newAggregation(Aggregation.match(commonCriteria),
                Aggregation.group("date").count().as("count"),
                Aggregation.project("count"));

        List<Object> dayAggregate = mongoTemplate.aggregate(dayAggregation, SERACH_COLLECTION, Object.class).getMappedResults();
        log.warn("({}) - {}", "总记录", dayAggregate.size());

        /**
         *db.user_behavior_info.find({
         *     uid:154623,
         *     oTime:{
         *         $gte: "2019-12-12 20",
         *         $lte: "2019-12-13 10"
         *     }})
         *   .projection({oTime : 1})
         *   .sort({oTime:1})
         *   .limit(1);
         */
        //2最后一次活跃时间
        Query lastestQuery = new Query(commonCriteria);
        lastestQuery.with(new Sort(Sort.Direction.DESC, "oTime")).limit(1).fields().include("oTime");
        mongoTemplate.executeQuery(lastestQuery, SERACH_COLLECTION, new DocumentCallbackHandler() {
            @Override
            public void processDocument(Document document) throws MongoException, DataAccessException {
                log.warn("({}) - {}","活跃天数", document);
            }
        });

        /**
         * db.user_behavior_info.find({
         *     uid:154623,
         *     oTime:{
         *         $gte: "2019-12-12 08",
         *         $lte: "2019-12-13 10"
         *     },
         *     oid:{$in: [3,5]},
         *     $and:[
         *         {ctx:{"$exists": true, "$ne" : ""}},
         *         {ctx:{"$exists": true, "$ne" : null}}
         *     ],
         * })
         *   .projection({ctx : 1})
         *   .sort({oTime : -1})
         *   .limit(20)
         */
        //3 最近搜索的20个关键词
        Query keywordQuery = new Query(commonCriteria);
        keywordQuery.addCriteria(Criteria.where("oid").in(3,5).andOperator(Criteria.where("ctx").exists(true).ne(""),Criteria.where("ctx").exists(true).ne(null)));
        keywordQuery.with(new Sort(Sort.Direction.DESC, "oTime")).limit(20).fields().include("ctx");
        mongoTemplate.executeQuery(keywordQuery, SERACH_COLLECTION, new DocumentCallbackHandler() {
            @Override
            public void processDocument(Document document) throws MongoException, DataAccessException {
                log.warn("({}) - {}", "关键词", document);
            }
        });

        /**
         * db.user_behavior_info.find({
         *     uid : 154623,
         *     oTime:{
         *         $gte: "2019-12-12 08",
         *         $lte: "2019-12-13 10"
         *     },
         *     oid : 28,
         *     $and:[
         *         {cid:{"$exists": true, "$ne" : ""}},
         *         {cid:{"$exists": true, "$ne" : null}}
         *     ]
         * })
         *   .projection({cid : 1})
         *   .sort({oTime : -1})
         *   .limit(3)
         */
        //4 三个详情页标题
        Query titleQuery = new Query(commonCriteria);
        titleQuery.addCriteria(Criteria.where("oid").is(28).andOperator(Criteria.where("cid").exists(true).ne(""),Criteria.where("cid").exists(true).ne(null)));
        titleQuery.with(new Sort(Sort.Direction.DESC,"oTime")).limit(3).fields().include("cid");
        mongoTemplate.executeQuery(titleQuery, SERACH_COLLECTION, new DocumentCallbackHandler() {
            @Override
            public void processDocument(Document document) throws MongoException, DataAccessException {
                log.warn("({}) - {}", "标题", document);
            }
        });

        //6 占比
        /*
        官网:
            [公告 预告 变更 结果 国土+拟在建+201VIP]
        wap:
             145	首页-招标公告-信息
             146	首页-中标通知-信息
             147	首页-拟在建项目-信息
             148	招标预告-信息
             149	招标变更-信息
        */
        Map<Integer,Integer> wap2Gw = new HashMap<>(8);
        wap2Gw.put(145,0);
        wap2Gw.put(146,3);
        wap2Gw.put(147,4);
        wap2Gw.put(148,1);
        wap2Gw.put(149,2);
        int len = 5;
        int[] analysisArray = new int[len];
        // 6-1 官网详情统计
        /**
         * db.user_behavior_info.aggregate([
         *      {"$match" :{uid:154623, oTime:{$gt:"2019-12-12 01", $lt:"2019-12-30 23"}, source:"gw", oid:28, cType:{"$exists" : true, "$ne" : null}}},
         *      {"$group" : {_id:"$cType", count:{$sum:1}}},
         *      {"$sort" : {_id:-1}},
         *      {"$project" : {count:1}},
         *      {"$limit" : 200 }
         * ])
         */
        Criteria analysisCriteria = Criteria.where("uid").is(154623)
                .and("oTime").gt("2019-12-12 08").lte("2019-12-15 10")
                .and("source").is("gw")
                .and("oid").is(28).and("cType").exists(true).ne(null);
        Aggregation analysisAggregation = Aggregation.newAggregation(Aggregation.match(analysisCriteria),
                Aggregation.group("cType").count().as("count"),
                Aggregation.project("count"));
        List<Map> analysisAggregate = mongoTemplate.aggregate(analysisAggregation, SERACH_COLLECTION, Map.class).getMappedResults();
        String searchKey = "_id", searchResult = "count";
        for (Map m: analysisAggregate) {
            int currKey = -1, currResult = -1;
            for(Object tmp : m.keySet()){
                if(searchKey.equals(tmp)){
                    currKey = (int)m.get(searchKey);
                }else if(searchResult.equals(tmp)){
                    currResult = (int)m.get(searchResult);
                }
            }
            if(currKey < 0 || currResult <0){
                continue;
            }
            if(currKey < len){
                analysisArray[currKey] += currResult;
            }else{
                analysisArray[len-1] += currResult;
            }
        }
        log.warn("({}) - {} - {}", "统计容器 ~", Arrays.toString(analysisArray),analysisAggregate);

        //6-2wap
        /**
         * db.user_behavior_info.aggregate([
         *      {"$match" :{uid:1062760,oTime:{$gt:"2019-12-12 01", $lt:"2019-12-30 23"}, source:"wap", oid:{"$in":[145,146,147,148,149]}}},
         *      {"$group" : {_id:"$oid", count:{$sum:1}}},
         *      {"$sort" : {_id:-1}},
         *      {"$project" : {_id:1,count:1}},
         *      {"$limit" : 200 }
         * ])
         */
        Criteria wapAnalysisCriteria = Criteria.where("uid").is(154623)
                .and("oTime").gt("2019-12-12 08").lte("2019-12-15 10")
                .and("source").is("wap")
                .and("oid").in(145,146,147,148,149);
        Aggregation wapAnalysisAggregation = Aggregation.newAggregation(Aggregation.match(wapAnalysisCriteria),
                Aggregation.group("oid").count().as("count"),
                Aggregation.project("count"));
        List<Map> wapAnalysisAggregate = mongoTemplate.aggregate(wapAnalysisAggregation, SERACH_COLLECTION, Map.class).getMappedResults();
        for (Map m: wapAnalysisAggregate) {
            int currKey = -1, currResult = -1;
            for(Object tmp : m.keySet()){
                if(searchKey.equals(tmp)){
                    currKey = (int)m.get(searchKey);
                }else if(searchResult.equals(tmp)){
                    currResult = (int)m.get(searchResult);
                }
            }
            if(currKey < 0 || currResult <0){
                continue;
            }
            if((currKey = wap2Gw.getOrDefault(currKey,-1)) < 0){
                continue;
            }
            if(currKey < len){
                analysisArray[currKey] += currResult;
            }else{
                analysisArray[len-1] += currResult;
            }
        }

    }
    @Test
    public  void test3(){
        /** 根据uid分组,找到组内最大的oTime元素并且返回
         db.user_behavior_info.aggregate([
             {"$match" :{oTime:{$gte:"2020-01-02 01", $lte:"2020-01-02 23"},}},
             {"$group" : {
             _id:"$uid",
             maxOTime:{$max: "$oTime"}
             }},
         ])
         */
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("oTime").gte("2020-01-03 00").lte("2020-01-03 23")),
                Aggregation.group("uid").max("OTime").as("maxOTime")
        );
        List<Map> results = this.mongoTemplate.aggregate(aggregation, SERACH_COLLECTION, Map.class).getMappedResults();

        /** 相当于sql子查询, select count(*) from (select uid, date from xx group by uid, date) as innerGroup group by innerGroup.uid
          ([
             {"$match" :{uid:{$in:[539165, 1263688, 6206610, 6145945, 3865420, 3103126, 468191, 5613937, 865574, 5622638, 3632959, 6238998, 3241094, 4005978, 3212588, 1191687, 5087383, 1499927, 6181232, 4971056, 5461576, 2209217, 6239957, 6238682, 5938627, 5316795, 6238591, 4375664, 2969586, 4999631, 5949444, 1118741, 6239291, 1895742, 2695898, 493519, 6097143, 5566450, 2424841, 5424060, 3090572, 2789915, 2006950, 5124920, 6157069, 5138471, 1406051, 2005488, 4287328, 5308321, 4750206, 1905464, 6103788, 6098505, 2387335, 2373863, 6238602, 270277, 5442604, 5972691, 4835936, 930826, 5820216, 4946035, 2798468, 5601769, 5846758, 6239604, 6240008, 1276609, 3493525, 6214374, 4632780, 6238211, 154623, 4921442, 1426542, 2144882, 5458970, 4725405, 2598182, 5184507, 4443418, 3680175, 5396824, 6036643, 5332221, 6239420, 506415, 1097197, 873266, 3219344, 4132363, 6239622, 5547474, 4506331, 6239354, 2261607, 1565551, 3835543, 5059356, 2662227, 588407, 5091765, 1703652, 388801, 2772027, 4601865, 2069436, 2260655, 6239486, 6239585, 2579675, 486520, 4533263, 6238812, 6139994, 6151484, 6239247, 5994331, 4605527, 1106052, 1562449, 3800511, 474971, 5085360, 4564286, 6233787, 2582272, 6238247, 6238540, 5817693, 4843726, 2825572, 6196700, 2472898, 6239588, 1831798, 4528491, 3185836, 6084568, 3215920, 6239210, 6238367, 1422763, 17, 5513839, 822921, 6239028, 1369496, 6238741, 6239532, 1640793, 1835398, 3759281, 5474639, 4219039, 6238688, 1192871, 4923975, 2874109, 3155196, 6051233, 375687, 5028253, 3209218, 5351510, 1469753, 2371305, 6238794, 1902088, 6095525, 6184925, 2655369, 5569797, 4962852, 5498357, 3431889, 6230808, 6233753, 1407304, 3193231, 1599057, 461503, 2787315, 6228524, 1654879, 6238798, 238885, 2806421, 5774183, 5997673, 3097158, 2438567, 6198948, 2736382, 4081454, 5289474, 3868017, 6239697, 6169779, 6239679, 3793310, 6165109, 6239998, 6238151, 6239457, 5834593, 968691, 6238624, 5401038, 5545656, 5547919, 6206594, 6239932, 3179555, 6238800, 5269619, 4242465, 1418337, 4831463, 6222989, 3771899, 5947112, 6125455, 6239835, 3972388, 5419587, 6238853, 6025899, 5209601, 6239501, 5249844, 5744592, 1625100, 4694606, 1722899, 207948, 6239077, 6151392, 3655255, 4722024, 6230478, 6239792, 3847186, 952697, 3498616, 4272229, 2933847, 6181338, 3493658, 3341625, 4706384, 2853369, 275453, 6239614, 1946587, 6239710, 2556965, 119843, 883144, 5001937, 5941087, 5078927, 3871329, 2474607, 4356893, 2315584, 4630791, 2656201, 6151602, 6184941, 6239827, 5254991, 6209940, 6030492, 3221020, 4691157, 691919, 5018032, 6239615, 5487508, 4028325, 3503646, 6239361, 1364398, 303943, 6238599, 6214214, 6239510, 6238409, 43395, 6239069, 3445820, 3931028, 6209892, 6147008, 4116055, 3847044, 6218367, 2717054, 6219651, 6238771, 4227010, 1444845, 6239910, 1890985, 4348189, 4895615, 4340383, 482746, 6238366, 1721033, 5458358, 6183089, 2931983, 794004, 4023097, 1117380, 6201252, 193467, 5959289, 6239788, 6239915, 4214749, 2691349, 6118619, 4930037, 578531, 5599985, 535497, 1331274, 5577955, 6238983, 5546217, 5981441, 497720, 5283905, 489921, 1942186, 4063473, 4800353, 6238494, 5366571, 4923326, 4010107, 4623583, 6233867, 3287058, 2029621, 6191880, 3839824, 6232487, 3651719, 1186465, 2944988, 6223765, 6217829, 3702651, 5169823, 3038201, 4127616, 456122, 3460259, 2508447, 2319053, 4819161, 6034386, 5964712, 1111847, 6232433, 2256929, 6239712, 1410569, 4810309, 4913610, 3415577, 643350, 6219535, 1025522, 3215278, 3822678, 314280, 5527187, 485118, 6238233, 3103910, 6132946, 451723, 6114789, 2936785, 4217952, 1080496, 2674075, 4632474, 6131535, 6238991, 3394876, 3336961, 2556405, 5004103, 3291964, 4269416, 5998098, 2081808, 6238093, 6239092, 2157600, 5498228, 2980869, 4739540, 5837638, 3800647, 1787023, 3771547, 2941145, 1480500, 5720546, 272551, 300841, 769147, 2454631, 2481573, 978899, 3992885, 1945740, 6239019, 6182762, 6033381, 3439881, 983602, 4505144, 4275511, 887200, 2086533, 2911862, 2725546, 4840857, 5017230, 1663295, 1596864, 4897055, 3069578, 337617, 1278831, 2267099, 5131958, 819651, 1308008, 6239954, 4203849, 3508920, 157066, 3287428, 3025960, 2226419, 3570789, 6239337, 6238326, 4653538, 4806403, 3328383, 2496380, 4973261, 3054780, 4130168, 5158558, 6239658, 5130708, 3891152, 4912436, 2589741, 975563, 5732925, 5997728, 6239627, 937267, 6210988, 6003164, 6238282, 2505274, 282527, 4897503, 6088701, 201763, 421424, 4909222, 2155885, 5516954, 5238299, 5039654, 6172407, 3202214, 5523880, 1980757, 1529673, 4616637, 6171017, 6200638, 3002351, 6238172]},oTime:{$gt:"2019-12-03 01", $lt:"2020-01-03 23"}}},
             {"$group" : {
             _id:{"date":"$date","uid":"$uid"},
             }},
             {"$group" : {
             _id : "$_id.uid",
             count: { $sum: 1 }
             //itemsSold: { $addToSet: "$_id.date" } //将聚合的元素放在set集合
             }},
         ]);
         */
        Aggregation groupAggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria
                        .where("uid").in(154623,154627)
                        .and("oTime").gte("2019-12-03 00").lte("2020-01-03 00")),
                Aggregation.group("uid","date"),
                Aggregation.group("_id.uid").count().as("count")
        );
        List<Map> mapList = mongoTemplate.aggregate(groupAggregation, SERACH_COLLECTION, Map.class).getMappedResults();
    }
}
