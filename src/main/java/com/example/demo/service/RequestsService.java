package com.example.demo.service;

import com.example.demo.Http.Assert;
import com.example.demo.Model.StepModel;
import com.example.demo.Model.StepModelList;
import com.example.demo.Tools.ExtentUtils;
import com.example.demo.Tools.MyLogger;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.NetworkMode;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.ResponseSpecification;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;
import org.junit.runners.Parameterized;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

import static com.example.demo.Tools.DataUntils.timeDate;
import static com.example.demo.Tools.MyLogger.initLogger;
import static io.restassured.RestAssured.filters;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;


@Service
public class RequestsService {

    @Rule
    public ErrorCollector collector = new ErrorCollector();
    public static List<StepModel> testcase;
    public static Response response;
    public String checkKey;
    public String selectkey;
    public Object check;
    public Object exp;
    public String ContentTypeJson = "application/json";
    public String ContentTypeFrom = "application/x-www-form-urlencoded";
    public static RequestSpecBuilder rsb = new RequestSpecBuilder();
    public static ResponseSpecBuilder rb= new ResponseSpecBuilder();
    public static ResponseSpecification rs;
    public  static String  LEVEL = "ALL";
    private static ExtentReports extent;
    private static String reportPath = String.format( System.getProperty("REPORTPATH")
            + "/reports/report_%s.html", timeDate());
    public static MyLogger logger;
    public static ExtentTest extentTest;
    public static File[] files;

    @Parameterized.Parameter
    public  static String CASEPATH;

    public static List<String> getCaseFolder(String casefolder){
        List<String> caselist = new ArrayList<String>();
        File file = new File(casefolder);
        if (file.exists()) {
            if (file.isDirectory()) {
                files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    caselist.add(String.valueOf(files[i]));
                }
            } else {
                caselist.add(casefolder);
            }
        } else {
            System.out.println(String.format("不存在文件夹:", casefolder));
        }
        return caselist;
    }

    public static Collection prepareData()
    {
        CASEPATH = System.getProperty("FILEPATH");
        List<String> caseFolder = getCaseFolder(CASEPATH);
        Object[] objects = (Object[])caseFolder.toArray();
        // 测试数据
        return Arrays.asList(objects);// 将数组转换成集合返回
    }

    @Rule
    public ExtentUtils eu = new ExtentUtils(extent,extentTest);

    public static void setup() {
        extent = new ExtentReports(reportPath, true, NetworkMode.OFFLINE);
        extentTest = extent.startTest("接口测试", "-");
        logger = new MyLogger(extent,extentTest);
        logger.log_info("初始化全局参数");
        rb.expectResponseTime(lessThan(1000L));
        rs = rb.build();
        RestAssured.filters(new RequestLoggingFilter(),new ResponseLoggingFilter());
        initLogger().setLevel(Level.ALL);
        RestAssured.useRelaxedHTTPSValidation();
        responseFilters();
    }

    /**
     * 响应拦截器
     */
    public static void responseFilters(){
        filters((new Filter() {
                    public Response filter(FilterableRequestSpecification requestSpec,
                                           FilterableResponseSpecification responseSpec, FilterContext ctx) {
                        Response response = ctx.next(requestSpec, responseSpec);
                        Response newResponse = new ResponseBuilder().clone(response)
                                .setContentType(ContentType.JSON)
                                .build();
                        logger.log_info("开启响应拦截器");
                        return newResponse;
                    }
                })
        );
    }

    public void run(StepModelList stepModelList) {
        prepareData();
        setup();
        logger.log_info("开始测试!");
        for( StepModel step: stepModelList.getStepModelList()){
             // TODO   try捕获异常
            try {
                logger.log_info("接口名称:" + step.info.name);
                if(step.given.request.equals("get")) {
                    Map queryParam = (Map) step.given.queryParam;
                    Map headers = (Map) step.given.headers;
                    if (step.given.queryParam == null && step.given.headers == null){
                        response = (Response) given().when().get(step.when.url).then().extract();
                    }else if (step.given.queryParam == null){
                        response = (Response) given().headers(headers).when().get(step.when.url).then().extract();
                    }else {
                        response = (Response) given().headers(headers).
                                params(queryParam).when().get(step.when.url).then().extract();
                    }
                }else if (step.given.request.equals("post")){
                    Map body = (Map) step.given.body;
                    Map headers = (Map) step.given.headers;
                    String ContentType = (String) headers.get("Content-Type");
                    if (ContentType.equals(ContentTypeFrom)){
                        // 打印日志.log().all()
                        response = (Response) given().headers(headers)
                                .formParams(body).when().post(step.when.url).then().extract();
                    } else if (ContentType.equals(ContentTypeJson)){
                        response = (Response) given().headers(headers)
                                .body(body).when().post(step.when.url).then().extract();
                    }
                }
                getResponse(response,step);

            //  catch保存结果
            } catch (Exception ex) {
                // TODO 捕获异常，并且保存起来
                System.out.println("-----------------------发生异常报错！------------");

            }
        }
    }

    private void getResponse(Response response, StepModel step){
        org.junit.Assert.assertEquals(response.statusCode(),step.then.statusCode);
        logger.log_info("断言接口状态码成功!");
        List getBody = (List) step.then.body;
        boolean flag = true;
        for (Object bd:getBody) {
            JSONObject object = JSONObject.fromObject(bd);
            Iterator<String> sIterator = object.keys();
            while(sIterator.hasNext()){
                selectkey = sIterator.next();
                logger.log_info("断言类型:" + selectkey);
                JSONArray aslist = object.getJSONArray(selectkey);
                logger.log_info("断言数据列表:" + aslist);
                checkKey = String.valueOf(aslist.get(0)) ;
                exp =  aslist.get(1);
                check = response.getBody().jsonPath().getString(checkKey);
                logger.log_info("响应解析的值:" + check);

                boolean assertFlag = selectAssert(selectkey,check,exp);
                if (assertFlag == false) {
                    flag = false;
                }
            }
        }
        logger.log_info(response.asString());
        // flag为true时执行成功，false时执行失败
        logger.log_info("执行结果:" + flag);
    }

    /**
     * 选择不同类型的断言方法
     * @param key
     * @param check
     * @param expect
     */
    public boolean selectAssert(String key,Object check,Object expect) {
        // 获取到rest-assured执行结果
        boolean flag = true;
        logger.log_info("实际值:" + check);
        logger.log_info("预期值:" + expect);
        Assert as = new Assert(collector);
        if (key.equals("eq")) {
            as.assertEqual(check, expect);
            if (!check.equals(expect)) {
                flag = false;
            }
        } else if (key.equals("nq")) {
            as.assertNoteuals(check, expect);

        } else if (key.equals("startsWith")) {
            as.assertStartsWith(check + "", expect + "");

        }
        return flag;
    }

}
