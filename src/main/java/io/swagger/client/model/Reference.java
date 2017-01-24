/*
 * librAIry API
 * Learn about [librAIry](http://librairy.github.io/) or follow the account `@librairy_fw` on twitter.     This research project receives funding from the European Commission's Seventh Framework Programme.Activity ICT (FP7-ICT-2013.8.1)  Grant agreement no: 611383 
 *
 * OpenAPI spec version: 1.0.0
 * Contact: librairy.framework@gmail.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package io.swagger.client.model;

import java.util.Objects;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Reference
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2017-01-19T14:34:01.940Z")
public class Reference {
  @SerializedName("creation")
  private String creation = null;

  @SerializedName("id")
  private String id = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("uri")
  private String uri = null;

  public Reference creation(String creation) {
    this.creation = creation;
    return this;
  }

   /**
   * Get creation
   * @return creation
  **/
  @ApiModelProperty(example = "null", value = "")
  public String getCreation() {
    return creation;
  }

  public void setCreation(String creation) {
    this.creation = creation;
  }

  public Reference id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(example = "null", value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Reference name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "null", value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Reference uri(String uri) {
    this.uri = uri;
    return this;
  }

   /**
   * Get uri
   * @return uri
  **/
  @ApiModelProperty(example = "null", value = "")
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Reference reference = (Reference) o;
    return Objects.equals(this.creation, reference.creation) &&
        Objects.equals(this.id, reference.id) &&
        Objects.equals(this.name, reference.name) &&
        Objects.equals(this.uri, reference.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(creation, id, name, uri);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Reference {\n");
    
    sb.append("    creation: ").append(toIndentedString(creation)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    uri: ").append(toIndentedString(uri)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
  
}

