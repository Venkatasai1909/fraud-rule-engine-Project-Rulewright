package com.fraud_rule_engine.rule;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.datasource.url=jdbc:h2:mem:task1;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=none", "spring.flyway.enabled=false", "spring.sql.init.mode=always", "spring.sql.init.schema-locations=classpath:schema-h2.sql", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"})
class Task1ApiIntegrationTests {
    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    JsonMapper mapper;

    @BeforeEach
    void reset() {
        jdbc.update("delete from evaluation_log");
        jdbc.update("delete from rule_version");
        jdbc.update("delete from fraud_rule");
        jdbc.update("update ruleset_state set version=0 where id=1");
        if (jdbc.queryForObject("select count(*) from ruleset_state", Integer.class) == 0)
            jdbc.update("insert into ruleset_state(id,version) values(1,0)");
    }

    String transaction(String id, int amount) {
        return "{\"transactionId\":\"" + id + "\",\"customerId\":\"cust-1\",\"amount\":" + amount + ",\"currency\":\"INR\",\"channel\":\"ECOM\",\"merchantCategory\":\"5732\",\"country\":\"IN\",\"newDevice\":false,\"timestamp\":\"2026-11-14T10:00:00Z\"}";
    }

    @Test
    void createActivateVersionEvaluateAndIdempotency() throws Exception {
        String v1 = "{\"name\":\"Threshold\",\"action\":\"REVIEW\",\"priority\":10,\"condition\":{\"field\":\"amount\",\"op\":\"gte\",\"value\":100}}";
        String created = mvc.perform(post("/api/v1/rules").contentType(MediaType.APPLICATION_JSON).content(v1)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String ruleId = mapper.readTree(created).get("ruleId").asText();
        mvc.perform(post("/api/v1/rules/" + ruleId + "/versions/1/activate")).andExpect(status().isOk()).andExpect(jsonPath("$.rulesetVersion").value(1));
        mvc.perform(post("/api/v1/evaluate").contentType(MediaType.APPLICATION_JSON).content(transaction("txn-1", 150))).andExpect(status().isOk()).andExpect(jsonPath("$.decision").value("REVIEW")).andExpect(jsonPath("$.matchedRules[0].version").value(1));
        String v2 = "{\"action\":\"BLOCK\",\"priority\":10,\"condition\":{\"field\":\"amount\",\"op\":\"gte\",\"value\":200}}";
        mvc.perform(post("/api/v1/rules/" + ruleId + "/versions").contentType(MediaType.APPLICATION_JSON).content(v2)).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/rules/" + ruleId + "/versions/2/activate")).andExpect(status().isOk()).andExpect(jsonPath("$.rulesetVersion").value(2));
        mvc.perform(post("/api/v1/evaluate").contentType(MediaType.APPLICATION_JSON).content(transaction("txn-2", 250))).andExpect(status().isOk()).andExpect(jsonPath("$.decision").value("BLOCK")).andExpect(jsonPath("$.matchedRules[0].version").value(2));
        // Same transactionId returns the original version-1 decision despite the active version-2 ruleset.
        mvc.perform(post("/api/v1/evaluate").contentType(MediaType.APPLICATION_JSON).content(transaction("txn-1", 250))).andExpect(status().isOk()).andExpect(jsonPath("$.decision").value("REVIEW")).andExpect(jsonPath("$.matchedRules[0].version").value(1));
        mvc.perform(get("/api/v1/evaluations/txn-1")).andExpect(status().isOk()).andExpect(jsonPath("$.matchedRules[0].version").value(1));
    }

    @Test
    void invalidConditionReturns400WithBadLeafPath() throws Exception {
        String body = "{\"name\":\"Bad\",\"action\":\"BLOCK\",\"priority\":1,\"condition\":{\"all\":[{\"field\":\"amount\",\"op\":\"gt\",\"value\":\"lots\"}]}}";
        mvc.perform(post("/api/v1/rules").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("$.all[0].value")));
    }
}
