package com.xupt.xuptfacerecognition.info;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataParser {

    // 根对象
    public static class Response {
        private int code;
        private String msg;
        @SerializedName("data")
        private List<DataItem> dataList;

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        public List<DataItem> getDataList() {
            return dataList;
        }
    }

    // 数据项对象
    public static class DataItem {
        @SerializedName("ID")
        private int id;
        @SerializedName("CreatedAt")
        private String createdAt;
        @SerializedName("UpdatedAt")
        private String updatedAt;
        @SerializedName("DeletedAt")
        private String deletedAt; // JSON 中为 null，用 String 类型接收
        @SerializedName("Data")
        private String data;
        @SerializedName("UserID")
        private String userID;
        @SerializedName("Status")
        private String status;

        public String getStatus() {
            return status;
        }
        public int getId() {
            return id;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getDeletedAt() {
            return deletedAt;
        }

        public String getData() {
            return data;
        }

        public String getUserID() {
            return userID;
        }
    }
}