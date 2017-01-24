package org.librairy.tests;

import io.swagger.client.ApiException;
import io.swagger.client.api.DocumentsapicontrollerApi;
import io.swagger.client.api.PartsapicontrollerApi;
import io.swagger.client.model.Annotation;
import io.swagger.client.model.DigitalObject;
import io.swagger.client.model.Reference;
import io.swagger.client.model.Text;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Ignore
public class ProvisionTest {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisionTest.class);

    private final DocumentsapicontrollerApi documentsApi = new DocumentsapicontrollerApi();

    private final PartsapicontrollerApi partsApi = new PartsapicontrollerApi();


    @Before
    public void clean() throws ApiException {
        documentsApi.documentsDeleteUsingDELETE();
    }


    @Test
    public void createDocument() throws ApiException, InterruptedException {

        // list documents
        Assert.assertTrue(documentsApi.documentsGetUsingGET(100.0, null).isEmpty());

        // create document
        Text text = new Text();
        text.setName("text-simple");
        text.setContent("Euclidean vectors are an example of a vector space");
        text.setLanguage("en");
        String id = "d1";
        Reference reference = documentsApi.documentsIdPostUsingPOST(id, text);
        Assert.assertEquals(id, reference.getId());
        Assert.assertEquals(text.getName(), reference.getName());
        Assert.assertEquals("http://librairy.org/items/"+id, reference.getUri());

        // list documents
        Assert.assertFalse(documentsApi.documentsGetUsingGET(100.0, null).isEmpty());

        // get document without content
        DigitalObject doc1 = documentsApi.documentsIdGetUsingGET(id, false);
        Assert.assertEquals(text.getLanguage(), doc1.getLanguage());
        Assert.assertEquals(id, doc1.getRef().getId());
        Assert.assertEquals(text.getName(), doc1.getRef().getName());
        Assert.assertEquals("http://librairy.org/items/"+id, doc1.getRef().getUri());
        Assert.assertNull(doc1.getContent());

        // get document with content
        DigitalObject doc2 = documentsApi.documentsIdGetUsingGET(id, true);
        Assert.assertEquals(text.getLanguage(), doc2.getLanguage());
        Assert.assertEquals(id, doc2.getRef().getId());
        Assert.assertEquals(text.getName(), doc2.getRef().getName());
        Assert.assertEquals("http://librairy.org/items/"+id, doc2.getRef().getUri());
        Assert.assertEquals(text.getContent(), doc2.getContent());


        // get annotations
        Thread.sleep(200);
        List<String> annotations = documentsApi.documentsIdAnnotationsGetUsingGET(id);
        Assert.assertFalse(annotations.isEmpty());
        Assert.assertEquals(1, annotations.size());
        Assert.assertEquals("lemma", annotations.get(0));

        // get annotation content
        Annotation annotation = documentsApi.documentsIdAnnotationsAidGetUsingGET(id, "lemma");
        Assert.assertEquals("vector vector space", annotation.getValue());

        // delete document
        documentsApi.documentsIdDeleteUsingDELETE(id);

        // list documents
        Assert.assertTrue(documentsApi.documentsGetUsingGET(100.0, null).isEmpty());
    }

    @Test
    public void annotateDocument() throws ApiException, InterruptedException {

        // create an empty content document
        Text text = new Text();
        text.setName("text-simple");
        String id = "d2";
        Reference reference = documentsApi.documentsIdPostUsingPOST(id, text);
        Assert.assertEquals(id, reference.getId());
        Assert.assertEquals(text.getName(), reference.getName());
        Assert.assertEquals("http://librairy.org/items/"+id, reference.getUri());

        // get document with content
        DigitalObject digObj = documentsApi.documentsIdGetUsingGET(id, true);
        Assert.assertEquals(id, digObj.getRef().getId());
        Assert.assertEquals(text.getName(), digObj.getRef().getName());
        Assert.assertEquals("http://librairy.org/items/"+id, digObj.getRef().getUri());
        Assert.assertNull(digObj.getLanguage());
        Assert.assertNull(digObj.getContent());

        // get annotations
        Thread.sleep(1000);
        Assert.assertTrue(documentsApi.documentsIdAnnotationsGetUsingGET(id).isEmpty());

        // annotate document
        Annotation annotation = new Annotation();
        annotation.setValue("lemma1 lemma2 lemma3");
        documentsApi.documentsIdAnnotationsAidPostUsingPOST(id, "lemma", annotation);

        // get annotations
        Assert.assertFalse(documentsApi.documentsIdAnnotationsGetUsingGET(id).isEmpty());

        // get annotation content
        Annotation annotationRsp = documentsApi.documentsIdAnnotationsAidGetUsingGET(id, "lemma");
        Assert.assertEquals(annotation.getValue(), annotationRsp.getValue());

        // delete document
        documentsApi.documentsIdDeleteUsingDELETE(id);
    }

    @Test
    public void createParts() throws InterruptedException, ApiException {

        // list parts
        Assert.assertTrue(partsApi.partsGetUsingGET(100.0, null).isEmpty());

        // create part
        Text text = new Text();
        text.setName("text-simple");
        text.setContent("Vector spaces are the subject of linear algebra and are well characterized by their dimension, which, roughly speaking, specifies the number of independent directions in the space");
        text.setLanguage("en");
        String id = "p1";
        Reference reference = partsApi.partsIdPostUsingPOST(id, text);
        Assert.assertEquals(id, reference.getId());
        Assert.assertEquals(text.getName(), reference.getName());
        Assert.assertEquals("http://librairy.org/parts/"+id, reference.getUri());

        // list parts
        Assert.assertFalse(partsApi.partsGetUsingGET(100.0, null).isEmpty());

        // get part without content
        DigitalObject digObj1 = partsApi.partsIdGetUsingGET(id, false);
//        Assert.assertEquals(text.getLanguage(), digObj1.getLanguage());
        Assert.assertEquals(id, digObj1.getRef().getId());
        Assert.assertEquals(text.getName(), digObj1.getRef().getName());
        Assert.assertEquals("http://librairy.org/parts/"+id, digObj1.getRef().getUri());
        Assert.assertNull(digObj1.getContent());

        // get part with content
        DigitalObject digObj2 = partsApi.partsIdGetUsingGET(id, true);
//        Assert.assertEquals(text.getLanguage(), digObj2.getLanguage());
        Assert.assertEquals(id, digObj2.getRef().getId());
        Assert.assertEquals(text.getName(), digObj2.getRef().getName());
        Assert.assertEquals("http://librairy.org/parts/"+id, digObj2.getRef().getUri());
        Assert.assertEquals(text.getContent(), digObj2.getContent());


        // get annotations
        Thread.sleep(1000);
        List<String> annotations = partsApi.partsIdAnnotationsGetUsingGET(id);
        Assert.assertFalse(annotations.isEmpty());
        Assert.assertEquals(1, annotations.size());
        Assert.assertEquals("lemma", annotations.get(0));

        // get annotation content
        Annotation annotation = partsApi.partsIdAnnotationsAidGetUsingGET(id, "lemma");
        Assert.assertEquals("vector space subject algebra dimension number direction space", annotation.getValue());

        // delete document
        partsApi.partsIdDeleteUsingDELETE(id);

        // list parts
        Assert.assertTrue(partsApi.partsGetUsingGET(100.0, null).isEmpty());

    }

    @Test
    public void annotatePart() throws ApiException, InterruptedException {

        // create an empty content part
        Text text = new Text();
        text.setName("text-simple");
        String id = "p2";
        Reference reference = partsApi.partsIdPostUsingPOST(id, text);
        Assert.assertEquals(id, reference.getId());
        Assert.assertEquals(text.getName(), reference.getName());
        Assert.assertEquals("http://librairy.org/parts/"+id, reference.getUri());

        // get part with content
        DigitalObject digObj = partsApi.partsIdGetUsingGET(id, true);
        Assert.assertEquals(id, digObj.getRef().getId());
        Assert.assertEquals(text.getName(), digObj.getRef().getName());
        Assert.assertEquals("http://librairy.org/parts/"+id, digObj.getRef().getUri());
        Assert.assertNull(digObj.getLanguage());
        Assert.assertNull(digObj.getContent());

        // get annotations
        Thread.sleep(1000);
        Assert.assertTrue(partsApi.partsIdAnnotationsGetUsingGET(id).isEmpty());

        // annotate part
        Annotation annotation = new Annotation();
        annotation.setValue("lemma1 lemma2 lemma3");
        partsApi.partsIdAnnotationsAidPostUsingPOST(id, "lemma", annotation);

        // get annotations
        Assert.assertFalse(partsApi.partsIdAnnotationsGetUsingGET(id).isEmpty());

        // get annotation content
        Annotation annotationRsp = partsApi.partsIdAnnotationsAidGetUsingGET(id, "lemma");
        Assert.assertEquals(annotation.getValue(), annotationRsp.getValue());

        // delete part
        partsApi.partsIdDeleteUsingDELETE(id);
    }

    @Test
    public void createDocumentWithParts() throws ApiException {

        // list documents
        Assert.assertTrue(documentsApi.documentsGetUsingGET(100.0, null).isEmpty());

        // create document
        Text docText = new Text();
        docText.setName("complex-doc");
        docText.setContent("Euclidean vectors are an example of a vector space. Vector spaces are the subject of linear algebra and are well characterized by their dimension, which, roughly speaking, specifies the number of independent directions in the space");
        docText.setLanguage("en");
        String docId = "doc";
        Reference docReference = documentsApi.documentsIdPostUsingPOST(docId, docText);
        Assert.assertEquals(docId, docReference.getId());
        Assert.assertEquals(docText.getName(), docReference.getName());
        Assert.assertEquals("http://librairy.org/items/"+docId, docReference.getUri());

        // list documents
        Assert.assertFalse(documentsApi.documentsGetUsingGET(100.0, null).isEmpty());

        // create part
        Text partText = new Text();
        partText.setName("text-simple");
        partText.setContent("Vector spaces are the subject of linear algebra and are well characterized by their dimension, which, roughly speaking, specifies the number of independent directions in the space");
        partText.setLanguage("en");
        String partId = "part";
        Reference partReference = partsApi.partsIdPostUsingPOST(partId, partText);
        Assert.assertEquals(partId, partReference.getId());
        Assert.assertEquals(partText.getName(), partReference.getName());
        Assert.assertEquals("http://librairy.org/parts/"+partId, partReference.getUri());

        // list parts of document
        Assert.assertTrue(documentsApi.documentsIdPartsGetUsingGET(docId, null, null).isEmpty());

        // add part from document
        Reference partReferenceAdded = documentsApi.documentsIdPartsIdPostUsingPOST(docId, partId);
        Assert.assertEquals(partReference, partReferenceAdded);

        // list parts of document
        List<Reference> parts = documentsApi.documentsIdPartsGetUsingGET(docId, null, null);
        Assert.assertEquals(1, parts.size());
        Reference partRefRes = parts.get(0);
        Assert.assertEquals(partReference, partRefRes);

        // delete document
        documentsApi.documentsIdDeleteUsingDELETE(docId);

        // list documents
        Assert.assertTrue(documentsApi.documentsGetUsingGET(100.0, null).isEmpty());

        // list parts
        Assert.assertTrue(partsApi.partsGetUsingGET(100.0, null).isEmpty());

        // list parts of document
        try{
            documentsApi.documentsIdPartsGetUsingGET(docId, null, null).isEmpty();
            Assert.assertFalse(true);
        }catch (ApiException e){
            Assert.assertEquals(404, e.getCode());
        }

    }

    @Test
    public void deleteAllDocuments() throws ApiException {

        documentsApi.documentsDeleteUsingDELETE();
        Assert.assertTrue(true);

        // list documents
        Assert.assertTrue(documentsApi.documentsGetUsingGET(100.0, null).isEmpty());
    }

    @Test
    public void deleteAllParts() throws ApiException {

        partsApi.partsDeleteUsingDELETE();
        Assert.assertTrue(true);

        // list documents
        Assert.assertTrue(partsApi.partsGetUsingGET(100.0, null).isEmpty());


    }
}
