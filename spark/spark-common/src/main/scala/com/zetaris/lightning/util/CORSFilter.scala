package com.zetaris.lightning.util

import javax.ws.rs.container.{ContainerRequestContext, ContainerResponseContext, ContainerResponseFilter}
import javax.ws.rs.ext.Provider

@Provider
class CORSFilter extends ContainerResponseFilter {
  override def filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext): Unit = {
    responseContext.getHeaders.add("Access-Control-Allow-Origin", "*")
    responseContext.getHeaders.add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
    responseContext.getHeaders.add("Access-Control-Allow-Credentials", "true")
    responseContext.getHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
    responseContext.getHeaders.add("Access-Control-Max-Age", "1209600")
  }
}
