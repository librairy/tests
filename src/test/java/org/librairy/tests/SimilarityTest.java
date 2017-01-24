package org.librairy.tests;

import io.swagger.client.ApiException;
import io.swagger.client.api.DocumentsapicontrollerApi;
import io.swagger.client.api.DomainsapicontrollerApi;
import io.swagger.client.api.PartsapicontrollerApi;
import io.swagger.client.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class SimilarityTest {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisionTest.class);

    private final DocumentsapicontrollerApi documentsApi = new DocumentsapicontrollerApi();

    private final DomainsapicontrollerApi domainsApi = new DomainsapicontrollerApi();

//    @Before
//    public void clean() throws ApiException {
//        documentsApi.documentsDeleteUsingDELETE();
//    }

    @Test
    public void trainLDAModelfromAnnotations() throws ApiException, InterruptedException, IOException {

        int numDocs = 20;

        // create documents
        LOG.info("creating documents ..");
        IntStream.range(1,numDocs+1).mapToObj(i -> "doc"+i).forEach(doc -> {
            Text text = new Text();
            text.setName(doc);
            try {
                documentsApi.documentsIdPostUsingPOST(doc, text);
                LOG.info("created " + doc);
            } catch (ApiException e) {
                LOG.error("Error creating document",e);
                Assert.assertFalse(true);
            }
        });


        // annotate documents
        LOG.info("annotating documents ..");
        IntStream.range(1,numDocs+1).forEach(index -> {
            try {
                String docId = "doc"+index;
                String annotationId = "lemma"+index;


                String content = new String(Files.readAllBytes(Paths.get("src/test/resources/annotations",annotationId+".txt")));
                Annotation annotation = new Annotation();
                annotation.setValue(content);

                documentsApi.documentsIdAnnotationsAidPostUsingPOST(docId,"lemma",annotation);
                LOG.info("annotated " + docId);
            } catch (Exception e) {
                LOG.error("Error annotating document",e);
                Assert.assertFalse(true);
            }
        });


        // create domain
        LOG.info("creating domain ..");
        String domainId = "domain1";
        Resource domain = new Resource();
        domain.setName("domain1");
        domain.setDescription("domain for testing purposes");
        domainsApi.domainsDomainIdPostUsingPOST(domainId, domain);
        LOG.info("created " + domainId);

        // minimize scheduled time
        // LDA
        Parameter scheduledLDAParameter = new Parameter();
        scheduledLDAParameter.setName("lda.delay");
        scheduledLDAParameter.setValue("30000");
        domainsApi.domainsDomainIdParametersPostUsingPOST(domainId, scheduledLDAParameter);
        // W2V
        Parameter scheduledW2VParameter = new Parameter();
        scheduledW2VParameter.setName("w2v.delay");
        scheduledW2VParameter.setValue("500");
        domainsApi.domainsDomainIdParametersPostUsingPOST(domainId, scheduledW2VParameter);


        // add documents to domain
        LOG.info("adding documents to domain ..");
        IntStream.range(1,numDocs+1).forEach(index ->{
            String docId = "doc"+index;

            try {
                domainsApi.domainsDomainIdDocumentsDocumentIdPostUsingPOST(domainId,docId);
                LOG.info("added " + docId + " to " + domainId);
            } catch (ApiException e) {
                LOG.error("Error adding doc to domain",e);
                Assert.assertFalse(true);
            }
        });

        // wait for model creation
        LOG.info("waiting for LDA Model creation  ..");
        Thread.sleep(180000);

        // Read domain
        LOG.info("getting domain details  ..");
        ContainerStats domainDetails = domainsApi.domainsDomainIdGetUsingGET(domainId);

        // get documents number
        Assert.assertEquals(numDocs, Long.valueOf(domainDetails.getDocuments()).intValue());

        // get parts number
        Assert.assertEquals(0, Long.valueOf(domainDetails.getParts()).intValue());

        // get subdomains number
        Assert.assertEquals(0, Long.valueOf(domainDetails.getSubdomains()).intValue());

        // get topic number
        Long topics = Double.valueOf(2*Math.sqrt(numDocs/2)).longValue();
        Assert.assertEquals(topics, Long.valueOf(domainDetails.getTopics()));

        // get similarity number
        long numFactorial = factorial(numDocs);
        long diffNumFactorial = factorial(numDocs - 2);
        long combinations = numFactorial / (2 * diffNumFactorial);
        Assert.assertEquals(combinations, Long.valueOf(domainDetails.getSimilarities()).intValue());

        // get similar docs to a given doc
        LOG.info("getting similar document ..");
        IntStream.range(1,numDocs+1).forEach(index ->{
            String docId = "doc"+index;

            String resourceType = null;
            String relatedId = null;
            Double size = Double.valueOf(numDocs);
            String offset = null;
            String relType = "similarity";
            try {
                List<Relation> relatedDocs = domainsApi.domainsDomainIdDocumentsDocumentIdRelationsGetUsingGET(domainId, docId, relType, resourceType, relatedId, size, offset);

                Assert.assertEquals(numDocs-1, relatedDocs.size());

                Double reference = Double.MAX_VALUE;
                for (Relation relation : relatedDocs){
                    Assert.assertTrue(relation.getScore() <= reference);
                    reference = relation.getScore();
                    Assert.assertEquals(relType, relation.getType());
                    Assert.assertNotNull(relation.getTo());
                    Assert.assertNotNull(relation.getTo().getName());
                    Assert.assertNotNull(relation.getTo().getId());
                    Assert.assertNotNull(relation.getTo().getUri());
                }

            }  catch (ApiException e) {
                LOG.error("Error getting similar docs from domain",e);
                Assert.assertFalse(true);
            }
        });

        // infer similar docs to a given text
        LOG.info("trying to infer similar document ..");
        Text text = new Text();
        text.setName("text1");
        String inferedDoc = "doc1";
        String textContent = new String(Files.readAllBytes(Paths.get("src/test/resources/documents",inferedDoc+".txt")));
        text.setContent(textContent);

        String relatedType = null;
        Integer size = 10;
        List<Relation> relatedInferedDocs = domainsApi.domainsDomainIdTextsPostUsingPOST(domainId, text, size, relatedType);
        LOG.info("document list returned: " + relatedInferedDocs);
        Assert.assertFalse(relatedInferedDocs.isEmpty());
        Assert.assertEquals(1, relatedInferedDocs.stream().filter(rel -> rel.getTo().getId().equalsIgnoreCase(inferedDoc)).count());

        // remove domain
        LOG.info("removing domain ..");
        domainsApi.domainsDomainIdDeleteUsingDELETE(domainId);

        // remove documents
        LOG.info("removing documents ..");
        documentsApi.documentsDeleteUsingDELETE();

    }

    @Test
    public void trainLDAModelfromTexts() throws ApiException, InterruptedException, IOException {

        int numDocs = 20;

        // create documents
        LOG.info("creating documents ..");
        IntStream.range(1,numDocs+1).mapToObj(i -> "doc"+i).forEach(doc -> {


            try {
                Text text = new Text();
                text.setName(doc);

                String content = new String(Files.readAllBytes(Paths.get("src/test/resources/documents",doc+".txt")));
                text.setContent(content);

                documentsApi.documentsIdPostUsingPOST(doc, text);
                LOG.info("created " + doc);
            } catch (ApiException e) {
                LOG.error("Error creating document",e);
                Assert.assertFalse(true);
            } catch (IOException e) {
                LOG.error("Error getting content of document",e);
                Assert.assertFalse(true);
            }
        });

        // create domain
        LOG.info("creating domain ..");
        String domainId = "domain1";
        Resource domain = new Resource();
        domain.setName("domain1");
        domain.setDescription("domain for testing purposes");
        domainsApi.domainsDomainIdPostUsingPOST(domainId, domain);
        LOG.info("created " + domainId);

        // minimize scheduled time
        // LDA
        Parameter scheduledLDAParameter = new Parameter();
        scheduledLDAParameter.setName("lda.delay");
        scheduledLDAParameter.setValue("30000");
        domainsApi.domainsDomainIdParametersPostUsingPOST(domainId, scheduledLDAParameter);
        // W2V
        Parameter scheduledW2VParameter = new Parameter();
        scheduledW2VParameter.setName("w2v.delay");
        scheduledW2VParameter.setValue("500");
        domainsApi.domainsDomainIdParametersPostUsingPOST(domainId, scheduledW2VParameter);


        // add documents to domain
        LOG.info("adding documents to domain ..");
        IntStream.range(1,numDocs+1).forEach(index ->{
            String docId = "doc"+index;

            try {
                domainsApi.domainsDomainIdDocumentsDocumentIdPostUsingPOST(domainId,docId);
                LOG.info("added " + docId + " to " + domainId);
            } catch (ApiException e) {
                LOG.error("Error adding doc to domain",e);
                Assert.assertFalse(true);
            }
        });

        // wait for model creation
        LOG.info("waiting for LDA Model creation  ..");
        Thread.sleep(180000);

        // Read domain
        LOG.info("getting domain details  ..");
        ContainerStats domainDetails = domainsApi.domainsDomainIdGetUsingGET(domainId);

        // get documents number
        Assert.assertEquals(numDocs, Long.valueOf(domainDetails.getDocuments()).intValue());

        // get parts number
        Assert.assertEquals(0, Long.valueOf(domainDetails.getParts()).intValue());

        // get subdomains number
        Assert.assertEquals(0, Long.valueOf(domainDetails.getSubdomains()).intValue());

        // get topic number
        Long topics = Double.valueOf(2*Math.sqrt(numDocs/2)).longValue();
        Assert.assertEquals(topics, Long.valueOf(domainDetails.getTopics()));

        // get similarity number
        long numFactorial = factorial(numDocs);
        long diffNumFactorial = factorial(numDocs - 2);
        long combinations = numFactorial / (2 * diffNumFactorial);
        Assert.assertEquals(combinations, Long.valueOf(domainDetails.getSimilarities()).intValue());

//         get similar docs to a given doc
        LOG.info("getting similar document ..");
        IntStream.range(1,numDocs+1).forEach(index ->{
            String docId = "doc"+index;

            String resourceType = null;
            String relatedId = null;
            Double size = Double.valueOf(numDocs);
            String offset = null;
            String relType = "similarity";
            try {
                List<Relation> relatedDocs = domainsApi.domainsDomainIdDocumentsDocumentIdRelationsGetUsingGET(domainId, docId, relType, resourceType, relatedId, size, offset);

                Assert.assertEquals(numDocs-1, relatedDocs.size());

                Double reference = Double.MAX_VALUE;
                for (Relation relation : relatedDocs){
                    Assert.assertTrue(relation.getScore() <= reference);
                    reference = relation.getScore();
                    Assert.assertEquals(relType, relation.getType());
                    Assert.assertNotNull(relation.getTo());
                    Assert.assertNotNull(relation.getTo().getName());
                    Assert.assertNotNull(relation.getTo().getId());
                    Assert.assertNotNull(relation.getTo().getUri());
                }

            }  catch (ApiException e) {
                LOG.error("Error getting similar docs from domain",e);
                Assert.assertFalse(true);
            }
        });

        // infer similar docs to a given text
        LOG.info("trying to infer similar document ..");
        Text text = new Text();
        text.setName("text1");
        String inferedDoc = "doc1";
        String textContent = new String(Files.readAllBytes(Paths.get("src/test/resources/documents",inferedDoc+".txt")));
        text.setContent(textContent);

        String relatedType = null;
        Integer size = 10;
        List<Relation> relatedInferedDocs = domainsApi.domainsDomainIdTextsPostUsingPOST(domainId, text, size, relatedType);
        LOG.info("document list returned: " + relatedInferedDocs);
        Assert.assertFalse(relatedInferedDocs.isEmpty());
        Assert.assertEquals(1, relatedInferedDocs.stream().filter(rel -> rel.getTo().getId().equalsIgnoreCase(inferedDoc)).count());

        // remove domain
        LOG.info("removing domain ..");
        domainsApi.domainsDomainIdDeleteUsingDELETE(domainId);

        // remove documents
        LOG.info("removing documents ..");
        documentsApi.documentsDeleteUsingDELETE();

    }


    private long factorial(int n) {
        if (n > 2000) throw new IllegalArgumentException(n + " is out of range");
        return LongStream.rangeClosed(2, n).reduce(1, (a, b) -> a * b);
    }
}
