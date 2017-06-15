package com.unicolour.joyspace;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Rule
    public JUnitRestDocumentation restDoc =
            new JUnitRestDocumentation("src/asciidoc/snippets/api");

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private RestDocumentationResultHandler document;

    @Before
    public void setup() {
        this.document = document("{method-name}",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()));

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(documentationConfiguration(this.restDoc))
                //.apply(springSecurity())
                .build();
    }

    @Test
    public void findPrintStationByQrCode() throws Exception {
        this.document.snippets(
                requestParameters(parameterWithName("qrCode").description("自助机二维码"))
        );

        this.mockMvc.perform(get("/api/printStation/findByQrCode")
                .param("qrCode", "https://mp.weixin.qq.com/a/~~wu3hXzBSt64~plUyoOB9Iyf8mEHP9BrkLA~~"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document);
    }

    @Test
    public void findPrintStationById() throws Exception {
        this.document.snippets(
                pathParameters(parameterWithName("id").description("自助机id"))
        );

        this.mockMvc.perform(get("/api/printStation/{id}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document);
    }

    @Test
    public void findPrintStationById404() throws Exception {
        this.mockMvc.perform(get("/api/printStation/{id}",10000))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andDo(document);
    }

    @Test
    public void findPrintStationByQrCode404() throws Exception {
        this.mockMvc.perform(get("/api/printStation/findByQrCode")
                .param("qrCode", "not_exist_qr_code"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andDo(document);
    }

    @Test
    public void findProductByPrintStationId() throws Exception {
        this.document.snippets(
                requestParameters(parameterWithName("printStationId").description("自助机id"))
        );

        this.mockMvc.perform(get("/api/product/findByPrintStation")
                .param("printStationId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document);
    }
}
