/*
 * Froody API
 * API for Froody application
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package io.github.froodyapp.api.api;

import io.github.froodyapp.api.invoker.ApiException;
import io.github.froodyapp.api.model_.ResponseOk;
import org.junit.Test;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for AdminApi
 */
@Ignore
public class AdminApiTest {

    private final AdminApi api = new AdminApi();

    
    /**
     * 
     *
     * Clean up user and entry database
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void adminCleanupGetTest() throws ApiException {
        String adminCode = null;
        ResponseOk response = api.adminCleanupGet(adminCode);

        // TODO: test validations
    }
    
}
