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


package io.github.froodyapp.api.model_;

import java.util.Objects;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;
import java.io.Serializable;

/**
 * Represents the Result of the /entry/add endpoint
 */
@ApiModel(description = "Represents the Result of the /entry/add endpoint")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2017-02-08T17:38:04.070+01:00")
public class ResponseEntryAdd implements Serializable {
  private static final long serialVersionUID = 1L;

  @SerializedName("entryId")
  private Long entryId = null;

  @SerializedName("managementCode")
  private Integer managementCode = null;

  @SerializedName("creationDate")
  private DateTime creationDate = null;

  public ResponseEntryAdd entryId(Long entryId) {
    this.entryId = entryId;
    return this;
  }

   /**
   * Entry.entryId ** UID of the entry, which was added to DB
   * @return entryId
  **/
  @ApiModelProperty(example = "null", value = "Entry.entryId ** UID of the entry, which was added to DB")
  public Long getEntryId() {
    return entryId;
  }

  public void setEntryId(Long entryId) {
    this.entryId = entryId;
  }

  public ResponseEntryAdd managementCode(Integer managementCode) {
    this.managementCode = managementCode;
    return this;
  }

   /**
   * Entry.ManagementCode ** Needed for deleting and managing entry
   * @return managementCode
  **/
  @ApiModelProperty(example = "null", value = "Entry.ManagementCode ** Needed for deleting and managing entry")
  public Integer getManagementCode() {
    return managementCode;
  }

  public void setManagementCode(Integer managementCode) {
    this.managementCode = managementCode;
  }

  public ResponseEntryAdd creationDate(DateTime creationDate) {
    this.creationDate = creationDate;
    return this;
  }

   /**
   * Entry ** Timestamp of creation
   * @return creationDate
  **/
  @ApiModelProperty(example = "null", value = "Entry ** Timestamp of creation")
  public DateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(DateTime creationDate) {
    this.creationDate = creationDate;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResponseEntryAdd responseEntryAdd = (ResponseEntryAdd) o;
    return Objects.equals(this.entryId, responseEntryAdd.entryId) &&
        Objects.equals(this.managementCode, responseEntryAdd.managementCode) &&
        Objects.equals(this.creationDate, responseEntryAdd.creationDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entryId, managementCode, creationDate);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResponseEntryAdd {\n");
    
    sb.append("    entryId: ").append(toIndentedString(entryId)).append("\n");
    sb.append("    managementCode: ").append(toIndentedString(managementCode)).append("\n");
    sb.append("    creationDate: ").append(toIndentedString(creationDate)).append("\n");
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

