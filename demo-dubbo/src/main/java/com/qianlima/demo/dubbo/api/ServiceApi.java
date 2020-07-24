package com.qianlima.demo.dubbo.api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @See http://dubbo.apache.org/zh-cn/blog/dubbo-rest.html
 * @Author Lun_qs
 * @Date 2019/12/26 9:58
 * @Version 0.0.1
 */
@Path("api") // #1
@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML}) // #2
@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
public interface ServiceApi {
    @GET // #3
    @Path("{id: \\d+}")
    String getUser(@PathParam("id") Long id);

    @GET // #4
    @Path("reg")
    String registerUser(@QueryParam("name") String name, @QueryParam("pwd") String pwd);

//    @POST // #4
//    @Path("xxxx")
//    String registerUser(Bean javaBean);

}
